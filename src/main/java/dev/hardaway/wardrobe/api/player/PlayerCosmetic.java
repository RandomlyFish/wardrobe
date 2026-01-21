package dev.hardaway.wardrobe.api.player;

import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;

public interface PlayerCosmetic {
    WardrobeCosmetic getCosmetic();

    String getTextureId();

    default boolean hasTextureId() {
        return this.getTextureId() != null && !this.getTextureId().isBlank();
    }
}
