package dev.hardaway.wardrobe.api.cosmetic.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.Message;
import dev.hardaway.wardrobe.WardrobePlugin;
import dev.hardaway.wardrobe.api.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.PlayerCosmetic;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class CosmeticAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, CosmeticAsset>> {

    public static final BuilderCodec<CosmeticAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(CosmeticAsset.class)
            .append(new KeyedCodec<>("NameKey", Codec.STRING),
                    (t, value) -> t.nameKey = value,
                    t -> t.nameKey
            ).add()
            .append(new KeyedCodec<>("Group", Codec.STRING),
                    (t, value) -> t.group = value,
                    t -> t.group
            ).addValidator(Validators.nonEmptyString()).add()
            .append(new KeyedCodec<>("Icon", Codec.STRING),
                    (t, value) -> t.icon = value,
                    t -> t.icon
            ).add()
            .build();

    public static final AssetCodecMapCodec<String, CosmeticAsset> CODEC = new AssetCodecMapCodec<>(
            Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data, true
    );

    public static final Supplier<AssetStore<String, CosmeticAsset, DefaultAssetMap<String, CosmeticAsset>>> ASSET_STORE = WardrobePlugin.createAssetStore(CosmeticAsset.class);

    public static DefaultAssetMap<String, CosmeticAsset> getAssetMap() {
        return ASSET_STORE.get().getAssetMap();
    }

    protected String id;
    protected AssetExtraInfo.Data data;

    protected String nameKey;
    protected String group;
    protected String icon;

    protected CosmeticAsset() {
    }

    public CosmeticAsset(String id, String nameKey, String group, String icon) {
        this.id = id;
        this.nameKey = nameKey;
        this.group = group;
        this.icon = icon;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nonnull
    public String getTranslationKey() {
        if (this.nameKey != null) {
            return nameKey;
        }

        return "wardrobe.cosmetics." + this.id + ".name";
    }

    public Message getName() {
        return Message.translation(this.getTranslationKey());
    }

    @Nonnull
    public String getGroup() {
        return group;
    }


    public String getIcon() {
        return icon;
    }

    public abstract void applyCosmetic(WardrobeContext context, PlayerCosmetic playerCosmetic);
}