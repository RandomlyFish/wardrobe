package dev.hardaway.wardrobe.api.cosmetic;

public interface WardrobeCategory extends WardrobeTab {

    default String getTranslationKey() {
        return "wardrobe.categories." + this.getId() + ".name";
    }
}