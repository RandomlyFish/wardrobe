package dev.hardaway.wardrobe.api.cosmetic;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import dev.hardaway.wardrobe.impl.asset.CosmeticSlotAsset;

import javax.annotation.Nullable;

public interface WardrobeCosmeticSlot extends WardrobeTab {

    static DefaultAssetMap<String, ? extends WardrobeCosmeticSlot> getAssetMap() {
        return CosmeticSlotAsset.getAssetMap(); // TODO: registry
    }
    WardrobeCategory getCategory();

    @Nullable
    CosmeticType getHytaleCosmeticType();
}
