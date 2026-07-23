package com.bmitracker.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class BackgroundHistoryStore {

    private static final String DIR = "data";
    private static final int MAX = 8;

    public static class Entry {
        public final String path;
        public final int cropX, cropY, cropW, cropH;

        public Entry(String path, int cx, int cy, int cw, int ch) {
            this.path = path;
            this.cropX = cx;
            this.cropY = cy;
            this.cropW = cw;
            this.cropH = ch;
        }
    }

    private static Path getPath(int userId) {
        return Paths.get(DIR, "bg_history_" + userId + ".txt");
    }

    public static List<Entry> load(int userId) {
        List<Entry> list = new ArrayList<>();
        Path path = getPath(userId);
        if (!Files.exists(path)) return list;
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;
                list.add(new Entry(parts[0],
                        Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]), Integer.parseInt(parts[4])));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static void add(int userId, Entry entry) {
        try {
            Files.createDirectories(Paths.get(DIR));
            List<Entry> all = load(userId);
            all.removeIf(e -> e.path.equals(entry.path));
            all.add(0, entry);
            if (all.size() > MAX) all = all.subList(0, MAX);

            Path path = getPath(userId);
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (Entry e : all) {
                    bw.write(e.path + "|" + e.cropX + "|" + e.cropY + "|" + e.cropW + "|" + e.cropH);
                    bw.newLine();
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}
