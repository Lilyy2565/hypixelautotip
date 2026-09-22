package com.lilyy2565.hypixelautotip;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Arrays;

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

            // Track Rewards gained from AutoTip
            general.addEntry(
                entryBuilder.startBooleanToggle(Component.literal("Track Rewards gained from AutoTip"),
                        ConfigManager.config.trackRewards)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("When enabled, the rewards gained from AutoTip are tracked."))
                    .setSaveConsumer(newValue -> ConfigManager.config.trackRewards = newValue)
                    .build()
            );

            // AutoTip prefix color
            general.addEntry(
                entryBuilder.startDropdownMenu(Component.literal("AutoTip Prefix Color"),
                        ConfigManager.config.autoTipPrefixColor, ChatColor::fromName,
                        value -> Component.literal(value.toString()))
                    .setDefaultValue(ChatColor.LIGHT_PURPLE)
                    .setSelections(Arrays.asList(ChatColor.values()))
                    .setTooltip(Component.literal("The color of the [AT] prefix in autotip messages."))
                    .setSaveConsumer(newValue -> ConfigManager.config.autoTipPrefixColor = newValue)
                    .build()
            );

            builder.setSavingRunnable(() -> {
                ConfigManager.config.autoTipEnabled = HypixelAutoTipClient.commandExecutionEnabled;
                ConfigManager.saveConfig();
            });
            return builder.build();
        };
    }

    public static Screen createConfigScreen(Screen parent) {
        return new HypixelAutoTipConfigScreen().getModConfigScreenFactory().create(parent);
    }
}
