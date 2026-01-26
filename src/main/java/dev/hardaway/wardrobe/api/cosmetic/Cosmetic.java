package dev.hardaway.wardrobe.api.cosmetic;

import dev.hardaway.wardrobe.api.WardrobePermissionHolder;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;

public interface Cosmetic extends WardrobePermissionHolder {

    String getId();

    String getCosmeticSlotId();

    void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic);

}
