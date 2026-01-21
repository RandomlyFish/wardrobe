package dev.hardaway.wardrobe.api.cosmetic;

import com.hypixel.hytale.server.core.cosmetics.CosmeticType;

import javax.annotation.Nullable;

public interface WardrobeGroup extends WardrobeTab {

    default String getTranslationKey() {
        return "wardrobe.groups." + this.getId() + ".name";
    }

    WardrobeCategory getCategory();

    @Nullable
    CosmeticType getHytaleCosmeticType();
}
