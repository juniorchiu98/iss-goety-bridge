package com.jj321.issgoety.mixin;

import com.jj321.issgoety.IssGoety;
import com.jj321.issgoety.focus.FocusSpell;
import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import io.redspace.ironsspellbooks.gui.overlays.SpellWheelOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/** Render the current focus item in the proxy's spell icon rectangle, including item models and resource packs. */
@Mixin(value = {SpellBarOverlay.class, SpellWheelOverlay.class}, remap = false)
public abstract class FocusSpellIconMixin {
    @Unique
    private static final ResourceLocation issgoety$focusIcon =
            new ResourceLocation(IssGoety.MOD_ID, "textures/gui/spell_icons/focus_cast.png");

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V",
            remap = true), remap = false)
    private void issgoety$renderFocusIcon(GuiGraphics graphics, ResourceLocation texture,
                                         int x, int y, float u, float v, int width, int height,
                                         int textureWidth, int textureHeight) {
        if (issgoety$focusIcon.equals(texture) && Minecraft.getInstance().player != null) {
            ItemStack focus = FocusSpell.getDisplayFocus(Minecraft.getInstance().player);
            if (!focus.isEmpty()) {
                // renderItem/flush change GL state; ISS's next flat spell icon expects its own state.
                boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
                boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
                boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
                boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
                int depthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
                int sourceRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
                int destinationRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
                int sourceAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
                int destinationAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
                float[] color = RenderSystem.getShaderColor().clone();
                var shader = RenderSystem.getShader();
                int shaderTexture = RenderSystem.getShaderTexture(0);
                graphics.flush();
                graphics.pose().pushPose();
                try {
                    graphics.pose().translate(x, y, 0);
                    graphics.pose().scale(width / 16.0F, height / 16.0F, 1);
                    graphics.renderItem(focus, 0, 0);
                    graphics.flush();
                } finally {
                    graphics.pose().popPose();
                    RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
                    if (shader != null) RenderSystem.setShader(() -> shader);
                    RenderSystem.setShaderTexture(0, shaderTexture);
                    RenderSystem.depthMask(depthMask);
                    RenderSystem.depthFunc(depthFunction);
                    if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
                    RenderSystem.blendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
                    if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
                    if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
                }
                return;
            }
        }
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }
}
