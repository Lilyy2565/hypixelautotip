package com.lilyy2565.hypixelautotip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.nio.file.Files;

public class TipStatsManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final File statsFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "hypixelautotip-stats.json");
    public static TipStats stats = new TipStats();

    public static void load() {
        try {
            if (Files.exists(statsFile.toPath())) {
                stats = GSON.fromJson(Files.readString(statsFile.toPath()), TipStats.class);

                if (stats == null) {
                    stats = new TipStats();
                }

                System.out.println("[Hypixel AutoTip] Statistics loaded from file.");
            }
            else {
                stats = new TipStats();
                save();
                System.out.println("[Hypixel AutoTip] Created new statistics file.");
            }
        }
        catch (Exception e) {
            System.err.println("[Hypixel AutoTip] Failed to load statistics:");
            e.printStackTrace();
            stats = new TipStats();
        }
    }

    public static void save() {
        try {
            Files.writeString(statsFile.toPath(), GSON.toJson(stats));

            System.out.println("[Hypixel AutoTip] Statistics saved.");
        } catch (Exception e) {
            System.err.println("[Hypixel AutoTip] Failed to save statistics:");
            e.printStackTrace();
        }
    }
}