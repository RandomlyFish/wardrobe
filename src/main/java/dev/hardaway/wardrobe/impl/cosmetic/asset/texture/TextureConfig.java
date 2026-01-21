package dev.hardaway.wardrobe.impl.cosmetic.asset.texture;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;

import javax.annotation.Nullable;

public interface TextureConfig {

    BuilderCodecMapCodec<TextureConfig> CODEC = new BuilderCodecMapCodec<>("Type", true);

    String getTexture(@Nullable String textureId);

    @Nullable
    default String getGradientSet() {
        return null;
    }
}
