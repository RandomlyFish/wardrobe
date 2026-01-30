package dev.hardaway.wardrobe.impl.cosmetic.texture;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StaticTextureConfig implements TextureConfig {

    public static final BuilderCodec<StaticTextureConfig> CODEC = BuilderCodec.builder(StaticTextureConfig.class, StaticTextureConfig::new)
            .append(new KeyedCodec<>("Texture", Codec.STRING),
                    (t, value) -> t.texture = value,
                    t -> t.texture
            ).add()

            .build();

    private String texture;

    @Nonnull
    public String getTexture(@Nullable String textureId) {
        return texture;
    }

    @Override
    public String[] collectVariants() {
        return new String[0];
    }
}
