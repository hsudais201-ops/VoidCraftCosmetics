package dev.voidcraft.cosmetics;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Built-in 3D cap preview. Press O to cycle through the available styles.
 * Custom pack rendering is not implemented yet.
 */
public final class CosmeticCapFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private static final Identifier[] TEXTURES = {
            Identifier.of("minecraft", "textures/block/purple_wool.png"),
            Identifier.of("minecraft", "textures/block/red_wool.png"),
            Identifier.of("minecraft", "textures/block/blue_wool.png"),
            Identifier.of("minecraft", "textures/block/black_wool.png")
    };
    private static final String[] STYLE_NAMES = {"Purple", "Red", "Blue", "Black"};
    private static int selectedStyle = 0;
    private final ModelPart cap;

    public CosmeticCapFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        root.addChild("cap", ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 10)
                .cuboid(-5.0F, -8.5F, -5.0F, 10.0F, 1.0F, 10.0F, new Dilation(0.0F)),
                ModelTransform.NONE);
        this.cap = TexturedModelData.of(modelData, 32, 16).createModel().getChild("cap");
    }

    /** Cycles through four built-in texture styles and then turns the cap off. */
    public static String cycleStyle() {
        selectedStyle = (selectedStyle + 1) % (TEXTURES.length + 1);
        return getStyleName();
    }

    public static String getStyleName() {
        return selectedStyle == 0 ? "Off" : STYLE_NAMES[selectedStyle - 1];
    }

    public static int getSelectedStyleIndex() {
        return selectedStyle;
    }

    public static void setSelectedStyleIndex(int index) {
        selectedStyle = Math.max(0, Math.min(index, TEXTURES.length));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (selectedStyle == 0 || player.isInvisible()) {
            return;
        }
        matrices.push();
        getContextModel().head.rotate(matrices);
        cap.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURES[selectedStyle - 1])),
                light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();
    }
}
