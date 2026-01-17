package dev.hardaway.wardrobe.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.cosmetics.CosmeticRegistry;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class AvatarCustomisationPage extends InteractiveCustomUIPage<AvatarCustomisationPage.PageEventData> {

    public CosmeticTab currentTab;
    public CosmeticSubTab currentSubTab;

    public AvatarCustomisationPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, PageEventData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Pages/AvatarCustomisation/AvatarCustomisation.ui");

        switchTab(commandBuilder, CosmeticTab.Head);
        buildCosmeticList(ref, store, commandBuilder, null);

        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchField", EventData.of("@SearchQuery", "#SearchField.Value"), false);

        for (CosmeticTab tab : CosmeticTab.values()) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Tab" + tab.toString().replace("_", "") + " #Button", EventData.of("Tab", tab.toString()), false);
        }

        for (CosmeticSubTab tab : CosmeticSubTab.values()) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Tab" + tab.toString().replace("_", "") + " #Button", EventData.of("SubTab", tab.toString()), false);
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PageEventData data) {
        super.handleDataEvent(ref, store, data);

        UICommandBuilder commandBuilder = new UICommandBuilder();

        if (data.tab != null) switchTab(commandBuilder, CosmeticTab.valueOf(data.tab));
        if (data.subTab != null) switchSubTab(commandBuilder, CosmeticSubTab.valueOf(data.subTab));

        buildCosmeticList(ref, store, commandBuilder, data.searchQuery);
        sendUpdate(commandBuilder);
    }

    private void buildCosmeticList(Ref<EntityStore> ref, Store<EntityStore> store, UICommandBuilder commandBuilder, @Nullable String searchQuery) {
        commandBuilder.clear("#Parts");
        if (currentSubTab.hasEmptyPart && (searchQuery == null || searchQuery.isEmpty()))
            commandBuilder.append("#Parts", "Pages/AvatarCustomisation/EmptyPart.ui");

        CosmeticRegistry cosmeticRegistry = CosmeticsModule.get().getRegistry();

        for (CosmeticType cosmeticType : CosmeticType.values()) {
            if (cosmeticType.toString().equalsIgnoreCase(currentSubTab.toString())) {
                Set<String> list = cosmeticRegistry.getByType(cosmeticType).keySet();
                for (int i = 0; i < list.size(); i++) {
                    commandBuilder.append("#Parts", "Pages/AvatarCustomisation/Cosmetic.ui");
                }
            }
        }
    }

    public void switchTab(UICommandBuilder commandBuilder, CosmeticTab tab) {
        resetTabs(commandBuilder);

        if (tab.subTabs.isEmpty()) {
            commandBuilder.set("#CategoryLabelArrow.Visible", false);
            commandBuilder.set("#CategoryName.Text", "");
        } else {
            commandBuilder.set("#CategoryLabelArrow.Visible", true);
            commandBuilder.set("#CategoryName.Text", tab.toString().replace("_", " "));
        }

        commandBuilder.set("#Tab" + tab.toString().replace("_", "") + " #Button #Selected.Visible", true);

        for (CosmeticSubTab subTab : CosmeticSubTab.values()) {
            if (tab.subTabs.contains(subTab))
                commandBuilder.set("#Tab" + subTab.toString().replace("_", "") + ".Visible", true);
        }

        commandBuilder.set("#Tab" + tab.defaultSubTab.toString().replace("_", "") + ".Visible", true);
        switchSubTab(commandBuilder, tab.defaultSubTab);

        currentTab = tab;
    }

    public void switchSubTab(UICommandBuilder commandBuilder, CosmeticSubTab subTab) {
        resetSubTabs(commandBuilder);
        commandBuilder.set("#Tab" + subTab.toString().replace("_", "") + " #Button #Selected.Visible", true);
        commandBuilder.set("#SubCategoryName.Text", subTab.toString().replace("_", " "));

        currentSubTab = subTab;
    }

    public static void resetTabs(UICommandBuilder commandBuilder) {
        for (CosmeticTab tab : CosmeticTab.values()) {
            commandBuilder.set("#Tab" + tab.toString().replace("_", "") + " #Button #Selected.Visible", false);
        }

        for (CosmeticSubTab tab : CosmeticSubTab.values()) {
            commandBuilder.set("#Tab" + tab.toString().replace("_", "") + ".Visible", false);
        }
    }

    public static void resetSubTabs(UICommandBuilder commandBuilder) {
        for (CosmeticSubTab tab : CosmeticSubTab.values()) {
            commandBuilder.set("#Tab" + tab.toString().replace("_", "") + " #Button #Selected.Visible", false);
        }
    }

    public static class PageEventData {
        public static final BuilderCodec<PageEventData> CODEC = BuilderCodec.builder(PageEventData.class, PageEventData::new)
                .append(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, (entry) -> entry.searchQuery).add()
                .append(new KeyedCodec<>("Tab", Codec.STRING), (entry, s) -> entry.tab = s, (entry) -> entry.tab).add()
                .append(new KeyedCodec<>("SubTab", Codec.STRING), (entry, s) -> entry.subTab = s, (entry) -> entry.subTab).add()
                .build();

        private String searchQuery;
        private String tab;
        private String subTab;
    }
}
