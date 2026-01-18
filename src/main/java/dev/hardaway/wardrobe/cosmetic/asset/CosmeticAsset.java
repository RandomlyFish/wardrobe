package dev.hardaway.wardrobe.cosmetic.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import dev.hardaway.wardrobe.WardrobeUtil;
import dev.hardaway.wardrobe.cosmetic.asset.config.TextureConfig;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class CosmeticAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, CosmeticAsset>> {

    public static final AssetCodec<String, CosmeticAsset> CODEC = AssetBuilderCodec
            .builder(CosmeticAsset.class, CosmeticAsset::new,
                    Codec.STRING,
                    (t, k) -> t.id = k,
                    (t) -> t.id,
                    (asset, data) -> asset.data = data,
                    (asset) -> asset.data
            )
            .append(new KeyedCodec<>("Group", Codec.STRING),
                    (t, value) -> t.group = value,
                    t -> t.group
            ).addValidator(Validators.nonEmptyString()).add()

            .append(new KeyedCodec<>("Model", Codec.STRING, true),
                    (t, value) -> t.model = value,
                    t -> t.model
            ).addValidator(CommonAssetValidator.MODEL_CHARACTER_ATTACHMENT).add()

            .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC, true),
                    (t, value) -> t.textureConfig = value,
                    t -> t.textureConfig
            ).add()

            .append(new KeyedCodec<>("Icon", Codec.STRING),
                    (t, value) -> t.icon = value,
                    t -> t.icon
            ).add()
            .build();

    public static final Supplier<AssetStore<String, CosmeticAsset, DefaultAssetMap<String, CosmeticAsset>>> ASSET_STORE = WardrobeUtil.createAssetStore(CosmeticAsset.class);

    public static DefaultAssetMap<String, CosmeticAsset> getAssetMap() {
        return ASSET_STORE.get().getAssetMap();
    }

    protected String id;
    protected AssetExtraInfo.Data data;

    protected String group;
    protected String model;
    protected TextureConfig textureConfig;
    protected String icon;

    @Override
    public String getId() {
        return id;
    }

    @Nonnull
    public String getGroup() {
        return group;
    }

    @Nonnull
    public String getModel() {
        return model;
    }

    @Nonnull
    public TextureConfig getTextureConfig() {
        return textureConfig;
    }

    public String getIcon() {
        return icon;
    }
}