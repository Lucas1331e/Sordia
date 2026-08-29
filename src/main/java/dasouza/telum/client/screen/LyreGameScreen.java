package dasouza.telum.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dasouza.telum.Telum;
import dasouza.telum.network.LyreGameResultPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Zelda: Ocarina of Time style musical minigame screen for the Lyre of Time.
 * 5 note keys (A, S, D, F, G) played at the player's own rhythm.
 */
public class LyreGameScreen extends Screen {

    private static final Identifier BUTTON_BG_TEXTURE = Telum.id("textures/gui/fondolyra.png");

    private static final int NOTE_COUNT = 5;
    private static final int[] NOTE_KEYS = {
            InputConstants.KEY_A,
            InputConstants.KEY_S,
            InputConstants.KEY_D,
            InputConstants.KEY_F,
            InputConstants.KEY_G
    };
    private static final String[] NOTE_LABELS = {"A", "S", "D", "F", "G"};
    private static final String[] NOTE_NAMES = {"Do", "Mi", "Sol", "La", "Do+"};
    private static final float[] NOTE_PITCHES = {0.5f, 0.667f, 0.75f, 0.888f, 1.0f};

    private static final int[][] NOTE_COLORS = {
            {0xFF00E5FF, 0xFF00B8D4},
            {0xFFFF4081, 0xFFC51162},
            {0xFFFFD740, 0xFFFFC400},
            {0xFF69F0AE, 0xFF00E676},
            {0xFFE040FB, 0xFFAA00FF}
    };

    private static final int[][] TRANSFORMATION_SONGS = {
            {2, 1, 0, 2, 1, 0}, // Canción del Tiempo: D - S - A - D - S - A
            {0, 1, 2, 0, 1, 2}, // Canción de los Ecos: A - S - D - A - S - D
            {1, 2, 3, 1, 2, 3}  // Canción de Retorno: S - D - F - S - D - F
    };
    private static final String[] SONG_NAMES = {
            "Canción del Tiempo",
            "Canción de los Ecos",
            "Canción de Retorno"
    };

    public record SongEntry(String title, int[] notes, int magicResultId, int encodedSongInt) {}

    private final List<SongEntry> knownSongs = new ArrayList<>();
    private int currentSongIndex = 0;

    private final int targetX, targetY, targetZ;
    private final int sourceBlockId, resultBlockId;

    private int[] targetSong;
    private String songTitle;
    private boolean isComposerMode = false;
    private final List<Integer> playedSequence = new ArrayList<>();
    private final List<PlayedNoteVisual> playedVisuals = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();

    private int ticksElapsed = 0;
    private boolean gameFinished = false;
    private boolean victory = false;
    private int finishTimer = 0;
    private boolean resultSent = false;
    private int errorFlashTicks = 0;
    private String feedbackMessage = "";
    private int feedbackTicks = 0;

    private final boolean[] keyPressed = new boolean[NOTE_COUNT];
    private final int[] keyPressAnim = new int[NOTE_COUNT];

    private final Random random = new Random();

    private int staffTop, staffBottom, staffLeft, staffRight;

    public static int encodeSong(List<Integer> notes) {
        if (notes == null || notes.size() < 6) return 1012340;
        int code = 1000000;
        int mult = 100000;
        for (int i = 0; i < 6; i++) {
            code += (notes.get(i) % 5) * mult;
            mult /= 10;
        }
        return code;
    }

    public static int[] decodeSong(int code) {
        if (code < 1000000) return new int[]{0, 1, 2, 0, 1, 2};
        int[] song = new int[6];
        song[0] = (code / 100000) % 10;
        song[1] = (code / 10000) % 10;
        song[2] = (code / 1000) % 10;
        song[3] = (code / 100) % 10;
        song[4] = (code / 10) % 10;
        song[5] = code % 10;
        return song;
    }

