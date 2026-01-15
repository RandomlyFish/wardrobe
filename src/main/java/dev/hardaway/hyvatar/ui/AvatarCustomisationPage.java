package dev.hardaway.hyvatar.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AvatarCustomisationPage extends InteractiveCustomUIPage<AvatarCustomisationPage.PageEventData> {

    public AvatarCustomisationPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, PageEventData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Pages/AvatarCustomisation/AvatarCustomisation.ui");

        switchCategory(commandBuilder, "Head");

        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchField", EventData.of("@SearchQuery", "#SearchField.Value"), false);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabHead #Button", EventData.of("Category", "Head"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabGeneral #Button", EventData.of("Category", "General"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabTorso #Button", EventData.of("Category", "Torso"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabLegs #Button", EventData.of("Category", "Legs"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabCapes #Button", EventData.of("Category", "Capes"), false);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabUnderwear #Button", EventData.of("SubCategory", "Underwear"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabBodyCharacteristics #Button", EventData.of("SubCategory", "Body Characteristics"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabFace #Button", EventData.of("SubCategory", "Face"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabMouth #Button", EventData.of("SubCategory", "Mouth"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabEars #Button", EventData.of("SubCategory", "Ears"), false);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabHaircut #Button", EventData.of("SubCategory", "Haircut"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabEyebrows #Button", EventData.of("SubCategory", "Eyebrows"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabEyes #Button", EventData.of("SubCategory", "Eyes"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabFacialHair #Button", EventData.of("SubCategory", "Facial Hair"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabHeadAccessory #Button", EventData.of("SubCategory", "Head Accessory"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabFaceAccessory #Button", EventData.of("SubCategory", "Face Accessory"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabEarAccessory #Button", EventData.of("SubCategory", "Ear Accessory"), false);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabUndertop #Button", EventData.of("SubCategory", "Undertop"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabOvertop #Button", EventData.of("SubCategory", "Overtop"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabGloves #Button", EventData.of("SubCategory", "Gloves"), false);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabPants #Button", EventData.of("SubCategory", "Pants"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabOverpants #Button", EventData.of("SubCategory", "Overpants"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabShoes #Button", EventData.of("SubCategory", "Shoes"), false);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabCape #Button", EventData.of("SubCategory", "Cape"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PageEventData data) {
        super.handleDataEvent(ref, store, data);

        UICommandBuilder commandBuilder = new UICommandBuilder();
        switchCategory(commandBuilder, data.category);
        switchSubCategory(commandBuilder, data.subCategory);
        sendUpdate(commandBuilder);
    }

    public static void switchCategory(UICommandBuilder commandBuilder, @Nullable String category) {
        if (category == null) return;

        commandBuilder.set("#CategoryName.Text", category);

        switch (category) {
            case "Head" -> {
                resetCategories(commandBuilder);
                commandBuilder.set("#TabHead #Button #Selected.Visible", true);

                commandBuilder.set("#TabHaircut.Visible", true);
                commandBuilder.set("#TabEyebrows.Visible", true);
                commandBuilder.set("#TabEyes.Visible", true);
                commandBuilder.set("#TabFacialHair.Visible", true);
                commandBuilder.set("#TabHeadAccessory.Visible", true);
                commandBuilder.set("#TabFaceAccessory.Visible", true);
                commandBuilder.set("#TabEarAccessory.Visible", true);
                switchSubCategory(commandBuilder, "Haircut");
            }
            case "General" -> {
                resetCategories(commandBuilder);
                commandBuilder.set("#TabGeneral #Button #Selected.Visible", true);

                commandBuilder.set("#TabUnderwear.Visible", true);
                commandBuilder.set("#TabBodyCharacteristics.Visible", true);
                commandBuilder.set("#TabFace.Visible", true);
                commandBuilder.set("#TabMouth.Visible", true);
                commandBuilder.set("#TabEars.Visible", true);
                switchSubCategory(commandBuilder, "Underwear");
            }
            case "Torso" -> {
                resetCategories(commandBuilder);
                commandBuilder.set("#TabTorso #Button #Selected.Visible", true);

                commandBuilder.set("#TabUndertop.Visible", true);
                commandBuilder.set("#TabOvertop.Visible", true);
                commandBuilder.set("#TabGloves.Visible", true);
                switchSubCategory(commandBuilder, "Undertop");
            }
            case "Legs" -> {
                resetCategories(commandBuilder);
                commandBuilder.set("#TabLegs #Button #Selected.Visible", true);

                commandBuilder.set("#TabPants.Visible", true);
                commandBuilder.set("#TabOverpants.Visible", true);
                commandBuilder.set("#TabShoes.Visible", true);
                switchSubCategory(commandBuilder, "Pants");
            }
            case "Capes" -> {
                resetCategories(commandBuilder);
                commandBuilder.set("#TabCapes #Button #Selected.Visible", true);

                commandBuilder.set("#TabCape.Visible", true);
                switchSubCategory(commandBuilder, "Cape");
            }
        }
    }

    public static void switchSubCategory(UICommandBuilder commandBuilder, @Nullable String subCategory) {
        if (subCategory == null) return;

        resetSubCategories(commandBuilder);
        commandBuilder.set("#Tab" + subCategory.replace(" ", "") + " #Button #Selected.Visible", true);
        commandBuilder.set("#SubCategoryName.Text", subCategory);
    }

    public static void resetCategories(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TabHead #Button #Selected.Visible", false);
        commandBuilder.set("#TabGeneral #Button #Selected.Visible", false);
        commandBuilder.set("#TabTorso #Button #Selected.Visible", false);
        commandBuilder.set("#TabLegs #Button #Selected.Visible", false);
        commandBuilder.set("#TabCapes #Button #Selected.Visible", false);

        commandBuilder.set("#TabUnderwear.Visible", false);
        commandBuilder.set("#TabBodyCharacteristics.Visible", false);
        commandBuilder.set("#TabFace.Visible", false);
        commandBuilder.set("#TabMouth.Visible", false);
        commandBuilder.set("#TabEars.Visible", false);

        commandBuilder.set("#TabHaircut.Visible", false);
        commandBuilder.set("#TabEyebrows.Visible", false);
        commandBuilder.set("#TabEyes.Visible", false);
        commandBuilder.set("#TabFacialHair.Visible", false);
        commandBuilder.set("#TabHeadAccessory.Visible", false);
        commandBuilder.set("#TabFaceAccessory.Visible", false);
        commandBuilder.set("#TabEarAccessory.Visible", false);

        commandBuilder.set("#TabUndertop.Visible", false);
        commandBuilder.set("#TabOvertop.Visible", false);
        commandBuilder.set("#TabGloves.Visible", false);

        commandBuilder.set("#TabPants.Visible", false);
        commandBuilder.set("#TabOverpants.Visible", false);
        commandBuilder.set("#TabShoes.Visible", false);

        commandBuilder.set("#TabCape.Visible", false);
    }

    public static void resetSubCategories(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TabUnderwear #Button #Selected.Visible", false);
        commandBuilder.set("#TabBodyCharacteristics #Button #Selected.Visible", false);
        commandBuilder.set("#TabFace #Button #Selected.Visible", false);
        commandBuilder.set("#TabMouth #Button #Selected.Visible", false);
        commandBuilder.set("#TabEars #Button #Selected.Visible", false);

        commandBuilder.set("#TabHaircut #Button #Selected.Visible", false);
        commandBuilder.set("#TabEyebrows #Button #Selected.Visible", false);
        commandBuilder.set("#TabEyes #Button #Selected.Visible", false);
        commandBuilder.set("#TabFacialHair #Button #Selected.Visible", false);
        commandBuilder.set("#TabHeadAccessory #Button #Selected.Visible", false);
        commandBuilder.set("#TabFaceAccessory #Button #Selected.Visible", false);
        commandBuilder.set("#TabEarAccessory #Button #Selected.Visible", false);

        commandBuilder.set("#TabUndertop #Button #Selected.Visible", false);
        commandBuilder.set("#TabOvertop #Button #Selected.Visible", false);
        commandBuilder.set("#TabGloves #Button #Selected.Visible", false);

        commandBuilder.set("#TabPants #Button #Selected.Visible", false);
        commandBuilder.set("#TabOverpants #Button #Selected.Visible", false);
        commandBuilder.set("#TabShoes #Button #Selected.Visible", false);

        commandBuilder.set("#TabCape #Button #Selected.Visible", false);
    }

    public static class PageEventData {
        public static final BuilderCodec<PageEventData> CODEC = BuilderCodec.builder(PageEventData.class, PageEventData::new)
                .append(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, (entry) -> entry.searchQuery).add()
                .append(new KeyedCodec<>("Category", Codec.STRING), (entry, s) -> entry.category = s, (entry) -> entry.category).add()
                .append(new KeyedCodec<>("SubCategory", Codec.STRING), (entry, s) -> entry.subCategory = s, (entry) -> entry.subCategory).add()
                .build();

        private String searchQuery;
        private String category;
        private String subCategory;
    }
}
