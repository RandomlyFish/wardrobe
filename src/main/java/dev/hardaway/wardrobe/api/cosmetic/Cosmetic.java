package dev.hardaway.wardrobe.api.cosmetic;

import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.property.WardrobePermissionHolder;

public interface Cosmetic extends WardrobePermissionHolder {

    String getId();

    void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic);

}