    public LyreGameScreen(int targetX, int targetY, int targetZ, int sourceBlockId, int resultBlockId) {
        super(Component.literal("Lyra del Tiempo"));
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.sourceBlockId = sourceBlockId;
        this.resultBlockId = resultBlockId;

        if (resultBlockId == dasouza.telum.block.EchoBarrelBlock.ECHO_BARREL_TUNING_MAGIC_ID) {
            this.isComposerMode = true;
            this.targetSong = new int[6];
            this.songTitle = "Componer Canción del Barril";
        } else if (resultBlockId == dasouza.telum.item.LyreItem.SCULK_SONG_MAGIC_ID) {
            this.isComposerMode = false;
            this.targetSong = new int[]{0, 4, 3, 2, 1, 0};
            this.songTitle = "Canción del Sculk";
        } else {
            this.isComposerMode = false;

            if (dasouza.telum.client.ClientSongRepertoire.hasLearnedSongId("backtime_song")) {
                knownSongs.add(new SongEntry("Canción del Tiempo", TRANSFORMATION_SONGS[0], 0, encodeSong(List.of(2, 1, 0, 2, 1, 0))));
            }
            if (dasouza.telum.client.ClientSongRepertoire.hasLearnedSongId("bed_song")) {
                knownSongs.add(new SongEntry("Canción de Retorno", TRANSFORMATION_SONGS[2], dasouza.telum.item.LyreItem.RETURN_SONG_MAGIC_ID, encodeSong(List.of(1, 2, 3, 1, 2, 3))));
            }
            if (dasouza.telum.client.ClientSongRepertoire.hasLearnedSongId("dawn_song")) {
                knownSongs.add(new SongEntry("Canción del Amanecer", new int[]{4, 3, 4, 3, 0, 1}, dasouza.telum.item.LyreItem.DAWN_SONG_MAGIC_ID, encodeSong(List.of(4, 3, 4, 3, 0, 1))));
            }
            if (dasouza.telum.client.ClientSongRepertoire.hasLearnedSongId("reveal_song")) {
                knownSongs.add(new SongEntry("Sonata de la Revelación", new int[]{3, 1, 3, 4, 0, 2}, dasouza.telum.item.LyreItem.REVEAL_SONG_MAGIC_ID, encodeSong(List.of(3, 1, 3, 4, 0, 2))));
            }
            if (dasouza.telum.client.ClientSongRepertoire.hasLearnedSongId("void_song")) {
                knownSongs.add(new SongEntry("Balada del Vacío", new int[]{4, 1, 0, 3, 4, 2}, dasouza.telum.item.LyreItem.VOID_SONG_MAGIC_ID, encodeSong(List.of(4, 1, 0, 3, 4, 2))));
            }

            for (var songData : dasouza.telum.client.ClientSongRepertoire.getSongs()) {
                knownSongs.add(new SongEntry(
                        songData.title(),
                        decodeSong(songData.encodedSongInt()),
                        dasouza.telum.item.LyreItem.ECHO_PROJECTION_SUMMON_MAGIC_ID,
                        songData.encodedSongInt()
                ));
            }

            if (!knownSongs.isEmpty()) {
                selectSong(0);
            } else {
                this.targetSong = new int[0];
                this.songTitle = "Ninguna canción aprendida";
            }
        }
    }

    private void selectSong(int index) {
        if (knownSongs.isEmpty()) return;
        currentSongIndex = (index + knownSongs.size()) % knownSongs.size();
        SongEntry entry = knownSongs.get(currentSongIndex);
        this.targetSong = entry.notes();
        this.songTitle = entry.title();
        this.playedSequence.clear();
    }

    private boolean playedSequenceMatches(List<Integer> played, int[] target) {
        if (target == null || target.length == 0 || played.size() < target.length) return false;
        int offset = played.size() - target.length;
        for (int i = 0; i < target.length; i++) {
            if (played.get(offset + i) != target[i]) {
                return false;
            }
        }
        return true;
    }

