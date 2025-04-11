package com.soniczac7.hypixelautotip;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import net.minecraft.util.Identifier;


public class HypixelAutoTipClient implements ClientModInitializer {
	// 12000 ticks = 10 minutes
    public static final int INTERVAL_TICKS = 20000;
    //private static final int INTERVAL_TICKS = 20; // 1 second
    public static int tickCounter = INTERVAL_TICKS; // Set to interval to immediatly send command on join
    
    // Toggle flag for whether the auto-command execution is enabled.
    public static boolean commandExecutionEnabled = false;
    
    // Our key binding that will toggle the auto-command execution.
    private KeyBinding toggleKeyBinding;

    // Define an identifier for your custom HUD layer.
    private static final Identifier DEBUG_LAYER = Identifier.of("hypixelautotip", "debug");
	
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		// Register the key binding. In this example, we use the P key.
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "Toggle AutoTip", // Translation key (set up in language files for display)
            GLFW.GLFW_KEY_KP_1,             // Default key: P
            "Hypixel AutoTip"    // Category for grouping related mod keybinds in the controls menu
        ));
        
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
            
            // If the toggle is off or the player hasn't joined the world, skip the auto-command logic.
            if (!commandExecutionEnabled || client.player == null) {
                return;
            }
            
            // Increment the tick counter.
            tickCounter++;
            
            // If enough ticks (20 minutes) have passed, send the command.
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
        String debugText = String.format("AutoTip: %s, Tick: %d",
                commandExecutionEnabled ? "Enabled" : "Disabled",
                tickCounter);

        // Draw the debug text at coordinates (10, 10).
        drawContext.drawText(client.textRenderer, debugText, 10, 10, 0xffffff, false);
    }
}