package com.jj321.issgoety.conversion;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConversionConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue MANA_PER_SOUL;
    public static final ForgeConfigSpec.DoubleValue POWER_PER_POTENCY;
    public static final ForgeConfigSpec.BooleanValue GOETY_DAMAGE_USES_ISS_RESIST;
    public static final ForgeConfigSpec.BooleanValue HIDE_UNIFIED_GOETY_ATTRIBUTES;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("soul_to_mana");
        MANA_PER_SOUL = b
                .comment("每 1 點靈魂能量換得多少魔力。小於 1 代表要付出更多靈魂(這就是代價)。")
                .defineInRange("manaPerSoul", 0.5D, 0.001D, 1000D);
        b.pop();

        b.push("attribute_bridge");
        POWER_PER_POTENCY = b
                .comment("ISS 法術強度(倍率,1.0 = 100%)每增加多少,換算成 Goety 的 1 級潛力。",
                         "例如 0.1 代表 ISS 強度 1.3 約等於 Goety 潛力 3。需要進遊戲調整。")
                .defineInRange("powerPerPotency", 0.1D, 0.001D, 10D);
        GOETY_DAMAGE_USES_ISS_RESIST = b
                .comment("Goety 法術傷害是否套用 ISS 的魔法抗性。學派對應由 data/issgoety/tags/damage_type/school_*.json 決定。")
                .define("goetyDamageUsesIssResist", true);
        HIDE_UNIFIED_GOETY_ATTRIBUTES = b
                .comment("已統一成 ISS 屬性的 Goety 屬性(潛力、施法速度、冷卻折扣)不再附加到任何生物身上,/attribute 也看不到。",
                         "屬性本身仍留在註冊表裡(Forge 無法移除已註冊的項目)。如果遊戲因為讀取這些屬性而出錯,把它關掉。")
                .define("hideUnifiedGoetyAttributes", true);
        b.pop();

        SPEC = b.build();
    }
}
