package dev.hardaway.wardrobe.api.cosmetic;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import dev.hardaway.wardrobe.api.WardrobePermissionHolder;
import dev.hardaway.wardrobe.api.WardrobeTranslatable;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.impl.asset.cosmetic.CosmeticAsset;

public interface WardrobeCosmetic extends WardrobePermissionHolder, WardrobeTranslatable {

    static DefaultAssetMap<String, ? extends WardrobeCosmetic> getAssetMap() {
        return CosmeticAsset.getAssetMap(); // TODO: registry
    }

    String getId();

    WardrobeVisibility getWardrobeVisibility();

    String getCosmeticSlotId();

    String[] getRequiredCosmeticIds();

    String[] getHiddenCosmeticSlotIds();

    String getIconPath();

    void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic);
}
