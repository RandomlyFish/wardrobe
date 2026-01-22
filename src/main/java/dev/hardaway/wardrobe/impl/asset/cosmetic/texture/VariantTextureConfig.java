package dev.hardaway.wardrobe.impl.asset.cosmetic.texture;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VariantTextureConfig implements TextureConfig {

    public static final BuilderCodec<VariantTextureConfig> CODEC = BuilderCodec.builder(VariantTextureConfig.class, VariantTextureConfig::new)
            .append(new KeyedCodec<>("Variants", new MapCodec<>(Entry.CODEC, HashMap::new), true), (t, value) -> t.variants = value, t -> t.variants).add()
            .build();

    private Map<String, Entry> variants;

    public Map<String, Entry> getVariants() {
        return variants;
    }

    @Nonnull
    @Override
    public String getTexture(@Nullable String textureId) {
        Entry entry = this.getVariants().get(textureId);
        return entry.getTexture();
    }

    @Override
    public String[] collectVariants() {
        return this.variants.keySet().toArray(String[]::new);
    }

    public static class Entry {

        public static final BuilderCodec<Entry> CODEC = BuilderCodec.builder(Entry.class, Entry::new)
                .append(new KeyedCodec<>("Texture", Codec.STRING, true),
                        (t, value) -> t.texture = value, t -> t.texture
                ).add()

                .append(new KeyedCodec<>("WardrobeColor", Codec.STRING_ARRAY, true),
                        (t, value) -> t.baseColor = value, t -> t.baseColor
                ).addValidator(Validators.nonNullArrayElements()).add()

                .build();

        private String texture;
        private String[] baseColor;

        @Nonnull
        public String getTexture() {
            return texture;
        }

        @Nonnull
        public String[] getBaseColor() {
            return baseColor;
        }
    }
}
