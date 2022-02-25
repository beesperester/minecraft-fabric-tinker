package beesperester.callofthewild.effects;

import java.time.Instant;

import beesperester.callofthewild.classes.Rectangle;
import beesperester.callofthewild.utilities.ColorUtilities;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import beesperester.callofthewild.CallOfTheWildMod;

public class HungerEffect implements IEffect {
    public static float saturationDepletionRate = 24000f / 4f / 20f;
    public static float hungerDepletionRate = 24000f / 2f / 20f;
    public static float starvationRate = 24000f * 3f / 20f;

    private long saturationTick;
    private long hungerTick;
    private long starvationTick;;

    public HungerEffect() {
        saturationTick = Instant.now().toEpochMilli();
        hungerTick = saturationTick;
        starvationTick = saturationTick;
    }

    public void tick(PlayerEntity player) {
        long currentTick = Instant.now().toEpochMilli();
        long deltaTime = (currentTick - saturationTick) / 1000L;

        HungerManager hungerManager = player.getHungerManager();

        // saturation
        if (deltaTime > saturationDepletionRate) {
            hungerManager.setSaturationLevel(Math.max(hungerManager.getSaturationLevel() - 1, 0));

            saturationTick = currentTick;
        }

        // hunger
        long hungerDeltaTime = (currentTick - hungerTick) / 1000L;

        if (hungerDeltaTime > hungerDepletionRate) {
            if (hungerManager.getSaturationLevel() == 0f) {
                hungerManager.setFoodLevel(Math.max(hungerManager.getFoodLevel() - 1, 0));
            }

            hungerTick = currentTick;
        }

        if (player.isCreative())
            return;

        // starvation
        long starvationDeltaTime = (currentTick - starvationTick) / 1000L;
        float minimumStarvationHealth = CallOfTheWildMod.CONFIG.allowDeathFromStarvation ? 0f : 0.5f;

        if (starvationDeltaTime > starvationRate) {
            if (hungerManager.getFoodLevel() == 0) {
                // damage player
                if (player.getHealth() > minimumStarvationHealth) {
                    player.setHealth(Math.max(player.getHealth() - 1.0f, minimumStarvationHealth));

                    player.sendMessage(Text.of("You are starving"), false);

                    player.animateDamage();
                }
            }

            starvationTick = currentTick;
        }
    }

    public Rectangle render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, float x, float y) {
        return new Rectangle(x, y, 0f, 0f);
        // float height = 0f;

        // long currentTick = Instant.now().toEpochMilli();
        // long saturationDelta = (currentTick - saturationTick) / 1000L;

        // client.textRenderer.drawWithShadow(
        // matrixStack,
        // String.format(
        // "Saturation: %.1f",
        // saturationDepletionRate - (float) saturationDelta),
        // x,
        // y + height,
        // ColorUtilities.gold.decimalCode);

        // height += 16f;

        // return new Rectangle(0f, 0f, 64f, height);
    }
}
