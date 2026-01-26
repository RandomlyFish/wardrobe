package dev.hardaway.wardrobe.impl.asset.cosmetic.appearance;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.hardaway.wardrobe.api.cosmetic.appearance.CosmeticAppearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;

import javax.annotation.Nullable;

public class ModelCosmeticAppearance implements CosmeticAppearance {

    public static final BuilderCodec<ModelCosmeticAppearance> CODEC = BuilderCodec.builder(ModelCosmeticAppearance.class, ModelCosmeticAppearance::new)
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
