package com.lilyy2565.hypixelautotip.mixin.client;

import com.lilyy2565.hypixelautotip.HypixelAutoTipDebugEntry;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugHudEntries.class)
public abstract class DebugScreenEntriesMixin {
    
    @Shadow
    public static Identifier register(Identifier id, DebugHudEntry entry) {
        return null;
    }
    
    @Inject(method = "<clinit>", at = @At(value = "RETURN"))
    private static void registerHypixelAutoTipDebug(CallbackInfo ci) {
        register(Identifier.of("hypixelautotip", "debug"), new HypixelAutoTipDebugEntry());
    }
}
