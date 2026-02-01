package dev.hardaway.wardrobe.api.menu.variant;

import dev.hardaway.wardrobe.api.property.WardrobeProperties;

public record CosmeticVariantEntry(
        String id,
        WardrobeProperties properties,
        String[] colors
) {
}
