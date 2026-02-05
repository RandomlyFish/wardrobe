package dev.hardaway.wardrobe.impl.cosmetic.appearance;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import dev.hardaway.wardrobe.api.cosmetic.appearance.Appearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.property.validator.WardrobeValidators;

import javax.annotation.Nullable;

public class ModelAppearance implements Appearance {

    public static final BuilderCodec<ModelAppearance> CODEC = BuilderCodec.builder(ModelAppearance.class, ModelAppearance::new)
            .append(new KeyedCodec<>("Model", Codec.STRING),
                    (a, value) -> a.model = value,
                    a -> a.model
            )
            .addValidator(WardrobeValidators.APPEARANCE_MODEL)
            .add()

            .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC),
                    (a, value) -> a.textureConfig = value,
                    a -> a.textureConfig
            )
            .addValidator(Validators.nonNull())
            .add()

            .build();

    private String model;
    private TextureConfig textureConfig;

    @Override
    public String getModel(String variantId) {
        return model;
    }

    @Override
    public TextureConfig getTextureConfig(@Nullable String optionId) {
        return textureConfig;
    }

    @Override
    public String[] collectVariants() {
        return new String[0];
    }
}
