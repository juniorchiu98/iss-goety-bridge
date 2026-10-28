package com.jj321.issgoety.focus;

import com.Polarice3.Goety.common.capabilities.soulenergy.FocusCooldown;
import com.Polarice3.Goety.utils.SEHelper;
import com.jj321.issgoety.main.ModSpells;
import io.redspace.ironsspellbooks.capabilities.magic.CooldownInstance;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerCooldowns;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Display-only mirror of Goety's public cooldown data. No casting or cooldown decisions. */
public final class FocusCooldownBridge {
    private FocusCooldownBridge() { }
    public record Cooldown(int totalTicks, int remainingTicks) {
        private static final Cooldown READY = new Cooldown(0, 0);
    }
    public static Cooldown read(Player player, ItemStack focus) {
        if (focus.isEmpty()) return Cooldown.READY;
        FocusCooldown tracker = SEHelper.getFocusCoolDown(player);
        boolean general = tracker.isOnCooldown(focus.getItem());
        if (!general && !tracker.isOnSpecificCooldown(focus)) return Cooldown.READY;
        // Use Goety's public persistence API instead of a Mixin into its private counter fields.
        ListTag saved = new ListTag();
        if (general) tracker.save(saved); else tracker.saveSpecifics(saved);
        String key = general ? "" : FocusCooldown.keyOf(focus);
        for (int i = 0; i < saved.size(); i++) {
            CompoundTag entry = saved.getCompound(i);
            if (general ? entry.getInt("Item") == Item.getId(focus.getItem()) : entry.getString("Key").equals(key)) {
                return fromSavedEntry(entry);

            }
        }
        return Cooldown.READY;
    }
    static Cooldown fromSavedEntry(CompoundTag entry) {
        int remaining = Math.max(0, entry.getInt("Time"));
        return remaining == 0 ? Cooldown.READY : new Cooldown(Math.max(remaining, entry.getInt("TotalTime")), remaining);
    }

    public static void mirror(Player player, PlayerCooldowns issCooldowns) {
        applyToIss(issCooldowns, ModSpells.FOCUS_CAST.get().getSpellId(), read(player, FocusSpell.getDisplayFocus(player)));
    }
    static void applyToIss(PlayerCooldowns cooldowns, String proxyId, Cooldown actual) {
        if (actual.remainingTicks() <= 0) {
            cooldowns.removeCooldown(proxyId);
            return;
        }
        CooldownInstance current = cooldowns.getSpellCooldowns().get(proxyId);
        if (current == null || current.getSpellCooldown() != actual.totalTicks() || current.getCooldownRemaining() != actual.remainingTicks()) {
            cooldowns.addCooldown(proxyId, actual.totalTicks(), actual.remainingTicks());
        }
    }
}


