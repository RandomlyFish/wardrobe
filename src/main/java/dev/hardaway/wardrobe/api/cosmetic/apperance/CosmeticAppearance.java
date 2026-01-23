package dev.hardaway.wardrobe.api.cosmetic.apperance;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;

public interface CosmeticAppearance {

    BuilderCodecMapCodec<CosmeticAppearance> CODEC = new BuilderCodecMapCodec<>("Type", true);

    String getModel(String modelVariantId);

    TextureConfig getTextureConfig(String cosmeticVariantId);

    String[] collectVariants();
}
