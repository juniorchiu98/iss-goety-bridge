package com.jj321.issgoety.conversion;

import com.Polarice3.Goety.utils.SEHelper;
import com.jj321.issgoety.main.ModItems;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

/** Soul affordability is read-only; conversion is paid only at ISS's actual mana deduction. */
public final class SoulConversion {
    private SoulConversion() { }

    public record Payment(float manaAfter, boolean affordable) { }

    public static boolean isWearingConverter(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> !handler.findCurios(ModItems.SOUL_CONVERTER.get()).isEmpty())
                .orElse(false);
    }

    private static boolean canConvert(Player player, CastSource source) {
        return player != null && source != null && source.consumesMana()
                && (!player.isCreative() || ServerConfigs.CREATIVE_MANA_COST.get())
                && isWearingConverter(player);
    }

    /** The original ISS learning, cooldown and cast checks still decide whether casting is allowed. */
    public static float availableMana(float actual, Player player, CastSource source) {
        if (!canConvert(player, source)) return actual;
        return manaWithSouls(actual, SEHelper.getSoulAmountInt(player), ConversionConfig.MANA_PER_SOUL.get());
    }

    /** Negative balance is the exact shortfall after other mods have adjusted SpellOnCastEvent's cost. */
    public static Payment payBalance(float balance, ServerPlayer player, CastSource source) {
        float nativeBalance = Math.max(balance, 0.0F);
        if (!(balance < 0.0F) || !canConvert(player, source)) return new Payment(nativeBalance, true);

        int souls = requiredSouls(-(double) balance, ConversionConfig.MANA_PER_SOUL.get());
        if (souls < 0 || SEHelper.getSoulAmountInt(player) < souls) return new Payment(0.0F, false);
        // This native helper selects the active Soul Arca or totem and applies Goety's soul-loss event.
        // A canceled/discounted loss event deliberately changes the payment, just as for Goety spells.
        SEHelper.decreaseSoulsNoDiscount(player, souls);
        return new Payment(0.0F, true);
    }

    static int requiredSouls(double deficit, double rate) {
        if (!Double.isFinite(deficit) || !Double.isFinite(rate) || rate <= 0.0D) return -1;
        if (deficit <= 0.0D) return 0;
        double souls = Math.ceil(deficit / rate);
        return !Double.isFinite(souls) || souls > Integer.MAX_VALUE ? -1 : (int) souls;
    }

    static float manaWithSouls(float actual, int souls, double rate) {
        if (!Float.isFinite(actual) || souls <= 0 || !Double.isFinite(rate) || rate <= 0.0D) return actual;
        double total = actual + souls * rate;
        if (total >= Float.MAX_VALUE) return Float.MAX_VALUE;
        float available = (float) total;
        // Rounding a credit up could pass validation even though the player cannot pay the difference.
        return available > total ? Math.nextDown(available) : available;
    }
}