package dev.hardaway.wardrobe.cosmetic;

import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;

import javax.annotation.Nullable;

public record BuiltinCosmetic(String model, String texture, @Nullable String gradientSet,
                              @Nullable String gradientId) {

    public ModelAttachment toModelAttachment() {
        return new ModelAttachment(
                model,
                texture,
                gradientSet,
                gradientId,
                1.0
        );
    }
}
