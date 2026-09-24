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
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.HoverEvent;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;


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

    // Stats reset confirmation
    private static boolean resetStatsConfirmationPending = false;
    private static long resetStatsConfirmationTime = 0;
	
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

        // Intercept chat messages to detect successful tip messages and log them.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            handleTipMessage(message);
        });

        // Load the tip statistics.
        TipStatsManager.load();

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
        return ClientCommands.literal(name).executes(context -> {  // No subcommand supplied
            context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §cInvalid command. Use §e/at help §cfor a list of commands."));
            return 1;
        })
            .then(ClientCommands.literal("status").executes(context -> {  // Status command
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §rHypixel AutoTip is currently §" + (commandExecutionEnabled ? "aenabled" : "cdisabled") + "§r."));
                if(isOnHypixel && commandExecutionEnabled){
                    int secondsRemaining = (INTERVAL_TICKS - tickCounter) / 20;
                    int minutesRemaining = secondsRemaining / 60;
                    int secsRemaining = secondsRemaining % 60;
                    context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §rNext tip in: §e" + minutesRemaining + "m " + secsRemaining + "s§r."));
                }
                if(!isOnHypixel){
                    context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §eThe current server is not detected as Hypixel.\n§eAutoTip will not run, if this is a mistake, please make a ")
                        .append(Component.literal("§b§nbug report").withStyle(style -> style
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent.OpenUrl(URI.create("https://github.com/Lilyy2565/hypixelautotip/issues/new?template=bug_report.yml")))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("§ehttps://github.com/Lilyy2565/hypixelautotip/issues/new?template=bug_report.yml"))))
                        .append(Component.literal("§r§e."))));
                }
                return 1;
            }))
            .then(ClientCommands.literal("config").executes(context -> {  // Config command
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §rOpening config screen..."));
                Minecraft client = context.getSource().getClient();
                //? if >=26.2 {
                client.execute(() -> client.gui.setScreen(HypixelAutoTipConfigScreen.createConfigScreen(client.gui.screen())));
                //? } else {
                //client.execute(() -> client.setScreen(HypixelAutoTipConfigScreen.createConfigScreen(client.screen)));
                //? }
                return 1;
            }))
            .then(ClientCommands.literal("info").executes(context -> {  // Mod info command
                String version = FabricLoader.getInstance().getModContainer("hypixelautotip").map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §rHypixel AutoTip v" + version + " by §5§oLilyy2565§r."));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §b§nReport a Bug")
                    .withStyle(style -> style.withClickEvent(new net.minecraft.network.chat.ClickEvent.OpenUrl(URI.create("https://github.com/Lilyy2565/HypixelAutoTip/issues/new?template=bug_report.yml")))
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("§ehttps://github.com/Lilyy2565/HypixelAutoTip/issues/new?template=bug_report.yml")))));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §b§nLink to Project's Github")
                    .withStyle(style -> style.withClickEvent(new net.minecraft.network.chat.ClickEvent.OpenUrl(URI.create("https://github.com/Lilyy2565/HypixelAutoTip")))
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("§ehttps://github.com/Lilyy2565/HypixelAutoTip")))));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §b§nLink to Project's Modrinth Page")
                    .withStyle(style -> style.withClickEvent(new net.minecraft.network.chat.ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/hypixelautotip")))
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("§ehttps://modrinth.com/mod/hypixelautotip")))));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §b§nLink to Project's CurseForge Page")
                    .withStyle(style -> style.withClickEvent(new net.minecraft.network.chat.ClickEvent.OpenUrl(URI.create("https://www.curseforge.com/minecraft/mc-mods/hypixel-auto-tip")))
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("§ehttps://www.curseforge.com/minecraft/mc-mods/hypixel-auto-tip")))));
                return 1;
            }))
            .then(ClientCommands.literal("stats").executes(context -> {  // Stats command
                TipStats stats = TipStatsManager.stats;
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §r§nHypixel AutoTip Statistics:"));
                if(!ConfigManager.config.trackRewards){ // Warn about tracking being disabled in the config
                    context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §eWARNING: Tracking rewards is disabled in the config!"));
                }
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §rTotal Tips: §e" + stats.totalTips));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §rTotal Players Tipped: §e" + stats.totalPlayersTipped));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §rTotal Games Tipped: §e" + stats.totalGamesTipped));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §rTotal Hypixel Experience: §e" + stats.totalHypixelExperience));
                if (!stats.totalRewards.isEmpty()) {
                    context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §rTotal Rewards:"));
                    for (Map.Entry<String, Long> entry : stats.totalRewards.entrySet()) {
                        context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §r - " + entry.getKey() + ": §e" + entry.getValue()));
                    }
                }
                else{
                    context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §cNo rewards have been received yet."));
                }
                return 1;
            }))
            .then(ClientCommands.literal("reload").executes(context -> {  // Reload configs command
                ConfigManager.loadConfig();
                TipStatsManager.load();
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §aReloaded Hypixel AutoTip!"));
                return 1;
            }))
            .then(ClientCommands.literal("resetstats").executes(context -> {  // Reset stats command
                resetStatsConfirmationPending = true;
                resetStatsConfirmationTime = System.currentTimeMillis();
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §eWARNING: This will reset all your Hypixel AutoTip statistics!"));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §eRun §5§o/at resetstats confirm §eto confirm the reset."));
                return 1;
            }).then(ClientCommands.literal("confirm").executes(context -> {  // Confirm reset stats command
                if (resetStatsConfirmationPending) {
                    if (System.currentTimeMillis() - resetStatsConfirmationTime > 30000) { // 30 seconds expiration
                        resetStatsConfirmationPending = false;
                        resetStatsConfirmationTime = 0;
                        context.getSource().sendFeedback(
                                Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §cThe reset confirmation has expired.")
                        );
                        return 1;
                    }
                    // Reset the statistics
                    TipStatsManager.stats = new TipStats();
                    TipStatsManager.save();
                    resetStatsConfirmationPending = false;
                    resetStatsConfirmationTime = 0;
                    context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §aAll Hypixel AutoTip statistics have been reset."));
                } else {
                    context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §cThere is no pending reset confirmation."));
                }
                return 1;
            })))
            .then(ClientCommands.literal("toggle").executes(context -> {  // Toggle command
                commandExecutionEnabled = !commandExecutionEnabled; // Toggle the state of tipping
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §rAutoTip is now §" + (commandExecutionEnabled ? "aenabled" : "cdisabled") + "§r."));
                if (ConfigManager.config.persistAutoTipEnabled) { // Persist the toggle state in the config if enabled
                    ConfigManager.config.autoTipEnabled = commandExecutionEnabled;
                    ConfigManager.saveConfig();
                }
                return 1;
            }))
            .then(ClientCommands.literal("help").executes(context -> {  // Help command
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §r§nHypixel AutoTip Help:"));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §2config §7- §rOpens the Hypixel AutoTip config screen."));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §2info §7- §rDisplays information about the mod."));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §2reload §7- §rReloads the config and statistics from file."));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §2resetstats §7- §rResets all statistics. Run again with 'confirm' to confirm the reset."));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §2stats §7- §rDisplays the lifetime tipping statistics."));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §2status §7- §rDisplays the current status of Hypixel AutoTip and time to next tip."));
                context.getSource().sendFeedback(Component.literal(ConfigManager.config.autoTipPrefixColor.getCode() + "§l[AT] §2toggle §7- §rToggles AutoTip on or off."));
                return 1;
            }));
            
    };

    private static void handleTipMessage(Component message) {
        if(!ConfigManager.config.trackRewards){
            return; // Dont run if its disabled in the config
        }
        
        String text = message.getString();

        if (!text.startsWith("§aYou tipped ")) {
            return;
        }

        System.out.println("[Hypixel AutoTip] A successful tip message was detected in chat.");

        TipStats stats = TipStatsManager.stats;

        // Count the successful tip
        stats.totalTips++;

        // Get players tipped and games tipped
        Pattern tipPattern = Pattern.compile("You tipped (\\d+) players in (\\d+) different games!");
        Matcher tipMatcher = tipPattern.matcher(text);

        if (tipMatcher.find()) {
            stats.totalPlayersTipped += Long.parseLong(tipMatcher.group(1));
            stats.totalGamesTipped += Long.parseLong(tipMatcher.group(2));
        }

        // Get rewards from the hover text
        if (message.getStyle().getHoverEvent() instanceof HoverEvent.ShowText hover) {
            String rewardText = hover.value().getString();

            for (String line : rewardText.split("\\R")) {
                line = line.replaceAll("§.", "").trim();

                if (line.isEmpty() || line.equals("Rewards")) {
                    continue;
                }

                System.out.println("[Hypixel AutoTip] Reward received: " + line);

                // Hypixel Experience
                if (line.matches("\\+\\d+ Hypixel Experience")) {
                    String amountString = line.replaceFirst("^\\+(\\d+) .*", "$1");
                    long amount = Long.parseLong(amountString);
                    stats.totalHypixelExperience += amount;
                    continue;
                }

                // Other rewards (coins, tokens, etc.)
                Matcher rewardMatcher = Pattern.compile("^\\+(\\d+) (.+)$").matcher(line);

                if (rewardMatcher.matches()) {
                    long amount = Long.parseLong(rewardMatcher.group(1));
                    String rewardName = rewardMatcher.group(2);
                    stats.totalRewards.merge(rewardName, amount, Long::sum);
                }
            }
        }

        TipStatsManager.save();
    }
}
