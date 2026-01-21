package dev.hardaway.wardrobe.api.cosmetic;

import dev.hardaway.wardrobe.api.WardrobePermissionHolder;
import dev.hardaway.wardrobe.api.WardrobeTranslatable;

public interface WardrobeTab extends WardrobePermissionHolder, WardrobeTranslatable {
    String getId();

    String getIconPath();

    String getSelectedIconPath();

    int getTabOrder();
}
