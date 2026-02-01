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
import dev.hardaway.wardrobe.api.property.WardrobeProperties;

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

            .append(new KeyedCodec<>("Properties", WardrobeProperties.CODEC),
                    (t, value) -> t.properties = value,
                    t -> t.properties
            ).add()

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

    private WardrobeProperties properties;

    private String selectedIcon;
    private int order = -1;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public WardrobeProperties getProperties() {
        return properties;
    }

    @Override
    public String getSelectedIconPath() {
        return selectedIcon;
    }

    @Override
    public int getTabOrder() {
        return order;
    }
}
