package dev.hardaway.wardrobe.impl.cosmetic.asset;

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
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCategory;

import java.util.function.Supplier;

public class CosmeticCategory implements WardrobeCategory, JsonAssetWithMap<String, DefaultAssetMap<String, CosmeticCategory>> {

    public static final AssetCodec<String, CosmeticCategory> CODEC = AssetBuilderCodec
            .builder(CosmeticCategory.class, CosmeticCategory::new,
                    Codec.STRING,
                    (t, k) -> t.id = k,
                    (t) -> t.id,
                    (asset, data) -> asset.data = data,
                    (asset) -> asset.data
            )

            .append(new KeyedCodec<>("NameKey", Codec.STRING),
                    (t, value) -> t.nameKey = value,
                    t -> t.nameKey
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


    public static final Supplier<AssetStore<String, CosmeticCategory, DefaultAssetMap<String, CosmeticCategory>>> ASSET_STORE = WardrobePlugin.createAssetStore(CosmeticCategory.class);

    public static DefaultAssetMap<String, CosmeticCategory> getAssetMap() {
        return ASSET_STORE.get().getAssetMap();
    }


    private String id;
    private AssetExtraInfo.Data data;

    protected String nameKey;

    private String icon;
    private String selectedIcon;
    private int order = -1;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTranslationKey() {
        if (this.nameKey != null) {
            return nameKey;
        }

        return WardrobeCategory.super.getTranslationKey();
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
}
