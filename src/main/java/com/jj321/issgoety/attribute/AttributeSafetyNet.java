package com.jj321.issgoety.attribute;

import com.jj321.issgoety.IssGoety;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;



/**
 * 安全網:Goety 附屬的物品(手持、盔甲、飾品)若帶有 Goety 的法術屬性加成,
 * 自動換成對應的 ISS 屬性,讓它們在統一後的屬性系統裡仍然有效。
 */
@Mod.EventBusSubscriber(modid = IssGoety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttributeSafetyNet {

    @SubscribeEvent
    public static void onItemModifiers(ItemAttributeModifierEvent event) {
        AttributeBridge.convertItemModifiers(event.getModifiers(), event::addModifier, event::removeModifier);
    }

    @SubscribeEvent
    public static void onCurioModifiers(CurioAttributeModifierEvent event) {
        AttributeBridge.convertItemModifiers(event.getModifiers(), event::addModifier, event::removeModifier);
    }
}
