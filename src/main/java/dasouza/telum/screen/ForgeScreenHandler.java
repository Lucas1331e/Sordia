package dasouza.telum.screen;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.component.TelumComponents;
import dasouza.telum.component.ToolPartData;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.item.TelumItems;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.tool.PartType;
import dasouza.telum.tool.ToolStatsCalculator;
import dasouza.telum.tool.ToolType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 3x3 Grid Forge Screen Handler for Telum.
 * Calculates exact CustomModelData index matching the 256 layered combination models,
 * ensuring assembled tools render with the exact sprite textures and designs of their original parts.
 */
public class ForgeScreenHandler extends AbstractContainerMenu {

    private final SimpleContainer inputSlots = new SimpleContainer(9) {
        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    };

    private final ResultContainer resultSlot = new ResultContainer();
    private final ContainerLevelAccess access;

    // Selected Tool Type (0: Pickaxe, 1: Sword, 2: Axe, 3: Shovel, 4: Hoe)
    private int selectedToolIndex = 0;

    public ForgeScreenHandler(int syncId, Inventory playerInv) {
        this(syncId, playerInv, ContainerLevelAccess.NULL);
    }

    public ForgeScreenHandler(int syncId, Inventory playerInv, ContainerLevelAccess access) {
        super(TelumScreenHandlers.FORGE_SCREEN_HANDLER, syncId);
        this.access = access;

        // 3x3 Input Grid (Slots 0 to 8)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                final int slotIdx = col + row * 3;
                this.addSlot(new Slot(inputSlots, slotIdx, 30 + col * 18, 17 + row * 18) {
                    @Override
                    public boolean isActive() {
                        ToolType targetTool = getSelectedToolType();
                        Map<Integer, PartType> pattern = getPatternForTool(targetTool);
                        return pattern != null && pattern.containsKey(slotIdx);
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        if (!isActive()) return false;
                        if (stack.is(TelumItems.LYRE_PART_LEFT) || stack.is(TelumItems.LYRE_PART_RIGHT)) {
                            return true;
                        }
                        ToolType targetTool = getSelectedToolType();
                        Map<Integer, PartType> pattern = getPatternForTool(targetTool);
                        if (pattern == null || !pattern.containsKey(slotIdx)) {
                            return false;
                        }
                        ToolPartData data = stack.get(TelumComponents.TOOL_PART);
                        return isValidPartForForgeSlot(targetTool, slotIdx, pattern.get(slotIdx), data);
                    }
                });
            }
        }

        // Result Slot (Slot 9)
        this.addSlot(new Slot(resultSlot, 0, 140, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                for (int i = 0; i < 9; i++) {
                    ItemStack inputStack = inputSlots.getItem(i);
                    if (!inputStack.isEmpty()) {
                        inputStack.shrink(1);
                    }
                }
                inputSlots.setChanged();
            }
        });

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public int getSelectedToolIndex() {
        return selectedToolIndex;
    }

    public ToolType getSelectedToolType() {
        return ToolType.values()[selectedToolIndex % ToolType.values().length];
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < ToolType.values().length) {
            ToolType newTool = ToolType.values()[id];
            Map<Integer, PartType> newPattern = getPatternForTool(newTool);

            for (int i = 0; i < 9; i++) {
                if (newPattern == null || !newPattern.containsKey(i)) {
                    ItemStack stack = inputSlots.getItem(i);
                    if (!stack.isEmpty()) {
                        if (player != null && !player.level().isClientSide()) {
                            player.getInventory().placeItemBackInInventory(stack.copy());
                        }
                        inputSlots.setItem(i, ItemStack.EMPTY);
                    }
                }
            }

            this.selectedToolIndex = id;
            updateResult();
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        updateResult();
    }

    private void updateResult() {
        boolean hasLeft = false;
        boolean hasRight = false;
        int totalItems = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack slotStack = inputSlots.getItem(i);
            if (!slotStack.isEmpty()) {
                totalItems++;
                if (slotStack.is(TelumItems.LYRE_PART_LEFT)) {
                    hasLeft = true;
                } else if (slotStack.is(TelumItems.LYRE_PART_RIGHT)) {
                    hasRight = true;
                }
            }
        }

        if (totalItems == 2 && hasLeft && hasRight) {
            resultSlot.setItem(0, new ItemStack(TelumItems.LYRE));
            return;
        }

        ToolType targetTool = getSelectedToolType();
        Map<Integer, PartType> pattern = getPatternForTool(targetTool);

        if (pattern == null) {
            resultSlot.setItem(0, ItemStack.EMPTY);
            return;
        }

        List<ToolPartData> parts = new ArrayList<>();
        Map<Integer, ToolPartData> slotParts = new java.util.HashMap<>();

        for (int i = 0; i < 9; i++) {
            ItemStack slotStack = inputSlots.getItem(i);

            if (pattern.containsKey(i)) {
                PartType requiredPart = pattern.get(i);
                if (slotStack.isEmpty()) {
                    resultSlot.setItem(0, ItemStack.EMPTY);
                    return;
                }
                ToolPartData data = slotStack.get(TelumComponents.TOOL_PART);
                if (!isValidPartForForgeSlot(targetTool, i, requiredPart, data)) {
                    resultSlot.setItem(0, ItemStack.EMPTY);
                    return;
                }
                parts.add(data);
                slotParts.put(i, data);
            } else {
                if (!slotStack.isEmpty()) {
                    resultSlot.setItem(0, ItemStack.EMPTY);
                    return;
                }
            }
        }


        AssembledToolData toolData = ToolStatsCalculator.calculate(targetTool, parts);

        ItemStack result = new ItemStack(TelumItems.ASSEMBLED_TOOL);
        result.set(TelumComponents.ASSEMBLED_TOOL, toolData);
        result.set(DataComponents.ATTRIBUTE_MODIFIERS,
                AssembledToolItem.createAttributes(toolData.attackDamage(), toolData.attackSpeed(), toolData));
        result.set(DataComponents.MAX_DAMAGE, toolData.durability());
        result.set(DataComponents.DAMAGE, 0);

        List<String> partNames = calculatePartModelNames(targetTool, slotParts);

        CustomModelData customModelData = new CustomModelData(
                List.of(),
                List.of(),
                partNames,
                List.of()
        );
        result.set(DataComponents.CUSTOM_MODEL_DATA, customModelData);

        resultSlot.setItem(0, result);
    }

    private List<String> calculatePartModelNames(ToolType tool, Map<Integer, ToolPartData> slotParts) {
        String l0 = "empty";
        String l1 = "empty";
        String l2 = "empty";
        String l3 = "empty";

        switch (tool) {
            case PICKAXE -> {
                ToolPartData leftHead = slotParts.get(0);
                ToolPartData eye = slotParts.get(1);
                ToolPartData rightHead = slotParts.get(2);
                ToolPartData handle = slotParts.get(4);
                if (handle != null) l0 = getHandleTexture(handle.material(), ToolType.PICKAXE);
                if (leftHead != null) l1 = getPickaxeLeftHeadTexture(leftHead.material());
                if (rightHead != null) l2 = getPickaxeRightHeadTexture(rightHead.material());
                if (eye != null) l3 = getEyeTexture(eye.material());
            }
            case SWORD -> {
                ToolPartData blade = slotParts.get(1);
                ToolPartData grip = slotParts.get(4);
                ToolPartData handle = slotParts.get(7);
                if (handle != null) l0 = getHandleTexture(handle.material(), ToolType.SWORD);
                if (grip != null) l1 = getSwordGripTexture(grip.material());
                if (blade != null) l2 = getBladeTexture(blade.material());
            }
            case AXE -> {
                ToolPartData head = slotParts.get(1);
                ToolPartData grip = slotParts.get(4);
                ToolPartData handle = slotParts.get(7);
                if (handle != null) l0 = getHandleTexture(handle.material(), ToolType.AXE);
                if (grip != null) l1 = getToolGripTexture(grip.material());
                if (head != null) l2 = getAxeHeadTexture(head.material());
            }
            case SHOVEL -> {
                ToolPartData head = slotParts.get(1);
                ToolPartData grip = slotParts.get(4);
                ToolPartData handle = slotParts.get(7);
                if (handle != null) l0 = getHandleTexture(handle.material(), ToolType.SHOVEL);
                if (grip != null) l1 = getToolGripTexture(grip.material());
                if (head != null) l2 = getShovelHeadTexture(head.material());
            }
            case HOE -> {
                ToolPartData head = slotParts.get(0);
                ToolPartData eye = slotParts.get(1);
                ToolPartData handle = slotParts.get(4);
                if (handle != null) l0 = getHandleTexture(handle.material(), ToolType.HOE);
                if (head != null) l1 = getHoeHeadTexture(head.material());
                if (eye != null) l2 = getEyeTexture(eye.material());
            }
            case TRIDENT -> {
                ToolPartData head = slotParts.get(1);
                ToolPartData eye = slotParts.get(4);
                ToolPartData handle = slotParts.get(7);
                if (handle != null) l0 = getHandleTexture(handle.material(), ToolType.TRIDENT);
                if (head != null) l1 = getTridentHeadTexture(head.material());
                if (eye != null) l2 = getTridentEyeTexture(eye.material());
            }
        }
        return List.of(l0, l1, l2, l3);
    }

    private String getHandleTexture(PartMaterial mat, ToolType tool) {
        if (mat == PartMaterial.SKELETON) return "handle_skeleton";
        if (mat == PartMaterial.SPIDER) return "eye_spider";
        String suffix = mat.getMaterialName() + "_stick";
        if (tool == ToolType.SWORD) return suffix + "_sword";
        if (tool == ToolType.TRIDENT) return suffix + "_trident";
        return suffix;
    }

    private String getToolGripTexture(PartMaterial mat) {
        if (mat == PartMaterial.ZOMBIE) return "grip_zombie";
        return mat.getMaterialName() + "_handle";
    }

    private String getSwordGripTexture(PartMaterial mat) {
        if (mat == PartMaterial.ZOMBIE) return "grip_zombie";
        return switch (mat) {
            case WOOD -> "wooden_sword_handle";
            case GOLD -> "golden_sword_handle";
            default -> mat.getMaterialName() + "_sword_handle";
        };
    }

    private String getEyeTexture(PartMaterial mat) {
        if (mat == PartMaterial.SPIDER) return "eye_spider";
        if (mat == PartMaterial.ENDERMAN) return "eye_enderman";
        return switch (mat) {
            case WOOD -> "wooden_eye";
            default -> mat.getMaterialName() + "_eye";
        };
    }

    private String getTridentEyeTexture(PartMaterial mat) {
        if (mat == PartMaterial.SPIDER) return "eye_spider";
        if (mat == PartMaterial.ENDERMAN) return "eye_enderman";
        if (mat == PartMaterial.BLAZE) return "blaze_trident_eye";
        return switch (mat) {
            case WOOD -> "wooden_eye_trident";
            default -> mat.getMaterialName() + "_eye_trident";
        };
    }

    private String getPickaxeLeftHeadTexture(PartMaterial mat) {
        if (mat == PartMaterial.GREED) return "head_greed";
        if (mat == PartMaterial.CREEPER) return "head_creeper";
        if (mat == PartMaterial.ZOMBIE) return "head_zombie";
        return switch (mat) {
            case WOOD -> "wooden_pickaxe_head_left";
            case GOLD -> "golden_pickaxe_head_left";
            default -> mat.getMaterialName() + "_pickaxe_head_left";
        };
    }

    private String getPickaxeRightHeadTexture(PartMaterial mat) {
        if (mat == PartMaterial.GREED) return "head_greed";
        if (mat == PartMaterial.CREEPER) return "head_creeper";
        if (mat == PartMaterial.ZOMBIE) return "head_zombie";
        return switch (mat) {
            case WOOD -> "wooden_pickaxe_head_right";
            case GOLD -> "golden_pickaxe_head_right";
            default -> mat.getMaterialName() + "_pickaxe_head_right";
        };
    }

    private String getAxeHeadTexture(PartMaterial mat) {
        if (mat == PartMaterial.GREED) return "head_greed";
        if (mat == PartMaterial.CREEPER) return "head_creeper";
        if (mat == PartMaterial.ZOMBIE) return "head_zombie";
        return switch (mat) {
            case WOOD -> "wooden_axe_head";
            case GOLD -> "golden_axe_head";
            default -> mat.getMaterialName() + "_axe_head";
        };
    }

    private String getShovelHeadTexture(PartMaterial mat) {
        if (mat == PartMaterial.GREED) return "head_greed";
        if (mat == PartMaterial.CREEPER) return "head_creeper";
        if (mat == PartMaterial.ZOMBIE) return "head_zombie";
        return switch (mat) {
            case WOOD -> "wooden_shovel_head";
            case GOLD -> "golden_shovel_head";
            default -> mat.getMaterialName() + "_shovel_head";
        };
    }

    private String getHoeHeadTexture(PartMaterial mat) {
        if (mat == PartMaterial.GREED) return "head_greed";
        if (mat == PartMaterial.CREEPER) return "head_creeper";
        if (mat == PartMaterial.ZOMBIE) return "head_zombie";
        return switch (mat) {
            case WOOD -> "wooden_hoe_head";
            case GOLD -> "golden_hoe_head";
            default -> mat.getMaterialName() + "_hoe_head";
        };
    }

    private String getBladeTexture(PartMaterial mat) {
        if (mat == PartMaterial.GREED) return "head_greed";
        if (mat == PartMaterial.CREEPER) return "head_creeper";
        if (mat == PartMaterial.ZOMBIE) return "head_zombie";
        return switch (mat) {
            case WOOD -> "wooden_blade";
            case GOLD -> "golden_blade";
            default -> mat.getMaterialName() + "_blade";
        };
    }

    private String getTridentHeadTexture(PartMaterial mat) {
        if (mat == PartMaterial.GREED) return "head_greed";
        if (mat == PartMaterial.CREEPER) return "head_creeper";
        if (mat == PartMaterial.ZOMBIE) return "head_zombie";
        if (mat == PartMaterial.AMETHYST) return "head_amethyst";
        if (mat == PartMaterial.PRISMARINE) return "prismarine_head_trident";
        if (mat == PartMaterial.SKULK) return "sculk_trident_head";
        if (mat == PartMaterial.WIND) return "wind_trident_head";
        if (mat == PartMaterial.BLAZE) return "blaze_trident_head";
        return switch (mat) {
            case WOOD -> "wood_head_generic";
            case GOLD -> "gold_head_generic";
            default -> mat.getMaterialName() + "_head_generic";
        };
    }

    public static Map<Integer, PartType> getPatternForTool(ToolType tool) {
        return switch (tool) {
            case PICKAXE -> Map.of(
                    0, PartType.HEAD,
                    1, PartType.EYE,
                    2, PartType.HEAD,
                    4, PartType.HANDLE
            );
            case SWORD -> Map.of(
                    1, PartType.BLADE,
                    4, PartType.GRIP,
                    7, PartType.HANDLE
            );
            case AXE -> Map.of(
                    1, PartType.HEAD,
                    4, PartType.GRIP,
                    7, PartType.HANDLE
            );
            case SHOVEL -> Map.of(
                    1, PartType.HEAD,
                    4, PartType.GRIP,
                    7, PartType.HANDLE
            );
            case HOE -> Map.of(
                    0, PartType.HEAD,
                    1, PartType.EYE,
                    4, PartType.HANDLE
            );
            case TRIDENT -> Map.of(
                    1, PartType.HEAD,
                    4, PartType.EYE,
                    7, PartType.HANDLE
            );
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            resultStack = slotStack.copy();

            if (slotIndex == 9) { // Result Slot
                if (!this.moveItemStackTo(slotStack, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, resultStack);
            } else if (slotIndex >= 10) { // Player inventory / hotbar
                ToolPartData partData = slotStack.get(TelumComponents.TOOL_PART);
                boolean movedToGrid = false;
                if (slotStack.is(TelumItems.LYRE_PART_LEFT) || slotStack.is(TelumItems.LYRE_PART_RIGHT)) {
                    for (int gridSlotIdx = 0; gridSlotIdx < 9; gridSlotIdx++) {
                        Slot gridSlot = this.slots.get(gridSlotIdx);
                        if (gridSlot.getItem().isEmpty()) {
                            if (this.moveItemStackTo(slotStack, gridSlotIdx, gridSlotIdx + 1, false)) {
                                movedToGrid = true;
                                break;
                            }
                        }
                    }
                } else if (partData != null) {
                    ToolType targetTool = getSelectedToolType();
                    Map<Integer, PartType> pattern = getPatternForTool(targetTool);
                    if (pattern != null) {
                        for (Map.Entry<Integer, PartType> entry : pattern.entrySet()) {
                            int gridSlotIdx = entry.getKey();
                            PartType requiredType = entry.getValue();
                            if (isValidPartForForgeSlot(targetTool, gridSlotIdx, requiredType, partData)) {
                                Slot gridSlot = this.slots.get(gridSlotIdx);
                                if (gridSlot.getItem().isEmpty()) {
                                    if (this.moveItemStackTo(slotStack, gridSlotIdx, gridSlotIdx + 1, false)) {
                                        movedToGrid = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (!movedToGrid) {
                    if (slotIndex < 37) {
                        if (!this.moveItemStackTo(slotStack, 37, 46, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(slotStack, 10, 37, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            } else { // From 3x3 crafting grid back to inventory
                if (!this.moveItemStackTo(slotStack, 10, 46, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == resultStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return resultStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            this.clearContainer(player, inputSlots);
        });
    }

    public static boolean isValidTridentHeadMaterial(PartMaterial mat) {
        return mat == PartMaterial.PRISMARINE ||
               mat == PartMaterial.SKULK ||
               mat == PartMaterial.WIND ||
               mat == PartMaterial.BLAZE ||
               mat == PartMaterial.CREEPER;
    }

    public static boolean isValidPartForForgeSlot(ToolType tool, int slotIdx, PartType requiredType, ToolPartData data) {
        if (data == null || data.partType() != requiredType) return false;
        if (tool == ToolType.TRIDENT && requiredType == PartType.HEAD) {
            return isValidTridentHeadMaterial(data.material());
        }
        return true;
    }
}
