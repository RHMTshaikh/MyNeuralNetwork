package Visualisation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Position {
    int x;
    int y;
    long time = -1; // To track the time of the event

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
class Touch {
    Position startPosition = new Position(-1, -1);
    Position endPosition = new Position(-1, -1);
    boolean active = false; // To track if the touch is currently down
    Touch() {
        this.startPosition.time = System.currentTimeMillis();
        this.endPosition.time = System.currentTimeMillis();
    }

    public void setX(int x) {
        if (this.startPosition.x == -1) this.startPosition.x = x;
        this.endPosition.x = x;
    }
    public void setY(int y) {
        if (this.startPosition.y == -1) this.startPosition.y = y;
        this.endPosition.y = y;
    }
    
    @Override
    public String toString() {
        return "(" + startPosition + " to " + endPosition + ")";
    }
}

public class AdbTouchEventParser {

    // Event types
    private static final int EV_SYN = 0x0000;
    private static final int EV_ABS = 0x0003;

    // ABS event codes for multi-touch (Type B protocol)
    private static final int ABS_MT_SLOT = 0x002f;        // 47 decimal
    private static final int ABS_MT_TRACKING_ID = 0x0039; // 57 decimal
    private static final int ABS_MT_POSITION_X = 0x0035;  // 53 decimal
    private static final int ABS_MT_POSITION_Y = 0x0036;  // 54 decimal

    // SYN event codes
    private static final int SYN_REPORT = 0x0000;

    // Regex to parse the getevent line
    private static final Pattern GETEVENT_PATTERN = Pattern.compile(
        "(?:\\[\\s*\\d+\\.\\d+\\s*\\]\\s+)?([0-9a-fA-F]{4})\\s+([0-9a-fA-F]{4})\\s+([0-9a-fA-F]{8})"
    );

    public static void main(String[] args) {
        boolean debug_mode = false; // Set to true for debugging
        String deviceEventPath = "/dev/input/event4"; // Replace with your device's event path

        Process adbProcess = null;

        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "shell", "getevent", deviceEventPath);
            // pb.redirectErrorStream(true); // Optional: merge stderr into stdout

            System.out.println("Starting adb getevent for " + deviceEventPath + "...");
            adbProcess = pb.start();
            final Process finalAdbProcess = adbProcess; // For use in lambda

            // Add a shutdown hook to ensure adb process is killed when Java app exits
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (finalAdbProcess != null && finalAdbProcess.isAlive()) {
                    System.out.println("Shutting down adb process...");
                    finalAdbProcess.destroyForcibly(); // Force kill
                }
            }));

            // Thread to read and print error stream from adb process
            // This is important to prevent the adb process from blocking if its error buffer fills up
            new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(finalAdbProcess.getErrorStream()))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        System.err.println("ADB_ERROR: " + errorLine);
                    }
                } catch (IOException e) {
                    // This can happen if the process is destroyed
                    if (!e.getMessage().toLowerCase().contains("stream closed")) {
                        System.err.println("Error reading adb error stream: " + e.getMessage());
                    }
                }
            }).start();


            Map<Integer, Touch> activeTouches = new HashMap<>();
            int currentSlot = 0;

            System.out.println("Listening for touch events. Press Ctrl+C to stop.");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(adbProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = GETEVENT_PATTERN.matcher(line.trim());
                    if (matcher.matches()) {
                        try {
                            int type = Integer.parseInt(matcher.group(1), 16);
                            int code = Integer.parseInt(matcher.group(2), 16);
                            int value = (int) Long.parseLong(matcher.group(3), 16);

                            if (type == EV_ABS) {
                                if (debug_mode) System.out.print("EV_ABS ");
                                Touch current_touch =  activeTouches.computeIfAbsent(currentSlot, k -> {
                                    Touch touch = new Touch();
                                    if (debug_mode) System.out.print("                         New Slot " + k + ": " + touch + " ");
                                    return touch;
                                });
                                switch (code) {
                                    case ABS_MT_SLOT:
                                        if (debug_mode) System.out.print("ABS_MT_SLOT        " + value);
                                        currentSlot = value;
                                        current_touch = activeTouches.computeIfAbsent(currentSlot, k -> {
                                            Touch touch = new Touch();
                                            if (debug_mode) System.out.print("     New Slot " + k + ": " + touch);
                                            return touch;
                                        });
                                        break;
                                    case ABS_MT_TRACKING_ID:
                                        if (debug_mode) System.out.print("ABS_MT_TRACKING_ID " + value);
                                        if (value == -1 || value == 0xffffffff) {
                                            current_touch.active = false;
                                            current_touch.endPosition.time = System.currentTimeMillis();
                                        } else {
                                            current_touch.active = true;
                                        }
                                        break;
                                    case ABS_MT_POSITION_X:
                                        if (debug_mode) System.out.print("ABS_MT_POSITION_X  " + value);
                                        current_touch.setX(value);
                                        break;
                                    case ABS_MT_POSITION_Y:
                                        if (debug_mode) System.out.print("ABS_MT_POSITION_Y  " + value);
                                        current_touch.setY(value);
                                        break;
                                }
                                if (debug_mode) System.out.println();
                            } else if (type == EV_SYN && code == SYN_REPORT) {
                                if (debug_mode) System.out.print("EV_SYN SYN_REPORT         " + value);
                                StringBuilder output = new StringBuilder("     Current Touches: ");
                                boolean hasActiveTouches = false;
                                activeTouches.entrySet().removeIf(entry -> {
                                    Touch touch = entry.getValue();
                                    if (!touch.active) {
                                        if (debug_mode) System.out.print("     Removing Slot " + entry.getKey() + ": " + touch.endPosition);
                                        else System.out.println("     Removing Slot " + entry.getKey() + ": " + touch.endPosition + '\n');
                                        return true; // Remove inactive touches
                                    }
                                    return false; // Keep active touches
                                });

                                for (Map.Entry<Integer, Touch> entry : activeTouches.entrySet()) {
                                    output.append("Slot ")
                                        .append(entry.getKey())
                                        .append(": ")
                                        .append(entry.getValue().endPosition)
                                        .append(" | ")
                                    ;

                                    hasActiveTouches = true;
                                }
                                if (hasActiveTouches) {
                                    System.out.println(output.substring(0, output.length() - 3));
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Could not parse line: " + line + " - " + e.getMessage());
                        }
                    }
                }
            }
            // Wait for the process to complete (it won't, naturally, for getevent unless an error occurs or it's killed)
            int exitCode = adbProcess.waitFor();
            System.out.println("adb process exited with code: " + exitCode);

        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Process interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Preserve interrupt status
        } finally {
            if (adbProcess != null && adbProcess.isAlive()) {
                System.out.println("Ensuring adb process is destroyed...");
                adbProcess.destroyForcibly();
            }
            System.out.println("Touch Event Parser stopped.");
        }
    }
}