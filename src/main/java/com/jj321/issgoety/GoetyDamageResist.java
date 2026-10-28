package com.jj321.issgoety;

import com.jj321.issgoety.conversion.ConversionConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 讓 ISS 的魔法抗性對 Goety 的法術傷害生效。
 * 哪種 Goety 傷害屬於哪個 ISS 學派,由 data/issgoety/tags/damage_type/school_*.json 決定,可用資料包增減。
 * 傷害來源本身(類型、擊退、燃燒等)維持 Goety 原樣,只多乘上 ISS 的抗性倍率。
 */
@Mod.EventBusSubscriber(modid = IssGoety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GoetyDamageResist {

    private static final Map<TagKey<DamageType>, RegistryObject<SchoolType>> SCHOOLS = new LinkedHashMap<>();

    static {
        add("school_fire", SchoolRegistry.FIRE);
        add("school_ice", SchoolRegistry.ICE);
        add("school_lightning", SchoolRegistry.LIGHTNING);
        add("school_nature", SchoolRegistry.NATURE);
        add("school_ender", SchoolRegistry.ENDER);
        add("school_eldritch", SchoolRegistry.ELDRITCH);
        add("school_evocation", SchoolRegistry.EVOCATION);
    }

    private static void add(String name, RegistryObject<SchoolType> school) {
        SCHOOLS.put(TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(IssGoety.MOD_ID, name)), school);
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!ConversionConfig.GOETY_DAMAGE_USES_ISS_RESIST.get()) return;
        DamageSource source = event.getSource();
        // ISS 自己的法術傷害已經在 ISS 內部算過抗性,不重複
        if (source instanceof SpellDamageSource) return;

        for (Map.Entry<TagKey<DamageType>, RegistryObject<SchoolType>> entry : SCHOOLS.entrySet()) {
            if (source.is(entry.getKey())) {
                float multiplier = DamageSources.getResist(event.getEntity(), entry.getValue().get());
                event.setAmount(event.getAmount() * multiplier);
                return;
            }
        }
    }
}
