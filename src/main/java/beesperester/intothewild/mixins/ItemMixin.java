package beesperester.intothewild.mixins;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import beesperester.intothewild.IntoTheWild;
import beesperester.intothewild.classes.EntityTemperature;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(at = @At("HEAD"), method = "appendTooltip")
    public void appendTooltipInject(ItemStack itemStack, @Nullable World world, List<Text> tooltip,
            TooltipContext context, CallbackInfo ci) {
        String translationKey = itemStack.getTranslationKey();

        for (EntityTemperature entityTemperature : IntoTheWild.CONFIG.entityProperties.stream()
                .filter(EntityTemperature.armorFilter).collect(Collectors.toList())) {
            if (entityTemperature.matches(translationKey)) {
                tooltip.add(
                        Text.of(String.format("Compensates %.2f °C of Temperature Effects", entityTemperature.value)));

                break;
            }
        }

    }

}