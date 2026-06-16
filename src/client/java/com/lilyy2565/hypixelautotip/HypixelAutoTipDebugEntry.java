package com.lilyy2565.hypixelautotip;

import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class HypixelAutoTipDebugEntry implements DebugScreenEntry {
    public static final Identifier ENTRY_ID = Identifier.fromNamespaceAndPath("hypixelautotip", "autotip_status");

    @Override
    public void display(DebugScreenDisplayer displayer, Level level, LevelChunk chunk, LevelChunk chunk2) {
        displayer.addToGroup(ENTRY_ID, HypixelAutoTipClient.getDebugInfo());
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }
}