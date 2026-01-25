package dev.hardaway.wardrobe.impl.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.WardrobeTranslationProperties;
import dev.hardaway.wardrobe.api.cosmetic.AppearanceCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCategory;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.cosmetic.apperance.CosmeticAppearance;
import dev.hardaway.wardrobe.api.cosmetic.apperance.TextureConfig;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.impl.asset.CosmeticCategoryAsset;
import dev.hardaway.wardrobe.impl.asset.CosmeticSlotAsset;
import dev.hardaway.wardrobe.impl.asset.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.asset.cosmetic.appearance.VariantCosmeticAppearance;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.GradientTextureConfig;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.VariantTextureConfig;
import dev.hardaway.wardrobe.impl.system.CosmeticSaveData;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class WardrobePage extends InteractiveCustomUIPage<WardrobePage.PageEventData> {

    private final List<WardrobeCategory> categories = WardrobeCategory.getAssetMap().getAssetMap().values().stream().sorted(Comparator.comparing(WardrobeCategory::getTabOrder)).collect(Collectors.toUnmodifiableList());
    private final Map<String, List<WardrobeCosmeticSlot>> groupMap = new HashMap<>();
    private final Map<String, List<WardrobeCosmetic>> cosmeticMap = HashMap.newHashMap(WardrobeCosmetic.getAssetMap().getAssetCount());

    private String selectedCategory;
    private String selectedGroup;
    private String searchQuery = "";

    private PlayerWardrobeComponent baseWardrobe;

    private final ServerCameraSettings cameraSettings = new ServerCameraSettings();

    public WardrobePage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, PageEventData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Wardrobe/Pages/Wardrobe.ui");

        for (int i = 0; i < categories.size(); i++) {
            WardrobeCategory category = categories.get(i);
            if (!category.hasPermission(this.playerRef.getUuid()))
                continue;

            this.groupMap.put(category.getId(), new ArrayList<>());
            commandBuilder.append("#Categories", "Wardrobe/Pages/Tab.ui");
            String selector = "#Categories[" + i + "] #Button";
            commandBuilder.set(selector + " #Icon.AssetPath", category.getIconPath());
            commandBuilder.set(selector + " #Selected #Icon.AssetPath", category.getSelectedIconPath());
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Category", category.getId()), false);
        }

        List<CosmeticSlotAsset> sortedGroups = CosmeticSlotAsset.getAssetMap().getAssetMap().values().stream().sorted(Comparator.comparing(CosmeticSlotAsset::getTabOrder)).toList();
        for (CosmeticSlotAsset group : sortedGroups) {
            if (!group.hasPermission(this.playerRef.getUuid()))
                continue;

            groupMap.getOrDefault(group.getCategory().getId(), new ArrayList<>()).add(group);
            cosmeticMap.put(group.getId(), new ArrayList<>());
        }

        List<CosmeticAsset> sortedCosmetics = CosmeticAsset.getAssetMap().getAssetMap().values().stream().sorted(Comparator.comparing(CosmeticAsset::getId)).toList();
        for (CosmeticAsset cosmetic : sortedCosmetics) {
            if (!cosmetic.hasPermission(this.playerRef.getUuid()))
                continue;

            cosmeticMap.getOrDefault(cosmetic.getCosmeticSlotId(), new ArrayList<>()).add(cosmetic);
        }

        WardrobeCategory selectedCategory = categories.getFirst();
        selectCategory(commandBuilder, eventBuilder, ref, store, selectedCategory);

        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchField", EventData.of("@SearchQuery", "#SearchField.Value"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ResetAvatar", EventData.of("Action", "Reset"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#DiscardChanges", EventData.of("Action", "Discard"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SaveChanges", EventData.of("Action", "Save"), false);

        PlayerWardrobeComponent wardrobe = store.getComponent(ref, PlayerWardrobeComponent.getComponentType());
        if (wardrobe != null) baseWardrobe = wardrobe.clone();

        TransformComponent bodyRotation = store.getComponent(ref, TransformComponent.getComponentType());
        float yaw = bodyRotation.getRotation().y;
        cameraSettings.isFirstPerson = false;
        cameraSettings.eyeOffset = true;
        cameraSettings.displayCursor = true;
        cameraSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast;
        cameraSettings.distance = 2.5F;
        cameraSettings.positionOffset = new Position(0, 0, 0);
        cameraSettings.positionLerpSpeed = 0.2F;
        cameraSettings.rotationType = RotationType.Custom;
        cameraSettings.rotation = new Direction((float) (yaw + Math.PI), 0, 0);
        cameraSettings.rotationLerpSpeed = 0.2F;
        cameraSettings.mouseInputType = MouseInputType.LookAtPlane;
        cameraSettings.planeNormal = new Vector3f((float) Math.sin(yaw), 2, (float) Math.cos(yaw));

        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, cameraSettings));
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
            selectGroup(commandBuilder, eventBuilder, ref, store, CosmeticSlotAsset.getAssetMap().getAsset(data.group));
        if (data.cosmetic != null)
            selectCosmetic(commandBuilder, eventBuilder, ref, store, Objects.requireNonNull(CosmeticAsset.getAssetMap().getAsset(data.cosmetic)), null, null);
        if (data.variant != null)
            selectCosmetic(commandBuilder, eventBuilder, ref, store, null, data.variant, null);
        if (data.texture != null)
            selectCosmetic(commandBuilder, eventBuilder, ref, store, null, null, data.texture);
        if (data.action != null) {
            if (data.action.equals("Reset")) store.removeComponentIfExists(ref, PlayerWardrobeComponent.getComponentType());

            if (data.action.equals("Discard")) {
                if (baseWardrobe != null) store.putComponent(ref, PlayerWardrobeComponent.getComponentType(), baseWardrobe);

                close();
            }

            if (data.action.equals("Save")) {
                close();
            }
        }

        sendUpdate(commandBuilder, eventBuilder, false);
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        super.onDismiss(ref, store);
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.FirstPerson, false, null));
    }

    public void selectCosmetic(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Ref<EntityStore> ref, Store<EntityStore> store, @Nullable WardrobeCosmetic cosmetic, @Nullable String variant, @Nullable String texture) {
        PlayerWardrobeComponent wardrobeComponent = store.ensureAndGetComponent(ref, PlayerWardrobeComponent.getComponentType());
        PlayerCosmetic wornCosmetic = wardrobeComponent.getCosmetic(this.selectedGroup);

        if (wornCosmetic != null && (cosmetic != null && Objects.equals(wornCosmetic.getCosmeticId(), cosmetic.getId()))) {
            wardrobeComponent.removeCosmetic(this.selectedGroup);
        } else {
            if (cosmetic == null) {
                if (wornCosmetic == null) return;

                cosmetic = WardrobeCosmetic.getAssetMap().getAsset(wornCosmetic.getCosmeticId());
            }

            if (cosmetic instanceof AppearanceCosmetic a) {
                CosmeticAppearance appearance = a.getAppearance();
                List<String> variants = Arrays.asList(appearance.collectVariants());
                if (variant == null || !variants.contains(variant)) {
                    if (wornCosmetic != null && variants.contains(wornCosmetic.getVariantId()))
                        variant = wornCosmetic.getVariantId();
                    else if (!variants.isEmpty()) variant = variants.getFirst();
                }

                List<String> textures = Arrays.asList(appearance.getTextureConfig(variant).collectVariants());

                if (texture == null || !textures.contains(texture)) {
                    if (wornCosmetic != null && textures.contains(wornCosmetic.getTextureId()))
                        texture = wornCosmetic.getTextureId();
                    else if (!textures.isEmpty()) texture = textures.getFirst();
                }
            }

            wardrobeComponent.setCosmetic(selectedGroup, new CosmeticSaveData(cosmetic.getId(), variant, texture));
        }

        wardrobeComponent.rebuild();
        buildCosmeticList(commandBuilder, eventBuilder, ref, store, searchQuery);
    }

    public void buildCosmeticList(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Ref<EntityStore> ref, Store<EntityStore> store, String searchQuery) {
        commandBuilder.clear("#Cosmetics");
        commandBuilder.clear("#Colors");
        Anchor anchor = new Anchor();
        anchor.setHeight(Value.of((int) (150 * 4.8 + 10 * (4.8 - 1) + 14)));
        anchor.setTop(Value.of(10));
        commandBuilder.setObject("#Cosmetics.Anchor", anchor);
        commandBuilder.set("#ColorsContainer.Visible", false);
        commandBuilder.set("#VariantsContainer.Visible", false);
        commandBuilder.set("#VariantsDropdown.Visible", false);
        commandBuilder.clear("#VariantsButtons");

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

        PlayerWardrobeComponent wardrobeComponent = store.getComponent(ref, PlayerWardrobeComponent.getComponentType());
        PlayerCosmetic wornCosmetic = wardrobeComponent == null ? null : wardrobeComponent.getCosmetic(this.selectedGroup);

        for (int i = 0; i < cosmetics.size(); i++) {
            WardrobeCosmetic cosmetic = cosmetics.get(i);
            commandBuilder.append("#Cosmetics", "Wardrobe/Pages/Cosmetic.ui");
            String selector = "#Cosmetics[" + i + "]";
            commandBuilder.set(selector + " #Button.Text", cosmetic.getTranslationProperties().getName());
            if (cosmetic.getIconPath() != null)
                commandBuilder.set(selector + " #Icon.AssetPath", cosmetic.getIconPath());
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector + " #Button", EventData.of("Cosmetic", cosmetic.getId()), false);

            if (wornCosmetic != null && Objects.equals(wornCosmetic.getCosmeticId(), cosmetic.getId()) && cosmetic instanceof AppearanceCosmetic appearanceCosmetic) {
                CosmeticAppearance appearance = appearanceCosmetic.getAppearance();
                String[] variants = appearance.collectVariants();
                if (variants.length > 0) {
                    commandBuilder.set("#VariantsContainer.Visible", true);

                    Map<String, VariantCosmeticAppearance.Entry> variantMap = null;
                    if (appearance instanceof VariantCosmeticAppearance v) {
                        variantMap = v.getVariants();
                    }

                    boolean useOptions = variantMap.values().stream().anyMatch(entry -> entry.getIconPath() != null);

                    ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList<>();
                    for (int c = 0; c < variants.length; c++) {
                        String variant = variants[c];
                        WardrobeTranslationProperties translationProperties = variantMap.get(variant).getTranslationProperties();

                        if (useOptions) {
                            String optionsSelector = "#VariantsButtons[" + c + "]";
                            commandBuilder.append("#VariantsButtons", "Wardrobe/Pages/ColorOption.ui");
                            commandBuilder.set(optionsSelector + " #Button #Icon.AssetPath", Optional.ofNullable(variantMap.get(variant).getIconPath()).orElse(""));

                            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, optionsSelector + " #Button", EventData.of("Variant", variant), false);
                        } else {
                            entries.add(new DropdownEntryInfo(LocalizableString.fromMessageId(translationProperties.getName().getMessageId()), variant));

                            if (wornCosmetic.getVariantId().equals(variant)) {
                                commandBuilder.set("#VariantsDropdown.Value", variant);
                            }
                        }
                    }

                    if (!useOptions) {
                        commandBuilder.set("#VariantsDropdown.Visible", true);
                        commandBuilder.set("#VariantsDropdown.Entries", entries);
                        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#VariantsDropdown", EventData.of("@Variant", "#VariantsDropdown.Value"));
                    }
                }

                TextureConfig textureConfig = appearance.getTextureConfig(wornCosmetic.getVariantId());
                String[] textures = textureConfig.collectVariants();
                if (textures.length > 0) {
                    commandBuilder.set("#ColorsContainer.Visible", true);

                    for (int c = 0; c < textures.length; c++) {
                        String texture = textures[c];
                        String[] colors = null;

                        if (textureConfig instanceof VariantTextureConfig v) {
                            colors = v.getVariants().get(texture).getColors();
                        }

                        if (textureConfig instanceof GradientTextureConfig g) {
                            colors = CosmeticsModule.get().getRegistry().getGradientSets().get(g.getGradientSet()).getGradients().get(texture).getBaseColor();
                        }

                        String colorSelector = "#Colors[" + c + "]";
                        commandBuilder.append("#Colors", "Wardrobe/Pages/ColorOption.ui");
                        // TODO: color stripes
                        commandBuilder.set(colorSelector + " #Button #Colors.Background", colors[0]);
//                        for (int n = 0; n < colors.length; n++) {
//                            commandBuilder.append(colorSelector + " #Button #Colors", "Wardrobe/Pages/Color.ui");
//                            commandBuilder.set(colorSelector + " #Button #Colors[" + n + "].Background", colors[n]);
//                        }

                        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, colorSelector + " #Button", EventData.of("Texture", texture), false);

                        if (wornCosmetic.getTextureId().equals(texture)) {
                            commandBuilder.set(colorSelector + " #Button #SelectedHighlight.Visible", true);
                        }
                    }
                }

                if (variants.length > 0 || textures.length > 0) {
                    anchor.setHeight(Value.of((int) (150 * 3.5 + 10 * (3.5 - 1) + 14)));
                    commandBuilder.setObject("#Cosmetics.Anchor", anchor);
                }
            }
        }

        this.searchQuery = searchQuery;
    }

    public void selectGroup(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Ref<EntityStore> ref, Store<EntityStore> store, WardrobeCosmeticSlot group) {
        List<WardrobeCosmeticSlot> groups = groupMap.get(selectedCategory);
        for (int i = 0; i < groups.size(); i++) {
            commandBuilder.set("#Groups[" + i + "] #Button #Selected.Visible", false);
        }
        commandBuilder.set("#Groups[" + groups.indexOf(group) + "] #Button #Selected.Visible", true);
        commandBuilder.set("#SubCategoryName.Text", group.getTranslationProperties().getName());
        selectedGroup = group.getId();

        buildCosmeticList(commandBuilder, eventBuilder, ref, store, searchQuery);
    }

    public void selectCategory(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Ref<EntityStore> ref, Store<EntityStore> store, WardrobeCategory category) {
        for (int i = 0; i < categories.size(); i++) {
            commandBuilder.set("#Categories[" + i + "] #Button #Selected.Visible", false);
        }
        commandBuilder.set("#Categories[" + categories.indexOf(category) + "] #Button #Selected.Visible", true);

        commandBuilder.clear("#Groups");
        List<WardrobeCosmeticSlot> groups = groupMap.get(category.getId());
        for (int i = 0; i < groups.size(); i++) {
            WardrobeCosmeticSlot group = groups.get(i);
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
            commandBuilder.set("#CategoryName.Text", category.getTranslationProperties().getName());
        }

        selectedCategory = category.getId();

        WardrobeCosmeticSlot selectedGroup = groups.getFirst();
        selectGroup(commandBuilder, eventBuilder, ref, store, selectedGroup);
    }

    public static class PageEventData {
        public static final BuilderCodec<PageEventData> CODEC = BuilderCodec.builder(PageEventData.class, PageEventData::new)
                .append(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, (entry) -> entry.searchQuery).add()
                .append(new KeyedCodec<>("Category", Codec.STRING), (entry, s) -> entry.category = s, (entry) -> entry.category).add()
                .append(new KeyedCodec<>("Group", Codec.STRING), (entry, s) -> entry.group = s, (entry) -> entry.group).add()
                .append(new KeyedCodec<>("Cosmetic", Codec.STRING), (entry, s) -> entry.cosmetic = s, (entry) -> entry.cosmetic).add()
                .append(new KeyedCodec<>("@Variant", Codec.STRING), (entry, s) -> entry.variant = s, (entry) -> entry.variant).add()
                .append(new KeyedCodec<>("Texture", Codec.STRING), (entry, s) -> entry.texture = s, (entry) -> entry.texture).add()
                .append(new KeyedCodec<>("Action", Codec.STRING), (entry, s) -> entry.action = s, (entry) -> entry.action).add()
                .build();

        private String searchQuery;
        private String category;
        private String group;
        private String cosmetic;
        private String variant;
        private String texture;
        private String action;
    }
}
