package dev.hardaway.wardrobe.ui;

import java.util.Arrays;
import java.util.List;

import static dev.hardaway.wardrobe.ui.CosmeticSubTab.*;

public enum CosmeticTab {
    Head(Haircuts, Eyebrows, Eyes, Facial_Hair, Head_Accessory, Face_Accessory, Ear_Accessory),
    General(Underwear, Body_Characteristics, Face, Mouth, Ears),
    Torso(Undertop, Overtop, Gloves),
    Legs(Pants, Overpants, Shoes),
    Capes(Cape);

    public final CosmeticSubTab defaultSubTab;
    public final List<CosmeticSubTab> subTabs;

    CosmeticTab(CosmeticSubTab defaultSubTab, CosmeticSubTab... subTabs) {
        this.defaultSubTab = defaultSubTab;
        this.subTabs = Arrays.stream(subTabs).toList();
    }
}
