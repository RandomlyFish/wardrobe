package dev.hardaway.wardrobe.ui;

public enum CosmeticSubTab {
    Haircuts(true),
    Eyebrows(true),
    Eyes(false),
    Facial_Hair(true),
    Head_Accessory(true),
    Face_Accessory(true),
    Ear_Accessory(true),

    Underwear(false),
    Body_Characteristics(false),
    Face(false),
    Mouth(false),
    Ears(false),

    Undertop(true),
    Overtop(true),
    Gloves(true),

    Pants(true),
    Overpants(true),
    Shoes(true),

    Cape(true);

    public final boolean hasEmptyPart;

    CosmeticSubTab(boolean hasEmptyPart) {
        this.hasEmptyPart = hasEmptyPart;
    }
}
