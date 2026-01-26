package dev.hardaway.wardrobe.impl.asset.cosmetic.builtin;

import com.hypixel.hytale.server.core.cosmetics.PlayerSkin;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;

import javax.annotation.Nullable;

public class HytalePlayerCosmetic implements PlayerCosmetic {

    private final String cosmeticId;
    private final String variantId;
    private final String textureId;

    public HytalePlayerCosmetic(PlayerSkin.PlayerSkinPartId partId) {
        this.cosmeticId = partId.getAssetId();
        this.variantId = partId.getVariantId();
        this.textureId = partId.getTextureId();
    }

    @Override
    public String getCosmeticId() {
        return cosmeticId;
    }

    @Nullable
    public String getVariantId() {
        return variantId;
    }

    @Override
    public String getTextureId() {
        return textureId;
    }
}
