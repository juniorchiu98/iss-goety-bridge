package com.jj321.issgoety.mixin;

import com.jj321.issgoety.conversion.SoulConversion;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Let ISS's native continuous-cast affordability check see convertible souls without prepaying them. */
@Mixin(value = MagicManager.class, remap = false)
public abstract class SoulConversionTickMixin {
    @Redirect(method = "lambda$tick$0", at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;getMana()F"), remap = false)
    private float issgoety$checkContinuousResources(MagicData data, boolean regenTick, Player player) {
        return SoulConversion.availableMana(data.getMana(), player, data.getCastSource());
    }
}