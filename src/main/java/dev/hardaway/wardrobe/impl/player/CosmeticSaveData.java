package dev.hardaway.wardrobe.impl.player;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;

import javax.annotation.Nullable;

// TODO: redo this this sucks
public final class CosmeticSaveData implements PlayerCosmetic {

    public static final BuilderCodec<CosmeticSaveData> CODEC = BuilderCodec.builder(CosmeticSaveData.class, CosmeticSaveData::new)
            .append(new KeyedCodec<>("Id", Codec.STRING, true), (t, value) -> t.id = value, t -> t.id).add()
            .append(new KeyedCodec<>("Variant", Codec.STRING), (t, value) -> t.variantId = value, t -> t.variantId).add()
            .append(new KeyedCodec<>("Texture", Codec.STRING), (t, value) -> t.textureId = value, t -> t.textureId).add()
            .build();

    private String id;
    @Nullable
    private String variantId;
    @Nullable
    private String textureId;

    public CosmeticSaveData() {
    }

    public CosmeticSaveData(String id) {
        this(id, null, null);
    }

    public CosmeticSaveData(String id, String variantId, String textureId) {
        this.id = id;
        this.variantId = variantId;
        this.textureId = textureId;
    }

    public String getCosmeticId() {
        return id;
    }

    public String getVariantId() {
        return variantId;
    }

    public String getTextureId() {
        return textureId;
    }

}
