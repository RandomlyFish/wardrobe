package dev.hardaway.wardrobe.api.cosmetic;

import dev.hardaway.wardrobe.api.menu.variant.CosmeticColorEntry;
import dev.hardaway.wardrobe.api.menu.variant.CosmeticVariantEntry;
import dev.hardaway.wardrobe.api.property.WardrobeProperties;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface WardrobeCosmetic extends Cosmetic {

    WardrobeProperties getProperties();

    String getCosmeticSlotId();

    Map<String, CosmeticVariantEntry> getVariantEntries();

    List<CosmeticColorEntry> getColorEntries(@Nullable String variantId);
}
