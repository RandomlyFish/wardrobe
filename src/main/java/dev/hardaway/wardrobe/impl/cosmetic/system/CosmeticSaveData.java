package dev.hardaway.wardrobe.impl.cosmetic.system;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.impl.cosmetic.asset.CosmeticAsset;

import javax.annotation.Nullable;

public final class CosmeticSaveData implements PlayerCosmetic {

    public static final BuilderCodec<CosmeticSaveData> CODEC = BuilderCodec.builder(CosmeticSaveData.class, CosmeticSaveData::new)
            .append(new KeyedCodec<>("Id", Codec.STRING, true), (t, value) -> t.id = value, t -> t.id).add()
            .append(new KeyedCodec<>("Variant", Codec.STRING), (t, value) -> t.variantId = value, t -> t.variantId).add()
            .build();

    private String id;
    @Nullable
    private String variantId;

    public CosmeticSaveData() {
    }

    public CosmeticSaveData(String id) {
        this(id, null);
    }

    public CosmeticSaveData(String id, @Nullable String variantId) {
        this.id = id;
        this.variantId = variantId;
    }

    public WardrobeCosmetic getCosmetic() {
        WardrobeCosmetic cosmetic = CosmeticAsset.getAssetMap().getAsset(this.id);
        if (cosmetic == null) {
            throw new IllegalStateException("Cosmetic not found: " + this.id);
        }
        return cosmetic;
    }

    @Nullable
    public String getTextureId() {
        return variantId;
    }

}
