package dev.hardaway.wardrobe.api.cosmetic.appearance;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;

import javax.annotation.Nullable;

public interface Appearance {

    BuilderCodecMapCodec<Appearance> CODEC = new BuilderCodecMapCodec<>("Type", true);

    String getModel(@Nullable String variantId);

    TextureConfig getTextureConfig(@Nullable String optionId);

    String[] collectVariants();
}
