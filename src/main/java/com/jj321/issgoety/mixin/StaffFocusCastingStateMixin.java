package com.jj321.issgoety.mixin;

import com.Polarice3.Goety.utils.MobUtil;
import com.jj321.issgoety.focus.NativeFocusCasting;
import com.jj321.issgoety.focus.StaffFocus;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Do not treat an ordinary ISS cast as Goety casting merely because a staff carries IWand. */
@Mixin(value = MobUtil.class, remap = false)
public abstract class StaffFocusCastingStateMixin {
    @Inject(method = "isSpellCasting", at = @At("HEAD"), cancellable = true, remap = false)
    private static void issgoety$distinguishStaffUse(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (StaffFocus.isStaff(entity.getUseItem()) && !NativeFocusCasting.active(entity.getUseItem())) cir.setReturnValue(false);
    }
}
