package com.bmitracker.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class WrongQuestionStore {

    private static final String DIR = "data";
    private static final String PREFIX = "wrong_";

    public static class Entry {
        public final String questionLine;
        public final String userAnswer;

        public Entry(String questionLine, String userAnswer) {
            this.questionLine = questionLine;
            this.userAnswer = userAnswer;
        }
    }

    private static Path getPath(int userId) {
        return Paths.get(DIR, PREFIX + userId + ".txt");
    }

    public static List<Entry> load(int userId) {
        List<Entry> list = new ArrayList<>();
        Path path = getPath(userId);
        if (!Files.exists(path)) return list;
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                int sep = line.lastIndexOf('|');
                if (sep < 0) continue;
                String qLine = line.substring(0, sep);
                String ans = sep + 1 < line.length() ? line.substring(sep + 1) : "-";
                list.add(new Entry(qLine, ans));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void save(int userId, List<Entry> entries) {
        try {
            Files.createDirectories(Paths.get(DIR));
            Path path = getPath(userId);
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (Entry e : entries) {
                    bw.write(e.questionLine + "|" + e.userAnswer);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void add(int userId, String questionLine, String userAnswer) {
        List<Entry> list = load(userId);
        String key = extractId(questionLine);
        for (Entry e : list) {
            if (extractId(e.questionLine).equals(key)) return;
        }
        list.add(new Entry(questionLine, userAnswer));
        save(userId, list);
    }

    public static void remove(int userId, String questionLine) {
        String key = extractId(questionLine);
        List<Entry> list = load(userId).stream()
                .filter(e -> !extractId(e.questionLine).equals(key))
                .collect(Collectors.toList());
        save(userId, list);
    }

    public static int count(int userId) {
        return load(userId).size();
    }

    public static void clear(int userId) {
        try {
            Files.deleteIfExists(getPath(userId));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractId(String questionLine) {
        String[] parts = questionLine.split("\\|", 2);
        return parts.length > 0 ? parts[0] : questionLine;
    }
}
