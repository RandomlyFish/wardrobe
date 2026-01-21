package dev.hardaway.wardrobe.impl.asset.cosmetic;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import dev.hardaway.wardrobe.WardrobePlugin;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeGroup;
import dev.hardaway.wardrobe.impl.asset.CosmeticGroupAsset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

// RequiredCosmetics[] - array of cosmetics that must be worn for this cosmetic to appear wearable
public abstract class CosmeticAsset implements WardrobeCosmetic, JsonAssetWithMap<String, DefaultAssetMap<String, CosmeticAsset>> {

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
            .append(new KeyedCodec<>("PermissionNode", Codec.STRING),
                    (t, value) -> t.permissionNode = value,
                    t -> t.permissionNode
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
    protected String permissionNode;

    protected CosmeticAsset() {
    }

    public CosmeticAsset(String id, String nameKey, String group, String icon, String permissionNode) {
        this.id = id;
        this.nameKey = nameKey;
        this.group = group;
        this.icon = icon;
        this.permissionNode = permissionNode;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTranslationKey() {
        if (this.nameKey != null) {
            return nameKey;
        }

        return WardrobeCosmetic.super.getTranslationKey();
    }

    @Nonnull
    public WardrobeGroup getGroup() {
        WardrobeGroup group = CosmeticGroupAsset.getAssetMap().getAsset(this.group);
        if (group == null) {
            throw new IllegalStateException("Group not found: " + this.group);
        }
        return group;
    }

    @Override
    public String getIconPath() {
        return icon;
    }

    @Nullable
    public String getPermissionNode() {
        return permissionNode;
    }
}