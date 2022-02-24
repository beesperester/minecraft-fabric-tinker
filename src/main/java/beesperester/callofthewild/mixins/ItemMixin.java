package beesperester.callofthewild.mixins;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import beesperester.callofthewild.CallOfTheWildMod;
import beesperester.callofthewild.classes.TemperatureProperty;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(at = @At("HEAD"), method = "appendTooltip")
    public void appendTooltipInject(ItemStack itemStack, @Nullable World world, List<Text> tooltip,
            TooltipContext context, CallbackInfo ci) {
        String translationKey = itemStack.getTranslationKey();

        for (TemperatureProperty itemProperties : CallOfTheWildMod.CONFIG.itemProperties) {
            if (translationKey.equals(itemProperties.translationKey)) {
                tooltip.add(
                        Text.of(String.format("Compensates %.2f °C of Temperature Effects", itemProperties.value)));

                break;
            }
        }

    }

}