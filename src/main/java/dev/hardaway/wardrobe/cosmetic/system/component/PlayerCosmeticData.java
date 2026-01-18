package dev.hardaway.wardrobe.cosmetic.system.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nullable;

public final class PlayerCosmeticData {

    public static final BuilderCodec<PlayerCosmeticData> CODEC = BuilderCodec.builder(PlayerCosmeticData.class, PlayerCosmeticData::new)
            .append(new KeyedCodec<>("Id", Codec.STRING, true), (t, value) -> t.id = value, t -> t.id).add()
            .append(new KeyedCodec<>("Variant", Codec.STRING), (t, value) -> t.variantId = value, t -> t.variantId).add()
            .build();

    private String id;
    @Nullable
    private String variantId;

    public PlayerCosmeticData() {
    }

    public PlayerCosmeticData(String id) {
        this(id, null);
    }

    public PlayerCosmeticData(String id, @Nullable String variantId) {
        this.id = id;
        this.variantId = variantId;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public String getVariantId() {
        return variantId;
    }
}
