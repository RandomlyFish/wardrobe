package dev.hardaway.wardrobe.impl.cosmetic.appearance;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.hardaway.wardrobe.api.cosmetic.appearance.Appearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;

import javax.annotation.Nullable;

public class ModelAppearance implements Appearance {

    public static final BuilderCodec<ModelAppearance> CODEC = BuilderCodec.builder(ModelAppearance.class, ModelAppearance::new)
            .append(new KeyedCodec<>("Model", Codec.STRING),
                    (t, value) -> t.model = value,
                    t -> t.model
            ).add()

            .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC),
                    (t, value) -> t.textureConfig = value,
                    t -> t.textureConfig
            ).add()

            .build();

    private String model;
    private TextureConfig textureConfig;

    @Override
    public String getModel(String cosmetic) {
        return model;
    }

    @Override
    public TextureConfig getTextureConfig(@Nullable String cosmeticVariantId) {
        return textureConfig;
    }

    @Override
    public String[] collectVariants() {
        return new String[0];
    }
}
