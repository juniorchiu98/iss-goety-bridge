package com.jj321.issgoety.mixin;

import com.jj321.issgoety.focus.GoetyWandSpellSelectionEvents;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Restore a saved bridge slot after SpellSelectionEvent has renamed the native slots. */
@Mixin(value = SpellSelectionManager.class, remap = false)
public abstract class GoetySpellSelectionMixin {
    @Shadow private int selectionIndex;
    @Shadow private boolean selectionValid;

    @Shadow public abstract SpellSelection getCurrentSelection();
    @Shadow public abstract List<SpellSelectionManager.SelectionOption> getSpellsForSlot(String slot);

    @Inject(method = "tryLastSelectionOrDefault", at = @At("HEAD"), cancellable = true, remap = false)
    private void issgoety$restoreRenamedSlot(CallbackInfo ci) {
        SpellSelection selected = getCurrentSelection();
        if (selected == null || !GoetyWandSpellSelectionEvents.isGoetyWandSlot(selected.equipmentSlot)) return;

        for (SpellSelectionManager.SelectionOption option : getSpellsForSlot(selected.equipmentSlot)) {
            if (option.slotIndex == selected.index) {
                // Resolve locally: rebuilding a manager must not send another selection packet
                // or replace the requested slot with the previous/default selection.
                selectionIndex = option.globalIndex;
                selectionValid = true;
                ci.cancel();
                return;
            }
        }
    }
}
