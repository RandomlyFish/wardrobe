package dev.hardaway.wardrobe.impl.cosmetic.appearance;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;

public class ModelAssetModelAppearance extends ModelAppearance {
    public static final BuilderCodec<ModelAssetModelAppearance> CODEC = BuilderCodec.builder(ModelAssetModelAppearance.class, ModelAssetModelAppearance::new)
            .append(new KeyedCodec<>("Model", Codec.STRING, true),
                    (a, value) -> a.model = value,
                    a -> a.model
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

            .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC),
                    (a, value) -> a.textureConfig = value,
                    a -> a.textureConfig
            )
            .addValidator(Validators.nonNull())
            .metadata(new UIPropertyTitle("Texture Configuration")).documentation("The Texture Configuration for this appearance.")
            .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
            .add()

            .build();
}
