package com.jj321.issgoety.focus;

import com.jj321.issgoety.IssGoety;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IssGoety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FocusCooldownClientEvents {
    private FocusCooldownClientEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null) {
            // Goety already syncs its tracker. Reuse ISS's existing overlay and countdown rendering.
            FocusCooldownBridge.mirror(Minecraft.getInstance().player, ClientMagicData.getCooldowns());
        }
    }
}
