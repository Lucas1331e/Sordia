package dasouza.telum.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dasouza.telum.Telum;
import dasouza.telum.client.ClientBookProgress;
import dasouza.telum.item.TelumItems;
import dasouza.telum.item.ToolPartItem;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.tool.PartType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class GuideBookScreen extends Screen {

    private static final Identifier TEX_FIRST = Telum.id("textures/book/book_firtsl.png");
    private static final Identifier TEX_FIRST_COMPLETE = Telum.id("textures/book/book_firts_completel.png");
    private static final Identifier TEX_MIDDLE = Telum.id("textures/book/book_middel.png");
    private static final Identifier TEX_MIDDLE_LEFT = Telum.id("textures/book/book_middel_left_done.png");
    private static final Identifier TEX_MIDDLE_RIGHT = Telum.id("textures/book/book_middel_right_done.png");
    private static final Identifier TEX_MIDDLE_BOTH = Telum.id("textures/book/book_middel_both_done.png");
    private static final Identifier TEX_LAST = Telum.id("textures/book/book_last.png");
    private static final Identifier TEX_LAST_DONE = Telum.id("textures/book/book_last_done.png");

    private static final int BOOK_WIDTH = 300;
    private static final int BOOK_HEIGHT = 181;

    // 22 Spreads: Spread 0 = Glossary 1, Spread 1 = Glossary 2 / Log Notes, Spreads 2..20 = 19 2-Page Materials, Spread 21 = Skulk (Single Left Page) & Back Cover
    private static final int TOTAL_SPREADS = 22;

    private static final List<PartMaterial> MATERIAL_ORDER = List.of(
            // Vanilla Minerals (9)
            PartMaterial.WOOD,
            PartMaterial.STONE,
            PartMaterial.COPPER,
            PartMaterial.IRON,
            PartMaterial.GOLD,
            PartMaterial.DIAMOND,
            PartMaterial.NETHERITE,
            PartMaterial.EMERALD,
            PartMaterial.AMETHYST,
            // Other Items (11) - SKULK is ALWAYS LAST (#20)
            PartMaterial.PRISMARINE,
            PartMaterial.BLAZE,
            PartMaterial.SPIDER,
            PartMaterial.SKELETON,
            PartMaterial.ZOMBIE,
            PartMaterial.CREEPER,
            PartMaterial.ENDERMAN,
            PartMaterial.WIND,
            PartMaterial.SULFUR,
            PartMaterial.GREED,
            PartMaterial.SKULK
    );

    // Pre-computed part items cache to avoid allocating ItemStacks every render frame
    private static final Map<PartMaterial, List<ItemStack>> PART_ITEMS_CACHE = new EnumMap<>(PartMaterial.class);

    static {
        for (PartMaterial mat : MATERIAL_ORDER) {
            List<ItemStack> list = new ArrayList<>();
            for (PartType type : PartType.values()) {
                if (TelumItems.isPartTypeAllowed(mat, type)) {
                    ToolPartItem item = TelumItems.getPartItem(type, mat);
                    if (item != null) {
                        list.add(new ItemStack(item));
                    }
                }
            }
            PART_ITEMS_CACHE.put(mat, Collections.unmodifiableList(list));
        }
    }

    private int currentSpread = 0;

    public GuideBookScreen() {
        super(Component.translatable("item.telum.guide_book"));
    }

    private String translate(String key) {
        return Component.translatable(key).getString();
    }

    private String translate(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    private int getToolCraftedMaterialsCount() {
        int count = 0;
        for (PartMaterial mat : MATERIAL_ORDER) {
            if (ClientBookProgress.isToolCrafted(mat.getMaterialName())) {
                count++;
            }
        }
        return count;
    }

    private boolean isAllMaterialsDone() {
        return getToolCraftedMaterialsCount() >= MATERIAL_ORDER.size();
    }

    private void setSpread(int spread) {
        if (spread < 0) spread = 0;
        if (spread >= TOTAL_SPREADS) spread = TOTAL_SPREADS - 1;
        if (this.currentSpread != spread) {
            this.currentSpread = spread;
            playPageTurnSound();
        }
    }

    private void playPageTurnSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        int key = keyEvent.key();
        if (key == InputConstants.KEY_LEFT) {
            setSpread(currentSpread - 1);
            return true;
        }
        if (key == InputConstants.KEY_RIGHT) {
            setSpread(currentSpread + 1);
            return true;
        }
        if (key == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        int left = (this.width - BOOK_WIDTH) / 2;
        int top = (this.height - BOOK_HEIGHT) / 2;

        // Navigation arrows on book outer margins
        if (mouseX >= left - 25 && mouseX <= left + 15 && mouseY >= top + 70 && mouseY <= top + 110) {
            setSpread(currentSpread - 1);
            return true;
        }
        if (mouseX >= left + BOOK_WIDTH - 15 && mouseX <= left + BOOK_WIDTH + 25 && mouseY >= top + 70 && mouseY <= top + 110) {
            setSpread(currentSpread + 1);
            return true;
        }

        // Glossary Return Button below the book
        if (currentSpread > 0) {
            int btnWidth = 90;
            int btnHeight = 16;
            int btnX = (this.width - btnWidth) / 2;
            int btnY = top + BOOK_HEIGHT + 6;

            if (mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight) {
                setSpread(0);
                return true;
            }
        }

        // Glossary clicks on Spread 0 (Glossary Page 1: Materials 0..9)
        if (currentSpread == 0) {
            int glossaryLeft = left + 152;
            int glossaryTop = top + 38;

            for (int i = 0; i < 10; i++) {
                int y = glossaryTop + i * 13;
                if (mouseX >= glossaryLeft && mouseX <= glossaryLeft + 105 && mouseY >= y && mouseY <= y + 12) {
                    setSpread(2 + i);
                    return true;
                }
            }
        }

        // Glossary clicks on Spread 1 (Glossary Page 2: Materials 10..19 on Left Page)
        if (currentSpread == 1) {
            int glossaryLeft = left + 14;
            int glossaryTop = top + 38;

            for (int i = 10; i < 20; i++) {
                int row = i - 10;
                int y = glossaryTop + row * 13;
                if (mouseX >= glossaryLeft && mouseX <= glossaryLeft + 105 && mouseY >= y && mouseY <= y + 12) {
                    setSpread(2 + i);
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - BOOK_WIDTH) / 2;
        int top = (this.height - BOOK_HEIGHT) / 2;

        // Dark background overlay
        gfx.fill(0, 0, this.width, this.height, 0x70000000);

        // Render book base background texture according to current spread state
        Identifier bgTexture = getSpreadTexture();
        gfx.blit(RenderPipelines.GUI_TEXTURED, bgTexture, left, top, 0.0f, 0.0f, BOOK_WIDTH, BOOK_HEIGHT, BOOK_WIDTH, BOOK_HEIGHT);

        Font font = this.getFont();

        if (currentSpread == 0) {
            renderSpread0Glossary1(gfx, font, left, top, mouseX, mouseY);
        } else if (currentSpread == 1) {
            renderSpread1Glossary2(gfx, font, left, top, mouseX, mouseY);
        } else if (currentSpread >= 2 && currentSpread <= 20) {
            int matIdx = currentSpread - 2;
            if (matIdx < 19) {
                PartMaterial mat = MATERIAL_ORDER.get(matIdx);
                renderMaterialLeftPage(gfx, font, left + 14, top + 14, mat);
                renderMaterialRightPage(gfx, font, left + 152, top + 14, mat);
            }
        } else if (currentSpread == 21) {
            // Spread 21 (Final Spread): Skulk on Left Page (Single page material), Back Cover on Right Page
            renderSkulkSinglePage(gfx, font, left + 14, top + 14);
        }

        // Render page navigation arrows
        if (currentSpread > 0) {
            gfx.text(font, "◄", left - 18, top + 84, 0xFFFFFFFF, true);
        }
        if (currentSpread < TOTAL_SPREADS - 1) {
            gfx.text(font, "►", left + BOOK_WIDTH + 8, top + 84, 0xFFFFFFFF, true);
        }

        // Render Glossary Return Button below the book
        if (currentSpread > 0) {
            int btnWidth = 90;
            int btnHeight = 16;
            int btnX = (this.width - btnWidth) / 2;
            int btnY = top + BOOK_HEIGHT + 6;

            boolean isHovered = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight;
            int bgFill = isHovered ? 0x90000000 : 0x55000000;
            int borderColor = isHovered ? 0xFFFFD740 : 0x80FFFFFF;
            int textColor = isHovered ? 0xFFFFD740 : 0xFFFFFFFF;

            gfx.fill(btnX, btnY, btnX + btnWidth, btnY + btnHeight, bgFill);
            gfx.outline(btnX, btnY, btnWidth, btnHeight, borderColor);
            gfx.centeredText(font, translate("gui.telum.guide_book.glossary_button"), btnX + btnWidth / 2, btnY + 4, textColor);
        }
    }

    private Identifier getSpreadTexture() {
        if (currentSpread == 0) {
            return isAllMaterialsDone() ? TEX_FIRST_COMPLETE : TEX_FIRST;
        } else if (currentSpread == 1) {
            return TEX_MIDDLE;
        } else if (currentSpread >= 2 && currentSpread <= 20) {
            int matIdx = currentSpread - 2;
            if (matIdx < 19) {
                PartMaterial mat = MATERIAL_ORDER.get(matIdx);
                boolean toolDone = ClientBookProgress.isToolCrafted(mat.getMaterialName());
                return toolDone ? TEX_MIDDLE_BOTH : TEX_MIDDLE;
            }
            return TEX_MIDDLE;
        } else {
            // Spread 21 (Final Spread): Skulk on Left Page, Leather Back Cover on Right Page
            boolean skulkDone = ClientBookProgress.isToolCrafted(PartMaterial.SKULK.getMaterialName());
            return skulkDone ? TEX_LAST_DONE : TEX_LAST;
        }
    }

    private void renderSpread0Glossary1(GuiGraphicsExtractor gfx, Font font, int left, int top, int mouseX, int mouseY) {
        int rightPageX = left + 152;
        int rightPageY = top + 14;

        int doneCount = getToolCraftedMaterialsCount();
        boolean completed = isAllMaterialsDone();

        if (completed) {
            gfx.text(font, translate("gui.telum.guide_book.glossary_complete"), rightPageX, rightPageY, 0xFFD4AF37, false);
        } else {
            gfx.text(font, translate("gui.telum.guide_book.glossary_1"), rightPageX, rightPageY, 0xFF404040, false);
        }

        // Clean pixel line separator (105px width)
        gfx.fill(rightPageX, rightPageY + 11, rightPageX + 105, rightPageY + 12, 0xFF888888);

        // Subheader progress count
        gfx.text(font, translate("gui.telum.guide_book.progress", doneCount), rightPageX, rightPageY + 14, 0xFF666666, false);

        // Glossary list: Items 1 to 10 in single column
        int listTop = rightPageY + 26;
        for (int i = 0; i < 10; i++) {
            PartMaterial mat = MATERIAL_ORDER.get(i);
            int y = listTop + i * 12;

            boolean isHovered = mouseX >= rightPageX && mouseX <= rightPageX + 105 && mouseY >= y && mouseY <= y + 11;
            boolean isToolCrafted = ClientBookProgress.isToolCrafted(mat.getMaterialName());
            boolean isPartCrafted = ClientBookProgress.isMaterialCrafted(mat.getMaterialName());

            String numberStr = (i + 1) + ".";
            String matName = getMaterialDisplayName(mat);

            int numColor = 0xFFD4AF37;
            // Yellow (0xFFFFD740) when a part is acquired, Green (0xFF008000) when full tool is crafted, Red on hover
            int nameColor = isHovered ? 0xFFFF0000 : (isToolCrafted ? 0xFF008000 : (isPartCrafted ? 0xFFFFD740 : 0xFF222222));

            gfx.text(font, numberStr, rightPageX, y, numColor, false);
            gfx.text(font, matName, rightPageX + 22, y, nameColor, false);
        }
    }

    private void renderSpread1Glossary2(GuiGraphicsExtractor gfx, Font font, int left, int top, int mouseX, int mouseY) {
        // Left Page: Glossary 2/2 (Items 11 to 20) shifted left to left+14
        int leftPageX = left + 14;
        int leftPageY = top + 14;

        int doneCount = getToolCraftedMaterialsCount();
        gfx.text(font, translate("gui.telum.guide_book.glossary_2"), leftPageX, leftPageY, 0xFF404040, false);
        gfx.fill(leftPageX, leftPageY + 11, leftPageX + 105, leftPageY + 12, 0xFF888888);
        gfx.text(font, translate("gui.telum.guide_book.progress", doneCount), leftPageX, leftPageY + 14, 0xFF666666, false);

        int listTop = leftPageY + 26;
        for (int i = 10; i < 20; i++) {
            PartMaterial mat = MATERIAL_ORDER.get(i);
            int row = i - 10;
            int y = listTop + row * 12;

            boolean isHovered = mouseX >= leftPageX && mouseX <= leftPageX + 105 && mouseY >= y && mouseY <= y + 11;
            boolean isToolCrafted = ClientBookProgress.isToolCrafted(mat.getMaterialName());
            boolean isPartCrafted = ClientBookProgress.isMaterialCrafted(mat.getMaterialName());

            String numberStr = (i + 1) + ".";
            String matName = getMaterialDisplayName(mat);

            int numColor = 0xFFD4AF37;
            // Yellow (0xFFFFD740) when a part is acquired, Green (0xFF008000) when full tool is crafted, Red on hover
            int nameColor = isHovered ? 0xFFFF0000 : (isToolCrafted ? 0xFF008000 : (isPartCrafted ? 0xFFFFD740 : 0xFF222222));

            gfx.text(font, numberStr, leftPageX, y, numColor, false);
            gfx.text(font, matName, leftPageX + 22, y, nameColor, false);
        }

        // Right Page: Log notes text (fitted strictly inside maxW=94)
        int rightX = left + 148;
        int rightY = top + 14;
        int maxW = 94;

        gfx.text(font, "📖 " + translate("gui.telum.guide_book.intro_title"), rightX, rightY, 0xFF404040, false);
        gfx.fill(rightX, rightY + 11, rightX + maxW, rightY + 12, 0xFF888888);

        String introText = translate("gui.telum.guide_book.intro_text");
        List<String> wrappedIntro = wrapText(font, introText, maxW);
        for (int i = 0; i < wrappedIntro.size(); i++) {
            gfx.text(font, wrappedIntro.get(i), rightX, rightY + 20 + i * 11, 0xFF333333, false);
        }
    }

    private void renderMaterialLeftPage(GuiGraphicsExtractor gfx, Font font, int x, int y, PartMaterial mat) {
        boolean isPartCrafted = ClientBookProgress.isMaterialCrafted(mat.getMaterialName());
        boolean isUnlocked = isPartCrafted || mat == PartMaterial.SKULK;
        int maxW = 100;

        // Header Material Name
        String displayName = getMaterialDisplayName(mat);
        int titleColor = 0xFF000000 | getMaterialTextColor(mat);
        gfx.text(font, displayName, x, y, titleColor, false);
        gfx.fill(x, y + 11, x + maxW, y + 12, 0xFF888888);

        // Section: Diseños (Part Sprites)
        gfx.text(font, translate("gui.telum.guide_book.part_designs"), x, y + 16, 0xFF555555, false);

        int spriteX = x;
        int spriteY = y + 27;
        List<ItemStack> partItems = PART_ITEMS_CACHE.getOrDefault(mat, Collections.emptyList());

        for (int i = 0; i < partItems.size(); i++) {
            ItemStack stack = partItems.get(i);
            int px = spriteX + i * 20;
            int py = spriteY;

            gfx.item(stack, px, py);
        }

        // Section: Stats Summary with Tooltip Colors
        int statsY = spriteY + 24;
        gfx.text(font, translate("gui.telum.guide_book.stats_header"), x, statsY, 0xFF8B0000, false);
        gfx.fill(x, statsY + 10, x + maxW, statsY + 11, 0xFFCCCCCC);

        int contentY = statsY + 15;

        if (isUnlocked) {
            // Stat 1: Durabilidad (Green 0xFF008000)
            gfx.text(font, "+ " + translate("gui.telum.guide_book.durability"), x, contentY, 0xFF008000, false);
            gfx.text(font, String.format("%.1fx", mat.getDurabilityMultiplier()), x + 65, contentY, 0xFF333333, false);

            // Stat 2: Daño Base (Red 0xFFCC0000)
            gfx.text(font, "> " + translate("gui.telum.guide_book.damage"), x, contentY + 12, 0xFFCC0000, false);
            gfx.text(font, String.format("%.1fx", mat.getDamageMultiplier()), x + 65, contentY + 12, 0xFF333333, false);

            // Stat 3: Vel. Minado (Aqua/Blue 0xFF0055A0)
            gfx.text(font, "* " + translate("gui.telum.guide_book.speed"), x, contentY + 24, 0xFF0055A0, false);
            gfx.text(font, String.format("%.1fx", mat.getMiningSpeedMultiplier()), x + 65, contentY + 24, 0xFF333333, false);

            // Stat 4: Nivel Minado (Gold 0xFFB8860B)
            gfx.text(font, "# " + translate("gui.telum.guide_book.level"), x, contentY + 36, 0xFFB8860B, false);
            gfx.text(font, String.valueOf(mat.getMiningLevel()), x + 65, contentY + 36, 0xFF333333, false);
        } else {
            // Locked Page (Stats hidden until part acquired)
            gfx.text(font, translate("gui.telum.guide_book.locked_unknown"), x, contentY, 0xFF888888, false);
            List<String> wrappedStatsHint = wrapText(font, translate("gui.telum.guide_book.locked_stats_hint"), maxW);
            for (int i = 0; i < wrappedStatsHint.size(); i++) {
                gfx.text(font, wrappedStatsHint.get(i), x, contentY + 14 + i * 10, 0xFF666666, false);
            }
        }
    }

    private void renderMaterialRightPage(GuiGraphicsExtractor gfx, Font font, int x, int y, PartMaterial mat) {
        boolean isPartCrafted = ClientBookProgress.isMaterialCrafted(mat.getMaterialName());
        boolean isUnlocked = isPartCrafted || mat == PartMaterial.SKULK;

        // Shift left to x-4 (left+148) and enforce strict maxW=94 so text NEVER overflows right page margin!
        int rightX = x - 4;
        int maxW = 94;

        // Subheader Right Page
        gfx.text(font, translate("gui.telum.guide_book.details_header"), rightX, y, 0xFF404040, false);
        gfx.fill(rightX, y + 11, rightX + maxW, y + 12, 0xFF888888);

        // Ability Description Section
        int abilityY = y + 16;
        gfx.text(font, translate("gui.telum.guide_book.ability_header"), rightX, abilityY, 0xFF8B0000, false);

        int endOfAbilityY = abilityY + 22;
        if (isUnlocked) {
            String abilityTitle = getMaterialAbilityTitle(mat);
            String abilityDesc = getMaterialAbilityDescription(mat);
            int matColor = 0xFF000000 | getMaterialTextColor(mat);

            // Ability Title rendered in material color!
            gfx.text(font, abilityTitle + ":", rightX, abilityY + 11, matColor, false);

            List<String> wrappedLines = wrapText(font, abilityDesc, maxW);
            int count = Math.min(3, wrappedLines.size());
            for (int i = 0; i < count; i++) {
                gfx.text(font, wrappedLines.get(i), rightX, abilityY + 22 + i * 10, 0xFF222222, false);
            }
            endOfAbilityY = abilityY + 22 + count * 10;
        } else {
            gfx.text(font, translate("gui.telum.guide_book.locked_ability"), rightX, abilityY + 12, 0xFF888888, false);
            List<String> wrappedAbilityHint = wrapText(font, translate("gui.telum.guide_book.locked_ability_hint"), maxW);
            for (int i = 0; i < wrappedAbilityHint.size(); i++) {
                gfx.text(font, wrappedAbilityHint.get(i), rightX, abilityY + 24 + i * 10, 0xFF666666, false);
            }
            endOfAbilityY = abilityY + 24 + wrappedAbilityHint.size() * 10;
        }

        // Obtaining Hint Section (Dynamically offset below ability text with 6px padding)
        int obtainY = endOfAbilityY + 6;
        gfx.text(font, isUnlocked ? translate("gui.telum.guide_book.obtain_header_unlocked") : translate("gui.telum.guide_book.obtain_header"), rightX, obtainY, isUnlocked ? 0xFF0055A0 : 0xFFD4AF37, false);
        gfx.fill(rightX, obtainY + 10, rightX + maxW, obtainY + 11, 0xFFCCCCCC);

        String obtainDesc = getMaterialObtainHint(mat);
        List<String> obtainLines = wrapText(font, obtainDesc, maxW);
        for (int i = 0; i < Math.min(5, obtainLines.size()); i++) {
            gfx.text(font, obtainLines.get(i), rightX, obtainY + 13 + i * 10, 0xFF333333, false);
        }
    }

    private void renderSkulkSinglePage(GuiGraphicsExtractor gfx, Font font, int x, int y) {
        PartMaterial mat = PartMaterial.SKULK;
        int maxW = 100;

        // Header Material Name
        String displayName = getMaterialDisplayName(mat);
        int titleColor = 0xFF000000 | getMaterialTextColor(mat);
        gfx.text(font, displayName, x, y, titleColor, false);
        gfx.fill(x, y + 11, x + maxW, y + 12, 0xFF888888);

        // Section: Part Sprites
        int spriteX = x;
        int spriteY = y + 15;
        List<ItemStack> partItems = PART_ITEMS_CACHE.getOrDefault(mat, Collections.emptyList());
        for (int i = 0; i < partItems.size(); i++) {
            ItemStack stack = partItems.get(i);
            gfx.item(stack, spriteX + i * 20, spriteY);
        }

        // Section: Stats Summary with Tooltip Colors
        int statsY = spriteY + 22;
        gfx.text(font, translate("gui.telum.guide_book.stats_header"), x, statsY, 0xFF8B0000, false);

        // Stat labels with colors
        gfx.text(font, "✚ Durab: " + String.format("%.1fx", mat.getDurabilityMultiplier()), x, statsY + 11, 0xFF008000, false);
        gfx.text(font, "⚔ Daño:  " + String.format("%.1fx", mat.getDamageMultiplier()), x, statsY + 21, 0xFFCC0000, false);
        gfx.text(font, "⛏ Veloc: " + String.format("%.1fx", mat.getMiningSpeedMultiplier()), x, statsY + 31, 0xFF0055A0, false);
        gfx.text(font, "◆ Nivel: " + mat.getMiningLevel(), x, statsY + 41, 0xFFB8860B, false);

        // Ability Section (Ability title rendered in material color)
        int abilityY = statsY + 54;
        gfx.text(font, translate("gui.telum.guide_book.ability_header"), x, abilityY, 0xFF8B0000, false);

        int matColor = 0xFF000000 | getMaterialTextColor(mat);
        gfx.text(font, getMaterialAbilityTitle(mat) + ":", x, abilityY + 11, matColor, false);
        gfx.text(font, getMaterialAbilityDescription(mat), x, abilityY + 21, 0xFF222222, false);

        // Obtaining Hint Section
        int obtainY = abilityY + 34;
        gfx.text(font, translate("gui.telum.guide_book.obtain_header_unlocked"), x, obtainY, 0xFF0055A0, false);
        String obtainDesc = getMaterialObtainHint(mat);
        List<String> obtainLines = wrapText(font, obtainDesc, maxW);
        for (int i = 0; i < Math.min(2, obtainLines.size()); i++) {
            gfx.text(font, obtainLines.get(i), x, obtainY + 10 + i * 9, 0xFF333333, false);
        }
    }

    private String getMaterialDisplayName(PartMaterial mat) {
        return translate(mat.getTranslationKey());
    }

    private int getMaterialTextColor(PartMaterial mat) {
        return switch (mat) {
            case WOOD -> 0x8B4513;
            case STONE -> 0x555555;
            case COPPER -> 0xD2691E;
            case PRISMARINE -> 0x008080;
            case SKULK -> 0x005577;
            case WIND -> 0x0088AA;
            case IRON -> 0x444444;
            case GOLD -> 0xB8860B;
            case DIAMOND -> 0x008B8B;
            case NETHERITE -> 0x333333;
            case BLAZE -> 0xD2691E;
            case SPIDER -> 0x800000;
            case SKELETON -> 0x555555;
            case ZOMBIE -> 0x006400;
            case CREEPER -> 0x008000;
            case ENDERMAN -> 0x4B0082;
            case SULFUR -> 0xB8860B;
            case AMETHYST -> 0x8A2BE2;
            case GREED -> 0xB8860B;
            case EMERALD -> 0x008000;
        };
    }

    private String getMaterialAbilityTitle(PartMaterial mat) {
        return translate("ability.telum." + mat.getMaterialName() + ".title");
    }

    private String getMaterialAbilityDescription(PartMaterial mat) {
        return translate("ability.telum." + mat.getMaterialName() + ".desc");
    }

    private String getMaterialObtainHint(PartMaterial mat) {
        return translate("obtain.telum." + mat.getMaterialName());
    }

    private List<String> wrapText(Font font, String text, int maxWidth) {
        List<String> result = new ArrayList<>();
        String[] paragraphs = text.split("\n");
        for (String paragraph : paragraphs) {
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                if (line.length() == 0) {
                    line.append(word);
                } else if (font.width(line + " " + word) <= maxWidth) {
                    line.append(" ").append(word);
                } else {
                    result.add(line.toString());
                    line = new StringBuilder(word);
                }
            }
            if (line.length() > 0) {
                result.add(line.toString());
            }
        }
        return result;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
