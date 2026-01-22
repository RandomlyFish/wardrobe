package dev.hardaway.wardrobe.impl.asset.cosmetic.texture;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TextureConfig {

    BuilderCodecMapCodec<TextureConfig> CODEC = new BuilderCodecMapCodec<>("Type", true);

    String getTexture(@Nullable String textureId);

    @Nullable
    default String getGradientSet() {
        return null;
    }

    String[] collectVariants();
}
