package com.jj321.issgoety.focus;

import com.jj321.issgoety.IssGoety;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Forward Forge item-use events to Goety with the actual held ISS staff stack. */
@Mod.EventBusSubscriber(modid = IssGoety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StaffFocusEvents {
    private StaffFocusEvents() { }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void rightClick(PlayerInteractEvent.RightClickItem event) {
        if (!StaffFocus.isStaff(event.getItemStack()) || !NativeFocusCasting.selected(event.getEntity(), event.getHand())) return;
        var result = NativeFocusCasting.use(event.getLevel(), event.getEntity(), event.getHand());
        event.setCancellationResult(result.getResult());
        event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void useTick(LivingEntityUseItemEvent.Tick event) {
        if (!NativeFocusCasting.active(event.getItem())) return;
        var entity = event.getEntity();
        GoetyWands.get().onUseTick(entity.level(), entity, event.getItem(), event.getDuration());
        NativeFocusCasting.clearIfIdle(event.getItem(), entity);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void stopUse(LivingEntityUseItemEvent.Stop event) {
        if (!NativeFocusCasting.active(event.getItem())) return;
        try { GoetyWands.get().releaseUsing(event.getItem(), event.getEntity().level(), event.getEntity(), event.getDuration()); }
        finally { NativeFocusCasting.clear(event.getItem()); }
        // Prevent CastingItem.releaseUsing from also entering ISS's spell-release helper.
        event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void finishUse(LivingEntityUseItemEvent.Finish event) {
        ItemStack active = event.getEntity().getUseItem();
        if (!NativeFocusCasting.active(active)) return;
        try { GoetyWands.get().finishUsingItem(active, event.getEntity().level(), event.getEntity()); }
        finally { NativeFocusCasting.clear(active); }
    }
    @SubscribeEvent
    public static void inventoryTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!StaffFocus.isStaff(stack)) continue;
            if (!player.level().isClientSide) StaffFocus.migrateLegacy(stack, player);
            NativeFocusCasting.clearIfIdle(stack, player);
            if (StaffFocus.has(stack)) GoetyWands.get().inventoryTick(stack, player.level(), player, slot, slot == player.getInventory().selected);
            if (!player.level().isClientSide) StaffFocus.ensureSpellSlot(stack);
        }
    }
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!StaffFocus.isStaff(event.getItemStack())) return;
        ItemStack focus = StaffFocus.get(event.getItemStack());
        if (!focus.isEmpty()) event.getToolTip().add(Component.translatable("tooltip.issgoety.socketed_focus", focus.getHoverName()).withStyle(ChatFormatting.AQUA));
    }
}


