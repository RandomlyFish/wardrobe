package dev.hardaway.wardrobe.asset.config;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;

import javax.annotation.Nullable;

public interface TextureConfig {

    BuilderCodecMapCodec<TextureConfig> CODEC = new BuilderCodecMapCodec<>("Type", true);

    String getTexture(@Nullable String variant);

    @Nullable
    default String getGradientSet() {
        return null;
    }
}
