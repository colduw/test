package lain.mods.cos.init.neoforge.mixins;

import lain.mods.cos.impl.client.PlayerRenderHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class MixinPlayerRenderer {

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void CosArmor_onExtractRenderState(Avatar player, AvatarRenderState state, float partialTicks, CallbackInfo info) {
        if (player instanceof AbstractClientPlayer acp) {
            PlayerRenderHandler.INSTANCE.onExtractPlayerRenderState(acp, state, partialTicks);
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void CosArmor_onFinishRenderState(Avatar player, AvatarRenderState state, float partialTicks, CallbackInfo info) {
        if (player instanceof AbstractClientPlayer acp) {
            PlayerRenderHandler.INSTANCE.onFinishPlayerRenderState(acp, state, partialTicks);
        }
    }

}
