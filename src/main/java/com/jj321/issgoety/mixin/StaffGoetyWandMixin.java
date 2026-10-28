package com.jj321.issgoety.mixin;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.jj321.issgoety.focus.GoetyWands;
import com.jj321.issgoety.focus.NativeFocusCasting;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;

/** IWand supplies Goety's own capability/share-tag defaults; mechanics run through Forge use events. */
@Mixin(value = StaffItem.class, remap = false)
public abstract class StaffGoetyWandMixin extends CastingItem implements IWand {
    protected StaffGoetyWandMixin(Item.Properties properties) { super(properties); }
    @Override public SpellType getSpellType() { return SpellType.NONE; }
    @Override public int SoulUse(LivingEntity caster, ItemStack stack) { return GoetyWands.get().SoulUse(caster, stack); }
    @Override public int ShotsFired(ItemStack stack) { return GoetyWands.get().ShotsFired(stack); }
    @Override public int getUseDuration(ItemStack stack) {
        return NativeFocusCasting.active(stack) ? GoetyWands.get().getUseDuration(stack) : super.getUseDuration(stack);
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) {
        return NativeFocusCasting.active(stack) ? GoetyWands.get().getUseAnimation(stack) : super.getUseAnimation(stack);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() != null && NativeFocusCasting.selected(context.getPlayer(), context.getHand())
                ? NativeFocusCasting.useOn(context) : super.useOn(context);
    }
    @Override public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return NativeFocusCasting.selected(player, hand) ? NativeFocusCasting.interact(stack, player, target, hand)
                : super.interactLivingEntity(stack, player, target, hand);
    }
    @Override public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        return NativeFocusCasting.selected(player, InteractionHand.MAIN_HAND) ? NativeFocusCasting.attack(stack, player, target)
                : super.onLeftClickEntity(stack, player, target);
    }
}


