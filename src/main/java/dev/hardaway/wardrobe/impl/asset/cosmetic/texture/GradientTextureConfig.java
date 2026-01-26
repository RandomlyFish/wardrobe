package dev.hardaway.wardrobe.impl.asset.cosmetic.texture;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GradientTextureConfig implements TextureConfig {

    public static final BuilderCodec<GradientTextureConfig> CODEC = BuilderCodec.builder(GradientTextureConfig.class, GradientTextureConfig::new)
            .append(new KeyedCodec<>("GradientSet", Codec.STRING, true),
                    (t, value) -> t.gradientSet = value,
                    t -> t.gradientSet
            ).add()

            .append(new KeyedCodec<>("GrayscaleTexture", Codec.STRING, true),
                    (t, value) -> t.grayscaleTexture = value,
                    t -> t.grayscaleTexture
            ).add()
            .build();

    private String gradientSet;
    private String grayscaleTexture;

    private GradientTextureConfig() {
    }

    public GradientTextureConfig(String gradientSet, String grayscaleTexture) {
        this.gradientSet = gradientSet;
        this.grayscaleTexture = grayscaleTexture;
    }

    @Nonnull
    @Override
    public String getTexture(@Nullable String textureId) {
        return grayscaleTexture;
    }

    @Nonnull
    @Override
    public String getGradientSet() {
        return gradientSet;
    }

    @Override
    public String[] collectVariants() {
        return CosmeticsModule.get().getRegistry().getGradientSets().get(this.getGradientSet()).getGradients().keySet().toArray(String[]::new);
    }
}
