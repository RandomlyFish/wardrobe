package dev.hardaway.wardrobe.api.cosmetic.appearance;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;

public interface Appearance {

    BuilderCodecMapCodec<Appearance> CODEC = new BuilderCodecMapCodec<>("Type", true);

    String getModel(String cosmeticVariantId);

    TextureConfig getTextureConfig(String cosmeticVariantId);

    String[] collectVariants();
}
