package dev.hardaway.wardrobe.api.cosmetic;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import dev.hardaway.wardrobe.impl.asset.CosmeticCategoryAsset;

public interface WardrobeCategory extends WardrobeTab {

    static DefaultAssetMap<String, ? extends WardrobeCategory> getAssetMap() {
        return CosmeticCategoryAsset.getAssetMap(); // TODO: registry
    }
}