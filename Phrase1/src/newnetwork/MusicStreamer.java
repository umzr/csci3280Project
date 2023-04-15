package newnetwork;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MusicStreamer {
    private final String file;
    private final List<String> peerAddress;
    private final P2PMusicStreaming app;
    private PipedInputStream audioStream;
    private PipedOutputStream chunkFeedingStream;
    private PriorityQueue<QueuedChunkWrapper> queuedChunks = new PriorityQueue<>();
    private AtomicInteger lastFedChunkNumber = new AtomicInteger(-1);
    private CompletableFuture<Void> streamingTask;
    private boolean open;

    public MusicStreamer(P2PMusicStreaming app, String file, List<String> peerAddress){
        this.app = app;
        this.file = file;
        this.peerAddress = peerAddress;
        chunkFeedingStream = new PipedOutputStream();
        try {
            audioStream = new PipedInputStream(chunkFeedingStream, 44100);
            open = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PipedInputStream getAudioStream() {
        return audioStream;
    }

    public CompletableFuture<Void> getStreamingTask() {
        return streamingTask;
    }

    public Future<Void> streamingJobs(){
        List<CompletableFuture<Boolean>> checkAvailabilityTask = new ArrayList<>();
        for (String peer : peerAddress){
            checkAvailabilityTask.add(CompletableFuture.supplyAsync(() -> this.app.askForAvailability(this.file, peer))
                    .orTimeout(5, TimeUnit.SECONDS)
                    .whenComplete((availability, ex) -> {
                        if(ex != null || !availability){
                            peerAddress.remove(peer);
                        }
                    }));
        }
        CompletableFuture task = CompletableFuture.allOf(checkAvailabilityTask.toArray(new CompletableFuture[0]))
                .whenComplete((v, ex) -> {
                    if(this.peerAddress.size() > 0){
                        Map<String, StreamingPeerStatus> statuses = new HashMap<>();
                        while (true){
                            QueuedChunkWrapper firstWrapper = queuedChunks.peek();
                            boolean hasRunning = false;
                            if (firstWrapper != null) {
                                hasRunning = true;
                                if (firstWrapper.chunkNum == lastFedChunkNumber.get() + 1) {
                                    try {
                                        chunkFeedingStream.write(firstWrapper.getBuffer());
                                        lastFedChunkNumber.getAndIncrement();
                                        queuedChunks.poll();
                                    } catch (IOException e) {
                                        break;
                                    }
                                }
                            }
                            List<String> address = this.peerAddress;
                            for (int i = 0; i < address.size(); i++) {
                                String peer = address.get(i);
                                if (!statuses.containsKey(peer)) {
                                    statuses.put(peer, new StreamingPeerStatus());
                                }
                                var peerStatus = statuses.get(peer);
                                if (peerStatus.ended) continue;
                                hasRunning = true;
                                if (peerStatus.isReadyForNextRequest()) {
                                    if(peerStatus.lastChunkIdx == -1) peerStatus.lastChunkIdx = i;
                                    else peerStatus.lastChunkIdx += address.size();
                                    var completableFuture = CompletableFuture.runAsync(() -> {
                                        int thisChunkIdx = peerStatus.lastChunkIdx;
                                        var audioChunk = app.requestAudioChunk(peer, this.file, peerStatus.lastChunkIdx);
                                        if(audioChunk.length == 0){
                                            peerStatus.ended = true;
                                        }
                                        else{
                                            queuedChunks.add(new QueuedChunkWrapper(thisChunkIdx, audioChunk));
                                        }
                                    });
                                    peerStatus.lastFuture = completableFuture;
                                }
                            }
                            if(!hasRunning) break;
                        }
                    }
                    try {
                        this.audioStream.close();
                        open = false;
                        this.chunkFeedingStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
        this.streamingTask = task;
        return task;
    }

    public boolean isOpen(){
        return audioStream != null && open;
    }

    public static class QueuedChunkWrapper implements Comparable<QueuedChunkWrapper>{
        private final int chunkNum;
        private final byte[] buffer;

        public QueuedChunkWrapper(int chunkNum, byte[] buffer){
            this.chunkNum = chunkNum;
            this.buffer = buffer;
        }

        public int getChunkNum() {
            return chunkNum;
        }

        public byte[] getBuffer() {
            return buffer;
        }

        @Override
        public int compareTo(QueuedChunkWrapper o) {
            return Integer.compare(this.chunkNum, o.chunkNum);
        }
    }

    private static class StreamingPeerStatus{
        public CompletableFuture lastFuture;
        public int lastChunkIdx = -1;
        public boolean ended;
        public StreamingPeerStatus(){

        }

        public boolean isReadyForNextRequest(){
            return lastFuture == null || lastFuture.isDone();
        }
    }
}
