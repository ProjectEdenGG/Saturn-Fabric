package gg.projecteden.titan.mixin;

import gg.projecteden.titan.config.ConfigItem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static gg.projecteden.titan.utils.Utils.getStoredItems;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract boolean hasNbt();

    @Shadow public abstract ItemStack copy();

    @Unique
    private static final Text HOVER = Text.literal("Hold ").formatted(Formatting.DARK_AQUA)
            .append(Text.literal("Shift").formatted(Formatting.YELLOW))
            .append(" to view contents").formatted(Formatting.DARK_AQUA);

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/item/ItemStack;getTooltip"
            + "(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;")
    private void addBackpackPreviewLore(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> ci) {
        if (!ConfigItem.DO_BACKPACK_PREVIEWS.getValue())
            return;

        if (!ConfigItem.PREVIEWS_REQUIRE_SHIFT.getValue() || Screen.hasShiftDown())
            return;

        if (!this.hasNbt())
            return;

        if (getStoredItems(this.copy()).isEmpty())
            return;

        var tooltip = ci.getReturnValue();
        tooltip.add(Text.empty());
        tooltip.add(HOVER);
    }
}
