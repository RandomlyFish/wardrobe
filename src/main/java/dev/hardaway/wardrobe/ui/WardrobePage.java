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
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.cosmetic.asset.CosmeticAsset;
import dev.hardaway.wardrobe.cosmetic.asset.category.CosmeticCategory;
import dev.hardaway.wardrobe.cosmetic.asset.category.CosmeticGroup;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import javax.annotation.Nonnull;
import java.util.*;

public class WardrobePage extends InteractiveCustomUIPage<WardrobePage.PageEventData> {

    private final List<CosmeticCategory> categories = CosmeticCategory.getAssetMap().getAssetMap().values().stream().sorted(Comparator.comparing(CosmeticCategory::getOrder)).toList();
    private final Map<CosmeticCategory, List<CosmeticGroup>> groupMap = HashMap.newHashMap(CosmeticCategory.getAssetMap().getAssetCount());
    private final Map<CosmeticGroup, List<CosmeticAsset>> cosmeticMap = HashMap.newHashMap(CosmeticAsset.getAssetMap().getAssetCount());

    private CosmeticCategory selectedCategory;
    private CosmeticGroup selectedGroup;
    private String searchQuery = "";

    private ServerCameraSettings cameraSettings = new ServerCameraSettings();

    public WardrobePage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, PageEventData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Wardrobe/Pages/Wardrobe.ui");

        for (int i = 0; i < categories.size(); i++) {
            CosmeticCategory category = categories.get(i);
            groupMap.put(category, new ArrayList<>());
            commandBuilder.append("#Categories", "Wardrobe/Pages/Tab.ui");
            String selector = "#Categories[" + i + "] #Button";
            commandBuilder.set(selector + " #Selected.Background", "(TexturePath: \"../Icons/Categories/General.png\")");
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Category", category.getId()), false);
        }

        List<CosmeticGroup> sortedGroups = CosmeticGroup.getAssetMap().getAssetMap().values().stream().sorted(Comparator.comparing(CosmeticGroup::getOrder)).toList();
        for (CosmeticGroup group : sortedGroups) {
            groupMap.getOrDefault(CosmeticCategory.getAssetMap().getAsset(group.getCategory()), new ArrayList<>()).add(group);
            cosmeticMap.put(group, new ArrayList<>());
        }

        List<CosmeticAsset> sortedCosmetics = CosmeticAsset.getAssetMap().getAssetMap().values().stream().sorted(Comparator.comparing(CosmeticAsset::getId)).toList();
        for (CosmeticAsset asset : sortedCosmetics) {
            cosmeticMap.get(CosmeticGroup.getAssetMap().getAsset(asset.getGroup())).add(asset);
        }

        CosmeticCategory selectedCategory = categories.getFirst();
        selectCategory(commandBuilder, eventBuilder, selectedCategory);

        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchField", EventData.of("@SearchQuery", "#SearchField.Value"), false);

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

        if (data.searchQuery != null) buildCosmeticList(commandBuilder, eventBuilder, data.searchQuery);
        if (data.category != null) selectCategory(commandBuilder, eventBuilder, CosmeticCategory.getAssetMap().getAsset(data.category));
        if (data.group != null) selectGroup(commandBuilder, eventBuilder, CosmeticGroup.getAssetMap().getAsset(data.group));
        if (data.cosmetic != null) wearCosmetic(commandBuilder, eventBuilder, CosmeticAsset.getAssetMap().getAsset(data.cosmetic));

        if (data.direction != null) {
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

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

    public void selectCategory(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, CosmeticCategory category) {
        for (int i = 0; i < categories.size(); i++) {
            commandBuilder.set("#Categories[" + i + "] #Button #Selected.Visible", false);
        }
        commandBuilder.set("#Categories[" + categories.indexOf(category) + "] #Button #Selected.Visible", true);

        commandBuilder.clear("#Groups");
        List<CosmeticGroup> groups = groupMap.get(category);
        for (int i = 0; i < groups.size(); i++) {
            CosmeticGroup group = groups.get(i);
            commandBuilder.append("#Groups", "Wardrobe/Pages/Tab.ui");
            // TODO: icon
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Groups[" + i + "] #Button", EventData.of("Group", group.getId()), false);
        }

        if (groups.isEmpty()) {
            commandBuilder.set("#CategoryLabelArrow.Visible", false);
            commandBuilder.set("#CategoryName.Text", "");
        } else {
            commandBuilder.set("#CategoryLabelArrow.Visible", true);
            commandBuilder.set("#CategoryName.Text", category.getId());
        }

        selectedCategory = category;

        CosmeticGroup selectedGroup = groups.getFirst();
        selectGroup(commandBuilder, eventBuilder, selectedGroup);
    }

    public void selectGroup(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, CosmeticGroup group) {
        List<CosmeticGroup> groups = groupMap.get(selectedCategory);
        for (int i = 0; i < groups.size(); i++) {
            commandBuilder.set("#Groups[" + i + "] #Button #Selected.Visible", false);
        }
        commandBuilder.set("#Groups[" + groups.indexOf(group) + "] #Button #Selected.Visible", true);
        commandBuilder.set("#SubCategoryName.Text", group.getId());
        selectedGroup = group;

        buildCosmeticList(commandBuilder, eventBuilder, searchQuery);
    }

    public void buildCosmeticList(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, String searchQuery) {
        commandBuilder.clear("#Cosmetics");

        List<CosmeticAsset> cosmetics = cosmeticMap.get(selectedGroup);
        if (!searchQuery.isEmpty()) {
            Object2IntMap<CosmeticAsset> map = new Object2IntOpenHashMap<>(cosmetics.size());

            for (CosmeticAsset value : cosmetics) {
                int fuzzyDistance = StringCompareUtil.getFuzzyDistance(value.getId(), searchQuery, Locale.ENGLISH);
                if (fuzzyDistance > 0) {
                    map.put(value, fuzzyDistance);
                }
            }

            cosmetics = map.keySet().stream().sorted().sorted(Comparator.comparingInt(map::getInt).reversed()).limit(20L).toList();
        }

        for (int i = 0; i < cosmetics.size(); i++) {
            CosmeticAsset cosmetic = cosmetics.get(i);
            commandBuilder.append("#Cosmetics", "Wardrobe/Pages/Cosmetic.ui");
            String selector = "#Cosmetics[" + i + "]";
            commandBuilder.set(selector + " #Button.Text", cosmetic.getId());
            // TODO: icon
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector + " #Button", EventData.of("Cosmetic", cosmetic.getId()), false);
        }

        // TODO: highlight cosmetics worn

        this.searchQuery = searchQuery;
    }

    public void wearCosmetic(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, CosmeticAsset cosmetic) {
        // TODO: apply cosmetic and reset cosmetic list
        buildCosmeticList(commandBuilder, eventBuilder, searchQuery);
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
