package com.jj321.issgoety.mixin;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.jj321.issgoety.focus.GoetyWandSpellSelectionEvents;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.network.casting.CastPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hands Goety wand use over to the currently selected ISS wheel entry. */
@Mixin(value = DarkWand.class, remap = false)
public abstract class DarkWandMixin {

    @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
            at = @At("HEAD"), cancellable = true, remap = true)
    private void issgoety$castSelectedIssEntry(Level level, Player player, InteractionHand hand,
                                                CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack wand = player.getItemInHand(hand);
        if (!(wand.getItem() instanceof IWand)) return;

        SpellSelectionManager.SelectionOption option = GoetyWandSpellSelectionEvents.getSelectedOption(player);
        // Let Goety's own use/tick/release pipeline handle its native continuous and charging focuses.
        if (GoetyWandSpellSelectionEvents.isGoetyFocusProxy(option)) return;
        if (!GoetyWandSpellSelectionEvents.isGoetyWandOption(option)) return;

        if (level.isClientSide) {
            PacketDistributor.sendToServer(new CastPacket());
        }
        CastType castType = option.spellData.getSpell().getCastType();
        cir.setReturnValue(castType == CastType.LONG || castType == CastType.CONTINUOUS
                ? InteractionResultHolder.consume(wand)
                : InteractionResultHolder.success(wand));
    }

    @Inject(method = "onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("HEAD"), cancellable = true, remap = true)
    private void issgoety$suppressNativeTickWhileIssEntrySelected(Level level, LivingEntity entity, ItemStack stack,
                                                                   int remainingUseTicks, CallbackInfo ci) {
        if (isIssEntrySelected(entity, stack)) ci.cancel();
    }

    @Inject(method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
            at = @At("HEAD"), cancellable = true, remap = true)
    private void issgoety$suppressNativeRelease(ItemStack stack, Level level, LivingEntity entity,
                                                 int timeLeft, CallbackInfo ci) {
        if (isIssEntrySelected(entity, stack)) ci.cancel();
    }

    @Inject(method = "finishUsingItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"), cancellable = true, remap = true)
    private void issgoety$suppressNativeFinish(ItemStack stack, Level level, LivingEntity entity,
                                                CallbackInfoReturnable<ItemStack> cir) {
        if (isIssEntrySelected(entity, stack)) cir.setReturnValue(stack);
    }

    private static boolean isIssEntrySelected(LivingEntity entity, ItemStack stack) {
        if (!(entity instanceof Player player) || !(stack.getItem() instanceof DarkWand)) return false;
        if (player.getItemInHand(InteractionHand.MAIN_HAND) != stack
                && player.getItemInHand(InteractionHand.OFF_HAND) != stack) return false;

        io.redspace.ironsspellbooks.api.magic.MagicData magicData =
                io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player);
        if (GoetyWandSpellSelectionEvents.isGoetyFocusProxy(
                GoetyWandSpellSelectionEvents.getSelectedOption(player))) return false;
        if (GoetyWandSpellSelectionEvents.isIssGoetySpellSlot(magicData.getCastingEquipmentSlot())) return true;
        if (magicData.getSyncedData() == null) return false;
        io.redspace.ironsspellbooks.gui.overlays.SpellSelection selection =
                magicData.getSyncedData().getSpellSelection();
        return selection != null && GoetyWandSpellSelectionEvents.isIssGoetySpellSlot(selection.equipmentSlot);
    }
}



