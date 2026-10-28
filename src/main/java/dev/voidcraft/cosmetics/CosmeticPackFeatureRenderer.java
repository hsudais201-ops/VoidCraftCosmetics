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
import net.minecraft.util.math.RotationAxis;
import java.io.IOException;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class CosmeticPackFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private static final Logger LOG = Logger.getLogger("VoidCraftCosmetics");
    private static final Identifier FALLBACK = Identifier.of("minecraft", "textures/block/purple_wool.png");
    private final List<CosmeticPackLoader.CosmeticPack> packs;
    private final java.util.Map<String, LoadedPack> loadedPacks = new java.util.LinkedHashMap<>();
    private String loadedIds = "";
    private static final Set<String> selectedPackIds = new LinkedHashSet<>();

    public CosmeticPackFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, List<CosmeticPackLoader.CosmeticPack> packs) {
        super(context);
        this.packs = packs;
        refreshSelectedPacks();
        if (loadedPacks.isEmpty() || player.isInvisible()) return;
        for (LoadedPack loaded : loadedPacks.values()) {
            switch (loaded.slot()) {
                case "head" -> renderOnPart(matrices, consumers, light, loaded, getContextModel().head, animationProgress);
                case "body", "back" -> renderOnPart(matrices, consumers, light, loaded, getContextModel().body, animationProgress);
                case "legs" -> {
                    renderOnPart(matrices, consumers, light, loaded, getContextModel().leftLeg, animationProgress);
                    renderOnPart(matrices, consumers, light, loaded, getContextModel().rightLeg, animationProgress);
                }
                case "feet" -> {
                    renderOnPart(matrices, consumers, light, loaded, getContextModel().leftLeg, animationProgress);
                    renderOnPart(matrices, consumers, light, loaded, getContextModel().rightLeg, animationProgress);
                }
                default -> { }
            }
        }
    }
}
