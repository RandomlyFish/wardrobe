package dev.hardaway.wardrobe.impl.cosmetic;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import dev.hardaway.wardrobe.WardrobePlugin;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.menu.WardrobeCategory;
import dev.hardaway.wardrobe.api.property.WardrobeCamera;
import dev.hardaway.wardrobe.api.property.WardrobeProperties;
import dev.hardaway.wardrobe.api.property.validator.WardrobeValidators;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CosmeticSlotAsset implements WardrobeCosmeticSlot, JsonAssetWithMap<String, DefaultAssetMap<String, CosmeticSlotAsset>> {

    public static final AssetBuilderCodec<String, CosmeticSlotAsset> CODEC = AssetBuilderCodec
            .builder(CosmeticSlotAsset.class, CosmeticSlotAsset::new,
                    Codec.STRING,
                    (t, k) -> t.id = k,
                    (t) -> t.id,
                    (asset, data) -> asset.data = data,
                    (asset) -> asset.data
            )

            .append(new KeyedCodec<>("Properties", WardrobeProperties.CODEC, true),
                    (t, value) -> t.properties = value,
                    t -> t.properties
            )
            .metadata(new UIPropertyTitle("Wardrobe Properties")).documentation("Properties for the Cosmetic Category to display in the Wardrobe Menu.")
            .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
            .add()

            .append(new KeyedCodec<>("CosmeticType", new EnumCodec<>(CosmeticType.class)),
                    (t, value) -> t.cosmeticType = value,
                    t -> t.cosmeticType
            )
            .metadata(UIDisplayMode.HIDDEN)
            .add()

            .append(new KeyedCodec<>("ArmorSlot", new EnumCodec<>(ItemArmorSlot.class)),
                    (t, value) -> t.armorSlot = value,
                    t -> t.armorSlot
            )
            .metadata(new UIPropertyTitle("Armor Slot")).documentation("The Armor Slot is used to determine if a Cosmetic in this slot should provide an alternate appearance when the Armor Slot is occupied.")
            .add()

            .append(new KeyedCodec<>("Category", Codec.STRING, true),
                    (t, value) -> t.category = value,
                    t -> t.category
            )
            .addValidator(CosmeticCategoryAsset.VALIDATOR_CACHE.getValidator().late())
            .metadata(new UIPropertyTitle("Cosmetic Category")).documentation("The Cosmetic Category this Cosmetic Slot should appear under in the Wardrobe Menu.")
            .add()

            .append(new KeyedCodec<>("Camera", WardrobeCamera.CODEC),
                    (t, value) -> t.camera = value,
                    t -> t.camera
            )
            .metadata(new UIPropertyTitle("Wardrobe Camera")).documentation("The Camera to use when this slot is selected in the Wardrobe Menu. The camera's position is relative to the player while its rotation to the world.")
            .add()

            .append(new KeyedCodec<>("Icon", Codec.STRING, true),
                    (t, value) -> t.icon = value,
                    t -> t.icon
            )
            .addValidator(Validators.nonNull())
            .addValidator(WardrobeValidators.ICON)
            .metadata(new UIPropertyTitle("Icon")).documentation("The icon to display on the Category button in the Wardrobe Menu.")
            .add()

            .append(new KeyedCodec<>("SelectedIcon", Codec.STRING, true),
                    (t, value) -> t.selectedIcon = value,
                    t -> t.selectedIcon
            )
            .addValidator(WardrobeValidators.ICON)
            .metadata(new UIPropertyTitle("Selected Icon")).documentation("The icon to display on the tab button in the Wardrobe Menu when the tab is selected.")
            .add()

            .append(new KeyedCodec<>("Order", Codec.INTEGER),
                    (t, value) -> t.order = value,
                    t -> t.order
            )
            .metadata(new UIPropertyTitle("Sorting Order")).documentation("The sorting order of this tab. Tabs are sorted by the sorting order with 0 at the top.")
            .add()
            .afterDecode(asset -> {
                if (asset.getIconPath() == null && asset.properties != null && asset.properties.getIcon() != null) { // DEPRECATED
                    asset.icon = asset.properties.getIcon();
                }
            })
            .build();

    public static final Codec<String> CHILD_ASSET = new ContainedAssetCodec<>(CosmeticSlotAsset.class, CODEC);

    public static final Supplier<AssetStore<String, CosmeticSlotAsset, DefaultAssetMap<String, CosmeticSlotAsset>>> ASSET_STORE = WardrobePlugin.createAssetStore(CosmeticSlotAsset.class);
    public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache(new AssetKeyValidator(CosmeticSlotAsset.ASSET_STORE));

    public static DefaultAssetMap<String, CosmeticSlotAsset> getAssetMap() {
        return ASSET_STORE.get().getAssetMap();
    }

    private String id;
    private AssetExtraInfo.Data data;

    private WardrobeProperties properties;

    private CosmeticType cosmeticType;
    private ItemArmorSlot armorSlot;

    private WardrobeCamera camera;
    private String category;
    private String icon;
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

    @Nullable
    @Override
    public ItemArmorSlot getArmorSlot() {
        return armorSlot;
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
    public WardrobeCamera getCamera() {
        return camera;
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
