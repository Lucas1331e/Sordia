package dasouza.telum.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.tool.PartType;

/**
 * Data component stored on tool part items.
 * Identifies what type of part it is and its material.
 */
public record ToolPartData(PartType partType, PartMaterial material) {

    public static final Codec<ToolPartData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    PartType.CODEC.fieldOf("part_type").forGetter(ToolPartData::partType),
                    PartMaterial.CODEC.fieldOf("material").forGetter(ToolPartData::material)
            ).apply(instance, ToolPartData::new)
    );

    /**
     * Returns a unique identifier string for this part combination.
     * Example: "handle_wood"
     */
    public String getId() {
        return partType.getPartName() + "_" + material.getMaterialName();
    }

    /**
     * Returns the texture path for this part.
     */
    public String getTexturePath() {
        return partType.getTexturePath(material);
    }
}
