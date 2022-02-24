package beesperester.callofthewild.effects;

import java.time.Instant;

import beesperester.callofthewild.CallOfTheWildMod;
import beesperester.callofthewild.classes.TemperatureProperty;
import beesperester.callofthewild.classes.Rectangle;
import beesperester.callofthewild.utilities.MathUtilities;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class ExposureEffect implements IEffect {
    public static float updateTemperatureRate = 1f;
    public static float updateDamageDefaultRate = 8f;

    public static float biomeDayMaxTemperature = 20f;
    public static float biomeDayMinTemperature = -20f;
    public static float biomeNightMaxTemperature = 10f;
    public static float biomeNightMinTemperature = -40f;
    public static float bodyTemperatureHarmlesssDeviation = 5f;
    public static float bodyTemperatureDangerousDeviation = 15f;
    public static float bodyDefaultTemperature = 36.5f;
    public static float environmentDefaultTemperature = 20.0f;
    public static float environmentTemperatureEffectStrength = 0.75f;

    public long temperatureTick;
    public float updateDamageRate;
    public long damageTick;

    public float bodyTemperature;
    public float environmentTemperature;

    public ExposureEffect() {
        temperatureTick = Instant.now().toEpochMilli();
        damageTick = temperatureTick;
        updateDamageRate = updateDamageDefaultRate;
        bodyTemperature = bodyDefaultTemperature;
        environmentTemperature = environmentDefaultTemperature;
    }

    public void tick(PlayerEntity player) {
        long currentTick = Instant.now().toEpochMilli();

        float lowerBounds = bodyDefaultTemperature - bodyTemperatureHarmlesssDeviation;
        float upperBounds = bodyDefaultTemperature + bodyTemperatureHarmlesssDeviation;

        // temperature
        long temperatureDeltaTime = (currentTick - temperatureTick) / 1000L;

        if (temperatureDeltaTime > updateTemperatureRate) {
            environmentTemperature = getEnvironmentTemperature(player);
            bodyTemperature = getBodyTemperature(player, environmentTemperature);

            float bias = 0f;

            if (bodyTemperature < lowerBounds || bodyTemperature > upperBounds) {
                float deltaTemperature = Math.abs(bodyDefaultTemperature - bodyTemperature);

                float deviationBias = MathUtilities.clamp(
                        (deltaTemperature - bodyTemperatureHarmlesssDeviation)
                                / (bodyTemperatureDangerousDeviation
                                        - bodyTemperatureHarmlesssDeviation),
                        0f,
                        1f);

                // CallOfTheWildMod.LOGGER.info(String.format("deviationBias %.2f",
                // deviationBias));

                bias = (float) Math.pow(deviationBias, 2.0);
            }

            updateDamageRate = MathUtilities.lerp(bias, updateDamageDefaultRate, 1f);

            temperatureTick = currentTick;
        }

        CallOfTheWildMod.LOGGER.info(String.format("updateDamageRate %.2f, body %.2f, lower %.2f",
                updateDamageRate, bodyTemperature, lowerBounds));

        // damage
        long damageDeltaTime = (currentTick - damageTick) / 1000L;
        float minHealth = CallOfTheWildMod.CONFIG.allowDeathFromExposure ? 0f : 0.5f;

        if (damageDeltaTime > updateDamageRate) {
            if (bodyTemperature < lowerBounds) {
                player.setHealth(Math.max(player.getHealth() - 1.0f, minHealth));

                player.sendMessage(Text.of("You are suffering from hypothermia"), false);

                player.animateDamage();
            } else if (bodyTemperature > upperBounds) {
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

    public static float getBodyTemperature(PlayerEntity player, float environmentTemperature) {
        float armorTemperature = getArmorTemperatureAmount(player);
        float deltaTemperature = Math.max(
                Math.abs(environmentTemperature - bodyDefaultTemperature)
                        - armorTemperature,
                0f);

        // CallOfTheWildMod.LOGGER.info(String.format("env %.2f, armor %.2f, delta
        // %.2f", environmentTemperature,
        // armorTemperature, deltaTemperature));

        float bias = MathUtilities.clamp(
                deltaTemperature / bodyDefaultTemperature * environmentTemperatureEffectStrength, 0f,
                1f);

        bias = (float) Math.pow(bias, 2.0);

        float bodyTemperature = MathUtilities.lerp(bias, bodyDefaultTemperature, environmentTemperature);

        return bodyTemperature;
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
            // water temperature does not fall below 4 degrees celsius but will cool the
            // environment
            environmentTemperature = Math.max(environmentTemperature * 0.5f, 4f);
        }

        environmentTemperature += getBlockTemperatureAmount(player, 8);

        return environmentTemperature;
    }

    public static float getBlockTemperatureAmount(PlayerEntity player, int radius) {
        BlockPos playerBlockPos = player.getBlockPos();
        float temperatureChangeAmount = 0f;

        for (BlockPos blockPos : BlockPos.iterateOutwards(playerBlockPos, radius, radius, radius)) {
            double distance = Math.sqrt(blockPos.getSquaredDistance(playerBlockPos));

            if (distance < radius) {
                Block block = player.world.getBlockState(blockPos).getBlock();

                String translationKey = block.getTranslationKey();

                float bias = (float) Math.max(Math.min(1.0 / Math.pow(distance, 2), 1.0), 0.0);

                for (TemperatureProperty blockProperties : CallOfTheWildMod.CONFIG.blockProperties) {
                    if (translationKey.equals(blockProperties.translationKey)) {
                        temperatureChangeAmount += bias * blockProperties.value;

                        break;
                    }
                }
            }
        }

        return temperatureChangeAmount;
    }

    public static float getArmorTemperatureAmount(PlayerEntity player) {
        float temperatureChangeAmount = 0f;

        for (ItemStack itemStack : player.getArmorItems()) {
            String translationKey = itemStack.getTranslationKey();

            for (TemperatureProperty itemProperties : CallOfTheWildMod.CONFIG.itemProperties) {
                if (translationKey.equals(itemProperties.translationKey)) {
                    temperatureChangeAmount += itemProperties.value;

                    break;
                }
            }
        }

        return temperatureChangeAmount;
    }
}
