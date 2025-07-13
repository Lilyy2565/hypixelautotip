package com.soniczac7.hypixelautotip.mixin.client;

import com.soniczac7.hypixelautotip.HypixelAutoTipClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class AutoTipDebugHudMixin {

    @Inject(method = "getLeftText", at = @At("RETURN"))
    private void addAutoTipInfo(CallbackInfoReturnable<List<String>> cir) {
        List<String> debugInfo = cir.getReturnValue();
        List<String> modInfo = HypixelAutoTipClient.getDebugInfo();
        debugInfo.addAll(modInfo);
    }
}
