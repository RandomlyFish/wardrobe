package dev.hardaway.wardrobe.impl.cosmetic.asset;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import dev.hardaway.wardrobe.impl.cosmetic.asset.texture.TextureConfig;

import javax.annotation.Nullable;

// Style
// Cover, Half, Default
public class HeadAccessoryCosmetic extends ModelAttachmentCosmetic {

    public static final BuilderCodec<HeadAccessoryCosmetic> CODEC = BuilderCodec.builder(HeadAccessoryCosmetic.class, HeadAccessoryCosmetic::new, ModelAttachmentCosmetic.CODEC)
            .append(new KeyedCodec<>("HatStyle", new EnumCodec<>(HatStyle.class)),
                    (t, value) -> t.hatStyle = value,
                    t -> t.hatStyle
            ).add()
            .build();

    private HatStyle hatStyle = HatStyle.DEFAULT;

    protected HeadAccessoryCosmetic() {
    }

    public HeadAccessoryCosmetic(String id, String nameKey, String group, String icon, @Nullable String permissionNode, String model, TextureConfig textureConfig, HatStyle hatStyle) {
        super(id, nameKey, group, icon, permissionNode, model, textureConfig);
        this.hatStyle = hatStyle;
    }

    public HatStyle getHatStyle() {
        return hatStyle;
    }

    public enum HatStyle {
        DEFAULT,
        HALF,
        COVERING
    }
}
