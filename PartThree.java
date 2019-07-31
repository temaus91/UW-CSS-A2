import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This program is a third part of the second assignment.
 * It is design to take input from a file, and process it.
 * The scheduler will be implemented as a priority based round robin (preemtive) Quantum = 3.
 * @author Artem Tarasenko
 */
public class PartThree {
    public static void main (String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("You must pass the path of the file to read from.");
        }

        // get file lines
        List<String> lines = readFileInList(args[0]);

        // print file information back to the user
        System.out.println("Number of processes: " + lines.size() + "\n");
        lines.forEach(System.out::println);

        // generate processes
        List<Process> processes = linesToProcesses(lines);

        // Schedule and execute processes
        int currTime = 0;
        boolean notDone = true;
        Process prevExecutedProcess = null;

        // Create a map to manage round robin. Priority to ProcessQueue
        Map<Integer, Queue<Process>> priorityToProcessQueue = new HashMap<>();

        while (notDone) {
            // determine task that have arrived by now and sort them by priority
            final int finalTime = currTime; // for lambda
            List<Process> arrived = processes.stream().filter(p -> p.getStartTime() <= finalTime).sorted(Comparator.comparing(Process::getPriority)).collect(Collectors.toList());
            int highestPriority = arrived.get(0).getPriority();
            arrived = arrived.stream().filter(p -> p.getPriority() == highestPriority).collect(Collectors.toList());
            Process highestPriorityProcess;
            if (arrived.size() > 1) {
                // get queue from map for highest priority (if it exists)
                Queue<Process> queue;
                if (priorityToProcessQueue.containsKey(highestPriority)) {
                    queue = priorityToProcessQueue.get(highestPriority);
                } else {
                    // create a new queue if it's not in the map.
                    queue = new ArrayDeque<>();
                    priorityToProcessQueue.put(highestPriority, queue);
                }
                // fill in the queue with processes
                arrived = arrived.stream().sorted(Comparator.comparing(Process::getStartTime)).collect(Collectors.toList());
                for (Process process : arrived) {
                    if (!queue.contains(process) && !process.equals(prevExecutedProcess)) {
                        queue.add(process);
                    }
                }
                if (arrived.contains(prevExecutedProcess)) {
                    queue.add(prevExecutedProcess);
                }
                // get process first in the highest priority queue
                highestPriorityProcess = priorityToProcessQueue.get(highestPriority).remove();
            } else {
                highestPriorityProcess = arrived.get(0);
            }
            // for round robin
            prevExecutedProcess = highestPriorityProcess;

            int burst = highestPriorityProcess.getBurst() < 3 ? highestPriorityProcess.getBurst() : 3;
            // execute first process
            for (int i = 0; i < burst; i++) {
                System.out.println("Executing " + highestPriorityProcess.getProcessName() + " from " + currTime + " to " + (currTime + 1));
                currTime++;
            }

            highestPriorityProcess.setBurst(highestPriorityProcess.getBurst() - burst);
            // if no work left for the current process - remove it.
            if (highestPriorityProcess.getBurst() == 0) {
                processes.remove(highestPriorityProcess);
                if (priorityToProcessQueue.containsKey(highestPriority) && priorityToProcessQueue.get(highestPriority).contains(highestPriorityProcess)) {
                    priorityToProcessQueue.get(highestPriority).remove(highestPriorityProcess);
                }
            }

            // determine if processes are left to run
            notDone = processes.size() > 0;

        }
    }

    /**
     * This method creates a list of processes from a list of string lines
     * @param lines - list of lines
     * @return list of processes
     */
    private static List<Process> linesToProcesses(List<String> lines) {
        List<Process> processes = new ArrayList<>();

        lines.stream().forEach(line -> {
            String[] splited = line.split("\\s+");
            if (splited.length != 4) {
                throw new IllegalArgumentException("A line in a file is not formatted correctly: " + line);
            } else {
                processes.add(
                        new Process(
                                splited[0],
                                new Integer(splited[1]),
                                new Integer(splited[2]),
                                new Integer(splited[3])
                        )
                );
            }
        });
        return processes;
    }

    /**
     *
     * @param filePath - full file path with a name to read from
     * @return a list of lines that file is made of
     */
    private static List<String> readFileInList(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Filepath is incorrect:" + filePath, e);
        }
    }

    private static class Process {
        private String processName;
        private int startTime;
        private int burst;
        private int priority;

        public Process(String processName, int startTime, int burst, int priority) {
            this.processName = processName;
            this.startTime = startTime;
            this.burst = burst;
            this.priority = priority;
        }

        public String getProcessName() {
            return processName;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getBurst() {
            return burst;
        }

        public void setBurst(int newBurst) {
            this.burst = newBurst;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return "Process{" +
                    "processName='" + processName + '\'' +
                    ", startTime=" + startTime +
                    ", burst=" + burst +
                    ", priority=" + priority +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Process process = (Process) o;
            return Objects.equals(processName, process.processName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(processName);
        }
    }

}
