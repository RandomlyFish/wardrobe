package dev.hardaway.wardrobe.impl.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCategory;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeGroup;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.impl.asset.CosmeticCategoryAsset;
import dev.hardaway.wardrobe.impl.asset.CosmeticGroupAsset;
import dev.hardaway.wardrobe.impl.asset.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.system.CosmeticSaveData;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class WardrobePage extends InteractiveCustomUIPage<WardrobePage.PageEventData> {

    private final ComponentType<EntityStore, PlayerWardrobeComponent> playerWardrobeComponentType;

    private final List<WardrobeCategory> categories = CosmeticCategoryAsset.getAssetMap().getAssetMap().values().stream().sorted(Comparator.comparing(WardrobeCategory::getTabOrder)).collect(Collectors.toUnmodifiableList());
    private final Map<WardrobeCategory, List<WardrobeGroup>> groupMap = HashMap.newHashMap(CosmeticCategoryAsset.getAssetMap().getAssetCount());
    private final Map<WardrobeGroup, List<WardrobeCosmetic>> cosmeticMap = HashMap.newHashMap(CosmeticAsset.getAssetMap().getAssetCount());

    private WardrobeCategory selectedCategory;
    private WardrobeGroup selectedGroup;
    private String searchQuery = "";

    private final ServerCameraSettings cameraSettings = new ServerCameraSettings();

    public WardrobePage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType) {
        super(playerRef, lifetime, PageEventData.CODEC);
        this.playerWardrobeComponentType = wardrobeComponentType;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Wardrobe/Pages/Wardrobe.ui");
        commandBuilder.append("Wardrobe/Pages/Buttons.ui");

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Rotate #RotateLeft", EventData.of("Direction", "Left"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Rotate #RotateRight", EventData.of("Direction", "Right"), false);

        for (int i = 0; i < categories.size(); i++) {
            WardrobeCategory category = categories.get(i);
            groupMap.put(category, new ArrayList<>());
            commandBuilder.append("#Categories", "Wardrobe/Pages/Tab.ui");
            String selector = "#Categories[" + i + "] #Button";
            commandBuilder.set(selector + " #Icon.AssetPath", category.getIconPath());
            commandBuilder.set(selector + " #Selected #Icon.AssetPath", category.getSelectedIconPath());
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Category", category.getId()), false);
        }

        List<CosmeticGroupAsset> sortedGroups = CosmeticGroupAsset.getAssetMap().getAssetMap().values().stream().sorted(Comparator.comparing(CosmeticGroupAsset::getTabOrder)).toList();
        for (CosmeticGroupAsset group : sortedGroups) {
            groupMap.getOrDefault(group.getCategory(), new ArrayList<>()).add(group);
            cosmeticMap.put(group, new ArrayList<>());
        }

        List<CosmeticAsset> sortedCosmetics = CosmeticAsset.getAssetMap().getAssetMap().values().stream().sorted(Comparator.comparing(CosmeticAsset::getId)).toList();
        for (CosmeticAsset asset : sortedCosmetics) {
            if (!asset.hasPermission(this.playerRef.getUuid()))
                continue;

            cosmeticMap.get(asset.getGroup()).add(asset);
        }

        WardrobeCategory selectedCategory = categories.getFirst();
        selectCategory(commandBuilder, eventBuilder, ref, store, selectedCategory);

        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchField", EventData.of("@SearchQuery", "#SearchField.Value"), false);

        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.ThirdPerson, false, null));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PageEventData data) {
        super.handleDataEvent(ref, store, data);

        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

        if (data.searchQuery != null) buildCosmeticList(commandBuilder, eventBuilder, ref, store, data.searchQuery);
        if (data.category != null)
            selectCategory(commandBuilder, eventBuilder, ref, store, CosmeticCategoryAsset.getAssetMap().getAsset(data.category));
        if (data.group != null)
            selectGroup(commandBuilder, eventBuilder, ref, store, CosmeticGroupAsset.getAssetMap().getAsset(data.group));
        if (data.cosmetic != null)
            selectCosmetic(commandBuilder, eventBuilder, ref, store, Objects.requireNonNull(CosmeticAsset.getAssetMap().getAsset(data.cosmetic)));

        if (data.direction != null) {
//            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
//            Teleport teleport = new Teleport(transformComponent.getTransform());
//            teleport.setRotation(transformComponent.getRotation().add(1, 0, 0));
//            store.putComponent(ref, Teleport.getComponentType(), teleport);
            if (data.direction.equals("Right")) {
            } else {
            }
        }

        sendUpdate(commandBuilder, eventBuilder, false);
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        super.onDismiss(ref, store);
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.FirstPerson, false, null));
    }

    public void selectCosmetic(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Ref<EntityStore> ref, Store<EntityStore> store, WardrobeCosmetic cosmetic) {
        PlayerWardrobeComponent wardrobeComponent = store.ensureAndGetComponent(ref, playerWardrobeComponentType);
        PlayerCosmetic wornCosmetic = wardrobeComponent.getCosmetic(this.selectedGroup);
        if (wornCosmetic != null && Objects.equals(wornCosmetic.getCosmetic(), cosmetic)) {
            wardrobeComponent.setCosmetic(selectedGroup, null);
        } else wardrobeComponent.setCosmetic(selectedGroup, new CosmeticSaveData(cosmetic.getId()));
        wardrobeComponent.rebuild();
        buildCosmeticList(commandBuilder, eventBuilder, ref, store, searchQuery);
    }

    public void buildCosmeticList(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Ref<EntityStore> ref, Store<EntityStore> store, String searchQuery) {
        commandBuilder.clear("#Cosmetics");

        List<WardrobeCosmetic> cosmetics = cosmeticMap.get(selectedGroup);
        if (!searchQuery.isEmpty()) {
            Object2IntMap<WardrobeCosmetic> map = new Object2IntOpenHashMap<>(cosmetics.size());

            for (WardrobeCosmetic value : cosmetics) {
                int fuzzyDistance = StringCompareUtil.getFuzzyDistance(value.getId(), searchQuery, Locale.ENGLISH);
                if (fuzzyDistance > 0) {
                    map.put(value, fuzzyDistance);
                }
            }

            cosmetics = map.keySet().stream().sorted().sorted(Comparator.comparingInt(map::getInt).reversed()).toList();
        }

        PlayerWardrobeComponent wardrobeComponent = store.getComponent(ref, playerWardrobeComponentType);
        PlayerCosmetic wornCosmetic = wardrobeComponent == null ? null : wardrobeComponent.getCosmetic(this.selectedGroup);

        for (int i = 0; i < cosmetics.size(); i++) {
            WardrobeCosmetic cosmetic = cosmetics.get(i);
            commandBuilder.append("#Cosmetics", "Wardrobe/Pages/Cosmetic.ui");
            String selector = "#Cosmetics[" + i + "]";
            commandBuilder.set(selector + " #Button.Text", cosmetic.getName());
            if (cosmetic.getIconPath() != null)
                commandBuilder.set(selector + " #Icon.AssetPath", cosmetic.getIconPath());
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector + " #Button", EventData.of("Cosmetic", cosmetic.getId()), false);

            if (wornCosmetic != null && Objects.equals(wornCosmetic.getCosmetic(), cosmetic)) {
//                commandBuilder.set(selector + " #Selected.Visible", true);
            }
        }

        this.searchQuery = searchQuery;
    }

    public void selectGroup(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Ref<EntityStore> ref, Store<EntityStore> store, WardrobeGroup group) {
        List<WardrobeGroup> groups = groupMap.get(selectedCategory);
        for (int i = 0; i < groups.size(); i++) {
            commandBuilder.set("#Groups[" + i + "] #Button #Selected.Visible", false);
        }
        commandBuilder.set("#Groups[" + groups.indexOf(group) + "] #Button #Selected.Visible", true);
        commandBuilder.set("#SubCategoryName.Text", group.getName());
        selectedGroup = group;

        buildCosmeticList(commandBuilder, eventBuilder, ref, store, searchQuery);
    }

    public void selectCategory(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Ref<EntityStore> ref, Store<EntityStore> store, WardrobeCategory category) {
        for (int i = 0; i < categories.size(); i++) {
            commandBuilder.set("#Categories[" + i + "] #Button #Selected.Visible", false);
        }
        commandBuilder.set("#Categories[" + categories.indexOf(category) + "] #Button #Selected.Visible", true);

        commandBuilder.clear("#Groups");
        List<WardrobeGroup> groups = groupMap.get(category);
        for (int i = 0; i < groups.size(); i++) {
            WardrobeGroup group = groups.get(i);
            commandBuilder.append("#Groups", "Wardrobe/Pages/Tab.ui");
            String selector = "#Groups[" + i + "] #Button";
            commandBuilder.set(selector + " #Icon.AssetPath", group.getIconPath());
            commandBuilder.set(selector + " #Selected #Icon.AssetPath", group.getSelectedIconPath());
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Group", group.getId()), false);
        }

        if (groups.isEmpty()) {
            commandBuilder.set("#CategoryLabelArrow.Visible", false);
            commandBuilder.set("#CategoryName.Text", "");
        } else {
            commandBuilder.set("#CategoryLabelArrow.Visible", true);
            commandBuilder.set("#CategoryName.Text", category.getName());
        }

        selectedCategory = category;

        WardrobeGroup selectedGroup = groups.getFirst();
        selectGroup(commandBuilder, eventBuilder, ref, store, selectedGroup);
    }

    public static class PageEventData {
        public static final BuilderCodec<PageEventData> CODEC = BuilderCodec.builder(PageEventData.class, PageEventData::new)
                .append(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, (entry) -> entry.searchQuery).add()
                .append(new KeyedCodec<>("Category", Codec.STRING), (entry, s) -> entry.category = s, (entry) -> entry.category).add()
                .append(new KeyedCodec<>("Group", Codec.STRING), (entry, s) -> entry.group = s, (entry) -> entry.group).add()
                .append(new KeyedCodec<>("Cosmetic", Codec.STRING), (entry, s) -> entry.cosmetic = s, (entry) -> entry.cosmetic).add()
                .append(new KeyedCodec<>("Direction", Codec.STRING), (entry, s) -> entry.direction = s, (entry) -> entry.direction).add()
                .build();

        private String searchQuery;
        private String category;
        private String group;
        private String cosmetic;
        private String direction;
    }
}
