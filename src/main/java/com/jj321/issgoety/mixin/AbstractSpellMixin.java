package com.jj321.issgoety.mixin;

import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.jj321.issgoety.conversion.SoulConversion;
import com.jj321.issgoety.focus.GoetyWandSpellSelectionEvents;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** ISS integration; Goety focus casting never enters this pipeline. */
@Mixin(value = AbstractSpell.class, remap = false)
public abstract class AbstractSpellMixin {
    @Unique
    private static final ThreadLocal<Boolean> issgoety$paymentFailed = new ThreadLocal<>();

    @Redirect(method = "canBeCastedBy", at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;getMana()F"), remap = false)
    private float issgoety$checkConvertibleMana(MagicData data, int spellLevel, CastSource source,
                                               MagicData magicData, Player player) {
        // Validation must not charge souls, change mana, or fire ChangeManaEvent.
        return SoulConversion.availableMana(data.getMana(), player, source);
    }

    @Redirect(method = "castSpell", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"), remap = false)
    private float issgoety$payFinalManaDeficit(float balance, float minimum, Level level, int spellLevel,
                                              ServerPlayer player, CastSource source, boolean addCooldown) {
        // balance already includes the final SpellOnCastEvent cost and the player's actual mana.
        SoulConversion.Payment payment = SoulConversion.payBalance(balance, player, source);
        issgoety$paymentFailed.set(!payment.affordable());
        return Math.max(payment.manaAfter(), minimum);
    }

    @Inject(method = "castSpell", at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;setMana(F)V"), cancellable = true, remap = false)
    private void issgoety$stopUnpaidCast(Level level, int spellLevel, ServerPlayer player, CastSource source,
                                        boolean addCooldown, CallbackInfo ci) {
        boolean failed = Boolean.TRUE.equals(issgoety$paymentFailed.get());
        issgoety$paymentFailed.remove();
        if (!failed) return;
        AbstractSpell self = (AbstractSpell) (Object) this;
        self.onServerCastComplete(level, spellLevel, player, MagicData.getPlayerMagicData(player), true);
        player.displayClientMessage(Component.translatable("ui.irons_spellbooks.cast_error_mana", self.getDisplayName(player))
                .withStyle(ChatFormatting.RED), true);
        ci.cancel();
    }

    @Inject(method = "attemptInitiateCast", at = @At("RETURN"), remap = false)
    private void issgoety$keepWandIssCastAlive(ItemStack stack, int spellLevel, Level level, Player player,
                                             CastSource source, boolean check, String equipmentSlot,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || !(player instanceof ServerPlayer serverPlayer)) return;
        MagicData data = MagicData.getPlayerMagicData(serverPlayer);
        if (!data.isCasting() || data.getCastType() != CastType.LONG
                || !GoetyWandSpellSelectionEvents.isIssGoetySpellSlot(data.getCastingEquipmentSlot())
                || player.isUsingItem()) return;
        InteractionHand hand = player.getMainHandItem().getItem() instanceof DarkWand ? InteractionHand.MAIN_HAND
                : player.getOffhandItem().getItem() instanceof DarkWand ? InteractionHand.OFF_HAND : null;
        if (hand != null) player.startUsingItem(hand);
    }
}