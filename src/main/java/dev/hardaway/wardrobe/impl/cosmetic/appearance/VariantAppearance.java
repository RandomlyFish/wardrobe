package dev.hardaway.wardrobe.impl.cosmetic.appearance;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.validation.Validators;
import dev.hardaway.wardrobe.api.cosmetic.appearance.Appearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.property.WardrobeProperties;
import dev.hardaway.wardrobe.api.property.validator.WardrobeValidators;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class VariantAppearance implements Appearance {

    public static final BuilderCodec<VariantAppearance> CODEC = BuilderCodec.builder(VariantAppearance.class, VariantAppearance::new)
            .append(new KeyedCodec<>("Variants", new MapCodec<>(VariantAppearance.Entry.CODEC, LinkedHashMap::new), true),
                    (a, value) -> a.variants = value, a -> a.variants
            )
            .metadata(new UIPropertyTitle("Variants")).documentation("The available variants of this appearance.")
            .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
            .add().build();

    private Map<String, Entry> variants;

    public Map<String, VariantAppearance.Entry> getVariants() {
        return variants;
    }

    @Override
    public String getModel(String variantId) {
        Entry entry = variants.get(variantId);
        return entry == null ? null : entry.getModel();
    }

    @Override
    public float getScale(String variantId) {
        Entry entry = variants.get(variantId);
        return entry == null ? 1.0F : entry.getScale();
    }

    @Override
    public TextureConfig getTextureConfig(String variantId) {
        Entry entry = variants.get(variantId);
        return entry == null ? null : entry.getTextureConfig();
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
                )
                .addValidator(Validators.nonNull())
                .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
                .add()

                .append(new KeyedCodec<>("Icon", Codec.STRING),
                        (t, value) -> t.icon = value,
                        t -> t.icon
                )
                .addValidator(WardrobeValidators.ICON)
                .metadata(new UIPropertyTitle("Icon")).documentation("A preview icon of the Cosmetic with this Variant applied to display in the Wardrobe Menu.")
                .add()

                .append(new KeyedCodec<>("Model", Codec.STRING, true),
                        (t, value) -> t.model = value, t -> t.model
                )
                .addValidator(Validators.nonNull())
                .metadata(new UIPropertyTitle("Model")).documentation("The model to display for this appearance.")
                .add()

                .append(new KeyedCodec<>("Scale", Codec.DOUBLE),
                        (a, d) -> a.scale = d.floatValue(),
                        (a) -> (double) a.scale
                )
                .add()

                .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC, true),
                        (t, value) -> t.textureConfig = value, t -> t.textureConfig
                )
                .addValidator(Validators.nonNull())
                .metadata(new UIPropertyTitle("Appearance")).documentation("The Texture Configuration for this appearance.")
                .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
                .add()
                .afterDecode(asset -> {
                    if (asset.getIcon() == null && asset.properties != null && asset.properties.getIcon() != null) { // DEPRECATED
                        asset.icon = asset.properties.getIcon();
                    }
                })
                .build();

        private WardrobeProperties properties;
        private String icon;
        private String model;
        private float scale = 1;
        private TextureConfig textureConfig;

        public WardrobeProperties getProperties() {
            return properties;
        }

        @Nullable
        public String getIcon() {
            return icon;
        }

        public String getModel() {
            return model;
        }

        public float getScale() {
            return scale;
        }

        public TextureConfig getTextureConfig() {
            return textureConfig;
        }
    }
}
