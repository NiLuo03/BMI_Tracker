package com.bmitracker.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ChatHistoryStore {

    private static final String DIR = "data";
    private static final int MAX_HISTORY = 10;
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    public static class HistoryEntry {
        public final String time;
        public final String preview;
        public final List<String[]> rounds;

        public HistoryEntry(String time, String preview, List<String[]> rounds) {
            this.time = time;
            this.preview = preview;
            this.rounds = rounds;
        }
    }

    private static Path getPath(int userId) {
        return Paths.get(DIR, "chat_history_" + userId + ".txt");
    }

    public static List<HistoryEntry> load(int userId) {
        List<HistoryEntry> list = new ArrayList<>();
        Path path = getPath(userId);
        if (!Files.exists(path)) return list;
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String time = null, preview = null;
            List<String[]> rounds = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("### ")) {
                    if (time != null && !rounds.isEmpty()) {
                        list.add(new HistoryEntry(time, preview, rounds));
                    }
                    time = line.substring(4);
                    preview = null;
                    rounds = new ArrayList<>();
                } else if (line.startsWith("@ ")) {
                    preview = line.substring(2);
                } else if (line.startsWith("> ") || line.startsWith("< ")) {
                    String role = line.startsWith("> ") ? "user" : "assistant";
                    String content = line.substring(2);
                    rounds.add(new String[]{role, content});
                }
            }
            if (time != null && !rounds.isEmpty()) {
                list.add(new HistoryEntry(time, preview, rounds));
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    public static void save(int userId, String preview, List<String[]> rounds) {
        try {
            Files.createDirectories(Paths.get(DIR));
            Path path = getPath(userId);
            List<HistoryEntry> all = load(userId);
            String time = LocalDateTime.now().format(TF);
            all.add(0, new HistoryEntry(time, preview, rounds));
            if (all.size() > MAX_HISTORY) all = all.subList(0, MAX_HISTORY);

            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (HistoryEntry e : all) {
                    bw.write("### " + e.time);
                    bw.newLine();
                    bw.write("@ " + (e.preview != null ? e.preview : ""));
                    bw.newLine();
                    for (String[] r : e.rounds) {
                        bw.write((r[0].equals("user") ? "> " : "< ") + r[1]);
                        bw.newLine();
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void clear(int userId) {
        try { Files.deleteIfExists(getPath(userId)); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
