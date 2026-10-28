package dev.voidcraft.cosmetics;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import java.io.InputStream;
import java.nio.file.Files;
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
    private final Identifier texture;

    public CosmeticPackFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, List<CosmeticPackLoader.CosmeticPack> packs) {
        super(context);
        LoadedPack loaded = build(packs);
        this.model = loaded == null ? null : loaded.model();
        this.texture = loaded == null ? FALLBACK : loaded.texture();
    }

    private static LoadedPack build(List<CosmeticPackLoader.CosmeticPack> packs) {
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
                ModelPart model = TexturedModelData.of(data, definition.textureWidth(), definition.textureHeight()).createModel();
                Identifier texture = loadTexture(pack, definition.textureWidth(), definition.textureHeight());
                return new LoadedPack(model, texture);
            } catch (IOException | RuntimeException e) {
                LOG.warning("Could not render pack " + pack.id() + ": " + e.getMessage());
            }
        }
        return null;
    }

    private static Identifier loadTexture(CosmeticPackLoader.CosmeticPack pack, int expectedWidth, int expectedHeight) throws IOException {
        if (Files.size(pack.texturePath()) > 8L * 1024L * 1024L) {
            throw new IOException("texture PNG exceeds 8 MiB limit");
        }
        NativeImage image;
        try (InputStream input = Files.newInputStream(pack.texturePath())) {
            image = NativeImage.read(input);
        }
        if (image.getWidth() < 1 || image.getHeight() < 1 || image.getWidth() > 4096 || image.getHeight() > 4096) {
            image.close();
            throw new IOException("texture dimensions must be between 1 and 4096");
        }
        if (image.getWidth() != expectedWidth || image.getHeight() != expectedHeight) {
            image.close();
            throw new IOException("PNG dimensions must match model texture_size (" + expectedWidth + "x" + expectedHeight + ")");
        }
        NativeImageBackedTexture nativeTexture = new NativeImageBackedTexture(image);
        try {
            return MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(
                    "voidcraft_" + pack.id().replaceAll("[^a-zA-Z0-9_.-]", "_"), nativeTexture);
        } catch (RuntimeException exception) {
            nativeTexture.close();
            throw exception;
        }
    }

    private record LoadedPack(ModelPart model, Identifier texture) {}

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (model == null || player.isInvisible()) return;
        matrices.push();
        getContextModel().head.rotate(matrices);
        model.render(matrices, consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture)), light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }
}
