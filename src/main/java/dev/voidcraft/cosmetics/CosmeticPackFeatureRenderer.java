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
    private final List<CosmeticPackLoader.CosmeticPack> packs;
    private LoadedPack loaded;
    private String loadedId = "";
    private static String selectedPackId = "";

    public CosmeticPackFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, List<CosmeticPackLoader.CosmeticPack> packs) {
        super(context);
        this.packs = packs;
        refreshSelectedPack();
    }

    private LoadedPack buildSelected(String selectedId) {
        if (selectedId == null || selectedId.isBlank()) return null;
        for (var pack : packs) {
            if (!pack.slot().equals("head") || !pack.id().equals(selectedId)) continue;
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

    public static String getSelectedPackId() { return selectedPackId; }

    public static void setSelectedPackId(String id) { selectedPackId = id == null ? "" : id; }

    public static String cycleSelectedPack(List<CosmeticPackLoader.CosmeticPack> packs) {
        List<CosmeticPackLoader.CosmeticPack> headPacks = packs.stream().filter(p -> p.slot().equals("head")).toList();
        if (headPacks.isEmpty()) { selectedPackId = ""; return "No valid head cosmetic packs found"; }
        int current = -1;
        for (int i = 0; i < headPacks.size(); i++) if (headPacks.get(i).id().equals(selectedPackId)) current = i;
        if (current + 1 >= headPacks.size()) { selectedPackId = ""; return "Off"; }
        var next = headPacks.get(current + 1);
        selectedPackId = next.id();
        return next.name();
    }

    private void refreshSelectedPack() {
        if (loadedId.equals(selectedPackId)) return;
        loadedId = selectedPackId;
        loaded = buildSelected(loadedId);
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
        refreshSelectedPack();
        if (loaded == null || player.isInvisible()) return;
        matrices.push();
        getContextModel().head.rotate(matrices);
        loaded.model().render(matrices, consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(loaded.texture())), light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }
}
