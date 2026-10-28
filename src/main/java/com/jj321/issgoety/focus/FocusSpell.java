package com.jj321.issgoety.focus;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.utils.WandUtil;
import com.jj321.issgoety.IssGoety;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Selection, icon and cooldown presentation only; Goety owns actual item use. */
public class FocusSpell extends AbstractSpell {
    private static final ResourceLocation ID = new ResourceLocation(IssGoety.MOD_ID, "focus_cast");
    private final DefaultConfig config = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON).setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(1).setCooldownSeconds(0).setAllowCrafting(false).build();
    @Override public ResourceLocation getSpellResource() { return ID; }
    @Override public DefaultConfig getDefaultConfig() { return config; }
    @Override public CastType getCastType() { return CastType.NONE; }
    @Override public MutableComponent getDisplayName(Player player) {
        ItemStack focus = getDisplayFocus(player);
        return focus.isEmpty() ? Component.translatable("spell.issgoety.focus_cast") : focus.getHoverName().copy();
    }
    public static ItemStack getDisplayFocus(LivingEntity entity) {
        if (entity == null) return ItemStack.EMPTY;
        if (entity instanceof Player player) {
            // Read the saved selection directly: constructing a manager here can reenter name/icon rendering.
            var synced = player.level().isClientSide ? ClientMagicData.getSyncedSpellData(player)
                    : MagicData.getPlayerMagicData(player).getSyncedData();
            var selection = synced == null ? null : synced.getSpellSelection();
            if (selection != null) {
                if (GoetyWandSpellSelectionEvents.GOETY_WAND_FOCUS_SLOT.equals(selection.equipmentSlot)) {
                    ItemStack wand = GoetyWandSpellSelectionEvents.getHeldGoetyWand(player);
                    return wand.isEmpty() ? ItemStack.EMPTY : IWand.getFocus(wand);
                }
                ItemStack selectedStack = SpellSelectionManager.MAINHAND.equals(selection.equipmentSlot) ? player.getMainHandItem()
                        : SpellSelectionManager.OFFHAND.equals(selection.equipmentSlot) ? player.getOffhandItem() : ItemStack.EMPTY;
                if (selectedStack.getItem() instanceof IWand && ISpellContainer.isSpellContainer(selectedStack)) {
                    ISpellContainer container = ISpellContainer.get(selectedStack);
                    // ISS stores selection indices in the active list, not in the underlying slot array.
                    var activeSpells = container.getActiveSpells();
                    if (selection.index >= 0 && selection.index < activeSpells.size()
                            && ID.equals(activeSpells.get(selection.index).spellData().getSpell().getSpellResource())) {
                        return IWand.getFocus(selectedStack);
                    }
                }
            }
        }
        return WandUtil.findFocus(entity);
    }
    @Override public boolean attemptInitiateCast(ItemStack stack, int level, Level world, Player player,
                                                CastSource source, boolean check, String slot) {
        // This entry is activated by right-click item use, never ISS's cast-key/timer pipeline.
        return false;
    }
}
