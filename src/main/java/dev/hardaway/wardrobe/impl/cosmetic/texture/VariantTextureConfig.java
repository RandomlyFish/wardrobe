package dev.hardaway.wardrobe.impl.cosmetic.texture;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.property.WardrobeProperties;
import dev.hardaway.wardrobe.api.property.validator.WardrobeValidators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class VariantTextureConfig implements TextureConfig {

    public static final BuilderCodec<VariantTextureConfig> CODEC = BuilderCodec.builder(VariantTextureConfig.class, VariantTextureConfig::new)
            .append(new KeyedCodec<>("Variants", new MapCodec<>(Entry.CODEC, LinkedHashMap::new), true), (t, value) -> t.variants = value, t -> t.variants)
            .metadata(new UIPropertyTitle("Texture Variants")).documentation("The available Texture Variants of this Texture Configuration.")
            .add()
            .build();

    private Map<String, Entry> variants;

    public Map<String, Entry> getVariants() {
        return variants;
    }

    @Nonnull
    @Override
    public String getTexture(@Nullable String variantId) {
        Entry entry = this.getVariants().get(variantId);
        return entry.getTexture();
    }

    @Override
    public String[] collectVariants() {
        return this.variants.keySet().toArray(String[]::new);
    }

    public static class Entry {

        public static final BuilderCodec<Entry> CODEC = BuilderCodec.builder(Entry.class, Entry::new)

                .append(new KeyedCodec<>("Properties", WardrobeProperties.CODEC, true),
                        (t, value) -> t.properties = value,
                        t -> t.properties
                )
                .addValidator(Validators.nonNull())
                .metadata(new UIPropertyTitle("Wardrobe Properties")).documentation("Properties for the Cosmetic to display in the Wardrobe Menu.")
                .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
                .add()

                .append(new KeyedCodec<>("Icon", Codec.STRING),
                        (t, value) -> t.icon = value,
                        t -> t.icon
                )
                .addValidator(WardrobeValidators.ICON)
                .metadata(new UIPropertyTitle("Icon")).documentation("An icon to display over the color preview in the Wardrobe Menu.")
                .add()

                .append(new KeyedCodec<>("Texture", Codec.STRING, true),
                        (t, value) -> t.texture = value, t -> t.texture
                )
                .addValidator(CommonAssetValidator.TEXTURE_CHARACTER_ATTACHMENT)
                .metadata(new UIPropertyTitle("Texture")).documentation("The texture to use.")
                .add()

                .append(new KeyedCodec<>("WardrobeColor", Codec.STRING_ARRAY, true),
                        (t, value) -> t.colors = value, t -> t.colors
                )
                .addValidator(WardrobeValidators.COLOR)
                .metadata(new UIPropertyTitle("Wardrobe Color")).documentation("An array of color strings in hexadecimal format. Each color in the array represents a stripe in the color preview. Used as the color preview in the Wardrobe Menu under 'Variants'.")
                .add()
                .afterDecode(asset -> {
                    if (asset.getIcon() == null && asset.properties != null && asset.properties.getIcon() != null) { // DEPRECATED
                        asset.icon = asset.properties.getIcon();
                    }
                })
                .build();

        private WardrobeProperties properties;
        private String icon;
        private String texture;
        private String[] colors;

        public WardrobeProperties getProperties() {
            return properties;
        }

        @Nullable
        public String getIcon() {
            return icon;
        }

        @Nonnull
        public String getTexture() {
            return texture;
        }


        @Nonnull
        public String[] getColors() {
            return colors;
        }
    }
}
