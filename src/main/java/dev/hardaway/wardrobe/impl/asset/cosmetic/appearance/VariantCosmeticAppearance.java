package dev.hardaway.wardrobe.impl.asset.cosmetic.appearance;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import dev.hardaway.wardrobe.api.WardrobeTranslationProperties;
import dev.hardaway.wardrobe.api.cosmetic.apperance.CosmeticAppearance;
import dev.hardaway.wardrobe.api.cosmetic.apperance.TextureConfig;

import java.util.LinkedHashMap;
import java.util.Map;

public class VariantCosmeticAppearance implements CosmeticAppearance {

    public static final BuilderCodec<VariantCosmeticAppearance> CODEC = BuilderCodec.builder(VariantCosmeticAppearance.class, VariantCosmeticAppearance::new)
            .append(new KeyedCodec<>("Variants", new MapCodec<>(VariantCosmeticAppearance.Entry.CODEC, LinkedHashMap::new), true), (t, value) -> t.variants = value, t -> t.variants).add()
            .build();

    private Map<String, Entry> variants;

    public Map<String, VariantCosmeticAppearance.Entry> getVariants() {
        return variants;
    }

    @Override
    public String getModel(String cosmeticVariantId) {
        return variants.get(cosmeticVariantId).getModel();
    }

    @Override
    public TextureConfig getTextureConfig(String cosmeticVariantId) {
        return variants.get(cosmeticVariantId).getTextureConfig();
    }

    @Override
    public String[] collectVariants() {
        return this.variants.keySet().toArray(String[]::new);
    }

    public static class Entry {

        public static final BuilderCodec<VariantCosmeticAppearance.Entry> CODEC = BuilderCodec.builder(VariantCosmeticAppearance.Entry.class, VariantCosmeticAppearance.Entry::new)

                .append(new KeyedCodec<>("TranslationProperties", WardrobeTranslationProperties.CODEC, true),
                        (t, value) -> t.translationProperties = value,
                        t -> t.translationProperties
                ).add()

                .append(new KeyedCodec<>("Icon", Codec.STRING),
                        (t, value) -> t.iconPath = value, t -> t.iconPath
                ).add()

                .append(new KeyedCodec<>("Model", Codec.STRING, true),
                        (t, value) -> t.model = value, t -> t.model
                ).add()

                .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC, true),
                        (t, value) -> t.textureConfig = value, t -> t.textureConfig
                ).add()

                .build();

        private WardrobeTranslationProperties translationProperties;
        private String iconPath;
        private String model;
        private TextureConfig textureConfig;

        public WardrobeTranslationProperties getTranslationProperties() {
            return translationProperties;
        }

        public String getIconPath() {
            return iconPath;
        }

        public String getModel() {
            return model;
        }

        public TextureConfig getTextureConfig() {
            return textureConfig;
        }
    }
}
