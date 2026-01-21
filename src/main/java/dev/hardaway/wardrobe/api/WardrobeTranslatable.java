package dev.hardaway.wardrobe.api;

import com.hypixel.hytale.server.core.Message;

public interface WardrobeTranslatable {

    String getTranslationKey();

    default Message getName() {
        return Message.translation(this.getTranslationKey());
    }
}
