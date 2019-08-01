import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This program is a first part of the second assignment.
 * It is design to take input from a file, and process it.
 * The scheduler will be implemented as a shortest job first with Quantum = 1.
 * @author Artem Tarasenko
 */
public class PartOne {
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

        while (notDone) {
            // determine task that have arrived by now and sort them by priority
            final int finalTime = currTime; // for lambda
            List<Process> arrived = processes.stream().filter(p -> p.getStartTime() <= finalTime).sorted(Comparator.comparing(Process::getBurst)).collect(Collectors.toList());
            Process highestPriorityProcess = arrived.get(0);

            // execute first process
            System.out.println("Executing " + highestPriorityProcess.getProcessName() + " from " + currTime + " to " + (currTime + 1));
            currTime++;

            highestPriorityProcess.setBurst(highestPriorityProcess.getBurst() - 1);
            // if no work left for the current process - remove it.
            if (highestPriorityProcess.getBurst() == 0) {
                processes.remove(highestPriorityProcess);
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
