package com.lilyy2565.hypixelautotip.mixin.client;

import com.lilyy2565.hypixelautotip.HypixelAutoTipDebugEntry;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudEntryVisibility;
import net.minecraft.client.gui.hud.debug.DebugProfileType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(DebugHudEntries.class)
public abstract class DebugScreenEntriesMixin {
    
    @Shadow
    @Final
    @Mutable
    private static Map<DebugProfileType, Map<Identifier, DebugHudEntryVisibility>> PROFILES;
    
    @Shadow
    public static Identifier register(Identifier id, DebugHudEntry entry) {
        return null;
    }
    
    @Inject(method = "<clinit>", at = @At(value = "RETURN"))
    private static void registerHypixelAutoTipDebug(CallbackInfo ci) {
        Identifier entryId = Identifier.of("hypixelautotip", "debug");
        register(entryId, new HypixelAutoTipDebugEntry());
        
        // Set default visibility to "In Overlay" for all profiles
        Map<DebugProfileType, Map<Identifier, DebugHudEntryVisibility>> updatedProfiles = new HashMap<>();
        for (Map.Entry<DebugProfileType, Map<Identifier, DebugHudEntryVisibility>> profileEntry : PROFILES.entrySet()) {
            Map<Identifier, DebugHudEntryVisibility> visibilityMap = new HashMap<>(profileEntry.getValue());
            visibilityMap.putIfAbsent(entryId, DebugHudEntryVisibility.IN_F3);
            updatedProfiles.put(profileEntry.getKey(), visibilityMap);
        }
        PROFILES = updatedProfiles;
    }
}
