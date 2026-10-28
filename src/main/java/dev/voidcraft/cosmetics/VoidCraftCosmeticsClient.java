package dev.voidcraft.cosmetics;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Initial client-side prototype. This toggles a status flag only; it does not
 * render cosmetic models yet.
 */
public final class VoidCraftCosmeticsClient implements ClientModInitializer {
    private KeyBinding openCosmeticsKey;
    private boolean previewEnabled = true;

    @Override
    public void onInitializeClient() {
        openCosmeticsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voidcraft_cosmetics.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.voidcraft_cosmetics"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openCosmeticsKey.wasPressed()) {
                previewEnabled = !previewEnabled;
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("VoidCraft Cosmetics preview: "
                                    + (previewEnabled ? "ON" : "OFF")
                                    + " (prototype only)"),
                            true
                    );
                }
            }
        });
    }
}
