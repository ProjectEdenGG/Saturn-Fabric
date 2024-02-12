package gg.projecteden.titan.mixin;

import gg.projecteden.titan.config.ConfigItem;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	private static final int ALWAYS_RENDER_WITHIN = 30;

	@Inject(at = @At("HEAD"), method = "shouldRender", cancellable = true)
	private void shouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
		if (!ConfigItem.STOP_ENTITY_CULLING.getValue())
			return;

		if (!(entity instanceof ArmorStandEntity || entity instanceof ItemFrameEntity))
			return;

		final Vec3i location = new Vec3i((int) x, (int) y, (int) z);
		if (entity.getBlockPos().isWithinDistance(location, ALWAYS_RENDER_WITHIN))
			info.setReturnValue(true);
	}

}
