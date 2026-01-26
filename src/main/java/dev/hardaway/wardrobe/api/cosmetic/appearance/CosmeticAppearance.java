package dev.hardaway.wardrobe.api.cosmetic.appearance;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;

public interface CosmeticAppearance {

    BuilderCodecMapCodec<CosmeticAppearance> CODEC = new BuilderCodecMapCodec<>("Type", true);

    String getModel(String cosmeticVariantId);

    TextureConfig getTextureConfig(String cosmeticVariantId);

    String[] collectVariants();
}
