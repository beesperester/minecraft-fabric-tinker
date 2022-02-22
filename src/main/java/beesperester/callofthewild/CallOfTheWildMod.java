package beesperester.callofthewild;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import beesperester.callofthewild.classes.Rectangle;
import beesperester.callofthewild.effects.ExposureEffect;
import beesperester.callofthewild.effects.HungerEffect;

public class CallOfTheWildMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("modid");
    public static Config CONFIG = new Config();

    public ClientPlayerEntity player;
    public HungerEffect hungerEffect;
    public ExposureEffect exposureEffect;

    @Override
    public void onInitialize() {
        hungerEffect = new HungerEffect();
        exposureEffect = new ExposureEffect();

        ClientTickEvents.END_CLIENT_TICK.register((client) -> tick(client));
        HudRenderCallback.EVENT.register(
                (matrixStack, tickDelta) -> hudRenderCallback(matrixStack, tickDelta));
    }

    public void hudRenderCallback(MatrixStack matrixStack, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();

        float x = window.getScaledWidth() * 0.025f; // window.getScaledWidth() * 0.5f;
        float y = window.getScaledWidth() * 0.025f;

        if (CONFIG.allowHungerEffects) {
            Rectangle hungerRectangle = hungerEffect.render(matrixStack, tickDelta, client, x, y);

            y += hungerRectangle.height;
        }

        if (CONFIG.allowExposureEffects) {
            Rectangle exposurRectangle = exposureEffect.render(matrixStack, tickDelta, client, x, y);

            y += exposurRectangle.height;
        }
    }

    public void tick(MinecraftClient client) {
        if (player == null) {
            if (client.player != null) {
                player = client.player;
            } else {
                return;
            }
        }

        if (CONFIG.allowHungerEffects) {
            hungerEffect.tick(player);
        }

        if (CONFIG.allowExposureEffects) {
            exposureEffect.tick(player);
        }
    }
}