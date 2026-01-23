package dev.hardaway.wardrobe.impl.asset.cosmetic;

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
import dev.hardaway.wardrobe.api.WardrobeTranslationProperties;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeVisibility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class CosmeticAsset implements WardrobeCosmetic, JsonAssetWithMap<String, DefaultAssetMap<String, CosmeticAsset>> {

    public static final BuilderCodec<CosmeticAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(CosmeticAsset.class)
            .append(new KeyedCodec<>("TranslationProperties", WardrobeTranslationProperties.CODEC, true),
                    (t, value) -> t.translationProperties = value,
                    t -> t.translationProperties
            ).add()
            .append(new KeyedCodec<>("CosmeticSlot", Codec.STRING, true),
                    (t, value) -> t.cosmeticSlotId = value,
                    t -> t.cosmeticSlotId
            ).add()

            .append(new KeyedCodec<>("WardrobeVisibility", new EnumCodec<>(WardrobeVisibility.class)),
                    (t, value) -> t.wardrobeVisibility = value,
                    t -> t.wardrobeVisibility
            ).add()

            .append(new KeyedCodec<>("Icon", Codec.STRING),
                    (t, value) -> t.iconPath = value,
                    t -> t.iconPath
            ).add()

            .append(new KeyedCodec<>("RequiresPermission", Codec.STRING),
                    (t, value) -> t.permissionNode = value,
                    t -> t.permissionNode
            ).add()

            .append(new KeyedCodec<>("RequiredCosmetics", Codec.STRING_ARRAY),
                    (t, value) -> t.requiredCosmetics = value,
                    t -> t.requiredCosmetics
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

    private WardrobeTranslationProperties translationProperties;
    private WardrobeVisibility wardrobeVisibility = WardrobeVisibility.ALWAYS;
    private String cosmeticSlotId;
    private String iconPath;
    private String permissionNode = "";
    private String[] requiredCosmetics = new String[0];
    private String[] hiddenCosmeticSlots = new String[0];

    protected CosmeticAsset() {
    }

    public CosmeticAsset(String id, WardrobeTranslationProperties translationProperties, WardrobeVisibility wardrobeVisibility, String cosmeticSlotId, String iconPath, String permissionNode) {
        this.id = id;
        this.translationProperties = translationProperties;
        this.wardrobeVisibility = wardrobeVisibility;
        this.cosmeticSlotId = cosmeticSlotId;
        this.iconPath = iconPath;
        this.permissionNode = permissionNode;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public WardrobeTranslationProperties getTranslationProperties() {
        return translationProperties;
    }

    @Override
    public WardrobeVisibility getWardrobeVisibility() {
        return wardrobeVisibility;
    }

    @Nonnull
    public String getCosmeticSlotId() {
        return cosmeticSlotId;
    }

    @Override
    public String[] getRequiredCosmeticIds() {
        return requiredCosmetics;
    }

    @Override
    public String[] getHiddenCosmeticSlotIds() {
        return hiddenCosmeticSlots;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    @Nullable
    public String getPermissionNode() {
        return permissionNode;
    }
}