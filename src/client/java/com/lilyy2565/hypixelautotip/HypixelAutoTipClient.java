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
            "key.hypixelautotip.toggle",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_NUMPAD1,
            KeyMapping.Category.MISC
        ));

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
                Minecraft.getInstance().gui.setOverlayMessage(
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
}