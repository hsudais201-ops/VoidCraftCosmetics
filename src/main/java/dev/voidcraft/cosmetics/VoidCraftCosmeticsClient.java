package dev.voidcraft.cosmetics;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class VoidCraftCosmeticsClient implements ClientModInitializer {
    private KeyBinding cycleCosmeticsKey;

    @Override
    public void onInitializeClient() {
        var discoveredPacks = CosmeticPackLoader.discover(
                net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir());
        System.out.println("[VoidCraft Cosmetics] Valid cosmetic pack definitions found: " + discoveredPacks.size());

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, helper, context) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                helper.register(new CosmeticCapFeatureRenderer(playerRenderer));
            }
        });

        cycleCosmeticsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voidcraft_cosmetics.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.voidcraft_cosmetics"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (cycleCosmeticsKey.wasPressed()) {
                String style = CosmeticCapFeatureRenderer.cycleStyle();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("VoidCraft cap style: " + style), true);
                }
            }
        });
    }
}
