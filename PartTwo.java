import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This program is a second part of the second assignment.
 * It is design to take input from a file, and process it.
 * The scheduler will be implemented as a round robin (non preemptive) Quantum = 3.
 * @author Artem Tarasenko
 */
public class PartTwo {
    public static void main (String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("You must pass the path of the file to read from.");
        }

        // get file lines
        List<String> lines = readFileInList(args[0]);

        // generate processes
        List<Process> processes = linesToProcesses(lines);
        int numOfProcesses = processes.size();

        // print file information back to the user
        System.out.println("Number of processes: " + numOfProcesses + "\n");
        lines.forEach(System.out::println);

        // create maps to track TAT, wait time, and total burst time
        Map<String, Integer> processToBurstTime = processes.stream().collect(
                Collectors.toMap(Process::getProcessName, Process::getBurst));
        Map<String,Integer> processToTAT = new HashMap<>();
        Map<String,Integer> processToWait = new HashMap<>();

        // Schedule and execute processes
        int currTime = 0;
        boolean notDone = true;
        Process prevExecutedProcess = null;

        // Create a map to manage round robin. Priority to ProcessQueue
        Queue<Process> myQueue = new ArrayDeque<>();

        while (notDone) {
            // determine task that have arrived by now and sort them by priority
            final int finalTime = currTime; // for lambda
            List<Process> arrived = processes.stream().filter(p -> p.getStartTime() <= finalTime).sorted(Comparator.comparing(Process::getStartTime)).collect(Collectors.toList());
            Process highestPriorityProcess;
            if (arrived.size() > 1) {
                // fill in the queue with processes
                for (Process process : arrived) {
                    if (!myQueue.contains(process) && !process.equals(prevExecutedProcess)) {
                        myQueue.add(process);
                    }
                }
                if (arrived.contains(prevExecutedProcess)) {
                    myQueue.add(prevExecutedProcess);
                }
                // get process first in the highest priority queue
                highestPriorityProcess = myQueue.remove();
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
                // update metrics like wait and TAT times
                String pName = highestPriorityProcess.getProcessName();
                Integer pTAT = currTime - highestPriorityProcess.getStartTime();
                Integer pWaitTime = pTAT - processToBurstTime.get(pName);
                processToTAT.put(pName,pTAT);
                processToWait.put(pName, pWaitTime);
                // remove
                processes.remove(highestPriorityProcess);
                myQueue.remove(highestPriorityProcess);
            }

            // determine if processes are left to run
            notDone = processes.size() > 0;

        }

        // print TAT and avg Wait time
        processToTAT.forEach( (pName, pTAT)-> System.out.println(pName + " TAT is: " + pTAT));
        AtomicInteger totalWaitTime = new AtomicInteger();
        processToWait.forEach( (pName, pWait) -> totalWaitTime.set(totalWaitTime.get() + pWait));
        double avgWait = (double) totalWaitTime.get() / numOfProcesses;
        System.out.println("Average wait time: " + avgWait);
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
            if (splited.length != 3) {
                throw new IllegalArgumentException("A line in a file is not formatted correctly: " + line);
            } else {
                processes.add(
                        new Process(
                                splited[0],
                                new Integer(splited[1]),
                                new Integer(splited[2])
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

        public Process(String processName, int startTime, int burst) {
            this.processName = processName;
            this.startTime = startTime;
            this.burst = burst;
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

        @Override
        public String toString() {
            return "Process{" +
                    "processName='" + processName + '\'' +
                    ", startTime=" + startTime +
                    ", burst=" + burst +
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
