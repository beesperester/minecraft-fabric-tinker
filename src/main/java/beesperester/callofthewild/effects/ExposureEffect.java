package beesperester.callofthewild.effects;

import java.time.Instant;

import beesperester.callofthewild.CallOfTheWildMod;
import beesperester.callofthewild.classes.Rectangle;
import beesperester.callofthewild.utilities.MathUtilities;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class ExposureEffect implements IEffect {
    public static float updateTemperatureRate = 4f;
    public static float damageRate = 4f;

    public static float biomeDayMaxTemperature = 20f;
    public static float biomeDayMinTemperature = -20f;
    public static float biomeNightMaxTemperature = 10f;
    public static float biomeNightMinTemperature = -40f;
    public static float bodyDefaultTemperature = 37f;
    public static float bodyMinTemperature = 35f;
    public static float bodyMaxTemperature = 43f;

    public float bodyTemperature;
    public float environmentTemperature;

    public float temperatureTick;
    public float damageTick;

    public ExposureEffect() {
        temperatureTick = (float) Instant.now().toEpochMilli();
        bodyTemperature = bodyDefaultTemperature;
    }

    public void tick(PlayerEntity player) {
        float currentTick = (float) Instant.now().toEpochMilli();

        // temperature
        float temperatureDeltaTime = (currentTick - temperatureTick) / 1000f;

        if (temperatureDeltaTime > updateTemperatureRate) {
            environmentTemperature = getEnvironmentTemperature(player) + getBlockTemperature(player, 8);

            float deltaTemperature = (environmentTemperature - bodyTemperature);

            float temperatureRate = deltaTemperature / bodyDefaultTemperature * 0.5f;

            float acclimatizeDelta = (bodyDefaultTemperature - bodyTemperature);

            bodyTemperature += MathUtilities.clamp(acclimatizeDelta, -2f, 1f)
                    + temperatureRate;

            temperatureTick = currentTick;
        }

        // damage
        float damageDeltaTime = (currentTick - temperatureTick) / 1000f;
        float minHealth = CallOfTheWildMod.CONFIG.allowDeathFromExposure ? 0f : 0.5f;

        if (damageDeltaTime > damageRate) {
            if (bodyTemperature < bodyMinTemperature) {

                player.setHealth(Math.max(player.getHealth() - 1.0f, minHealth));

                player.sendMessage(Text.of("You are suffering from hypothermia"), false);

                player.animateDamage();
            } else if (bodyTemperature > bodyMaxTemperature) {
                player.setHealth(Math.max(player.getHealth() - 1.0f, minHealth));

                player.sendMessage(Text.of("You are suffering from hyperthermia"), false);

                player.animateDamage();
            }

            damageTick = currentTick;
        }
    }

    public Rectangle render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, float x, float y) {
        int color = 16755200; // gold
        int environmentTextColor = 5636095; // aqua

        client.textRenderer.drawWithShadow(
                matrixStack,
                String.format(
                        "%.1f °C",
                        bodyTemperature),
                x,
                y,
                color);

        client.textRenderer.drawWithShadow(
                matrixStack,
                String.format(
                        "%.1f °C",
                        environmentTemperature),
                x,
                y + 16f,
                environmentTextColor);

        return new Rectangle(x, y, 64f, 32f);
    }

    public static float getEnvironmentTemperature(PlayerEntity player) {
        float environmentTemperature = 0f;
        float biomeTemperature = player.world.getBiome(player.getBlockPos()).getTemperature();

        float biomeDayTemperature = MathUtilities.lerp(biomeTemperature, biomeDayMinTemperature,
                biomeDayMaxTemperature);
        float biomeNightTemperature = MathUtilities.lerp(biomeTemperature, biomeNightMinTemperature,
                biomeNightMaxTemperature);

        float bias = (float) (Math.sin(Math.toRadians(((player.world.getTimeOfDay() / 24000f) * 360f))) * 0.5f) + 0.5f;

        if (player.world.isRaining()) {
            // rain will reduce the temperature bias
            bias *= 0.5f;
        }

        environmentTemperature = MathUtilities.lerp(bias, biomeNightTemperature, biomeDayTemperature);

        if (player.isSubmergedInWater()) {
            // water temperature does not fall below 4 degrees celsius
            environmentTemperature = Math.max(environmentTemperature * 0.5f, 4f);
        }

        return environmentTemperature;
    }

    public static float getBlockTemperature(PlayerEntity player, int radius) {
        BlockPos playerBlockPos = player.getBlockPos();
        float blockTemperature = 0f;

        for (BlockPos blockPos : BlockPos.iterateOutwards(playerBlockPos, radius, radius, radius)) {
            double distance = Math.sqrt(blockPos.getSquaredDistance(playerBlockPos));

            if (distance < radius) {
                Block block = player.world.getBlockState(blockPos).getBlock();

                String translationKey = block.getTranslationKey();

                float bias = Math.max(Math.min(1f / (float) Math.pow(distance, 2), 1f), 0f);

                // if (translationKey.contains("campfire")) {
                // blockTemperature += bias * campFireTemperature;
                // } else if (translationKey.contains("torch")) {
                // blockTemperature += bias * torchTemperature;
                // } else if (translationKey.contains("lantern")) {
                // blockTemperature += bias * lanternTemperature;
                // }
            }
        }

        return blockTemperature;
    }
}
