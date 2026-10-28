package com.jj321.issgoety.focus;

import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.jj321.issgoety.IssGoety;
import com.jj321.issgoety.main.ModSpells;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.compat.Curios;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;

/** Routes loaded ISS spellbook spells and the held wand's current Goety focus through the ISS wheel. */
@Mod.EventBusSubscriber(modid = IssGoety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GoetyWandSpellSelectionEvents {

    public static final String GOETY_WAND_BOOK_SLOT = Curios.SPELLBOOK_SLOT + "_goety_wand";
    public static final String GOETY_WAND_ITEM_SLOT = "goety_wand";
    public static final String GOETY_WAND_FOCUS_SLOT = "goety_wand_focus";

    private GoetyWandSpellSelectionEvents() {
    }

    public static boolean isGoetyWandSlot(String slot) {
        return GOETY_WAND_BOOK_SLOT.equals(slot) || GOETY_WAND_ITEM_SLOT.equals(slot)
                || GOETY_WAND_FOCUS_SLOT.equals(slot);
    }

    /** Slots that represent ISS casts and therefore need Goety's native wand callbacks suppressed. */
    public static boolean isIssGoetySpellSlot(String slot) {
        return GOETY_WAND_BOOK_SLOT.equals(slot) || GOETY_WAND_ITEM_SLOT.equals(slot);
    }

    public static boolean isGoetyWandOption(SpellSelectionManager.SelectionOption option) {
        return option != null && isGoetyWandSlot(option.slot);
    }

    public static boolean isGoetyFocusProxy(SpellSelectionManager.SelectionOption option) {
        if (!isGoetyWandOption(option)) return false;
        return isFocusProxySpell(option);
    }

    /** The selected spell may still carry ISS's native hand slot on the client before slot remapping. */
    public static boolean isFocusProxySpell(SpellSelectionManager.SelectionOption option) {
        return option != null && option.spellData.getSpell().getSpellResource()
                .equals(ModSpells.FOCUS_CAST.get().getSpellResource());
    }

    public static SpellSelectionManager.SelectionOption getSelectedOption(Player player) {
        return new SpellSelectionManager(player).getSelection();
    }

    public static ItemStack getHeldGoetyWand(Player player) {
        if (player.getMainHandItem().getItem() instanceof DarkWand) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof DarkWand) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void syncHeldWandSpellSlot(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ItemStack mainHand = event.player.getMainHandItem();
        ItemStack offHand = event.player.getOffhandItem();
        if (mainHand.getItem() instanceof DarkWand) GoetyWands.ensureSpellSlot(mainHand);
        if (offHand.getItem() instanceof DarkWand) GoetyWands.ensureSpellSlot(offHand);
    }

    @SubscribeEvent
    public static void addGoetyWandOptions(SpellSelectionManager.SpellSelectionEvent event) {
        Player player = event.getEntity();
        ItemStack wandStack = getHeldGoetyWand(player);
        if (wandStack.isEmpty()) return;

        int slotIndex = 0;
        for (SpellSelectionManager.SelectionOption option : event.getManager().getAllSpells()) {
            if (option.slot.startsWith(Curios.SPELLBOOK_SLOT)) {
                // Preserve each loaded SpellData level and keep ISS spellbook mana/cooldown semantics.
                option.slot = GOETY_WAND_BOOK_SLOT;
                option.slotIndex = slotIndex++;
            }
        }

        int wandSlotIndex = 0;
        if (player.getMainHandItem().getItem() instanceof DarkWand) {
            for (SpellSelectionManager.SelectionOption option : event.getManager().getAllSpells()) {
                if (SpellSelectionManager.MAINHAND.equals(option.slot)) {
                    if (isFocusProxy(option)) {
                        option.slot = GOETY_WAND_FOCUS_SLOT;
                        option.slotIndex = 0;
                    } else {
                        option.slot = GOETY_WAND_ITEM_SLOT;
                        option.slotIndex = wandSlotIndex++;
                    }
                }
            }
        } else if (player.getOffhandItem().getItem() instanceof DarkWand) {
            for (SpellSelectionManager.SelectionOption option : event.getManager().getAllSpells()) {
                if (SpellSelectionManager.OFFHAND.equals(option.slot)) {
                    if (isFocusProxy(option)) {
                        option.slot = GOETY_WAND_FOCUS_SLOT;
                        option.slotIndex = 0;
                    } else {
                        option.slot = GOETY_WAND_ITEM_SLOT;
                        option.slotIndex = wandSlotIndex++;
                    }
                }
            }
        }
    }

    private static boolean isFocusProxy(SpellSelectionManager.SelectionOption option) {
        return option.spellData.getSpell().getSpellResource()
                .equals(ModSpells.FOCUS_CAST.get().getSpellResource());
    }
}

