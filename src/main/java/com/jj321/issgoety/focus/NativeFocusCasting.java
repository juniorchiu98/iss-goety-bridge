package com.jj321.issgoety.focus;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.items.magic.DarkWand;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Routes the real staff stack into Goety. Contains no spell timing or resource rules. */
public final class NativeFocusCasting {
    private static final String NATIVE_USE = "IssGoetyNativeFocusUse";
    private NativeFocusCasting() { }
    public static boolean selected(Player player, InteractionHand hand) {
        var option = GoetyWandSpellSelectionEvents.getSelectedOption(player);
        return GoetyWandSpellSelectionEvents.isFocusProxySpell(option)
                && (hand == InteractionHand.MAIN_HAND ? SpellSelectionManager.MAINHAND : SpellSelectionManager.OFFHAND).equals(option.slot);
    }
    public static boolean active(ItemStack stack) {
        return StaffFocus.isStaff(stack) && stack.hasTag() && stack.getTag().getBoolean(NATIVE_USE);
    }
    public static void clear(ItemStack stack) {
        if (stack.hasTag()) stack.getTag().remove(NATIVE_USE);
    }
    public static void clearIfIdle(ItemStack stack, LivingEntity entity) {
        if (!entity.isUsingItem() || entity.getUseItem() != stack) clear(stack);
    }
    public static InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        DarkWand goety = GoetyWands.get();
        if (goety == null) return InteractionResultHolder.pass(stack);
        prepare(stack, player, goety);

        try { return goety.use(world, player, hand); }
        finally { clearIfIdle(stack, player); }
    }
    private static void prepare(ItemStack stack, Player player, DarkWand goety) {
        stack.getOrCreateTag();
        goety.setSpellConditions(goety.getSpell(stack), stack, player);
        stack.getOrCreateTag().putInt(IWand.CASTTIME, goety.CastDuration(stack));
        stack.getOrCreateTag().putBoolean(NATIVE_USE, true);
    }
    public static InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        DarkWand goety = GoetyWands.get();
        if (goety == null || player == null) return InteractionResult.PASS;
        prepare(stack, player, goety);
        try { return goety.useOn(context); }
        finally { clearIfIdle(stack, player); }
    }
    public static InteractionResult interact(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        DarkWand goety = GoetyWands.get();
        if (goety == null) return InteractionResult.PASS;
        prepare(stack, player, goety);
        try { return goety.interactLivingEntity(stack, player, target, hand); }
        finally { clearIfIdle(stack, player); }
    }
    public static boolean attack(ItemStack stack, Player player, Entity target) {
        return GoetyWands.get().onLeftClickEntity(stack, player, target);
    }}

