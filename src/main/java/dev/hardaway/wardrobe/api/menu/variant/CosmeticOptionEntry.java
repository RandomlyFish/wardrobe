package dev.hardaway.wardrobe.api.menu.variant;

import dev.hardaway.wardrobe.api.property.WardrobeProperties;

import javax.annotation.Nullable;

public record CosmeticOptionEntry(
        String id,
        WardrobeProperties properties,
        @Nullable String icon
){
}
