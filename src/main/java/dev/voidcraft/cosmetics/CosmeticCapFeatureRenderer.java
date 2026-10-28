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
 * A basic 3D cap attached to the player's head. Uses Minecraft's purple wool
 * texture as a temporary skin while custom pack texture loading is developed.
 */
public final class CosmeticCapFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/block/purple_wool.png");
    private static boolean enabled = true;
    private final ModelPart cap;

    public CosmeticCapFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        root.addChild("cap", ModelPartBuilder.create()
                .uv(0, 0)
                // Crown sits above the player's head.
                .cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 2.0F, 8.0F, new Dilation(0.0F))
                // Wider brim gives the accessory a recognizable cap silhouette.
                .uv(0, 10)
                .cuboid(-5.0F, -8.5F, -5.0F, 10.0F, 1.0F, 10.0F, new Dilation(0.0F)),
                ModelTransform.NONE);
        this.cap = TexturedModelData.of(modelData, 32, 16).createModel().getChild("cap");
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!enabled || player.isInvisible()) {
            return;
        }

        matrices.push();
        getContextModel().head.rotate(matrices);
        cap.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE)),
                light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();
    }
}
