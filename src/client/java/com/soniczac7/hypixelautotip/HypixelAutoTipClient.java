package com.lilyy2565.hypixelautotip;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import net.minecraft.util.Identifier;


public class HypixelAutoTipClient implements ClientModInitializer {
	
    public static int INTERVAL_TICKS = 20000;
    //private static final int INTERVAL_TICKS = 20; // 1 second
    public static int tickCounter = INTERVAL_TICKS; // Set to interval to immediatly send command on join
    
    // Toggle flag for whether the auto-command execution is enabled.
    public static boolean commandExecutionEnabled = true;
    private static boolean isOnHypixel = false;
    private static boolean unknownServer = false;
    
    // Keybindings
    private KeyBinding toggleKeyBinding;
    private KeyBinding debugToggleKeyBinding;

    // Define an identifier for your custom HUD layer.
    private static final Identifier DEBUG_LAYER = Identifier.of("hypixelautotip", "debug");
	
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		
        // Load the configuration.
        ConfigManager.loadConfig();
        INTERVAL_TICKS = ConfigManager.config.intervalTicks;

        // Register the mod toggle key binding.
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "Toggle AutoTip", // Translation key (set up in language files for display)
            GLFW.GLFW_KEY_KP_1,             // Default key: KP_1 (Numpad 1)
            "Hypixel AutoTip"    // Category for grouping related mod keybinds in the controls menu
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
            ServerInfo serverInfo = client.getCurrentServerEntry();
            if (serverInfo != null) {
                String serverAddress = serverInfo.address;  // The server IP/info

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
        
        // Note: Debug info is now displayed in F3 menu via AutoTipDebugHudMixin
        // Press F3 to see AutoTip status when debug mode is enabled (F4 key)

        // Register a client tick event.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check if the toggle key was pressed.
            while (toggleKeyBinding.wasPressed()) {
                commandExecutionEnabled = !commandExecutionEnabled;
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(
                    Text.literal("AutoTip toggled: " + (commandExecutionEnabled ? "Enabled" : "Disabled")),
                    true // 'true' makes it display in the action bar
                );

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
                client.player.networkHandler.sendChatCommand("tipall");
            }
        });
	}

    // Static method to get debug info for F3 screen (called by mixin)
    public static java.util.List<String> getDebugInfo() {
        java.util.List<String> info = new java.util.ArrayList<>();
        
        info.add("");
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