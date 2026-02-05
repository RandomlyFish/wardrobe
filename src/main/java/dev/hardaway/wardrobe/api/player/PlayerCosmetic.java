package dev.hardaway.wardrobe.api.player;

import javax.annotation.Nullable;

public interface PlayerCosmetic {
    String getCosmeticId();

    @Nullable
    String getOptionId();

    @Nullable
    String getVariantId();
}
