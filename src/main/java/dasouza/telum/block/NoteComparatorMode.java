package dasouza.telum.block;

import net.minecraft.util.StringRepresentable;

public enum NoteComparatorMode implements StringRepresentable {
    STRICT("strict"),
    NOTE_ONLY("note_only");

    private final String name;

    NoteComparatorMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
