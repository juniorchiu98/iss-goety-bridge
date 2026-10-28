package com.jj321.issgoety.attribute;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.api.magic.SpellType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.jj321.issgoety.conversion.ConversionConfig;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Goety 屬性 → ISS 屬性 的對應與換算。目前只處理對得上的:
 * spell_potency、casting_speed、cooldown_discount,以及 frost/storm/nether/wild/void/necromancy 的學派潛力。
 * abyss / geomancy / wind 沒有對應,維持 Goety 原本的屬性。
 */
public final class AttributeBridge {

    private static final Map<SpellType, RegistryObject<Attribute>> SCHOOL_MAP = Map.of(
            SpellType.FROST, AttributeRegistry.ICE_SPELL_POWER,
            SpellType.STORM, AttributeRegistry.LIGHTNING_SPELL_POWER,
            SpellType.NETHER, AttributeRegistry.FIRE_SPELL_POWER,
            SpellType.WILD, AttributeRegistry.NATURE_SPELL_POWER,
            SpellType.VOID, AttributeRegistry.ENDER_SPELL_POWER,
            SpellType.NECROMANCY, AttributeRegistry.BLOOD_SPELL_POWER,
            SpellType.ILL, AttributeRegistry.EVOCATION_SPELL_POWER
    );

    private AttributeBridge() {
    }

    /** 安全網用:Goety 屬性 id(路徑) → ISS 屬性。true 表示是「潛力」(整數等級),false 表示是時間類折扣。 */
    private record Target(RegistryObject<Attribute> attribute, boolean potency) {
    }

    private static final Map<String, Target> ITEM_MAP = Map.of(
            "spell_potency", new Target(AttributeRegistry.SPELL_POWER, true),
            "frost_potency", new Target(AttributeRegistry.ICE_SPELL_POWER, true),
            "storm_potency", new Target(AttributeRegistry.LIGHTNING_SPELL_POWER, true),
            "nether_potency", new Target(AttributeRegistry.FIRE_SPELL_POWER, true),
            "wild_potency", new Target(AttributeRegistry.NATURE_SPELL_POWER, true),
            "void_potency", new Target(AttributeRegistry.ENDER_SPELL_POWER, true),
            "necromancy_potency", new Target(AttributeRegistry.BLOOD_SPELL_POWER, true),
            "ill_potency", new Target(AttributeRegistry.EVOCATION_SPELL_POWER, true),
            "casting_speed", new Target(AttributeRegistry.CAST_TIME_REDUCTION, false),
            "cooldown_discount", new Target(AttributeRegistry.COOLDOWN_REDUCTION, false)
    );

    /**
     * 把物品上的 Goety 法術屬性加成換成對應的 ISS 屬性加成。
     * 只處理「加算」類型的加成;沒有對應的 Goety 屬性、或其他運算方式的,原樣保留。
     */
    public static void convertItemModifiers(Multimap<Attribute, AttributeModifier> modifiers,
                                            BiConsumer<Attribute, AttributeModifier> add,
                                            BiConsumer<Attribute, AttributeModifier> remove) {
        for (Map.Entry<Attribute, AttributeModifier> entry : ImmutableList.copyOf(modifiers.entries())) {
            ResourceLocation key = ForgeRegistries.ATTRIBUTES.getKey(entry.getKey());
            if (key == null || !"goety".equals(key.getNamespace())) continue;
            Target target = ITEM_MAP.get(key.getPath());
            if (target == null) continue;

            AttributeModifier original = entry.getValue();
            if (original.getOperation() != AttributeModifier.Operation.ADDITION) continue;

            double amount = target.potency()
                    ? original.getAmount() * ConversionConfig.POWER_PER_POTENCY.get()
                    : original.getAmount();
            UUID id = UUID.nameUUIDFromBytes((original.getId() + "|issgoety|" + key).getBytes(StandardCharsets.UTF_8));
            AttributeModifier converted = new AttributeModifier(id, original.getName() + " (ISS)",
                    amount, AttributeModifier.Operation.MULTIPLY_BASE);

            remove.accept(entry.getKey(), original);
            add.accept(target.attribute().get(), converted);
        }
    }

    /** 沒有對應 ISS 屬性、仍由 Goety 原本屬性處理的學派。 */
    private static boolean keepsGoetyAttribute(SpellType type) {
        return type == SpellType.ABYSS || type == SpellType.GEOMANCY || type == SpellType.WIND;
    }

    /** 這個法術是否由 ISS 屬性處理(只有深淵、地質、風三個學派仍交給 Goety 原本的屬性)。 */
    public static boolean isHandled(SpellType type) {
        return type == null || !keepsGoetyAttribute(type);
    }

    /** 這個 Goety 屬性是否已統一成 ISS 屬性,而且設定要求把它從生物身上隱藏。 */
    public static boolean shouldHide(ResourceLocation attributeId) {
        if (attributeId == null || !"goety".equals(attributeId.getNamespace())) return false;
        if (!ITEM_MAP.containsKey(attributeId.getPath())) return false;
        try {
            return ConversionConfig.HIDE_UNIFIED_GOETY_ATTRIBUTES.get();
        } catch (IllegalStateException notLoadedYet) {
            return ConversionConfig.HIDE_UNIFIED_GOETY_ATTRIBUTES.getDefault();
        }
    }

    /** 沒有學派時:只看 ISS 全域法術強度。 */
    public static int potency(LivingEntity entity) {
        return toPotency(entity.getAttributeValue(AttributeRegistry.SPELL_POWER.get()));
    }

    /** 有學派時:ISS 全域強度 × 該學派強度(與 ISS 自己的算法一致)。 */
    public static int potency(LivingEntity entity, ISpell spell) {
        SpellType type = spell.getSpellType();
        double power = entity.getAttributeValue(AttributeRegistry.SPELL_POWER.get());
        RegistryObject<Attribute> school = type == null ? null : SCHOOL_MAP.get(type);
        if (school != null) {
            power *= entity.getAttributeValue(school.get());
        }
        return toPotency(power);
    }

    private static int toPotency(double power) {
        double levels = (power - 1.0D) / ConversionConfig.POWER_PER_POTENCY.get();
        return (int) Math.max(0, Math.round(levels));
    }

    /** Goety 把這個值當成「時間倍率」使用(1 - 屬性),所以直接回傳 ISS 的時間倍率 2 - softCap(屬性)。 */
    public static double castTimeMultiplier(LivingEntity entity) {
        return 2.0D - Utils.softCapFormula(entity.getAttributeValue(AttributeRegistry.CAST_TIME_REDUCTION.get()));
    }

    public static double cooldownMultiplier(LivingEntity entity) {
        return 2.0D - Utils.softCapFormula(entity.getAttributeValue(AttributeRegistry.COOLDOWN_REDUCTION.get()));
    }
}
