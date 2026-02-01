package dev.hardaway.wardrobe.api.property;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.Message;

public class WardrobeTranslationProperties {
    public static final BuilderCodec<WardrobeTranslationProperties> CODEC = BuilderCodec.builder(WardrobeTranslationProperties.class, WardrobeTranslationProperties::new)
            .append(
                    new KeyedCodec<>("Name", Codec.STRING),
                    (data, s) -> data.nameKey = s,
                    data -> data.nameKey
            ).add()

            .append(
                    new KeyedCodec<>("Description", Codec.STRING),
                    (data, s) -> data.descriptionKey = s,
                    data -> data.descriptionKey
            ).add()
            .build();

    private String nameKey;
    private String descriptionKey;

    WardrobeTranslationProperties() {
    }

    public WardrobeTranslationProperties(String nameKey, String descriptionKey) {
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
    }

    public String getNameKey() {
        return this.nameKey;
    }

    public Message getName() {
        return Message.translation(this.getNameKey());
    }

    public String getDescriptionKey() {
        return this.descriptionKey;
    }

    public Message getDescription() {
        return Message.translation(this.getDescriptionKey());
    }
}