package newnetwork;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class InterleaveJobAllocator {
    private final Map<String, PriorityQueue<Integer>> waitingJobs;

    public InterleaveJobAllocator(List<String> ids, int totalJobs){
        waitingJobs = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            PriorityQueue<Integer> queue = new PriorityQueue<>();
            for (int j = i; j < totalJobs; j+=ids.size()) {
                queue.add(j);
            }
            waitingJobs.put(id, queue);
        }
    }

    public void assignJob(String id, int jobId){
        if(waitingJobs.containsKey(id)){
            waitingJobs.get(id).add(jobId);
        }
    }

    /**
     * Get next job for given id
     * @param id
     * @return job id, or null if such id is not registered or no more jobs for that id
     */
    public Integer getNextJob(String id){
        if(!waitingJobs.containsKey(id)) return null;
        return waitingJobs.get(id).poll();
    }

    /**
     * notify the allocator that this id has left and request reallocating jobs
     * @param id
     */
    public void notifyIdQuit(String id){
        synchronized (waitingJobs){
            PriorityQueue<Integer> removedQueue = waitingJobs.remove(id);
            if (removedQueue != null && !waitingJobs.isEmpty()){
                while(!removedQueue.isEmpty()){
                    for (PriorityQueue<Integer> queue : waitingJobs.values()) {
                        Integer jobId = removedQueue.poll();
                        if(jobId != null){
                            queue.add(jobId);
                        }
                    }
                }
            }
        }
    }

    /**
     * notify the allocator that a new member joins and request reallocating jobs
     * @param id
     */
    public void notifyIdEnter(String id){
        PriorityQueue<Integer> newQueue = new PriorityQueue<>();
        PriorityQueue<Integer> fullQueue = new PriorityQueue<>();
        synchronized (waitingJobs){
            for (PriorityQueue<Integer> queue : waitingJobs.values()){
                fullQueue.addAll(queue);
                queue.clear();
            }
            waitingJobs.put(id, newQueue);
            while(!fullQueue.isEmpty()){
                for (PriorityQueue<Integer> queue : waitingJobs.values()) {
                    Integer jobId = fullQueue.poll();
                    if(jobId != null){
                        queue.add(jobId);
                    }
                }
            }
        }
    }
}
