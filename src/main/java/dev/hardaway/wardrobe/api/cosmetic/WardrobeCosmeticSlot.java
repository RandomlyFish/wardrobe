package dev.hardaway.wardrobe.api.cosmetic;

import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import dev.hardaway.wardrobe.api.menu.WardrobeCategory;
import dev.hardaway.wardrobe.api.menu.WardrobeTab;

import javax.annotation.Nullable;

public interface WardrobeCosmeticSlot extends WardrobeTab {

    WardrobeCategory getCategory();

    @Nullable
    ItemArmorSlot getArmorSlot();

    @Nullable
    CosmeticType getHytaleCosmeticType();
}
