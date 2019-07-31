import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
        // TODO: Do it.

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
    }

}
