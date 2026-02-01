package dev.hardaway.wardrobe.api.menu.variant;

import dev.hardaway.wardrobe.api.property.WardrobeProperties;
import dev.hardaway.wardrobe.api.property.WardrobeTranslationProperties;

import javax.annotation.Nullable;

public record CosmeticVariantEntry(
        String id,
        WardrobeProperties properties
) {
}
