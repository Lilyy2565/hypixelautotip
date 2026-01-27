package com.lilyy2565.hypixelautotip.mixin.client;

import com.lilyy2565.hypixelautotip.HypixelAutoTipClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(DebugHud.class)
public class AutoTipDebugHudMixin {

    @ModifyVariable(method = "method_51745", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private List<String> addAutoTipInfo(List<String> debugInfo, net.minecraft.client.gui.DrawContext context, List<String> text, boolean left) {
        if (left) {
            List<String> modInfo = HypixelAutoTipClient.getDebugInfo();
            debugInfo.addAll(modInfo);
        }
        return debugInfo;
    }
}
