package dasouza.telum.item;

import net.minecraft.world.item.Item;

/**
 * Piece of Sordia item obtained from archaeology (suspicious sand & gravel).
 * Must be cleaned on the Archaeology Table.
 */
public class PieceOfSordiaItem extends Item {

    public PieceOfSordiaItem(Properties properties) {
        super(properties.stacksTo(64));
    }
}
