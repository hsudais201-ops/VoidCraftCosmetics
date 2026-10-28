package dev.voidcraft.cosmetics;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public final class CosmeticPackFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private static final Logger LOG = Logger.getLogger("VoidCraftCosmetics");
    private static final Identifier FALLBACK = Identifier.of("minecraft", "textures/block/purple_wool.png");
    private final ModelPart model;

    public CosmeticPackFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, List<CosmeticPackLoader.CosmeticPack> packs) {
        super(context);
        this.model = build(packs);
    }

    private static ModelPart build(List<CosmeticPackLoader.CosmeticPack> packs) {
        for (var pack : packs) {
            if (!pack.slot().equals("head")) continue;
            try {
                var definition = CosmeticModelDefinition.read(pack.modelPath());
                ModelData data = new ModelData();
                ModelPartData root = data.getRoot();
                int index = 0;
                for (var part : definition.parts()) {
                    ModelPartBuilder builder = ModelPartBuilder.create();
                    for (var cube : part.cubes()) {
                        float[] p = cube.origin();
                        float[] s = cube.size();
                        int[] uv = cube.uv();
                        builder.uv(uv[0], uv[1]).cuboid(p[0], p[1], p[2], s[0], s[1], s[2], new Dilation(0));
                    }
                    float[] p = part.origin();
                    float[] r = part.rotation();
                    root.addChild("part_" + index++, builder, ModelTransform.of(p[0], p[1], p[2],
                            (float)Math.toRadians(r[0]), (float)Math.toRadians(r[1]), (float)Math.toRadians(r[2])));
                }
                return TexturedModelData.of(data, definition.textureWidth(), definition.textureHeight()).createModel();
            } catch (IOException | RuntimeException e) {
                LOG.warning("Could not render pack " + pack.id() + ": " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (model == null || player.isInvisible()) return;
        matrices.push();
        getContextModel().head.rotate(matrices);
        model.render(matrices, consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(FALLBACK)), light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }
}
