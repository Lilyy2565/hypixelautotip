package com.soniczac7.hypixelautotip.mixin.client;

import com.soniczac7.hypixelautotip.HypixelAutoTipClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(DebugHud.class)
public class DebugHudOverlayMixin {

    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    private void addAutoTipDebugInfo(CallbackInfoReturnable<List<String>> cir) {
        List<String> original = cir.getReturnValue();
        // In case the returned list is immutable, make a mutable copy.
        List<String> mutable = new ArrayList<>(original);
        mutable.add(String.format("AutoTip: %s, Tick: %d",
                HypixelAutoTipClient.commandExecutionEnabled ? "Enabled" : "Disabled",
                HypixelAutoTipClient.tickCounter));
        cir.setReturnValue(mutable);
    }
}

/*package com.soniczac7.hypixelautotip.mixin.client;

import com.soniczac7.hypixelautotip.HypixelAutoTipClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class DebugHudOverlayMixin {

    @Inject(method = "getLeftText()Ljava/util/List;", at = @At("RETURN"))
    private void addAutoTipDebugInfo(CallbackInfoReturnable<List<Text>> cir) {
        List<Text> debugLines = cir.getReturnValue();
        debugLines.add(Text.literal(String.format("AutoTip: %s, Tick: %d",
            HypixelAutoTipClient.commandExecutionEnabled ? "Enabled" : "Disabled",
            HypixelAutoTipClient.tickCounter)));
    }
}
*/