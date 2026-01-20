package dev.hardaway.wardrobe.api.cosmetic.asset;

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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import dev.hardaway.wardrobe.WardrobePlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CosmeticGroup implements JsonAssetWithMap<String, DefaultAssetMap<String, CosmeticGroup>> {

    public static final AssetCodec<String, CosmeticGroup> CODEC = AssetBuilderCodec
            .builder(CosmeticGroup.class, CosmeticGroup::new,
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

            .append(new KeyedCodec<>("CosmeticType", new EnumCodec<>(CosmeticType.class)),
                    (t, value) -> t.cosmeticType = value,
                    t -> t.cosmeticType
            ).add()


            .append(new KeyedCodec<>("Category", Codec.STRING, true),
                    (t, value) -> t.category = value,
                    t -> t.category
            ).addValidator(Validators.nonEmptyString()).add()


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


    public static final Supplier<AssetStore<String, CosmeticGroup, DefaultAssetMap<String, CosmeticGroup>>> ASSET_STORE = WardrobePlugin.createAssetStore(CosmeticGroup.class);

    public static DefaultAssetMap<String, CosmeticGroup> getAssetMap() {
        return ASSET_STORE.get().getAssetMap();
    }

    private String id;
    private AssetExtraInfo.Data data;

    protected String nameKey;

    private CosmeticType cosmeticType;

    private String category;
    private String icon;
    private String selectedIcon;
    private int order = -1;

    @Override
    public String getId() {
        return id;
    }

    @Nonnull
    public String getTranslationKey() {
        if (this.nameKey != null) {
            return nameKey;
        }

        return "wardrobe.groups." + this.id + ".name";
    }

    public Message getName() {
        return Message.translation(this.getTranslationKey());
    }

    @Nullable
    public CosmeticType getCosmeticType() {
        return cosmeticType;
    }

    public String getCategory() {
        return category;
    }

    public String getIcon() {
        return icon;
    }

    public String getSelectedIcon() {
        return selectedIcon;
    }

    public int getOrder() {
        return order;
    }
}
