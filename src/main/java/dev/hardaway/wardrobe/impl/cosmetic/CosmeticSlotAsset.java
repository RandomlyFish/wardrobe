package dev.hardaway.wardrobe.impl.cosmetic;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import dev.hardaway.wardrobe.WardrobePlugin;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.menu.WardrobeCategory;
import dev.hardaway.wardrobe.api.property.WardrobeProperties;
import dev.hardaway.wardrobe.api.property.WardrobeTranslationProperties;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CosmeticSlotAsset implements WardrobeCosmeticSlot, JsonAssetWithMap<String, DefaultAssetMap<String, CosmeticSlotAsset>> {

    public static final AssetCodec<String, CosmeticSlotAsset> CODEC = AssetBuilderCodec
            .builder(CosmeticSlotAsset.class, CosmeticSlotAsset::new,
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

            .append(new KeyedCodec<>("CosmeticType", new EnumCodec<>(CosmeticType.class)),
                    (t, value) -> t.cosmeticType = value,
                    t -> t.cosmeticType
            ).add()

            .append(new KeyedCodec<>("ArmorSlot", new EnumCodec<>(ItemArmorSlot.class)),
                    (t, value) -> t.armorSlot = value,
                    t -> t.armorSlot
            ).add()

            .append(new KeyedCodec<>("Category", Codec.STRING, true),
                    (t, value) -> t.category = value,
                    t -> t.category
            ).addValidator(Validators.nonEmptyString()).add()

            .append(new KeyedCodec<>("SelectedIcon", Codec.STRING, true),
                    (t, value) -> t.selectedIcon = value,
                    t -> t.selectedIcon
            ).addValidator(Validators.nonEmptyString()).add()

            .append(new KeyedCodec<>("Order", Codec.INTEGER),
                    (t, value) -> t.order = value,
                    t -> t.order
            ).add().build();


    public static final Supplier<AssetStore<String, CosmeticSlotAsset, DefaultAssetMap<String, CosmeticSlotAsset>>> ASSET_STORE = WardrobePlugin.createAssetStore(CosmeticSlotAsset.class);

    public static DefaultAssetMap<String, CosmeticSlotAsset> getAssetMap() {
        return ASSET_STORE.get().getAssetMap();
    }

    private String id;
    private AssetExtraInfo.Data data;

    private WardrobeProperties properties;

    private CosmeticType cosmeticType;
    private ItemArmorSlot armorSlot;

    private String category;
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

    @Nullable
    @Override
    public CosmeticType getHytaleCosmeticType() {
        return cosmeticType;
    }

    public WardrobeCategory getCategory() {
        WardrobeCategory category = CosmeticCategoryAsset.getAssetMap().getAsset(this.category);
        if (category == null) {
            throw new IllegalStateException("Category not found: " + this.category);
        }
        return category;
    }

    @Nullable
    @Override
    public ItemArmorSlot getArmorSlot() {
        return armorSlot;
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
