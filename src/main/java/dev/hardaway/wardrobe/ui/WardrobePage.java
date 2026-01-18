package dev.hardaway.wardrobe.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.cosmetic.asset.CosmeticAsset;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WardrobePage extends InteractiveCustomUIPage<WardrobePage.PageEventData> {

    private CosmeticSubTab subTab;
    private List<CosmeticAsset> cosmetics;
    private CosmeticAsset cosmetic;
    private ServerCameraSettings cameraSettings;

    public WardrobePage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, PageEventData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Pages/Wardrobe/Wardrobe.ui");

        selectTab(commandBuilder, CosmeticTab.Head);
        buildCosmeticList(ref, store, commandBuilder, eventBuilder, null);

        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchField", EventData.of("@SearchQuery", "#SearchField.Value"), false);

        for (CosmeticTab tab : CosmeticTab.values()) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Tab" + tab.toString().replace("_", "") + " #Button", EventData.of("Tab", tab.toString()), false);
        }

        for (CosmeticSubTab tab : CosmeticSubTab.values()) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Tab" + tab.toString().replace("_", "") + " #Button", EventData.of("SubTab", tab.toString()), false);
        }

        cameraSettings = new ServerCameraSettings();
        cameraSettings.isFirstPerson = false;
        cameraSettings.rotation = new Direction();
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.ThirdPerson, true, cameraSettings));

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Left", EventData.of("Direction", "Left"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Right", EventData.of("Direction", "Right"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PageEventData data) {
        super.handleDataEvent(ref, store, data);

        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

        if (data.tab != null) selectTab(commandBuilder, CosmeticTab.valueOf(data.tab));
        if (data.subTab != null) selectSubTab(commandBuilder, CosmeticSubTab.valueOf(data.subTab));
        buildCosmeticList(ref, store, commandBuilder, eventBuilder, data.searchQuery);

        // TODO: make the buttons rotate the body/head
        sendUpdate(commandBuilder);
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        super.onDismiss(ref, store);
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.FirstPerson, false, null));
    }

    private void buildCosmeticList(Ref<EntityStore> ref, Store<EntityStore> store, UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, @Nullable String searchQuery) {
        commandBuilder.clear("#Parts");

        CosmeticType cosmeticType = null;
        for (CosmeticType type : CosmeticType.values()) {
            if (type.toString().equalsIgnoreCase(subTab.toString()) || (type == CosmeticType.CAPES && subTab.toString().equals("Cape"))) {
                cosmeticType = type;
                break;
            }
        }
        if (cosmeticType == null) return;

        ArrayList<CosmeticAsset> tabCosmetics = new ArrayList<>();
        for (CosmeticAsset cosmetic : CosmeticAsset.getAssetMap().getAssetMap().values()) {
//            if (cosmetic.getGroup() == cosmeticType) tabCosmetics.add(cosmetic);
        }

        if (tabCosmetics.isEmpty()) return;

        if (searchQuery == null || searchQuery.isEmpty()) {
            cosmetics = tabCosmetics.stream().sorted().sorted(Comparator.comparing(CosmeticAsset::getId)).collect(Collectors.toList());
        } else {
            Object2IntMap<CosmeticAsset> map = new Object2IntOpenHashMap<>(tabCosmetics.size());

            for (CosmeticAsset value : tabCosmetics) {
                int fuzzyDistance = StringCompareUtil.getFuzzyDistance(value.getId(), searchQuery, Locale.ENGLISH);
                if (fuzzyDistance > 0) {
                    map.put(value, fuzzyDistance);
                }
            }

            cosmetics = map.keySet().stream().sorted().sorted(Comparator.comparingInt(map::getInt).reversed()).limit(20L).collect(Collectors.toList());
        }

        int bound = cosmetics.size();
        for(int i = 0; i < bound; ++i) {
            CosmeticAsset cosmetic = cosmetics.get(i);
            String selector = "#Parts[" + i + "]";
            commandBuilder.append("#Parts", "Pages/Wardrobe/Cosmetic.ui");
            commandBuilder.set(selector + " #Button.Text", cosmetic.getId());
            // FIXME
//            if (cosmetic.getIcon() != null) commandBuilder.setObject(selector + " #Icon.Background", new PatchStyle(Value.of(cosmetic.getId())));
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector + " #Button", EventData.of("Cosmetic", cosmetic.getId()), false);
        }

//        if (!cosmetics.isEmpty()) {
//            if (!cosmetics.contains(selectedCosmetic)) {
//                selectModel(ref, store, cosmetics.getFirst(), commandBuilder);
//            } else if (selectedCosmetic != null) {
//                selectModel(ref, store, selectedCosmetic, commandBuilder);
//            }
//        }
    }

    public void selectTab(UICommandBuilder commandBuilder, CosmeticTab selected) {
        for (CosmeticTab tab : CosmeticTab.values()) {
            commandBuilder.set("#Tab" + tab.toString().replace("_", "") + " #Button #Selected.Visible", false);
        }

        for (CosmeticSubTab tab : CosmeticSubTab.values()) {
            commandBuilder.set("#Tab" + tab.toString().replace("_", "") + ".Visible", false);
        }

        if (selected.subTabs.isEmpty()) {
            commandBuilder.set("#CategoryLabelArrow.Visible", false);
            commandBuilder.set("#CategoryName.Text", "");
        } else {
            commandBuilder.set("#CategoryLabelArrow.Visible", true);
            commandBuilder.set("#CategoryName.Text", selected.toString().replace("_", " "));
        }

        commandBuilder.set("#Tab" + selected.toString().replace("_", "") + " #Button #Selected.Visible", true);

        for (CosmeticSubTab subTab : CosmeticSubTab.values()) {
            if (selected.subTabs.contains(subTab))
                commandBuilder.set("#Tab" + subTab.toString().replace("_", "") + ".Visible", true);
        }

        commandBuilder.set("#Tab" + selected.defaultSubTab.toString().replace("_", "") + ".Visible", true);
        selectSubTab(commandBuilder, selected.defaultSubTab);
    }

    public void selectSubTab(UICommandBuilder commandBuilder, CosmeticSubTab selected) {
        for (CosmeticSubTab tab : CosmeticSubTab.values()) {
            commandBuilder.set("#Tab" + tab.toString().replace("_", "") + " #Button #Selected.Visible", false);
        }

        commandBuilder.set("#Tab" + selected.toString().replace("_", "") + " #Button #Selected.Visible", true);
        commandBuilder.set("#SubCategoryName.Text", selected.toString().replace("_", " "));

        this.subTab = selected;
    }

    public static class PageEventData {
        public static final BuilderCodec<PageEventData> CODEC = BuilderCodec.builder(PageEventData.class, PageEventData::new)
                .append(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, (entry) -> entry.searchQuery).add()
                .append(new KeyedCodec<>("Tab", Codec.STRING), (entry, s) -> entry.tab = s, (entry) -> entry.tab).add()
                .append(new KeyedCodec<>("SubTab", Codec.STRING), (entry, s) -> entry.subTab = s, (entry) -> entry.subTab).add()
                .append(new KeyedCodec<>("Cosmetic", Codec.STRING), (entry, s) -> entry.cosmetic = s, (entry) -> entry.cosmetic).add()
                .append(new KeyedCodec<>("Direction", Codec.STRING), (entry, s) -> entry.direction = s, (entry) -> entry.direction).add()
                .build();

        private String searchQuery;
        private String tab;
        private String subTab;
        private String cosmetic;
        private String direction;
    }
}
