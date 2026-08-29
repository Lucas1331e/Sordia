package dasouza.telum.tool;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Defines all possible part types that can be used to assemble modular tools.
 * Each part type occupies a specific texture layer when rendered.
 */
public enum PartType implements StringRepresentable {
    // Structural parts (layer 0)
    HANDLE("handle", 0),       // Palo — used by Pickaxe, Axe, Shovel, Hoe
    GRIP("grip", 0),           // Mango — used by Sword

    // Core part (layer 1)
    EYE("eye", 1),             // Ojo/Centro — used by ALL tools

    // Head/Blade parts (layer 2)
    HEAD("head", 2),           // Cabeza — used by Pickaxe, Axe, Shovel, Hoe
    BLADE("blade", 2);         // Filo — used by Sword

    public static final Codec<PartType> CODEC = StringRepresentable.fromEnum(PartType::values);

    private final String name;
    private final int textureLayer;

    PartType(String name, int textureLayer) {
        this.name = name;
        this.textureLayer = textureLayer;
    }

    public String getPartName() {
        return name;
    }

    public int getTextureLayer() {
        return textureLayer;
    }

    public String getTexturePath(PartMaterial material) {
        return "telum:item/part/" + name + "_" + material.getMaterialName();
    }

    public String getTranslationKey() {
        return "part.telum." + name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
