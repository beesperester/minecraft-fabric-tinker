package beesperester.callofthewild;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import beesperester.callofthewild.utilities.MathUtilities;

public class CallOfTheWildModMonolith implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("modid");

    public static boolean allowHungerEffects = false;

    public static boolean allowDeathFromStarvation = false;
    public static float hungerDepletionRate = 2.0f;
    public static float saturationDepletionRate = 2.0f;
    public static float starvationRate = 1.0f;

    public static boolean allowDeathFromExposure = false;
    public static float environmentTemperature = 0.0f;
    public static float bodyDefaultTemperature = 37f;
    public static float updateTemperatureRate = 4.0f;
    public static float biomeDayMaxTemperature = 20f;
    public static float biomeDayMinTemperature = -20f;
    public static float biomeNightMaxTemperature = 10f;
    public static float biomeNightMinTemperature = -40f;
    public static float acclimatizeRate = 1.0f;
    public static float bodyTemperatureDangerousThreshold = 0.1f;

    public static float campFireTemperature = 200f;
    public static float torchTemperature = 50f;
    public static float lanternTemperature = 50f;

    public ClientPlayerEntity player;
    public World world;

    public long hungerTick;
    public long saturationTick;
    public long starvationTick;
    public long temperatureTick;

    public float bodyTemperature;

    @Override
    public void onInitialize() {
        hungerTick = Instant.now().toEpochMilli();
        saturationTick = hungerTick;
        starvationTick = hungerTick;
        temperatureTick = hungerTick;

        bodyTemperature = bodyDefaultTemperature;

        ClientTickEvents.END_CLIENT_TICK.register((client) -> tick(client));
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> renderStatus(matrixStack, tickDelta));
    }

    public void renderStatus(MatrixStack matrixStack, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();

        float x = window.getScaledWidth() * 0.025f; // window.getScaledWidth() * 0.5f;
        float y = window.getScaledWidth() * 0.025f; // window.getScaledHeight() * 0.5f;
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

        // LOGGER.info("Hello from the hud render callback");
    }

    public void tick(MinecraftClient client) {
        if (player == null) {
            if (client.player != null) {
                player = client.player;

                world = player.world;
            } else {
                return;
            }
        }

        HungerManager hungerManager = player.getHungerManager();
        long currentTick = Instant.now().toEpochMilli();

        if (allowHungerEffects) {
            // saturation
            float saturationDeltaTime = (float) (currentTick - saturationTick) / 1000.0f;

            if (saturationDeltaTime > saturationDepletionRate) {
                hungerManager.setSaturationLevel(Math.max(hungerManager.getSaturationLevel() - 1, 0));

                saturationTick = currentTick;
            }

            // hunger
            float hungerDeltaTime = (float) (currentTick - hungerTick) / 1000.0f;

            if (hungerDeltaTime > hungerDepletionRate) {
                if (hungerManager.getSaturationLevel() == 0f) {
                    hungerManager.setFoodLevel(Math.max(hungerManager.getFoodLevel() - 1, 0));
                }

                hungerTick = currentTick;
            }

            // starvation
            float starvationDeltaTime = (float) (currentTick - starvationTick) / 1000.0f;

            if (starvationDeltaTime > starvationRate) {
                if (hungerManager.getFoodLevel() == 0) {
                    float minimumStarvationHealth = allowDeathFromStarvation ? 0f : 0.5f;

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

        // exposure
        float temperatureDeltaTime = (float) (currentTick - temperatureTick) / 1000.0f;
        float minimumExposureHealth = allowDeathFromExposure ? 0f : 0.5f;

        if (temperatureDeltaTime > updateTemperatureRate) {
            environmentTemperature = getEnvironmentTemperature(player, world) + getBlockTemperature(player, world, 8);

            float deltaTemperature = (environmentTemperature - bodyTemperature);

            float temperatureRate = deltaTemperature / bodyDefaultTemperature * 0.5f;

            float acclimatizeDelta = (bodyDefaultTemperature - bodyTemperature);

            bodyTemperature += MathUtilities.clamp(acclimatizeDelta, -acclimatizeRate * 2f, acclimatizeRate)
                    + temperatureRate;

            if (bodyTemperature < bodyDefaultTemperature
                    - (bodyDefaultTemperature * bodyTemperatureDangerousThreshold)) {

                player.setHealth(Math.max(player.getHealth() - 1.0f, minimumExposureHealth));

                player.sendMessage(Text.of("You are suffering from hypothermia"), false);

                player.animateDamage();
            } else if (bodyTemperature > bodyDefaultTemperature
                    + (bodyDefaultTemperature * bodyTemperatureDangerousThreshold)) {
                player.setHealth(Math.max(player.getHealth() - 1.0f, minimumExposureHealth));

                player.sendMessage(Text.of("You are suffering from hyperthermia"), false);

                player.animateDamage();
            }

            temperatureTick = currentTick;
        }

    }

    public static float getBiomeTemperature(Biome biome) {
        float biomeTemperature = biome.getTemperature();

        return (biomeTemperature / 4f) * (biomeDayMaxTemperature - biomeDayMinTemperature)
                + biomeTemperature;
    }

    public static float getEnvironmentTemperature(PlayerEntity player, World world) {
        float environmentTemperature = 0f;
        float biomeTemperature = world.getBiome(player.getBlockPos()).getTemperature();

        float biomeDayTemperature = MathUtilities.lerp(biomeTemperature, biomeDayMinTemperature,
                biomeDayMaxTemperature);
        float biomeNightTemperature = MathUtilities.lerp(biomeTemperature, biomeNightMinTemperature,
                biomeNightMaxTemperature);

        float bias = (float) (Math.sin(Math.toRadians(((world.getTimeOfDay() / 24000f) * 360f))) * 0.5f) + 0.5f;

        if (world.isRaining()) {
            // rain will reduce the temperature bias
            bias *= 0.5f;
        }

        environmentTemperature = MathUtilities.lerp(bias, biomeNightTemperature, biomeDayTemperature);

        if (player.isSubmergedInWater()) {
            // water temperature does not fall below 4 degrees celsius
            environmentTemperature = Math.max(environmentTemperature, 4f);
        }

        return environmentTemperature;
    }

    public static float getBlockTemperature(PlayerEntity player, World world, int radius) {
        BlockPos playerBlockPos = player.getBlockPos();
        float blockTemperature = 0f;

        for (BlockPos blockPos : BlockPos.iterateOutwards(playerBlockPos, radius, radius, radius)) {
            double distance = Math.sqrt(blockPos.getSquaredDistance(playerBlockPos));

            if (distance < radius) {
                Block block = world.getBlockState(blockPos).getBlock();

                String translationKey = block.getTranslationKey();

                float bias = Math.max(Math.min(1f / (float) Math.pow(distance, 2), 1f), 0f);

                if (translationKey.contains("campfire")) {
                    blockTemperature += bias * campFireTemperature;
                } else if (translationKey.contains("torch")) {
                    blockTemperature += bias * torchTemperature;
                } else if (translationKey.contains("lantern")) {
                    blockTemperature += bias * lanternTemperature;
                }
            }
        }

        return blockTemperature;
    }
}
