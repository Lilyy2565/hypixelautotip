package com.lilyy2565.hypixelautotip;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HypixelAutoTipConfigScreen implements ModMenuApi {

    public HypixelAutoTipConfigScreen() {
        // default constructor
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        System.out.println("HypixelAutoTip: getModConfigScreenFactory() called!");

        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Hypixel AutoTip Config"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            var general = builder.getOrCreateCategory(Component.literal("General"));

            // Enable mod config
            general.addEntry(
                entryBuilder.startBooleanToggle(Component.literal("Enable AutoTip"),
                        HypixelAutoTipClient.commandExecutionEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> HypixelAutoTipClient.commandExecutionEnabled = newValue)
                    .build()
            );

            // Persist AutoTip toggle on restart
            general.addEntry(
                entryBuilder.startBooleanToggle(Component.literal("Persist Enable AutoTip on restart"),
                        ConfigManager.config.persistAutoTipEnabled)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("When enabled, the Enable AutoTip toggle is restored on restart."))
                    .setSaveConsumer(newValue -> ConfigManager.config.persistAutoTipEnabled = newValue)
                    .build()
            );

            // Command execution interval config
            general.addEntry(
                entryBuilder.startIntField(Component.literal("Interval (in ticks)"),
                        HypixelAutoTipClient.INTERVAL_TICKS)
                    .setDefaultValue(20000)
                    .setTooltip(Component.literal("Interval in ticks between auto-tips."))
                    .setSaveConsumer(newValue -> {
                        HypixelAutoTipClient.INTERVAL_TICKS = newValue;
                        ConfigManager.config.intervalTicks = newValue;
                    })
                    .build()
            );

            // Additional entries can be added here.
            builder.setSavingRunnable(() -> {
                ConfigManager.config.autoTipEnabled = HypixelAutoTipClient.commandExecutionEnabled;
                ConfigManager.saveConfig();
            });
            return builder.build();
        };
    }

    // Optional helper method to open the config screen directly
    public static Screen createConfigScreen(Screen parent) {
        return new HypixelAutoTipConfigScreen().getModConfigScreenFactory().create(parent);
    }
}
