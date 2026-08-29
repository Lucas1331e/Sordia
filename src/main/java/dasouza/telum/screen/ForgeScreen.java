package dasouza.telum.screen;

import dasouza.telum.tool.PartType;
import dasouza.telum.tool.ToolType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.Map;

public class ForgeScreen extends AbstractContainerScreen<ForgeScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("telum", "textures/gui/forge.png");

    private static final String[] TOOL_ICONS = {"PIC", "ESP", "HAC", "PAL", "AZA"};

    public ForgeScreen(ForgeScreenHandler handler, Inventory playerInv, Component title) {
        super(handler, playerInv, title);
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        // Check clicks on the 6 Tool Selector Buttons on the left margin
        for (int i = 0; i < 6; i++) {
            int btnX = left - 24;
            int btnY = top + 2 + i * 23;

            if (event.x() >= btnX && event.x() <= btnX + 20 &&
                event.y() >= btnY && event.y() <= btnY + 20) {

                if (this.minecraft != null && this.minecraft.player != null && this.minecraft.gameMode != null) {
                    this.menu.clickMenuButton(this.minecraft.player, i);
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, i);
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Main GUI background
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);

        // Render 6 Tool Type Selector Buttons on the left margin
        int currentToolIndex = this.menu.getSelectedToolIndex();

        for (int i = 0; i < 6; i++) {
            int btnX = x - 24;
            int btnY = y + 2 + i * 23;
            boolean isSelected = (i == currentToolIndex);

            int bgFill = isSelected ? 0xFF555560 : 0xFF333338;
            int border = isSelected ? 0xFFFFD740 : 0xFF666666;

            gfx.fill(btnX, btnY, btnX + 20, btnY + 20, bgFill);
            gfx.outline(btnX, btnY, 20, 20, border);

            // Tool Item Logo Icon
            net.minecraft.world.item.Item toolItem = switch (i) {
                case 0 -> net.minecraft.world.item.Items.IRON_PICKAXE;
                case 1 -> net.minecraft.world.item.Items.IRON_SWORD;
                case 2 -> net.minecraft.world.item.Items.IRON_AXE;
                case 3 -> net.minecraft.world.item.Items.IRON_SHOVEL;
                case 4 -> net.minecraft.world.item.Items.IRON_HOE;
                case 5 -> net.minecraft.world.item.Items.TRIDENT;
                default -> net.minecraft.world.item.Items.IRON_PICKAXE;
            };
            gfx.item(new net.minecraft.world.item.ItemStack(toolItem), btnX + 2, btnY + 2);
        }

        // Render dark part silhouettes in empty grid slots for the active tool pattern
        ToolType selectedTool = this.menu.getSelectedToolType();
        Map<Integer, Identifier> silhouettes = getSilhouettesForTool(selectedTool);

        if (silhouettes != null) {
            for (Map.Entry<Integer, Identifier> entry : silhouettes.entrySet()) {
                int slotIdx = entry.getKey();
                Identifier silhouetteTex = entry.getValue();

                int slotRow = slotIdx / 3;
                int slotCol = slotIdx % 3;
                int slotX = x + 30 + slotCol * 18;
                int slotY = y + 17 + slotRow * 18;

                // Only draw silhouette if the slot is currently empty
                if (this.menu.getSlot(slotIdx).getItem().isEmpty()) {
                    gfx.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0x40000000);
                    gfx.blit(RenderPipelines.GUI_TEXTURED, silhouetteTex, slotX + 1, slotY + 1, 0.0f, 0.0f, 16, 16, 16, 16, 0x70222222);
                }
            }
        }

        super.extractContents(gfx, mouseX, mouseY, partialTick);
    }

    private static Map<Integer, Identifier> getSilhouettesForTool(ToolType tool) {
        return switch (tool) {
            case PICKAXE -> Map.of(
                    0, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wooden_pickaxe_head_left.png"),
                    1, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wooden_eye.png"),
                    2, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wooden_pickaxe_head_right.png"),
                    4, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wood_stick.png")
            );
            case SWORD -> Map.of(
                    1, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wooden_blade.png"),
                    4, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wooden_sword_handle.png"),
                    7, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wood_stick.png")
            );
            case AXE -> Map.of(
                    1, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wooden_axe_head.png"),
                    4, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wood_handle.png"),
                    7, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wood_stick.png")
            );
            case SHOVEL -> Map.of(
                    1, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wooden_shovel_head.png"),
                    4, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wood_handle.png"),
                    7, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wood_stick.png")
            );
            case HOE -> Map.of(
                    0, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wooden_hoe_head.png"),
                    1, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wooden_eye.png"),
                    4, Identifier.fromNamespaceAndPath("telum", "textures/item/part/wood_stick.png")
            );
            case TRIDENT -> Map.of(
                    1, Identifier.fromNamespaceAndPath("telum", "textures/item/part/prismarine_head_trident.png"),
                    4, Identifier.fromNamespaceAndPath("telum", "textures/item/part/prismarine_eye_trident.png"),
                    7, Identifier.fromNamespaceAndPath("telum", "textures/item/part/prismarine_stick_trident.png")
            );
        };
    }
}
