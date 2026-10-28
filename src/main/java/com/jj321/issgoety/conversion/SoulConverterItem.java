package com.jj321.issgoety.conversion;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * 靈魂轉換飾品。本身沒有任何邏輯,只是「標記」:
 * 戴著它時,ISS 法術魔力不足會改為消耗 Goety 靈魂能量(見 SoulConversion 與 AbstractSpellMixin)。
 */
public class SoulConverterItem extends Item implements ICurioItem {

    public SoulConverterItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return "charm".equals(slotContext.identifier());
    }
}
