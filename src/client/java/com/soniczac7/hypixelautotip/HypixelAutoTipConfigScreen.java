package com.soniczac7.hypixelautotip;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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
                    .setTitle(Text.literal("Hypixel AutoTip Config"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            var general = builder.getOrCreateCategory(Text.literal("General"));

            // Enable mod config
            general.addEntry(
                entryBuilder.startBooleanToggle(Text.literal("Enable AutoTip"),
                        HypixelAutoTipClient.commandExecutionEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> HypixelAutoTipClient.commandExecutionEnabled = newValue)
                    .build()
            );

            // Command execution interval config
            general.addEntry(
                entryBuilder.startIntField(Text.literal("Interval (in ticks)"),
                        HypixelAutoTipClient.INTERVAL_TICKS)
                    .setDefaultValue(20000)
                    .setTooltip(Text.literal("Interval in ticks between auto-tips."))
                    .setSaveConsumer(newValue -> HypixelAutoTipClient.INTERVAL_TICKS = newValue)
                    .build()
            );

            // Additional entries can be added here.
            builder.setSavingRunnable(() -> {
                // Persist configuration if needed.
            });
            return builder.build();
        };
    }

    // Optional helper method to open the config screen directly
    public static Screen createConfigScreen(Screen parent) {
        return new HypixelAutoTipConfigScreen().getModConfigScreenFactory().create(parent);
    }
}
