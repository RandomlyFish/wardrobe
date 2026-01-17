package dev.hardaway.wardrobe.cosmetic;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nullable;

public final class WardrobeCosmeticData {

    public static final BuilderCodec<WardrobeCosmeticData> CODEC = BuilderCodec.builder(WardrobeCosmeticData.class, WardrobeCosmeticData::new)
            .append(new KeyedCodec<>("Id", Codec.STRING, true), (t, value) -> t.id = value, t -> t.id).add()
            .append(new KeyedCodec<>("Variant", Codec.STRING), (t, value) -> t.variantId = value, t -> t.variantId).add()
            .build();

    private String id;
    @Nullable
    private String variantId;

    public WardrobeCosmeticData() {
    }

    public WardrobeCosmeticData(String id) {
        this(id, null);
    }

    public WardrobeCosmeticData(String id, @Nullable String variantId) {
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
