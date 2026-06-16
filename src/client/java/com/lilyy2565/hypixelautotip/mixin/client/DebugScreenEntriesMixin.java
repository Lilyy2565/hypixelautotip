package com.lilyy2565.hypixelautotip.mixin.client;

import com.lilyy2565.hypixelautotip.HypixelAutoTipDebugEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(DebugScreenEntries.class)
public abstract class DebugScreenEntriesMixin {

    @Shadow
    @Final
    @Mutable
    private static Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> PROFILES;

    @Shadow
    public static Identifier register(Identifier id, DebugScreenEntry entry) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void hypixelautotip$registerDebugEntry(CallbackInfo ci) {
        Identifier entryId = HypixelAutoTipDebugEntry.ENTRY_ID;
        register(entryId, new HypixelAutoTipDebugEntry());

        Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> updatedProfiles = new HashMap<>();
        for (Map.Entry<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> profileEntry : PROFILES.entrySet()) {
            Map<Identifier, DebugScreenEntryStatus> statusMap = new HashMap<>(profileEntry.getValue());
            statusMap.putIfAbsent(entryId, DebugScreenEntryStatus.IN_OVERLAY);
            updatedProfiles.put(profileEntry.getKey(), Map.copyOf(statusMap));
        }

        PROFILES = Map.copyOf(updatedProfiles);
    }
}