package beesperester.callofthewild.effects;

import java.time.Instant;

import beesperester.callofthewild.classes.Rectangle;
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

    private float saturationTick = (float) Instant.now().toEpochMilli();
    private float hungerTick = (float) Instant.now().toEpochMilli();
    private float starvationTick = (float) Instant.now().toEpochMilli();

    public HungerEffect() {
        saturationTick = (float) Instant.now().toEpochMilli();
        hungerTick = saturationTick;
        starvationTick = saturationTick;
    }

    public void tick(PlayerEntity player) {
        float currentTick = (float) Instant.now().toEpochMilli();
        float deltaTime = (currentTick - saturationTick) / 1000f;

        HungerManager hungerManager = player.getHungerManager();

        // saturation
        if (deltaTime > saturationDepletionRate) {
            hungerManager.setSaturationLevel(Math.max(hungerManager.getSaturationLevel() - 1, 0));

            saturationTick = currentTick;
        }

        // hunger
        float hungerDeltaTime = (currentTick - hungerTick) / 1000f;

        if (hungerDeltaTime > hungerDepletionRate) {
            if (hungerManager.getSaturationLevel() == 0f) {
                hungerManager.setFoodLevel(Math.max(hungerManager.getFoodLevel() - 1, 0));
            }

            hungerTick = currentTick;
        }

        // starvation
        float starvationDeltaTime = (currentTick - starvationTick) / 1000.0f;
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
        return new Rectangle(0f, 0f, 0f, 0f);
    }
}
