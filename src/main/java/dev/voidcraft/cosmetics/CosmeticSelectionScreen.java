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
    private final Runnable reloadPacks;
    private int packIndex;

    public CosmeticSelectionScreen(Screen parent, List<CosmeticPackLoader.CosmeticPack> packs, Runnable saveSettings, Runnable reloadPacks) {
        super(Text.literal("VoidCraft Cosmetics"));
        this.parent = parent;
        this.packs = packs;
        this.saveSettings = saveSettings;
        this.reloadPacks = reloadPacks;
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
                CosmeticPackLoader.CosmeticPack pack = packs.get(packIndex);
                boolean wasEquipped = CosmeticPackFeatureRenderer.getSelectedPackIds().contains(pack.id());
                CosmeticPackFeatureRenderer.equipPackId(pack.id());
                saveSettings.run();
                if (client != null && client.player != null) {
                    client.player.sendMessage(Text.literal((wasEquipped ? "Already equipped: " : "Equipped cosmetic: ") + pack.name()), true);
                }
            } else if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("No cosmetic selected. Install a pack and reload."), true);
            }
        }).dimensions(center - 105, y + 28, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Turn custom cosmetic off"), button -> {
            CosmeticPackFeatureRenderer.clearSelectedPacks();
            packIndex = -1;
            saveSettings.run();
            refreshPackLabel();
        }).dimensions(center + 5, y + 28, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cap: " + CosmeticCapFeatureRenderer.getStyleName()), button -> {
            CosmeticCapFeatureRenderer.cycleStyle();
            saveSettings.run();
            clearAndInit();
        }).dimensions(center - 105, y + 56, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Unequip selected"), button -> {
            if (packIndex >= 0 && packIndex < packs.size()) {
                CosmeticPackLoader.CosmeticPack pack = packs.get(packIndex);
                boolean wasEquipped = CosmeticPackFeatureRenderer.getSelectedPackIds().contains(pack.id());
                CosmeticPackFeatureRenderer.unequipPackId(pack.id());
                saveSettings.run();
                if (client != null && client.player != null) {
                    client.player.sendMessage(Text.literal((wasEquipped ? "Unequipped cosmetic: " : "Was not equipped: ") + pack.name()), true);
                }
            } else if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("No cosmetic selected."), true);
            }
        }).dimensions(center + 5, y + 56, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reload packs"), button -> {
            reloadPacks.run();
            packIndex = packs.isEmpty() ? -1 : Math.min(Math.max(packIndex, 0), packs.size() - 1);
            clearAndInit();
        }).dimensions(center - 105, y + 84, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(center + 5, y + 84, 100, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!packs.isEmpty() && keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT) {
            packIndex = packIndex <= 0 ? packs.size() - 1 : packIndex - 1;
            refreshPackLabel();
            return true;
        }
        if (!packs.isEmpty() && keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT) {
            packIndex = packIndex >= packs.size() - 1 ? 0 : packIndex + 1;
            refreshPackLabel();
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void refreshPackLabel() {
        if (client != null && client.player != null) {
            String name = packIndex < 0 || packs.isEmpty() ? "Off" : packs.get(packIndex).name();
            client.player.sendMessage(Text.literal("Highlighted cosmetic: " + name), true);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        String selected = packIndex < 0 || packs.isEmpty() ? "Off" : packs.get(packIndex).name();
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Highlighted cosmetic: " + selected), width / 2, height / 2 - 100, 0xFFFFFF);
        if (packIndex >= 0 && packIndex < packs.size()) {
            CosmeticPackLoader.CosmeticPack pack = packs.get(packIndex);
            boolean equipped = CosmeticPackFeatureRenderer.getSelectedPackIds().contains(pack.id());
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(equipped ? "Status: Equipped" : "Status: Not equipped"), width / 2, height / 2 - 86, equipped ? 0xAAFFAA : 0xFFCC66);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Slot: " + pack.slot() + " | Animation: " + (pack.animation().isBlank() ? "none" : pack.animation())), width / 2, height / 2 - 72, 0xCCCCFF);
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Choose an installed cosmetic below"), width / 2, height / 2 - 86, 0xAAAAAA);
        }
        List<String> equippedNames = packs.stream()
                .filter(pack -> CosmeticPackFeatureRenderer.getSelectedPackIds().contains(pack.id()))
                .map(CosmeticPackLoader.CosmeticPack::name)
                .toList();
        int maxTextWidth = Math.max(80, width - 24);
        List<String> equippedLines = new java.util.ArrayList<>();
        String currentLine = "Equipped: ";
        if (equippedNames.isEmpty()) {
            equippedLines.add("Equipped: none");
        } else {
            for (String name : equippedNames) {
                String item = (currentLine.equals("Equipped: ") ? "" : ", ") + name;
                if (textRenderer.getWidth(currentLine + item) > maxTextWidth && !currentLine.equals("Equipped: ")) {
                    equippedLines.add(currentLine);
                    currentLine = "  " + name;
                } else {
                    currentLine += item;
                }
                if (equippedLines.size() == 2 && textRenderer.getWidth(currentLine) > maxTextWidth) {
                    currentLine = textRenderer.trimToWidth(currentLine, maxTextWidth - textRenderer.getWidth("…")) + "…";
                    break;
                }
            }
            if (!currentLine.isBlank() && equippedLines.size() < 3) equippedLines.add(currentLine);
        }
        int equippedY = height / 2 + 48;
        for (int i = 0; i < equippedLines.size(); i++) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(equippedLines.get(i)), width / 2, equippedY + i * 11, 0xAAFFAA);
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Installed packs: " + packs.size()), width / 2, equippedY + equippedLines.size() * 11, 0xAAAAAA);
        if (packs.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Add pack JSON, model JSON and PNG under config/voidcraft-cosmetics"), width / 2, height / 2 + 80, 0xFFCC66);
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
