package dev.hardaway.wardrobe.api.cosmetic;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.TextureConfig;

public interface CosmeticAppearance {

    BuilderCodecMapCodec<CosmeticAppearance> CODEC = new BuilderCodecMapCodec<>("Type", true);

    String getModel(String modelVariantId);

    TextureConfig getTextureConfig(String cosmeticVariantId);
}
