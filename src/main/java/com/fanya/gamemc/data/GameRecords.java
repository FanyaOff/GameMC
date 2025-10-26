package com.fanya.gamemc.data;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GameRecords {
    private static final String FILE_NAME = "arcade_records.txt";
    private static final Path SAVE_PATH = Paths.get(System.getProperty("user.home"), ".fanya_gamemc", FILE_NAME);
    private static GameRecords instance;
    private final Properties records = new Properties();

    private GameRecords() {
        load();
    }

    public static GameRecords getInstance() {
        if (instance == null) instance = new GameRecords();
        return instance;
    }

    public int getBestScore(String gameKey) {
        String val = records.getProperty(gameKey, "0");
        try { return Integer.parseInt(val); } catch (Exception e) { return 0; }
    }

    public void setBestScore(String gameKey, int score) {
        int prev = getBestScore(gameKey);
        if (score > prev) {
            records.setProperty(gameKey, String.valueOf(score));
            save();
        }
    }

    private void load() {
        try {
            if (Files.exists(SAVE_PATH)) {
                try (InputStream in = Files.newInputStream(SAVE_PATH)) {
                    records.load(in);
                }
            }
        } catch (Exception ignored) {}
    }

    private void save() {
        try {
            Files.createDirectories(SAVE_PATH.getParent());
            try (OutputStream out = Files.newOutputStream(SAVE_PATH)) {
                records.store(out, "Arcade Minigame Best Scores");
            }
        } catch (Exception ignored) {}
    }
}
