package dev.voidcraft.cosmetics;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class CosmeticSelectionScreen extends Screen {
    private final Screen parent;
    private final List<CosmeticPackLoader.CosmeticPack> packs;
    private final Runnable saveSettings;
    private int packIndex;

    public CosmeticSelectionScreen(Screen parent, List<CosmeticPackLoader.CosmeticPack> packs, Runnable saveSettings) {
        super(Text.literal("VoidCraft Cosmetics"));
        this.parent = parent;
        this.packs = List.copyOf(packs);
        this.saveSettings = saveSettings;
        String selected = CosmeticPackFeatureRenderer.getSelectedPackId();
        this.packIndex = -1;
        for (int i = 0; i < this.packs.size(); i++) {
            if (this.packs.get(i).id().equals(selected)) {
                this.packIndex = i;
                break;
            }
        }
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int y = Math.max(44, this.height / 2 - 65);
        addDrawableChild(ButtonWidget.builder(Text.literal("Previous cosmetic"), button -> {
            if (!packs.isEmpty()) {
                packIndex = packIndex <= 0 ? packs.size() - 1 : packIndex - 1;
                refreshPackLabel();
            }
        }).dimensions(center - 105, y, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Next cosmetic"), button -> {
            if (!packs.isEmpty()) {
                packIndex = packIndex >= packs.size() - 1 ? 0 : packIndex + 1;
                refreshPackLabel();
            }
        }).dimensions(center + 5, y, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Equip selected"), button -> {
            if (packIndex >= 0 && packIndex < packs.size()) {
                CosmeticPackFeatureRenderer.setSelectedPackId(packs.get(packIndex).id());
                saveSettings.run();
                if (client != null && client.player != null) {
                    client.player.sendMessage(Text.literal("Equipped cosmetic: " + packs.get(packIndex).name()), true);
                }
            }
        }).dimensions(center - 105, y + 28, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Turn custom cosmetic off"), button -> {
            CosmeticPackFeatureRenderer.setSelectedPackId("");
            packIndex = -1;
            saveSettings.run();
            refreshPackLabel();
        }).dimensions(center + 5, y + 28, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cycle built-in cap: " + CosmeticCapFeatureRenderer.getStyleName()), button -> {
            CosmeticCapFeatureRenderer.cycleStyle();
            saveSettings.run();
            clearAndInit();
        }).dimensions(center - 105, y + 56, 210, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(center - 50, y + 84, 100, 20).build());
    }

    private void refreshPackLabel() {
        if (client != null && client.player != null) {
            String name = packIndex < 0 || packs.isEmpty() ? "Off" : packs.get(packIndex).name();
            client.player.sendMessage(Text.literal("Selected preview: " + name), true);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        String selected = packIndex < 0 || packs.isEmpty() ? "Off" : packs.get(packIndex).name();
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Custom cosmetic: " + selected), width / 2, height / 2 - 88, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Found packs: " + packs.size() + " | P cycles packs, O cycles cap"), width / 2, height / 2 + 66, 0xAAAAAA);
        if (packs.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Add pack JSON, model JSON and PNG under config/voidcraft-cosmetics"), width / 2, height / 2 + 80, 0xFFCC66);
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
