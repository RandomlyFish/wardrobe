package dev.hardaway.wardrobe.api.cosmetic;

import dev.hardaway.wardrobe.api.WardrobePermissionHolder;
import dev.hardaway.wardrobe.api.WardrobeTranslatable;

import javax.annotation.Nullable;

public interface WardrobeCosmetic extends Cosmetic, WardrobeTranslatable {

    WardrobeVisibility getWardrobeVisibility();

    String[] getRequiredCosmeticIds();

    @Nullable
    String getIconPath();

//    void createWardrobeEntry();
}
