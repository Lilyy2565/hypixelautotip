package com.lilyy2565.hypixelautotip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    // The config file should be stored in the Fabric config directory.
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "hypixelautotip.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static HypixelAutoTipConfig config = new HypixelAutoTipConfig();

    public static void loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, HypixelAutoTipConfig.class);
                System.out.println("Config loaded: " + config.intervalTicks);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No config file found. Saving default config.");
            saveConfig();
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
            System.out.println("Config saved: " + config.intervalTicks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
