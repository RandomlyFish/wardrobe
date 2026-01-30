package dev.hardaway.wardrobe.api.cosmetic;

import dev.hardaway.wardrobe.api.menu.WardrobeVisibility;
import dev.hardaway.wardrobe.api.menu.variant.CosmeticColorEntry;
import dev.hardaway.wardrobe.api.menu.variant.CosmeticVariantEntry;
import dev.hardaway.wardrobe.api.property.WardrobeTranslatable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface WardrobeCosmetic extends Cosmetic, WardrobeTranslatable {

    WardrobeVisibility getWardrobeVisibility();

    String getCosmeticSlotId();

    String[] getRequiredCosmeticIds();

    @Nullable
    String getIconPath();

    Map<String, CosmeticVariantEntry> getVariantEntries();

    List<CosmeticColorEntry> getColorEntries(@Nullable String variantId);
}