    private void triggerVictory() {
        gameFinished = true;
        victory = true;
        finishTimer = 35;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.5f);
            mc.player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.5f, 1.2f);
        }

        for (int i = 0; i < 40; i++) {
            spawnNoteParticles(this.width / 2, staffTop + 25, 0xFFFFD740, 1);
        }
    }

    @Override
    protected void init() {
        super.init();
        staffLeft = (this.width - 280) / 2;
        staffRight = staffLeft + 280;
        staffTop = 85;
        staffBottom = 155;
    }

    @Override
    public void tick() {
        super.tick();
        ticksElapsed++;

        if (errorFlashTicks > 0) errorFlashTicks--;
        if (feedbackTicks > 0) feedbackTicks--;

        for (int i = 0; i < NOTE_COUNT; i++) {
            if (keyPressed[i] && keyPressAnim[i] < 5) {
                keyPressAnim[i]++;
            } else if (!keyPressed[i] && keyPressAnim[i] > 0) {
                keyPressAnim[i]--;
            }
        }

        Iterator<PlayedNoteVisual> visIter = playedVisuals.iterator();
        while (visIter.hasNext()) {
            PlayedNoteVisual vis = visIter.next();
            vis.life--;
            vis.x -= 1.2f;
            if (vis.life <= 0 || vis.x < staffLeft) {
                visIter.remove();
            }
        }

        Iterator<Particle> partIter = particles.iterator();
        while (partIter.hasNext()) {
            Particle p = partIter.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.05f;
            p.life--;
            if (p.life <= 0) partIter.remove();
        }

        if (gameFinished) {
            finishTimer--;
            if (finishTimer <= 0 && !resultSent) {
                sendResult();
                this.onClose();
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        int key = keyEvent.key();

        if (key == InputConstants.KEY_ESCAPE) {
            resultSent = true;
            this.onClose();
            return true;
        }

        if (!isComposerMode && !gameFinished) {
            if (key == InputConstants.KEY_LEFT) {
                selectSong(currentSongIndex - 1);
                playNoteSound(0);
                return true;
            }
            if (key == InputConstants.KEY_RIGHT) {
                selectSong(currentSongIndex + 1);
                playNoteSound(4);
                return true;
            }
        }

        if (gameFinished) return true;

        for (int i = 0; i < NOTE_COUNT; i++) {
            if (key == NOTE_KEYS[i]) {
                keyPressed[i] = true;
                handleNotePlayed(i);
                return true;
            }
        }

        return super.keyPressed(keyEvent);
    }

    private void handleNotePlayed(int noteIndex) {
        playNoteSound(noteIndex);

        int buttonX = getButtonCenterX(noteIndex);
        int buttonY = this.height - 45;
        spawnNoteParticles(buttonX, buttonY, NOTE_COLORS[noteIndex][0], 8);

        float noteStaffY = staffTop + (4 - noteIndex) * 14;
        playedVisuals.add(new PlayedNoteVisual(staffRight - 30, noteStaffY, noteIndex, 80));

        keyPressAnim[noteIndex] = 5;
        new Thread(() -> {
            try {
                Thread.sleep(120);
                keyPressed[noteIndex] = false;
            } catch (InterruptedException ignored) {}
        }).start();

        if (!gameFinished) {
            if (isComposerMode) {
                playedSequence.add(noteIndex);

                if (playedSequence.size() == 6) {
                    int code = encodeSong(playedSequence);
                    if (dasouza.telum.client.ClientSongRepertoire.containsSongCode(code)) {
                        errorFlashTicks = 20;
                        feedbackMessage = "✗ ¡Esta canción ya pertenece a otro Barril del Eco! Intenta otra combinación.";
                        feedbackTicks = 35;
                        playedSequence.clear();

                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player != null) {
                            mc.player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8f, 0.5f);
                        }
                        return;
                    }

                    triggerVictory();
                }
            } else if (targetSong != null && targetSong.length > 0) {
                int currentStep = playedSequence.size();
                if (currentStep < targetSong.length && noteIndex == targetSong[currentStep]) {
                    playedSequence.add(noteIndex);
                    if (playedSequence.size() == targetSong.length) {
                        triggerVictory();
                    }
                } else {
                    // Silently reset progress on wrong note without error message/flash
                    playedSequence.clear();
                    if (noteIndex == targetSong[0]) {
                        playedSequence.add(noteIndex);
                        if (playedSequence.size() == targetSong.length) {
                            triggerVictory();
                        }
                    }
                }
            }
        }
    }

    private void playNoteSound(int noteIndex) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            float pitch = NOTE_PITCHES[noteIndex];
            mc.player.playSound(SoundEvents.NOTE_BLOCK_HARP.value(), 1.0f, pitch);
        }
    }

    private void spawnNoteParticles(int x, int y, int color, int count) {
        for (int i = 0; i < count; i++) {
            float angle = (float) (Math.PI * 2 * i / count);
            float speed = 1.0f + random.nextFloat() * 2.0f;
            particles.add(new Particle(
                    x, y,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed - 1.0f,
                    color,
                    20 + random.nextInt(10)
            ));
        }
    }

    private int getButtonCenterX(int index) {
        int totalWidth = NOTE_COUNT * 44 + (NOTE_COUNT - 1) * 10;
        int left = (this.width - totalWidth) / 2;
        return left + index * (44 + 10) + 22;
    }

    private int getButtonLeft(int index) {
        int totalWidth = NOTE_COUNT * 44 + (NOTE_COUNT - 1) * 10;
        int left = (this.width - totalWidth) / 2;
        return left + index * (44 + 10);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        int bgAlpha = victory ? 0x70000000 : (errorFlashTicks > 0 ? 0x60550000 : 0x45000000);
        gfx.fill(0, 0, this.width, this.height, bgAlpha);

        Font font = this.getFont();

        String titleText = isComposerMode
                ? "♪ " + songTitle + " ♪"
                : "◄ " + songTitle + " (" + (currentSongIndex + 1) + "/" + knownSongs.size() + ") ►";
        int titleColor = victory ? 0xFFFFD740 : 0xFFE0E0E0;
        gfx.centeredText(font, titleText, this.width / 2, 8, titleColor);

        if (!isComposerMode && knownSongs.size() > 1) {
            gfx.centeredText(font, "Usa las flechas (◄ ►) para cambiar de canción", this.width / 2, 20, 0xFF69F0AE);
        } else if (resultBlockId == dasouza.telum.item.LyreItem.SCULK_SONG_MAGIC_ID) {
            gfx.centeredText(font, "Resonando con las ruinas de la Ancient City...", this.width / 2, 20, 0xFF00E5FF);
        } else if (sourceBlockId != 0) {
            Block source = Block.stateById(sourceBlockId).getBlock();
            String subtitle = "Restaurando desde: " + source.getName().getString();
            gfx.centeredText(font, subtitle, this.width / 2, 20, 0xFFAAAAAA);
        }

        if (targetSong.length > 0) {
            int totalTargetWidth = targetSong.length * 36;
            int targetStartX = (this.width - totalTargetWidth) / 2;
            int targetY = 44;

            gfx.centeredText(font, "Secuencia Requerida:", this.width / 2, 32, 0xFFDDDDDD);

            for (int i = 0; i < targetSong.length; i++) {
                int noteIdx = targetSong[i];
                int boxX = targetStartX + i * 36;

                boolean isCompleted = i < playedSequence.size();
                boolean isCurrent = i == playedSequence.size();

                int boxBg = isCompleted ? 0xFF00E676 : (isCurrent ? 0xFFFFD740 : 0x40FFFFFF);
                int textColor = isCompleted || isCurrent ? 0xFF000000 : 0xFFFFFFFF;

                gfx.fill(boxX, targetY, boxX + 28, targetY + 20, boxBg);
                if (isCurrent) {
                    gfx.outline(boxX - 2, targetY - 2, 32, 24, 0xFFFFFFFF);
                }

                String label = NOTE_LABELS[noteIdx];
                int labelX = boxX + (28 - font.width(label)) / 2;
                gfx.text(font, label, labelX, targetY + 6, textColor, false);
            }
        }

        int staffBgColor = 0x30000000;
        gfx.fill(staffLeft - 10, staffTop - 10, staffRight + 10, staffBottom + 10, staffBgColor);
        gfx.outline(staffLeft - 10, staffTop - 10, (staffRight - staffLeft) + 20, (staffBottom - staffTop) + 20, 0x60FFFFFF);

        for (int i = 0; i < 5; i++) {
            int lineY = staffTop + i * 14;
            gfx.fill(staffLeft, lineY, staffRight, lineY + 1, 0x80FFFFFF);
        }

        gfx.text(font, "𝄞", staffLeft + 4, staffTop + 10, 0x60FFFFFF, true);

        for (PlayedNoteVisual vis : playedVisuals) {
            int noteColor = NOTE_COLORS[vis.noteIndex][0];
            int alpha = Math.min(255, vis.life * 4);
            int colorWithAlpha = (alpha << 24) | (noteColor & 0x00FFFFFF);

            int nx = (int) vis.x;
            int ny = (int) vis.y;

            // Render musical note symbol centered directly on the staff line
            gfx.text(font, "♪", nx - 3, ny - 4, colorWithAlpha, true);
        }

        if (feedbackTicks > 0) {
            gfx.centeredText(font, feedbackMessage, this.width / 2, staffBottom + 18, 0xFFFF4081);
        }

        int buttonWidth = 44;
        int buttonHeight = 44;
        int buttonY = this.height - 60;

        int texWidth = 256;
        int texHeight = 64;
        int keysBgX = (this.width - texWidth) / 2;
        int keysBgY = buttonY - 10;
        gfx.blit(RenderPipelines.GUI_TEXTURED, BUTTON_BG_TEXTURE,
                keysBgX, keysBgY,
                0.0f, 0.0f,
                texWidth, texHeight,
                texWidth, texHeight);

        for (int i = 0; i < NOTE_COUNT; i++) {
            int btnX = getButtonLeft(i);
            int btnColor = NOTE_COLORS[i][0];

            int animOffset = keyPressAnim[i];
            int currentY = buttonY + animOffset;

            int btnBgAlpha = keyPressed[i] ? 0x90 : 0x40;
            int fillBg = (btnBgAlpha << 24) | (btnColor & 0x00FFFFFF);
            gfx.fill(btnX, currentY, btnX + buttonWidth, currentY + buttonHeight, fillBg);
            gfx.outline(btnX, currentY, buttonWidth, buttonHeight, keyPressed[i] ? 0xFFFFFFFF : 0x80FFFFFF);

            String label = NOTE_LABELS[i];
            int labelX = btnX + (buttonWidth - font.width(label)) / 2;
            gfx.text(font, label, labelX, currentY + 14, 0xFFFFFFFF, true);

            String noteName = NOTE_NAMES[i];
            int nameX = btnX + (buttonWidth - font.width(noteName)) / 2;
            gfx.text(font, noteName, nameX, currentY + 26, btnColor, true);
        }

        for (Particle p : particles) {
            float lifeRatio = (float) p.life / p.maxLife;
            int alpha = (int) (255 * lifeRatio);
            int pColor = (alpha << 24) | (p.color & 0x00FFFFFF);
            int pSize = (int) (3 * lifeRatio);
            gfx.fill((int) p.x - pSize, (int) p.y - pSize,
                    (int) p.x + pSize, (int) p.y + pSize, pColor);
        }

        if (victory) {
            gfx.fill(0, 0, this.width, this.height, 0x70000000);
            gfx.centeredText(font, "✦ ¡MELODÍA RESTAURADA! ✦", this.width / 2, this.height / 2 - 20, 0xFFFFD740);
            gfx.centeredText(font, "Transformando bloque con la música del pasado...", this.width / 2, this.height / 2 + 5, 0xFF69F0AE);
        }

        gfx.text(font, "Presiona ESC para salir", 10, this.height - 15, 0x80FFFFFF, true);
    }

    private void sendResult() {
        if (resultSent) return;
        resultSent = true;

        int scoreValue = isComposerMode ? encodeSong(playedSequence) : (victory && !knownSongs.isEmpty() ? knownSongs.get(currentSongIndex).encodedSongInt() : 0);
        int finalResultId = isComposerMode ? resultBlockId : (!knownSongs.isEmpty() ? knownSongs.get(currentSongIndex).magicResultId() : resultBlockId);

        ClientPlayNetworking.send(new LyreGameResultPayload(
                victory, scoreValue,
                targetX, targetY, targetZ,
                finalResultId
        ));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (!resultSent && gameFinished) {
            sendResult();
        }
        super.onClose();
    }

    private static class PlayedNoteVisual {
        float x, y;
        final int noteIndex;
        int life;

        PlayedNoteVisual(float x, float y, int noteIndex, int life) {
            this.x = x;
            this.y = y;
            this.noteIndex = noteIndex;
            this.life = life;
        }
    }

    private static class Particle {
        float x, y, vx, vy;
        int color;
        int life;
        final int maxLife;

        Particle(float x, float y, float vx, float vy, int color, int life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.life = life;
            this.maxLife = life;
        }
    }
}
