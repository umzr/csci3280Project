

import newnetwork.InterleaveJobAllocator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class InterleavingBitmaps {

    public static void main(String[] args) {
        String basePath = "p2pDemo/";
        String outputFilename = "interleaved_bitmap.bmp";

        File bmpsFolder = new File(basePath);
        File[] bmpFiles = bmpsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".bmp"));

        if (bmpFiles == null || bmpFiles.length == 0) {
            System.err.println("No bitmaps found.");
            return;
        }

        int numBitmaps = bmpFiles.length;

        try {
            List<BitmapSender> senders = new ArrayList<>();
            List<String> senderIDs = new ArrayList<>();
            for (int i = 0; i < numBitmaps; i++) {
                String id = bmpFiles[i].getName().substring(0, bmpFiles[i].getName().length() - 4);
                String path = basePath + id + ".bmp";
                BitmapSender sender = new BitmapSender(id, path);
                senders.add(sender);
                senderIDs.add(id);
            }

            int width = senders.get(0).bitmap.getWidth();
            int height = senders.get(0).bitmap.getHeight();

            BufferedImage interleavedBitmap = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            InterleaveJobAllocator allocator = new InterleaveJobAllocator(senderIDs, width * height);

            while (true){
                boolean endStream = true;
                for(BitmapSender sender : senders){
                    if(sender.ended)
                        continue;

                    endStream = false;
                    if(sender.isReadyForNextRequest()){
                        Integer chunkIdx = allocator.getNextJob(sender.ID);
                        if(chunkIdx == null) sender.ended = true;
                        else {
                            var completableFuture = CompletableFuture.runAsync(() -> {
                                sender.drawPixel(interleavedBitmap, chunkIdx);
                            });
                            sender.lastFuture = completableFuture;
                        }
                    }
                }
                if(endStream)
                    break;
            }


            File output = new File(basePath + outputFilename);
            ImageIO.write(interleavedBitmap, "BMP", output);
            System.out.println("Saved to: " + basePath + outputFilename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class BitmapSender {
        private String ID;
        private String bitmapPath;
        private BufferedImage bitmap;

        public CompletableFuture lastFuture;
        public boolean ended;

        public boolean isReadyForNextRequest(){
            return lastFuture == null || lastFuture.isDone();
        }

        public BitmapSender(String ID, String bitmapPath) throws IOException {
            this.ID = ID;
            this.bitmapPath = bitmapPath;
            this.bitmap = ImageIO.read(new File(bitmapPath));
            this.ended = false;
        }

        public void drawPixel(BufferedImage interleavedBitmap, int index) {
            int width = interleavedBitmap.getWidth();
            int x = index % width;
            int y = index / width;


            int color = bitmap.getRGB(x, y);
            interleavedBitmap.setRGB(x, y, color);
        }
    }
}