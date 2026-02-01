package dev.hardaway.wardrobe.api.menu.variant;

import dev.hardaway.wardrobe.api.property.WardrobeProperties;

public record CosmeticColorEntry(
        String id,
        WardrobeProperties properties,
        String[] colors
) {
}
