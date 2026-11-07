package mod.cdv.gdb.repair;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Custom Minecraft-style industrial repair GUI, with independent slots and real skin avatar. */
public final class RepairStationScreen extends AbstractContainerScreen<RepairStationMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        "gundb", "textures/gui/container/gun_repair_station.png");
    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 230;
    private static final int TEXTURE_HEIGHT = 256;

    public RepairStationScreen(RepairStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        inventoryLabelX = 38;
        inventoryLabelY = 138;
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, TEXTURE_HEIGHT);
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawCenteredString(font, title, 160, 17, 0xF4F3EB);
        if (Minecraft.getInstance().player != null) {
            PlayerFaceRenderer.draw(g, Minecraft.getInstance().player.getSkin().texture(), 11, 11, 17);
        }
        int gunsmithLevel = menu.gunsmithLevel();
        String skill = Component.translatable("gui.gundb.repair.gunsmith_level").getString() + gunsmithLevel;
        g.drawString(font, skill, 32, 11, 0xFFF0AF, true);
        String progress = gunsmithLevel == RepairStationRules.MAX_LEVEL ? "MAX"
            : (menu.gunsmithExperience()-RepairStationRules.LEVEL_START[gunsmithLevel])
               +"/"+(RepairStationRules.LEVEL_START[gunsmithLevel+1]-RepairStationRules.LEVEL_START[gunsmithLevel]);
        g.drawString(font, progress, 32, 23, 0xE4E8DC, true);
        g.drawCenteredString(font, Component.translatable("gui.gundb.repair.gun"), 75, 46, 0xF1F2EA);
        g.drawCenteredString(font, Component.translatable("gui.gundb.repair.material"), 176, 46, 0xF1F2EA);
        g.drawCenteredString(font, Component.translatable("gui.gundb.repair.result"), 265, 46, 0xF1F2EA);

        ItemStack gun = menu.displayedGun();
        if (gun.isEmpty()) {
            drawInfo(g, Component.translatable("gui.gundb.repair.put_gun").getString(), 29, 100, 94, 0xC4CDD0);
        } else {
            drawInfo(g, gun.getHoverName().getString(), 29, 100, 94, 0xEBF1E9);
        }

        if (!gun.isEmpty() && menu.fullRequiredMaterials() > 0) {
            String material = new ItemStack(RepairStationRegistry.SCREWDRIVER.get()).getHoverName().getString();
            String need = Component.translatable("gui.gundb.repair.required").getString()
                + material + " ×" + menu.fullRequiredMaterials();
            drawInfo(g, need, 136, 100, 96, 0xFFCE7C);
        } else if (!gun.isEmpty() && !gun.isDamaged()) {
            drawInfo(g, Component.translatable("gui.gundb.repair.healthy").getString(), 136, 100, 96, 0xA0E0B5);
        } else if (!gun.isEmpty()) {
            drawInfo(g, Component.translatable("gui.gundb.repair.unsupported").getString(), 136, 100, 96, 0xEDA19C);
        } else {
            drawInfo(g, Component.translatable("gui.gundb.repair.required").getString() + "—", 136, 100, 96, 0xD5D6D0);
        }
        if (menu.materialsUsedInPreview() > 0) {
            drawInfo(g, "Lv " + menu.previewLevelCost(), 243, 100, 41, 0xC8F2B0);
        } else {
            drawInfo(g, "Lv —", 243, 100, 41, 0xC1C6C6);
        }
        g.drawString(font, Component.translatable("container.inventory"), inventoryLabelX, inventoryLabelY, 0x30363C, false);
    }

    private void drawInfo(GuiGraphics g, String input, int x, int y, int maxWidth, int color) {
        if (font.width(input) <= maxWidth) {
            g.drawString(font, input, x, y + 5, color, false);
            return;
        }
        String first = font.plainSubstrByWidth(input, maxWidth);
        if (first.isEmpty()) return;
        String remaining = input.substring(first.length());
        String second = font.plainSubstrByWidth(remaining, maxWidth - font.width("…"));
        if (second.length() < remaining.length()) second += "…";
        g.drawString(font, first, x, y, color, false);
        g.drawString(font, second, x, y + 10, color, false);
    }

    @Override protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);
        int x = mouseX - leftPos, y = mouseY - topPos;
        if (y < 97 || y > 120) return;
        ItemStack gun = menu.displayedGun();
        if (!gun.isEmpty() && x >= 27 && x <= 127) {
            g.renderTooltip(font, gun.getHoverName(), mouseX, mouseY);
        } else if (!gun.isEmpty() && x >= 133 && x <= 234) {
            String full = Component.translatable("gui.gundb.repair.required").getString()
                + new ItemStack(RepairStationRegistry.SCREWDRIVER.get()).getHoverName().getString()
                + " ×" + menu.fullRequiredMaterials();
            g.renderTooltip(font, Component.literal(full), mouseX, mouseY);
        }
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
