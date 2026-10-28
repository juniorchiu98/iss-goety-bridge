package com.jj321.issgoety.mixin;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.init.ModAttributes;
import com.jj321.issgoety.attribute.AttributeBridge;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 讓 Goety 讀取屬性的集中入口改讀 ISS 屬性。 */
@Mixin(value = ModAttributes.class, remap = false)
public abstract class ModAttributesMixin {

    @Inject(method = "getPotency(Lnet/minecraft/world/entity/LivingEntity;)I",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void issgoety$potency(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(AttributeBridge.potency(entity));
    }

    @Inject(method = "getPotency(Lnet/minecraft/world/entity/LivingEntity;Lcom/Polarice3/Goety/api/magic/ISpell;)I",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void issgoety$potencyForSpell(LivingEntity entity, ISpell spell, CallbackInfoReturnable<Integer> cir) {
        if (AttributeBridge.isHandled(spell.getSpellType())) {
            cir.setReturnValue(AttributeBridge.potency(entity, spell));
        }
    }

    @Inject(method = "getCastingSpeed(Lnet/minecraft/world/entity/LivingEntity;)D",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void issgoety$castingSpeed(LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(AttributeBridge.castTimeMultiplier(entity));
    }

    @Inject(method = "getCooldownDiscount(Lnet/minecraft/world/entity/LivingEntity;)D",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void issgoety$cooldownDiscount(LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(AttributeBridge.cooldownMultiplier(entity));
    }

    @Inject(method = "lambda$modifyEntityAttributes$29(Lnet/minecraftforge/event/entity/EntityAttributeModificationEvent;Lnet/minecraft/world/entity/EntityType;Lnet/minecraftforge/registries/RegistryObject;)V",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void issgoety$skipUnifiedAttributes(EntityAttributeModificationEvent event, EntityType<?> type,
                                                       RegistryObject<Attribute> attribute, CallbackInfo ci) {
        if (AttributeBridge.shouldHide(attribute.getId())) {
            ci.cancel();
        }
    }
}


