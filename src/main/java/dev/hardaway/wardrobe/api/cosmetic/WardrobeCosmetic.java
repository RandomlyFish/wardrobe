package dev.hardaway.wardrobe.api.cosmetic;

import dev.hardaway.wardrobe.api.WardrobePermissionHolder;
import dev.hardaway.wardrobe.api.WardrobeTranslatable;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;

import java.util.List;

public interface WardrobeCosmetic extends WardrobePermissionHolder, WardrobeTranslatable {

    String getId();

    default String getTranslationKey() {
        return "wardrobe.cosmetics." + this.getId() + ".name";
    }

    WardrobeGroup getGroup();

    String getIconPath();

    void applyCosmetic(WardrobeContext context, WardrobeGroup group, PlayerCosmetic playerCosmetic);

    List<String> getVariants();
}
