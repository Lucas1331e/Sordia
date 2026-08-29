package dasouza.telum.mixin.client;

import dasouza.telum.Telum;
import dasouza.telum.client.SulfurClientHudTracker;
import dasouza.telum.component.AssembledToolData;
import dasouza.telum.effect.TelumEffects;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.tool.PartMaterial;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class GuiMixin {

    @Unique
    private static final Identifier SHATTER_HEART_TEXTURE = Telum.id("textures/gui/shatter_heart.png");

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void renderCustomHudOverlays(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isSpectator()) {
            return;
        }

        // 1. Shatter Effect Overlay
        if (!mc.player.isCreative() && TelumEffects.SHATTER != null && mc.player.hasEffect(TelumEffects.SHATTER)) {
            MobEffectInstance effect = mc.player.getEffect(TelumEffects.SHATTER);
            if (effect != null) {
                int shatterLevel = effect.getAmplifier() + 1;
                float maxHealth = mc.player.getMaxHealth();

                int left = mc.getWindow().getGuiScaledWidth() / 2 - 91;
                int top = mc.getWindow().getGuiScaledHeight() - 39;

                if (mc.player.getArmorValue() > 0) {
                    top -= 10;
                }

                int startHeart = (int) Math.ceil(maxHealth / 2.0);

                for (int i = 0; i < shatterLevel; i++) {
                    int heartIdx = startHeart + i;
                    int row = heartIdx / 10;
                    int col = heartIdx % 10;
                    int x = left + col * 8;
                    int y = top - row * 10;

                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SHATTER_HEART_TEXTURE, x, y, 0.0f, 0.0f, 9, 9, 9, 9);
                }
            }
        }

        // 2. Sulfur Tool 3-Hit Charge Indicator (Subtle vertical rectangles above XP bar center)
        AssembledToolData toolData = AssembledToolItem.getToolData(mc.player.getMainHandItem());
        int sulfurLvl = toolData != null ? toolData.getMaterialLevel(PartMaterial.SULFUR) : 0;

        if (sulfurLvl >= 1 && SulfurClientHudTracker.getDisplayTicks() > 0) {
            int charge = SulfurClientHudTracker.getCurrentCharge();
            int displayTicks = SulfurClientHudTracker.getDisplayTicks();

            int centerX = mc.getWindow().getGuiScaledWidth() / 2;
            int top = mc.getWindow().getGuiScaledHeight() - 36; // Positioned subtly above center of XP bar

            // Smooth fade out during last 10 ticks
            int alpha = 255;
            if (displayTicks < 10) {
                alpha = (int) (255.0f * (displayTicks / 10.0f));
            }

            // 3 subtle vertical rectangles
            // Width: 3px, Height: 7px, Gap: 3px -> Total width = 15px
            int startX = centerX - 7;

            for (int i = 0; i < 3; i++) {
                int rx = startX + i * 6;
                int ry = top;
                boolean isFilled = (charge > 0 && i < charge);

                int borderAlpha = Math.min(alpha, 180);
                int borderColor = (borderAlpha << 24) | 0x111111;

                int fillColor;
                if (isFilled) {
                    fillColor = (alpha << 24) | 0xE5C158; // Sulfur Gold / Yellow
                } else {
                    int greyAlpha = Math.min(alpha, 140);
                    fillColor = (greyAlpha << 24) | 0x444444; // Translucent Subtle Grey
                }

                // Dark subtle border
                guiGraphics.fill(rx - 1, ry - 1, rx + 4, ry + 8, borderColor);
                // Inner filled rectangle
                guiGraphics.fill(rx, ry, rx + 3, ry + 7, fillColor);
            }
        }
    }
}
