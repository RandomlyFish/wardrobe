package dev.hardaway.wardrobe.impl.cosmetic;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import dev.hardaway.wardrobe.WardrobePlugin;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.property.WardrobeProperties;
import dev.hardaway.wardrobe.api.property.WardrobeVisibility;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.property.WardrobeTranslationProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class CosmeticAsset implements WardrobeCosmetic, JsonAssetWithMap<String, DefaultAssetMap<String, CosmeticAsset>> {

    public static final BuilderCodec<CosmeticAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(CosmeticAsset.class)
            .append(new KeyedCodec<>("Properties", Codec.STRING, true),
                    (t, value) -> t.cosmeticSlotId = value,
                    t -> t.cosmeticSlotId
            ).add()

            .append(new KeyedCodec<>("CosmeticSlot", Codec.STRING, true),
                    (t, value) -> t.cosmeticSlotId = value,
                    t -> t.cosmeticSlotId
            ).add()
            .append(new KeyedCodec<>("HiddenCosmeticSlots", Codec.STRING_ARRAY),
                    (t, value) -> t.hiddenCosmeticSlots = value,
                    t -> t.hiddenCosmeticSlots
            ).add()

            .build();

    public static final AssetCodecMapCodec<String, CosmeticAsset> CODEC = new AssetCodecMapCodec<>(
            Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data, true
    );

    public static final Supplier<AssetStore<String, CosmeticAsset, DefaultAssetMap<String, CosmeticAsset>>> ASSET_STORE = WardrobePlugin.createAssetStore(CosmeticAsset.class);

    public static DefaultAssetMap<String, CosmeticAsset> getAssetMap() {
        return ASSET_STORE.get().getAssetMap();
    }

    private String id;
    private AssetExtraInfo.Data data;

    private String cosmeticSlotId;
    private String[] hiddenCosmeticSlots = new String[0];
    private WardrobeProperties properties;

    protected CosmeticAsset() {
    }

    public CosmeticAsset(String id, String cosmeticSlotId, String[] hiddenCosmeticSlots, WardrobeProperties properties) {
        this.id = id;
        this.cosmeticSlotId = cosmeticSlotId;
        this.hiddenCosmeticSlots = hiddenCosmeticSlots;
        this.properties = properties;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public WardrobeProperties getProperties() {
        return properties;
    }

    @Nonnull
    public String getCosmeticSlotId() {
        return cosmeticSlotId;
    }

    public String[] getHiddenCosmeticSlotIds() {
        return hiddenCosmeticSlots;
    }

    @Override
    public void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic) {
        context.hideSlots(this.getHiddenCosmeticSlotIds());
    }
}