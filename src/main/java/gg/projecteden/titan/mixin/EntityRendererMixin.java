package gg.projecteden.titan.mixin;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
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
		if (!(entity instanceof ArmorStandEntity armorStand))
			return;

		if (armorStand.getBlockPos().getSquaredDistance(new Vec3i(x, y, z)) <= ALWAYS_RENDER_WITHIN * ALWAYS_RENDER_WITHIN)
			info.setReturnValue(true);
	}

}
