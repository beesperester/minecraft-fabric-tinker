package beesperester.intothewild.effects;

import beesperester.intothewild.classes.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

public interface IEffect {
    public void tick(PlayerEntity player);

    public Rectangle render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, float x, float y);
}
