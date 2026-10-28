package com.jj321.issgoety.focus;

import com.Polarice3.Goety.api.items.magic.IFocus;
import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler;
import com.jj321.issgoety.main.ModSpells;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/** Native Goety focus storage plus migration of the former bridge NBT and ISS display entry. */
public final class StaffFocus {
    private static final String LEGACY_FOCUS = "IssGoetyFocus";
    private static final String LEGACY_SOUL_TICKS = "IssGoetyFocusSoulTicks";
    private StaffFocus() { }
    public static boolean isStaff(ItemStack stack) { return stack.getItem() instanceof StaffItem; }
    public static InteractionHand getHeldStaffHand(Player player) {
        if (isStaff(player.getMainHandItem())) return InteractionHand.MAIN_HAND;
        return isStaff(player.getOffhandItem()) ? InteractionHand.OFF_HAND : null;
    }
    public static boolean isFocus(ItemStack stack) { return stack.getItem() instanceof IFocus; }
    public static boolean isSupported(ItemStack stack) { return isFocus(stack); }
    public static ItemStack get(ItemStack staff) {
        var handler = staff.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (handler instanceof SoulUsingItemHandler nativeHandler && !nativeHandler.getSlot().isEmpty()) return nativeHandler.getSlot();
        CompoundTag tag = staff.getTag();
        return tag != null && tag.contains(LEGACY_FOCUS, Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound(LEGACY_FOCUS)) : ItemStack.EMPTY;
    }
    public static boolean has(ItemStack staff) { return !get(staff).isEmpty(); }
    public static void migrateLegacy(ItemStack staff, Entity owner) {
        CompoundTag tag = staff.getTag();
        if (tag == null || !tag.contains(LEGACY_FOCUS, Tag.TAG_COMPOUND)) return;
        var handler = staff.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (!(handler instanceof SoulUsingItemHandler nativeHandler)) return;
        ItemStack legacy = ItemStack.of(tag.getCompound(LEGACY_FOCUS));
        ItemStack current = nativeHandler.getSlot();
        if (current.isEmpty()) {
            nativeHandler.setStackInSlot(0, legacy);
        } else if (!ItemStack.isSameItemSameTags(current, legacy)) {
            // Preserve a distinct old focus if a native focus was already installed before migration.
            if (!(owner instanceof Player player)) return;
            if (!legacy.isEmpty() && !player.getInventory().add(legacy)) player.drop(legacy, false);
        }
        if (current.isEmpty() || ItemStack.isSameItemSameTags(current, legacy)) {
            tag.putInt(IWand.SECONDS, Math.floorMod(tag.getInt(LEGACY_SOUL_TICKS), 20));
        }
        tag.remove(LEGACY_FOCUS);
        tag.remove(LEGACY_SOUL_TICKS);
    }
    public static void ensureSpellSlot(ItemStack staff) {
        if (!isStaff(staff)) return;
        ISpellContainerMutable container = ISpellContainer.getOrCreate(staff).mutableCopy();
        var proxy = ModSpells.FOCUS_CAST.get();
        boolean present = container.getIndexForSpell(proxy) >= 0;
        boolean needed = has(staff);
        if (present == needed) return;
        if (needed) {
            if (container.getActiveSpellCount() >= container.getMaxSpellCount()) container.setMaxSpellCount(container.getMaxSpellCount() + 1);
            container.addSpell(proxy, 1, true);
        } else container.removeSpell(proxy);
        ISpellContainer.set(staff, container.toImmutable());
    }
}
