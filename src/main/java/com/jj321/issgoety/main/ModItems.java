package com.jj321.issgoety.main;

import com.jj321.issgoety.IssGoety;
import com.jj321.issgoety.conversion.SoulConverterItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IssGoety.MOD_ID);

    /** 靈魂轉換飾品:戴在 Curios 欄位時,把 Goety 靈魂能量單向轉成 ISS 魔力。 */
    public static final RegistryObject<Item> SOUL_CONVERTER =
            ITEMS.register("soul_converter", SoulConverterItem::new);
}
