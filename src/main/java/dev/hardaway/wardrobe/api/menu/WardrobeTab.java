package dev.hardaway.wardrobe.api.menu;

import dev.hardaway.wardrobe.api.property.WardrobePermissionHolder;
import dev.hardaway.wardrobe.api.property.WardrobeTranslatable;

public interface WardrobeTab extends WardrobePermissionHolder, WardrobeTranslatable {
    String getId();

    String getIconPath();

    String getSelectedIconPath();

    int getTabOrder();
}
