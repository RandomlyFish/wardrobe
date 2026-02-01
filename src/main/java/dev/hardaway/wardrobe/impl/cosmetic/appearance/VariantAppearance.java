package dev.hardaway.wardrobe.impl.cosmetic.appearance;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import dev.hardaway.wardrobe.api.cosmetic.appearance.Appearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.property.WardrobeProperties;
import dev.hardaway.wardrobe.api.property.WardrobeTranslationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

public class VariantAppearance implements Appearance {

    public static final BuilderCodec<VariantAppearance> CODEC = BuilderCodec.builder(VariantAppearance.class, VariantAppearance::new)
            .append(new KeyedCodec<>("Variants", new MapCodec<>(VariantAppearance.Entry.CODEC, LinkedHashMap::new), true), (t, value) -> t.variants = value, t -> t.variants).add()
            .build();

    private Map<String, Entry> variants;

    public Map<String, VariantAppearance.Entry> getVariants() {
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

        public static final BuilderCodec<VariantAppearance.Entry> CODEC = BuilderCodec.builder(VariantAppearance.Entry.class, VariantAppearance.Entry::new)

                .append(new KeyedCodec<>("Properties", WardrobeProperties.CODEC, true),
                        (t, value) -> t.properties = value,
                        t -> t.properties
                ).add()

                .append(new KeyedCodec<>("Model", Codec.STRING, true),
                        (t, value) -> t.model = value, t -> t.model
                ).add()

                .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC, true),
                        (t, value) -> t.textureConfig = value, t -> t.textureConfig
                ).add()

                .build();

        private WardrobeProperties properties;
        private String model;
        private TextureConfig textureConfig;

        public WardrobeProperties getProperties() {
            return properties;
        }

        public String getModel() {
            return model;
        }

        public TextureConfig getTextureConfig() {
            return textureConfig;
        }
    }
}
