package dev.hardaway.wardrobe.impl.cosmetic;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.Validators;
import dev.hardaway.wardrobe.WardrobePlugin;
import dev.hardaway.wardrobe.api.menu.WardrobeCategory;
import dev.hardaway.wardrobe.api.property.WardrobeTranslationProperties;

import java.util.function.Supplier;

public class CosmeticCategoryAsset implements WardrobeCategory, JsonAssetWithMap<String, DefaultAssetMap<String, CosmeticCategoryAsset>> {

    public static final AssetCodec<String, CosmeticCategoryAsset> CODEC = AssetBuilderCodec
            .builder(CosmeticCategoryAsset.class, CosmeticCategoryAsset::new,
                    Codec.STRING,
                    (t, k) -> t.id = k,
                    (t) -> t.id,
                    (asset, data) -> asset.data = data,
                    (asset) -> asset.data
            )

            .append(new KeyedCodec<>("TranslationProperties", WardrobeTranslationProperties.CODEC),
                    (t, value) -> t.translationProperties = value,
                    t -> t.translationProperties
            ).add()

            .append(new KeyedCodec<>("Icon", Codec.STRING, true),
                    (t, value) -> t.icon = value,
                    t -> t.icon
            ).addValidator(Validators.nonEmptyString()).add()

            .append(new KeyedCodec<>("SelectedIcon", Codec.STRING, true),
                    (t, value) -> t.selectedIcon = value,
                    t -> t.selectedIcon
            ).addValidator(Validators.nonEmptyString()).add()

            .append(new KeyedCodec<>("Order", Codec.INTEGER),
                    (t, value) -> t.order = value,
                    t -> t.order
            ).add().build();


    public static final Supplier<AssetStore<String, CosmeticCategoryAsset, DefaultAssetMap<String, CosmeticCategoryAsset>>> ASSET_STORE = WardrobePlugin.createAssetStore(CosmeticCategoryAsset.class);

    public static DefaultAssetMap<String, CosmeticCategoryAsset> getAssetMap() {
        return ASSET_STORE.get().getAssetMap();
    }


    private String id;
    private AssetExtraInfo.Data data;

    private WardrobeTranslationProperties translationProperties;

    private String icon;
    private String selectedIcon;
    private int order = -1;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public WardrobeTranslationProperties getTranslationProperties() {
        return translationProperties;
    }

    @Override
    public String getIconPath() {
        return icon;
    }

    @Override
    public String getSelectedIconPath() {
        return selectedIcon;
    }

    @Override
    public int getTabOrder() {
        return order;
    }

    @Override
    public String getPermissionNode() {
        return "";
    }
}
