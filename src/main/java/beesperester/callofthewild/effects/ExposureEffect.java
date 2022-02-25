package beesperester.callofthewild.effects;

import java.time.Instant;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import beesperester.callofthewild.CallOfTheWildMod;
import beesperester.callofthewild.classes.BiomeTemperature;
import beesperester.callofthewild.classes.EntityTemperature;
import beesperester.callofthewild.classes.Rectangle;
import beesperester.callofthewild.utilities.ColorUtilities;
import beesperester.callofthewild.utilities.MathUtilities;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

public class ExposureEffect implements IEffect {
    public static float updateTemperatureRate = 1f;
    public static float updateDamageDefaultRate = 60f;

    public static float bodyTemperatureHarmlesssDeviation = 10f;
    public static float bodyTemperatureDangerousDeviation = 30f;
    public static float bodyDefaultTemperature = 36.5f;

    public static float environmentDefaultTemperature = 20.0f;
    public static float environmentTemperatureEffectStrength = 1f;

    public long temperatureTick;
    public float updateDamageRate;
    public long damageTick;

    public float bodyTemperature;
    public float environmentTemperature;
    public float armorTemperature;

    public ExposureEffect() {
        temperatureTick = Instant.now().toEpochMilli();
        damageTick = temperatureTick;
        updateDamageRate = updateDamageDefaultRate;
        bodyTemperature = bodyDefaultTemperature;
        environmentTemperature = environmentDefaultTemperature;
        armorTemperature = 0f;
    }

    public void tick(PlayerEntity player) {
        long currentTick = Instant.now().toEpochMilli();

        float lowerBounds = bodyDefaultTemperature - 1.5f;
        float upperBounds = bodyDefaultTemperature + 1.5f;

        // temperature
        long temperatureDeltaTime = (currentTick - temperatureTick) / 1000L;

        if (temperatureDeltaTime > updateTemperatureRate) {
            environmentTemperature = getEnvironmentTemperature(player);
            armorTemperature = getArmorTemperatureAmount(player);
            bodyTemperature = getBodyTemperature(player, environmentTemperature, armorTemperature);

            float deltaTemperature = MathUtilities.distance(environmentTemperature, bodyTemperature);

            float bias = MathUtilities.clamp(deltaTemperature / bodyTemperatureDangerousDeviation, 0f, 1f);

            bias = MathUtilities.easeInCubic(bias);

            updateDamageRate = MathUtilities.lerp(bias, updateDamageDefaultRate, 1f);

            temperatureTick = currentTick;
        }

        if (player.isCreative())
            return;

        // damage
        long damageDeltaTime = (currentTick - damageTick) / 1000L;
        float minHealth = CallOfTheWildMod.CONFIG.allowDeathFromExposure ? 0f : 0.5f;

        if (damageDeltaTime > updateDamageRate) {
            if (bodyTemperature < lowerBounds) {
                player.setHealth(Math.max(player.getHealth() - 1.0f, minHealth));

                player.sendMessage(new LiteralText("§bYou are suffering from hypothermia"), false);

                player.animateDamage();
            } else if (bodyTemperature > upperBounds) {
                player.setHealth(Math.max(player.getHealth() - 1.0f, minHealth));

                player.sendMessage(new LiteralText("§cYou are suffering from hyperthermia"), false);

                player.animateDamage();
            }

            damageTick = currentTick;
        }
    }

