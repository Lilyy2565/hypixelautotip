package com.lilyy2565.hypixelautotip;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import java.net.URI;


public class HypixelAutoTipClient implements ClientModInitializer {
	
    public static int INTERVAL_TICKS = 20000;
    //private static final int INTERVAL_TICKS = 20; // 1 second
    public static int tickCounter = INTERVAL_TICKS; // Set to interval to immediatly send command on join
    
    // Toggle flag for whether the auto-command execution is enabled.
    public static boolean commandExecutionEnabled = true;
    private static boolean isOnHypixel = false;
    private static boolean unknownServer = false;
    
    // Keybindings
    private KeyMapping toggleKeyBinding;
	
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		
        // Load the configuration.
        ConfigManager.loadConfig();
        INTERVAL_TICKS = ConfigManager.config.intervalTicks;
        if (ConfigManager.config.persistAutoTipEnabled) {
            commandExecutionEnabled = ConfigManager.config.autoTipEnabled;
        } else {
            commandExecutionEnabled = true;
        }

        // Register the mod toggle key binding.
        toggleKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "Toggle AutoTip",
            //? if >=26.3 {
            InputConstants.Type.KEYBOARD,
            //?} else
            //InputConstants.Type.KEYSYM,

            InputConstants.KEY_NUMPAD1,
            KeyMapping.Category.MISC
        ));

        // Command registration for the /autotip command.
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(createCommand("autotip"));
            dispatcher.register(createCommand("at"));
        });

        // DEBUG Keybinds
        /*KeyBinding configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "Open Hypixel AutoTip Config", GLFW.GLFW_KEY_F5, "Hypixel AutoTip"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKeyBinding.wasPressed()) {
                client.setScreen(HypixelAutoTipConfigScreen.createConfigScreen(client.currentScreen));
            }
        });*/
        // END DEBUG Keybinds

        // Listen for when the player joins a server.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerData serverInfo = client.getCurrentServer();
            if (serverInfo != null) {
                String serverAddress = serverInfo.ip;

                if (serverAddress.contains("hypixel.net")) {
                    isOnHypixel = true;
                    unknownServer = false;
                } else {
                    isOnHypixel = false;
                    unknownServer = false;
                }
            } else {
                isOnHypixel = false;
                unknownServer = true;
            }
        });
        
        // Debug status is exposed through Minecraft's debug entry system.
        // Use F3+F6 to set this entry to Off / Overlay / Always On.

        // Register a client tick event.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check if the toggle key was pressed.
            while (toggleKeyBinding.consumeClick()) {
                commandExecutionEnabled = !commandExecutionEnabled;
                Minecraft.getInstance().gui
                    //? if >=26.2
                    .hud
                    .setOverlayMessage(
                        Component.literal("AutoTip toggled: " + (commandExecutionEnabled ? "Enabled" : "Disabled")),
                        true // 'true' makes it display in the action bar
                    );

                if (ConfigManager.config.persistAutoTipEnabled) {
                    ConfigManager.config.autoTipEnabled = commandExecutionEnabled;
                    ConfigManager.saveConfig();
                }

                // Reset counter when enabled
                if(commandExecutionEnabled){
                    tickCounter = INTERVAL_TICKS;
                }


                // Give client feedback about the new toggle state.
                /*if (client.player != null) {
                    client.player.sendMessage(
                        Text.literal("AutoTip toggled: " + (commandExecutionEnabled ? "Enabled" : "Disabled")), 
                        false
                    );
                }*/
            }

            // If the toggle is off, the player hasn't joined the world, the player isnt on hypixel or is in an unknown server skip the auto-command logic.
            if (!commandExecutionEnabled || client.player == null || !isOnHypixel || unknownServer) {
                return;
            }
            
            // Increment the tick counter.
            tickCounter++;
            
            // If enough ticks have passed, send the command.
            if (tickCounter >= INTERVAL_TICKS) {
                tickCounter = 0;
                // This sends the command as if the player typed it.
                client.player.connection.sendCommand("tipall");
            }
        });
	}

    // Static method to get debug info for F3 screen (called by mixin)
    public static java.util.List<String> getDebugInfo() {
        java.util.List<String> info = new java.util.ArrayList<>();
        
        // Add header
        info.add("§6[Hypixel AutoTip]");
        
        // Server status
        if (unknownServer) {
            info.add("Server: §cUnknown");
        } else if (isOnHypixel) {
            info.add("Server: §aHypixel");
        } else {
            info.add("Server: §cNot Hypixel");
        }
        
        // AutoTip status
        info.add("AutoTip: " + (commandExecutionEnabled ? "§aEnabled" : "§cDisabled"));
        
        // Tick counter (only show when on Hypixel and enabled)
        if (isOnHypixel && commandExecutionEnabled) {
            int secondsRemaining = (INTERVAL_TICKS - tickCounter) / 20;
            int minutesRemaining = secondsRemaining / 60;
            int secsRemaining = secondsRemaining % 60;
            info.add(String.format("Next tip: §e%dm %ds", minutesRemaining, secsRemaining));
        }
        
        return info;
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> createCommand(String name) {
        return ClientCommands.literal(name)
            .then(ClientCommands.literal("status").executes(context -> {
                context.getSource().sendFeedback(Component.literal("§d§l[AT] §rHypixel AutoTip is currently §" + (commandExecutionEnabled ? "aenabled" : "cdisabled") + "§r."));
                return 1;
            }))
            .then(ClientCommands.literal("config").executes(context -> {
                context.getSource().sendFeedback(Component.literal("§d§l[AT] §rOpening config screen..."));
                Minecraft client = context.getSource().getClient();
                //? if >=26.2 {
                client.execute(() -> client.gui.setScreen(HypixelAutoTipConfigScreen.createConfigScreen(client.gui.screen())));
                //? } else {
                //client.execute(() -> client.setScreen(HypixelAutoTipConfigScreen.createConfigScreen(client.screen)));
                //? }
                return 1;
            }))
            .then(ClientCommands.literal("info").executes(context -> {
                String version = FabricLoader.getInstance().getModContainer("hypixelautotip").map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
                context.getSource().sendFeedback(Component.literal("§d§l[AT] §rHypixel AutoTip v" + version + " by §5§oLilyy2565§r."));
                context.getSource().sendFeedback(Component.literal("§d§l[AT] §b§nLink to Project's Github")
                    .withStyle(style -> style.withClickEvent(new net.minecraft.network.chat.ClickEvent.OpenUrl(URI.create("https://github.com/Lilyy2565/HypixelAutoTip")))
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("§ehttps://github.com/Lilyy2565/HypixelAutoTip")))));
                context.getSource().sendFeedback(Component.literal("§d§l[AT] §b§nLink to Project's Modrinth Page")
                    .withStyle(style -> style.withClickEvent(new net.minecraft.network.chat.ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/hypixelautotip")))
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("§ehttps://modrinth.com/mod/hypixelautotip")))));
                context.getSource().sendFeedback(Component.literal("§d§l[AT] §b§nLink to Project's CurseForge Page")
                    .withStyle(style -> style.withClickEvent(new net.minecraft.network.chat.ClickEvent.OpenUrl(URI.create("https://www.curseforge.com/minecraft/mc-mods/hypixel-auto-tip")))
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("§ehttps://www.curseforge.com/minecraft/mc-mods/hypixel-auto-tip")))));
                return 1;
            }));
    };
}
