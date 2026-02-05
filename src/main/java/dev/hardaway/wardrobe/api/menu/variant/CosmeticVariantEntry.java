package dev.hardaway.wardrobe.api.menu.variant;

import dev.hardaway.wardrobe.api.property.WardrobeProperties;

import javax.annotation.Nullable;

public record CosmeticVariantEntry(
        String id,
        WardrobeProperties properties,
        String[] colors,
        @Nullable String icon
) {
}
