package dev.voidcraft.cosmetics;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VoidCraftCosmeticsClient implements ClientModInitializer {
    private static final Logger LOGGER = Logger.getLogger("VoidCraftCosmetics");
    private KeyBinding cycleCosmeticsKey;
    private Path settingsFile;

    @Override
    public void onInitializeClient() {
        Path gameDirectory = FabricLoader.getInstance().getGameDir();
        var discoveredPacks = CosmeticPackLoader.discover(gameDirectory);
        System.out.println("[VoidCraft Cosmetics] Valid cosmetic pack definitions found: " + discoveredPacks.size());

        settingsFile = gameDirectory.resolve("config").resolve("voidcraft-cosmetics").resolve("settings.properties");
        loadSettings();

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, helper, context) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                helper.register(new CosmeticCapFeatureRenderer(playerRenderer));
                helper.register(new CosmeticPackFeatureRenderer(playerRenderer, discoveredPacks));
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
                saveSettings();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("VoidCraft cap style: " + style), true);
                }
            }
        });
    }

    private void loadSettings() {
        Properties properties = new Properties();
        if (Files.isRegularFile(settingsFile)) {
            try (InputStream input = Files.newInputStream(settingsFile)) {
                properties.load(input);
                int index = Integer.parseInt(properties.getProperty("capStyle", "1"));
                CosmeticCapFeatureRenderer.setSelectedStyleIndex(index);
            } catch (IOException | NumberFormatException exception) {
                LOGGER.log(Level.WARNING, "Could not read cosmetics settings; using default style", exception);
            }
        } else {
            CosmeticCapFeatureRenderer.setSelectedStyleIndex(1);
            saveSettings();
        }
    }

    private void saveSettings() {
        try {
            Files.createDirectories(settingsFile.getParent());
            Properties properties = new Properties();
            properties.setProperty("capStyle", Integer.toString(CosmeticCapFeatureRenderer.getSelectedStyleIndex()));
            try (OutputStream output = Files.newOutputStream(settingsFile)) {
                properties.store(output, "VoidCraft Cosmetics settings");
            }
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Could not save cosmetics settings", exception);
        }
    }
}
