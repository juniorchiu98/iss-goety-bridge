package com.jj321.issgoety.main;

import com.jj321.issgoety.IssGoety;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IssGoety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModCreativeTab {
    private ModCreativeTab() {
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(com.Polarice3.Goety.init.ModCreativeTab.TAB.getKey())) {
            event.accept(ModItems.SOUL_CONVERTER.get());
        }
    }
}