    public Rectangle render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, float x, float y) {
        float height = 0f;

        client.textRenderer.drawWithShadow(
                matrixStack,
                String.format(
                        "Body: %.1f °C",
                        bodyTemperature),
                x,
                y + height,
                ColorUtilities.gold.decimalCode);

        height += 16f;

        client.textRenderer.drawWithShadow(
                matrixStack,
                String.format(
                        "Armor: %.1f °C",
                        armorTemperature),
                x,
                y + height,
                ColorUtilities.green.decimalCode);

        height += 16f;

        client.textRenderer.drawWithShadow(
                matrixStack,
                String.format(
                        "Environment: %.1f °C",
                        environmentTemperature),
                x,
                y + height,
                ColorUtilities.aqua.decimalCode);

        height += 16f;

        return new Rectangle(x, y, 64f, height);
    }

    public static float getBodyTemperature(PlayerEntity player, float environmentTemperature, float armorTemperature) {
        float delta = Math.max(MathUtilities.distance(environmentTemperature, bodyDefaultTemperature)
                - bodyTemperatureHarmlesssDeviation - armorTemperature, 0f);

        float bias = MathUtilities.clamp(
                delta / bodyTemperatureDangerousDeviation,
                0f,
                1f);

        bias = MathUtilities.easeInCubic(bias);

        bias *= environmentTemperatureEffectStrength;

        CallOfTheWildMod.LOGGER
                .info(String.format("env %.2f, armor %.2f, delta %.2f, bias %.2f", environmentTemperature,
                        armorTemperature, delta, bias));

        float exposureEffect = (environmentTemperature < bodyDefaultTemperature)
                ? -bodyTemperatureDangerousDeviation
                : bodyTemperatureDangerousDeviation;

        float bodyTemperature = bodyDefaultTemperature + (bias * exposureEffect);

        return bodyTemperature;
    }

    public static float getEnvironmentTemperature(PlayerEntity player) {
        float environmentTemperature = 0f;
        float biomeTemperatureCode = player.world.getBiome(player.getBlockPos()).getTemperature();

        Collections.sort(CallOfTheWildMod.CONFIG.biomeTemperatures, (a, b) -> {
            return (int) (a.code - b.code);
        });

        float biomeDayTemperature = 0f;
        float biomeNightTemperature = 0f;

        for (BiomeTemperature biomeTemperature : CallOfTheWildMod.CONFIG.biomeTemperatures) {
            if (biomeTemperatureCode <= biomeTemperature.code) {
                biomeDayTemperature = biomeTemperature.day;
                biomeNightTemperature = biomeTemperature.night;

                break;
            }
        }

        CallOfTheWildMod.LOGGER
                .info(String.format("biome %.2f, day %.2f, night %.2f", biomeTemperatureCode,
                        biomeDayTemperature, biomeNightTemperature));

        float bias = (float) (Math.sin(Math.toRadians(((player.world.getTimeOfDay() / 24000f) * 360f))) * 0.5f) + 0.5f;

        environmentTemperature = MathUtilities.lerp(bias, biomeNightTemperature, biomeDayTemperature);

        if (player.world.isRaining()) {
            // rain will reduce the temperature bias
            environmentTemperature *= environmentTemperature > 0f ? 0.5f : 1.0f;
        }

        if (player.isSubmergedInWater()) {
            // water temperature does not fall below 4 degrees celsius but will cool the
            // environment
            environmentTemperature = Math.max(environmentTemperature * 0.5f, 4f);
        }

        // add block temperature
        environmentTemperature += getBlockTemperatureAmount(player, 8);

        // add item temperature
        environmentTemperature += getItemTemperature(player);

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

                for (EntityTemperature blockProperties : CallOfTheWildMod.CONFIG.entityProperties) {
                    if (blockProperties.matches(translationKey)) {
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

            for (EntityTemperature entityTemperature : CallOfTheWildMod.CONFIG.entityProperties.stream()
                    .filter(EntityTemperature.armorFilter)
                    .collect(Collectors.toList())) {
                if (entityTemperature.matches(translationKey)) {
                    temperatureChangeAmount += entityTemperature.value;

                    break;
                }
            }
        }

        return temperatureChangeAmount;
    }

    public static float getItemTemperature(PlayerEntity player) {
        float temperatureChangeAmount = 0f;

        for (ItemStack itemStack : player.getItemsHand()) {
            String translationKey = itemStack.getTranslationKey();

            // adds block temperature properties
            for (EntityTemperature blockProperties : CallOfTheWildMod.CONFIG.entityProperties.stream()
                    .filter(EntityTemperature.blockFilter).collect(Collectors.toList())) {
                if (blockProperties.matches(translationKey)) {
                    temperatureChangeAmount += blockProperties.value;

                    break;
                }
            }

            // adds item temperature properties
            for (EntityTemperature itemProperties : CallOfTheWildMod.CONFIG.entityProperties.stream()
                    .filter(EntityTemperature.itemFilter)
                    .filter(EntityTemperature.notArmorFilter)
                    .collect(Collectors.toList())) {
                if (itemProperties.matches(translationKey)) {
                    temperatureChangeAmount += itemProperties.value;

                    break;
                }
            }
        }

        return temperatureChangeAmount;
    }
}
