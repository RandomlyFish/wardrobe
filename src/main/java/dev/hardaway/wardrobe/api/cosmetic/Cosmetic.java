package dev.hardaway.wardrobe.api.cosmetic;

import dev.hardaway.wardrobe.api.player.PlayerCosmetic;

public interface Cosmetic {

    String getId();

    void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic);

}
