package dev.hardaway.wardrobe.impl.asset.cosmetic.texture;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StaticTextureConfig implements TextureConfig {

    public static final BuilderCodec<StaticTextureConfig> CODEC = BuilderCodec.builder(StaticTextureConfig.class, StaticTextureConfig::new)
            .append(new KeyedCodec<>("Texture", Codec.STRING),
                    (t, value) -> t.texture = value,
                    t -> t.texture
            ).addValidator(CommonAssetValidator.TEXTURE_CHARACTER_ATTACHMENT).add()

            .build();

    private String texture;

    @Nonnull
    public String getTexture(@Nullable String textureId) {
        return texture;
    }
}
