package com.fanya.gamemc.util;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker {

    private static final String PROJECT_ID = "Y8NDA63J";
    private static final String API_URL = "https://api.modrinth.com/v2/project/" + PROJECT_ID + "/version";

    private volatile String latestVersion = "";
    private final String currentVersion;

    private volatile boolean done = false;
    private volatile boolean failed = false;

    public VersionChecker() {
        currentVersion = FabricLoader.getInstance()
                .getModContainer("gamemc")
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();

        System.out.println("(gamemc) Current version: " + currentVersion);
    }

    public void fetchLatestVersionAsync() {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(connection.getInputStream()));

                JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();

                if (array.size() == 0) {
                    failed = true;
                    done = true;
                    return;
                }

                JsonObject newest = array.get(0).getAsJsonObject();
                String newestDate = newest.get("date_published").getAsString();

                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    String date = obj.get("date_published").getAsString();

                    if (date.compareTo(newestDate) > 0) {
                        newest = obj;
                        newestDate = date;
                    }
                }

                latestVersion = newest.get("version_number").getAsString();

            } catch (Exception e) {
                failed = true;
            }
            done = true;
        });

        thread.setDaemon(true);
        thread.start();
    }

    public boolean isReady() {
        return done && !failed;
    }

    public boolean isUpdateAvailable() {
        if (!done || latestVersion.isEmpty()) return false;

        String latestMod = extractModVersion(latestVersion);
        String currentMod = extractModVersion(currentVersion);

        return !latestMod.equals(currentMod);
    }

    public String extractModVersion(String v) {
        int plus = v.indexOf('+');
        return (plus == -1) ? v : v.substring(0, plus);
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
