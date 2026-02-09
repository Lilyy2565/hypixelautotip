package com.lilyy2565.hypixelautotip;

import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HypixelAutoTipDebugEntry implements DebugHudEntry {
    
    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk worldChunk, @Nullable WorldChunk worldChunk2) {
        Identifier sectionId = Identifier.of("hypixelautotip", "debug");
        
        // Get debug info from HypixelAutoTipClient (centralized logic)
        List<String> debugInfo = HypixelAutoTipClient.getDebugInfo();
        
        // Add all lines to the debug section
        lines.addLinesToSection(sectionId, debugInfo);
    }
}
