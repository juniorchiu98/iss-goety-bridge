package com.jj321.issgoety.focus;

import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.Polarice3.Goety.api.items.magic.IWand;
import com.jj321.issgoety.main.ModSpells;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** Goety 魔杖查找與 ISS 聚晶法術槽同步。 */
public final class GoetyWands {

    private static DarkWand cached;

    private GoetyWands() {
    }

    public static DarkWand get() {
        if (cached == null) {
            DarkWand fallback = null;
            for (Item item : ForgeRegistries.ITEMS) {
                if (item instanceof DarkWand wand) {
                    if (item.getClass() == DarkWand.class) {
                        cached = wand;
                        break;
                    }
                    if (fallback == null) fallback = wand;
                }
            }
            if (cached == null) cached = fallback;
        }
        return cached;
    }


    /** Ensure the held wand exposes the single shared focus proxy in its ISS item spell slot. */
    public static void ensureSpellSlot(ItemStack wand) {
        if (!(wand.getItem() instanceof DarkWand)) return;

        ItemStack focus = IWand.getFocus(wand);
        boolean shouldHaveSpell = StaffFocus.isSupported(focus);
        ISpellContainerMutable container = ISpellContainer.getOrCreate(wand).mutableCopy();
        int spellIndex = container.getIndexForSpell(ModSpells.FOCUS_CAST.get());
        if (!shouldHaveSpell) {
            if (spellIndex < 0) return;
            container.removeSpell(ModSpells.FOCUS_CAST.get());
            ISpellContainer.set(wand, container.toImmutable());
            return;
        }
        if (spellIndex >= 0) return;

        if (container.getActiveSpellCount() >= container.getMaxSpellCount()) {
            container.setMaxSpellCount(container.getMaxSpellCount() + 1);
        }
        container.addSpell(ModSpells.FOCUS_CAST.get(), 1, true);
        ISpellContainer.set(wand, container.toImmutable());
    }
}


