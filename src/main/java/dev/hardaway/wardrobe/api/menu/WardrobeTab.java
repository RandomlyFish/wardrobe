package dev.hardaway.wardrobe.api.menu;

import dev.hardaway.wardrobe.api.property.WardrobeProperties;

public interface WardrobeTab {

    String getId();

    WardrobeProperties getProperties();

    String getSelectedIconPath();

    int getTabOrder();
}
