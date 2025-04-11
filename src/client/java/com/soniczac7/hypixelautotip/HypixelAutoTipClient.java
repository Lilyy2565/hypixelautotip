package com.soniczac7.hypixelautotip;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import net.minecraft.util.Identifier;


public class HypixelAutoTipClient implements ClientModInitializer {
	
    public static final int INTERVAL_TICKS = 20000;
    //private static final int INTERVAL_TICKS = 20; // 1 second
    public static int tickCounter = INTERVAL_TICKS; // Set to interval to immediatly send command on join
    
    // Toggle flag for whether the auto-command execution is enabled.
    public static boolean commandExecutionEnabled = false;
    private static boolean isOnHypixel = false;
    private static boolean unknownServer = false;
    private static boolean doDebug = false;
    
    // Keybindings
    private KeyBinding toggleKeyBinding;
    private KeyBinding debugToggleKeyBinding;

    // Define an identifier for your custom HUD layer.
    private static final Identifier DEBUG_LAYER = Identifier.of("hypixelautotip", "debug");
	
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		
        // Register the mod toggle key binding.
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "Toggle AutoTip", // Translation key (set up in language files for display)
            GLFW.GLFW_KEY_KP_1,             // Default key: KP_1 (Numpad 1)
            "Hypixel AutoTip"    // Category for grouping related mod keybinds in the controls menu
        ));

        // Register the debug toggle key binding.
        debugToggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "Toggle Debug", // Translation key (set up in language files for display)
            GLFW.GLFW_KEY_F4,             // Default key: F4
            "Hypixel AutoTip"    // Category for grouping related mod keybinds in the controls menu
        ));

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
        
        // Register a HUD layer before the chat layer.
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            renderHud(matrixStack, MinecraftClient.getInstance().getRenderTickCounter());
        });

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
            
            while (debugToggleKeyBinding.wasPressed()) {
                doDebug = !doDebug;
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(
                    Text.literal("AutoTip Debug toggled: " + (doDebug ? "Enabled" : "Disabled")),
                    true // 'true' makes it display in the action bar
                );
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

    // This method will be called to render your debug info.
    private static void renderHud(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        if(unknownServer && doDebug){
            drawContext.drawText(client.textRenderer, "AutoTip: Could not fetch server address!", 10, 10, 0xff0000, false);
            return;
        }
        else if(!isOnHypixel && doDebug){
            drawContext.drawText(client.textRenderer, "AutoTip: Not on Hypixel!", 10, 10, 0xff0000, false);
            return;
        }
        else if (doDebug){
            String debugText = String.format("AutoTip Debug: %s, Tick: %d/%d",
            commandExecutionEnabled ? "Enabled" : "Disabled",
            tickCounter, INTERVAL_TICKS);

            // Draw the debug text at coordinates (10, 10).
            drawContext.drawText(client.textRenderer, debugText, 10, 10, 0xffffff, false);
        }
    }
}