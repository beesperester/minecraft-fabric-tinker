package beesperester.intothewild;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import beesperester.intothewild.classes.Rectangle;
import beesperester.intothewild.effects.ExposureEffect;
import beesperester.intothewild.effects.HungerEffect;

public class IntoTheWildClient implements ClientModInitializer {
    public ClientPlayerEntity player;
    public HungerEffect hungerEffect;
    public ExposureEffect exposureEffect;

    @Override
    public void onInitializeClient() {
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

        if (IntoTheWild.CONFIG.allowHungerEffects) {
            Rectangle hungerRectangle = hungerEffect.render(matrixStack, tickDelta, client, x, y);

            y += hungerRectangle.height;
        }

        if (IntoTheWild.CONFIG.allowExposureEffects) {
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

        if (IntoTheWild.CONFIG.allowHungerEffects) {
            hungerEffect.tick(player);
        }

        if (IntoTheWild.CONFIG.allowExposureEffects) {
            exposureEffect.tick(player);
        }
    }
}
