package gg.projecteden.titan.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class InventoryOverlay {
    public static final Identifier TEXTURE_54 = new Identifier("textures/gui/container/generic_54.png");

    public static final InventoryProperties INV_PROPS_TEMP = new InventoryProperties();


    public static void renderInventoryBackground(InventoryRenderType type, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.applyModelViewMatrix();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        int rows = switch (type) {
            case FIXED_27 -> 0;
            case FIXED_36 -> 1;
            case FIXED_45 -> 2;
            case FIXED_54 -> 3;
        };

        int h1 = 61 + (rows * 18);
        int h2 = 54 + (rows * 18);

        renderInventoryBackground(x, y, h1, h2, buffer);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        tessellator.draw();
    }

    public static void drawTexturedRectBatched(int x, int y, int u, int v, int width, int height, BufferBuilder buffer) {
        drawTexturedRectBatched(x, y, u, v, width, height, 0, buffer);
    }

    public static void drawTexturedRectBatched(int x, int y, int u, int v, int width, int height, float zLevel, BufferBuilder buffer) {
        float pixelWidth = 0.00390625F;

        buffer.vertex(x        , y + height, zLevel).texture( u          * pixelWidth, (v + height) * pixelWidth).next();
        buffer.vertex(x + width, y + height, zLevel).texture((u + width) * pixelWidth, (v + height) * pixelWidth).next();
        buffer.vertex(x + width, y         , zLevel).texture((u + width) * pixelWidth,  v           * pixelWidth).next();
        buffer.vertex(x        , y         , zLevel).texture( u          * pixelWidth,  v           * pixelWidth).next();
    }

    public static void renderInventoryBackground(int x, int y, int h1, int h2, BufferBuilder buffer) {
        RenderSystem.setShaderTexture(0, TEXTURE_54);

        drawTexturedRectBatched(x      , y     ,   0,   0,   7,  h1, buffer); // left (top)
        drawTexturedRectBatched(x +   7, y     ,   7,   0, 169,   7, buffer); // top (right)
        drawTexturedRectBatched(x + 169, y +  7, 169, 107,   7,  h1 - 1, buffer); // right (bottom)
        drawTexturedRectBatched(x      , y + h1,   0, 215, 180,   7, buffer); // bottom (left)
        drawTexturedRectBatched(x +   7, y +  7,   7,  17, 162,  h2, buffer); // middle
    }

    /**
     * Returns the instance of the shared/temporary properties instance,
     * with the values set for the type of inventory provided.
     * Don't hold on to the instance, as the values will mutate when this
     * method is called again!
     * @param type
     * @param totalSlots
     * @return
     */
    public static InventoryProperties getInventoryPropsTemp(InventoryRenderType type, int totalSlots) {
        totalSlots = Integer.parseInt(type.name().replace("FIXED_", ""));

        INV_PROPS_TEMP.slotsPerRow = 9;
        INV_PROPS_TEMP.slotOffsetX = 8;
        INV_PROPS_TEMP.slotOffsetY = 8;
        int rows = (int) (Math.ceil((double) totalSlots / (double) INV_PROPS_TEMP.slotsPerRow));
        INV_PROPS_TEMP.width = Math.min(INV_PROPS_TEMP.slotsPerRow, totalSlots) * 18 + 14;
        INV_PROPS_TEMP.height = rows * 18 + 14;

        return INV_PROPS_TEMP;
    }

    public static void renderInventoryStacks(InventoryRenderType type, Inventory inv, int startX, int startY, int slotsPerRow, int startSlot, int maxSlots, MinecraftClient mc, DrawContext drawContext) {
        final int slots = inv.size();
        int x = startX;
        int y = startY;

        if (maxSlots < 0) {
            maxSlots = slots;
        }

        for (int slot = startSlot, i = 0; slot < slots && i < maxSlots;) {
            for (int column = 0; column < slotsPerRow && slot < slots && i < maxSlots; ++column, ++slot, ++i) {
                ItemStack stack = inv.getStack(slot);

                if (stack.isEmpty() == false) {
                    renderStackAt(stack, x, y, 1, mc, drawContext);
                }

                x += 18;
            }

            x = startX;
            y += 18;
        }
    }

    public static void renderStackAt(ItemStack stack, float x, float y, float scale, MinecraftClient mc, DrawContext drawContext) {
        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        matrixStack.translate(x, y, 0.f);
        matrixStack.scale(scale, scale, 1);

        DiffuseLighting.enableGuiDepthLighting();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        drawContext.drawItem(stack, 0, 0);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        drawContext.drawItemInSlot( mc.textRenderer, stack, 0, 0);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        matrixStack.pop();
    }

    public static class InventoryProperties {
        public int width = 176;
        public int height = 83;
        public int slotsPerRow = 9;
        public int slotOffsetX = 8;
        public int slotOffsetY = 8;
    }

    public enum InventoryRenderType {
        FIXED_27,
        FIXED_36,
        FIXED_45,
        FIXED_54;
    }
}