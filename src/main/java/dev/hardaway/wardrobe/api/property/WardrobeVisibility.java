package dev.hardaway.wardrobe.api.property;

import com.hypixel.hytale.codec.codecs.EnumCodec;

public enum WardrobeVisibility {
    ALWAYS, PERMISSION, NEVER;

    public static final EnumCodec<WardrobeVisibility> CODEC = new EnumCodec<>(WardrobeVisibility.class);
}
