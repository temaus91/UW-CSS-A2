import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This program is a third part of the second assignment.
 * It is design to take input from a file, and process it.
 * The scheduler will be implemented as a priority based round robin (preemtive) Quantum = 3.
 */
public class PartThree {
    public static void main (String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("You must pass the path of the file to read from.");
        }

        // get file lines
        List<String> lines = readFileInList(args[0]);

        // print file information back to the user
        System.out.println("Number of processes: " + lines.size());
        lines.forEach(System.out::println);

        // generate processes
        List<Process> processes = linesToProcesses(lines);
//        processes.forEach(System.out::println); // toString() all the processes

        // Schedule and execute processes
        int currTime = 0;
        boolean notDone = true;

        while (notDone) {
            // determine task that have arrived by now and sort them by priority
            final int finalTime = currTime; // for lambda
            List<Process> arrived = processes.stream().filter(p -> p.getStartTime() <= finalTime).sorted(Comparator.comparing(Process::getPriority)).collect(Collectors.toList());
            int highestPriority = arrived.get(0).getPriority();
            arrived = arrived.stream().filter(p -> p.getPriority() == highestPriority).collect(Collectors.toList());
            if (arrived.size() > 1) {
                // TODO: Handle special case
                System.out.println("RoundRobinSpecialCase!"); //TODO: Remove. Debugging.
            }
//            System.out.println("Processes at " + time); // TODO: Remove. Debugging
//            arrived.forEach(System.out::println); // TODO: Remove. Debugging

            //TODO: if equal priority, should not be round robin.
            Process highestPriorityProcess = arrived.get(0);
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
            }

//            System.out.println("Processes after burst at " + time); // TODO: Remove. Debugging
//            arrived.forEach(System.out::println); // TODO: Remove. Debugging

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
