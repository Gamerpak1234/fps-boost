package com.fpsboost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Raises the JVM process's OS scheduling priority so the OS gives the game
 * more CPU time relative to background processes.
 *
 * Notes on real-world behavior:
 * - Windows: setting a process to "High" priority does not require
 *   administrator rights in the vast majority of cases, so this generally
 *   just works.
 * - Linux/macOS: lowering the "nice" value (raising priority) requires
 *   elevated privileges (root) or the CAP_SYS_NICE capability. Without it,
 *   the renice call will fail silently and the game keeps running at
 *   normal priority - this is expected, not a bug.
 * - This only affects OS thread/process scheduling. It won't fix a GPU
 *   bottleneck, and it won't do much if your CPU isn't the bottleneck.
 */
public final class ProcessPriorityBooster {
    private static final Logger LOGGER = LoggerFactory.getLogger("fpsboost");

    private ProcessPriorityBooster() {
    }

    public static void applyHighPriority() {
        long pid = ProcessHandle.current().pid();
        String os = System.getProperty("os.name", "").toLowerCase();

        try {
            if (os.contains("win")) {
                applyWindows(pid);
            } else if (os.contains("mac")) {
                applyUnix(pid, -10);
            } else {
                // Linux and other unix-likes
                applyUnix(pid, -10);
            }
        } catch (Exception e) {
            LOGGER.warn("[fpsboost] Could not raise process priority: {}", e.getMessage());
        }
    }

    private static void applyWindows(long pid) throws Exception {
        // "High" priority (not "Realtime" - that one can starve system
        // processes and cause instability, so we deliberately avoid it).
        String cmd = "(Get-Process -Id " + pid + ").PriorityClass = 'High'";
        ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe", "-NoProfile", "-NonInteractive", "-Command", cmd
        );
        run(pb, "Windows PriorityClass=High");
    }

    private static void applyUnix(long pid, int niceValue) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "renice", "-n", String.valueOf(niceValue), "-p", String.valueOf(pid)
        );
        run(pb, "renice " + niceValue);
    }

    private static void run(ProcessBuilder pb, String label) throws Exception {
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(' ');
            }
        }
        int exit = proc.waitFor();
        if (exit == 0) {
            LOGGER.info("[fpsboost] Process priority raised ({})", label);
        } else {
            LOGGER.warn("[fpsboost] Priority command '{}' exited with {} - this usually means "
                    + "the OS denied the request (e.g. no admin/root). Game will run at normal priority. Output: {}",
                    label, exit, output.toString().trim());
        }
    }
}
