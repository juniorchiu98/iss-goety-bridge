package com.jj321.issgoety;

import com.jj321.issgoety.conversion.ConversionConfig;
import com.jj321.issgoety.main.ModItems;
import com.jj321.issgoety.main.ModSpells;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(IssGoety.MOD_ID)
public class IssGoety {
    public static final String MOD_ID = "issgoety";

    public IssGoety() {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModSpells.SPELLS.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConversionConfig.SPEC);
    }
}

