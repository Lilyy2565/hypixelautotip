package com.lilyy2565.hypixelautotip.mixin.client;

import net.minecraft.client.gui.hud.debug.DebugHudEntryVisibility;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugHudEntryVisibility.class)
public class DebugHudEntriesVisibilityMixin {
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void initializeHypixelAutoTipVisibility(CallbackInfo ci) {
        // Set HypixelAutoTip debug entry to show "In Overlay" by default
        // This is equivalent to 1 (SHOWN state in the debug HUD)
        // We'll try to set it if the entry exists
        try {
            Identifier hypixelAutoTipId = Identifier.of("hypixelautotip", "debug");
            
            // Get the visibility map and set it to shown (value 1)
            // This uses reflection to access the internal visibility storage
            java.lang.reflect.Field visibilityField = DebugHudEntryVisibility.class.getDeclaredField("visibility");
            visibilityField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Identifier, Integer> visibilityMap = 
                (java.util.Map<Identifier, Integer>) visibilityField.get(this);
            
            // Set to 1 (SHOWN/In Overlay state)
            visibilityMap.putIfAbsent(hypixelAutoTipId, 1);
        } catch (Exception e) {
            // Silently fail if we can't set the default - user can enable manually
        }
    }
}
