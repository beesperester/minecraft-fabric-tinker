package beesperester.intothewild.effects;

import java.time.Instant;

import beesperester.intothewild.classes.Rectangle;
import beesperester.intothewild.utilities.ColorUtilities;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;

import beesperester.intothewild.IntoTheWild;

public class HungerEffect implements IEffect {
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
        if (deltaTime > IntoTheWild.CONFIG.saturationDepletionRate) {
            hungerManager.setSaturationLevel(Math.max(hungerManager.getSaturationLevel() - 1, 0));

            saturationTick = currentTick;
        }

        // hunger
        long hungerDeltaTime = (currentTick - hungerTick) / 1000L;

        if (hungerDeltaTime > IntoTheWild.CONFIG.hungerDepletionRate) {
            if (hungerManager.getSaturationLevel() == 0f) {
                hungerManager.setFoodLevel(Math.max(hungerManager.getFoodLevel() - 1, 0));
            }

            hungerTick = currentTick;
        }

        if (player.isCreative())
            return;

        // starvation
        long starvationDeltaTime = (currentTick - starvationTick) / 1000L;
        float minimumStarvationHealth = IntoTheWild.CONFIG.allowDeathFromStarvation ? 0f : 0.5f;

        if (starvationDeltaTime > IntoTheWild.CONFIG.starvationRate) {
            if (hungerManager.getFoodLevel() == 0) {
                // damage player
                if (player.getHealth() > minimumStarvationHealth) {
                    player.setHealth(Math.max(player.getHealth() - 1.0f, minimumStarvationHealth));

                    player.sendMessage(new LiteralText(ColorUtilities.red.chatCode).append("You are starving"), false);

                    player.animateDamage();
                }
            }

            starvationTick = currentTick;
        }
    }

    public Rectangle render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, float x, float y) {
        return new Rectangle(x, y, 0f, 0f);
    }
}
