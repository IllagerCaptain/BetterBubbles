package com.betterbubbles.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	@Unique
	private static final Identifier AIR_EMPTY_TEXTURE = Identifier.of("betterbubbles", "hud/air_empty");
	@Unique
	private static final Identifier AIR_HALF_TEXTURE = Identifier.of("betterbubbles","hud/air_half");
	@Unique
	private static final Identifier AIR_FULL_TEXTURE = Identifier.of("betterbubbles","hud/air_full");

	@Shadow private int getHeartRows(int health) { return 0; }
	@Shadow private PlayerEntity getCameraPlayer() { return null; }
	@Shadow private LivingEntity getRiddenEntity() { return null; }
	@Shadow private int getHeartCount(LivingEntity entity) { return 0; }

	@Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;getHeartRows(I)I", shift = At.Shift.BEFORE), cancellable = true)
	private void cancelVanillaAirMeter(DrawContext context, CallbackInfo ci) {
		PlayerEntity playerEntity = this.getCameraPlayer();
		if (playerEntity != null) {
			int t = context.getScaledWindowHeight() - 39;
			int u = playerEntity.getMaxAir();
			int v = Math.min(playerEntity.getAir(), u);
			if (playerEntity.isSubmergedIn(FluidTags.WATER) || v < u) {
				ci.cancel();
				int w = this.getHeartRows(this.getHeartCount(this.getRiddenEntity())) + 1;
				int r = t - w * 10;
				int m = context.getScaledWindowWidth() / 2 + 91;
				int x = MathHelper.ceil((double)(v - 2) * 10.0 / (double)u);
				int s = MathHelper.ceil((double)(v - 1) * 10.0 / (double)u);
				int y = MathHelper.ceil((double)v * 10.0 / (double)u) - x;
				RenderSystem.enableBlend();

				for (int z = 0; z < x + y; ++z) {
					if (z < x) {
						context.drawGuiTexture(AIR_FULL_TEXTURE, m - z * 8 - 9, r, 9, 9);
					} else if (z == x) {
						context.drawGuiTexture(AIR_HALF_TEXTURE, m - z * 8 - 9, r, 9, 9);
						if (z == s && playerEntity.isSubmergedIn(FluidTags.WATER)) {
							MinecraftClient client = MinecraftClient.getInstance();
							client.execute(() -> {
								if (client.player != null) {
									assert client.world != null;
									client.world.playSound(
											client.player,
											client.player.getX(),
											client.player.getY(),
											client.player.getZ(),
											SoundEvent.of(Identifier.of("betterbubbles", "ui.pop")),
											SoundCategory.MASTER,
											1.0F,
											2.0F - ((x + y) / 10F)
									);
								}
							});
						}
					}
				}
				for (int z = 10; z > x + y; z--) {
					context.drawGuiTexture(AIR_EMPTY_TEXTURE, m - z * 8 - 1, r, 9, 9);
				}

				RenderSystem.disableBlend();
			}
		}
	}
}