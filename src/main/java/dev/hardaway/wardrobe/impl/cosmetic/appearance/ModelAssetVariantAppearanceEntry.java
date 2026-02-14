package dev.hardaway.wardrobe.impl.cosmetic.appearance;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.property.WardrobeProperties;
import dev.hardaway.wardrobe.api.property.validator.WardrobeValidators;

public class ModelAssetVariantAppearanceEntry extends VariantAppearanceEntry {
    public static final BuilderCodec<ModelAssetVariantAppearanceEntry> CODEC = BuilderCodec.builder(ModelAssetVariantAppearanceEntry.class, ModelAssetVariantAppearanceEntry::new)
            .append(new KeyedCodec<>("Properties", WardrobeProperties.CODEC, true),
                    (t, value) -> t.properties = value,
                    t -> t.properties
            )
            .addValidator(Validators.nonNull())
            .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
            .add()

            .append(new KeyedCodec<>("Icon", Codec.STRING),
                    (t, value) -> t.icon = value,
                    t -> t.icon
            )
            .addValidator(WardrobeValidators.ICON)
            .metadata(new UIPropertyTitle("Icon")).documentation("A preview icon of the Cosmetic with this Variant applied to display in the Wardrobe Menu.")
            .add()

            .append(new KeyedCodec<>("Model", Codec.STRING, true),
                    (t, value) -> t.model = value, t -> t.model
            )
            .addValidator(ModelAsset.VALIDATOR_CACHE.getValidator().late())
            .addValidator(Validators.nonNull())
            .metadata(new UIPropertyTitle("Model")).documentation("The model asset to use for the Player Model.")
            .add()

            .append(new KeyedCodec<>("Scale", Codec.FLOAT),
                    (a, d) -> a.scale = d,
                    (a) -> a.scale
            )
            .metadata(new UIPropertyTitle("Player Model Scale")).documentation("The scale to use for the Player Model.")
            .add()

            .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC, true),
                    (t, value) -> t.textureConfig = value, t -> t.textureConfig
            )
            .addValidator(Validators.nonNull())
            .metadata(new UIPropertyTitle("Texture Configuration")).documentation("The Texture Configuration for this appearance.")
            .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
            .add()
            .afterDecode(asset -> {
                if (asset.getIcon() == null && asset.properties != null && asset.properties.getIcon() != null) { // DEPRECATED
                    asset.icon = asset.properties.getIcon();
                }
            })
            .build();
}
