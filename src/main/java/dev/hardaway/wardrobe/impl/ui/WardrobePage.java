package dev.hardaway.wardrobe.impl.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
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
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.appearance.AppearanceCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeTab;
import dev.hardaway.wardrobe.api.cosmetic.appearance.CosmeticAppearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.menu.WardrobeMenu;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.asset.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.asset.cosmetic.appearance.VariantCosmeticAppearance;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.GradientTextureConfig;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.VariantTextureConfig;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class WardrobePage extends InteractiveCustomUIPage<WardrobePage.PageEventData> {

    private static final int COSMETICS_PER_ROW = 5;
    private static final int OPTIONS_PER_ROW = 13;

    private WardrobeMenu menu;
    private PlayerWardrobe baseWardrobe;
    private boolean shouldClose = true;

    public WardrobePage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, PageEventData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Wardrobe/Pages/Wardrobe.ui");

        menu = new WardrobeMenu(playerRef.getUuid());

        buildCosmetics(commandBuilder, eventBuilder, ref, store);
        this.buildWardrobeTabs(commandBuilder, eventBuilder);

        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchField", EventData.of("@SearchQuery", "#SearchField.Value"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ResetAvatar", MenuAction.Reset.getEvent(), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#DiscardChanges", MenuAction.Discard.getEvent(), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SaveChanges", MenuAction.Save.getEvent(), false);

        PlayerWardrobe wardrobe = store.getComponent(ref, PlayerWardrobe.getComponentType());
        if (wardrobe != null) baseWardrobe = wardrobe.clone();

        setupCamera(ref, store);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PageEventData data) {
        super.handleDataEvent(ref, store, data);

        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

        if (data.searchQuery != null) {
            menu.setSearchQuery(data.searchQuery);
            buildCosmetics(commandBuilder, eventBuilder, ref, store);
        }

        if (data.category != null) {
            menu.selectCategory(data.category);
            this.buildWardrobeTabs(commandBuilder, eventBuilder);

            buildCosmetics(commandBuilder, eventBuilder, ref, store);
        }

        if (data.slot != null) {
            menu.selectSlot(data.slot);
            this.buildTabs(commandBuilder, eventBuilder, "Slots", s -> s.equals(this.menu.getSelectedSlot()), this.menu.getSlots().toArray(WardrobeTab[]::new));
            buildCosmetics(commandBuilder, eventBuilder, ref, store);
        }

        if (data.cosmetic != null || data.variant != null || data.texture != null) {
            PlayerWardrobe wardrobe = store.ensureAndGetComponent(ref, PlayerWardrobe.getComponentType());
            if (menu.selectCosmetic(wardrobe,
                    data.cosmetic != null ? CosmeticAsset.getAssetMap().getAsset(data.cosmetic) : null,
                    data.variant,
                    data.texture)) {
                buildCosmetics(commandBuilder, eventBuilder, ref, store);
                shouldClose = false;
            }
        }

        switch (data.action) {
            case null -> {} // Do nothing
            case Reset -> {
                if (store.removeComponentIfExists(ref, PlayerWardrobe.getComponentType())) {
                    buildCosmetics(commandBuilder, eventBuilder, ref, store);
                    shouldClose = baseWardrobe == null;
                }
            }
            case Discard -> {
                if (baseWardrobe != null)
                    store.putComponent(ref, PlayerWardrobe.getComponentType(), (PlayerWardrobeComponent) baseWardrobe);
                else store.removeComponentIfExists(ref, PlayerWardrobe.getComponentType());

                shouldClose = true;
                close();
            }
            case Save -> {
                shouldClose = true;
                close();
            }
        }

        sendUpdate(commandBuilder, eventBuilder, false);
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        if (!shouldClose) {
            shouldClose = true;
            store.getExternalData().getWorld().execute(() -> {
                Player player = store.getComponent(ref, Player.getComponentType());
                player.getPageManager().openCustomPage(ref, store, new WardrobeDismissPage(playerRef, CustomPageLifetime.CantClose, baseWardrobe));
            });
            return;
        }

        super.onDismiss(ref, store);
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.FirstPerson, false, null));
    }

    private void setupCamera(Ref<EntityStore> ref, Store<EntityStore> store) {
        TransformComponent bodyRotation = store.getComponent(ref, TransformComponent.getComponentType());
        float yaw = bodyRotation.getRotation().y;

        ServerCameraSettings cameraSettings = new ServerCameraSettings();
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

    private void buildWardrobeTabs(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder) {
        this.buildTabs(commandBuilder, eventBuilder, "Categories", s -> s.equals(this.menu.getSelectedCategory()), this.menu.getCategories().toArray(WardrobeTab[]::new));
        this.buildTabs(commandBuilder, eventBuilder, "Slots", s -> s.equals(this.menu.getSelectedSlot()), this.menu.getSlots().toArray(WardrobeTab[]::new));
    }

    private void buildTabs(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, String tabGroup, Predicate<String> selected, WardrobeTab... tabs) {
        String tabSelector = "#" + tabGroup;
        commandBuilder.clear(tabSelector);

        for (int i = 0; i < tabs.length; i++) {
            WardrobeTab tab = tabs[i];
            commandBuilder.append(tabSelector, "Wardrobe/Pages/Tab.ui");

            String selector = tabSelector + "[" + i + "] #Button";
            commandBuilder.set(selector + " #Icon.AssetPath", tab.getIconPath());

            if (selected.test(tab.getId())) {
                commandBuilder.set(selector + " #Selected #Icon.AssetPath", tab.getSelectedIconPath());
                commandBuilder.set(selector + " #Selected.Visible", true);
                commandBuilder.set("#SubCategoryName.Text", tab.getTranslationProperties().getName());
            }

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector,
                    EventData.of(tabGroup, tab.getId()),
                    false
            );
        }
    }

    private void buildCosmetics(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Ref<EntityStore> ref, Store<EntityStore> store) {
        commandBuilder.clear("#Cosmetics");
        commandBuilder.clear("#Colors");

        commandBuilder.set("#ColorsContainer.Visible", false);
        commandBuilder.set("#VariantsContainer.Visible", false);
        commandBuilder.set("#VariantsDropdown.Visible", false);

        Anchor anchor = new Anchor();
        anchor.setTop(Value.of(10));
        int anchorHeight = (int) (150 * 4.8 + 10 * (4.8 - 1) + 14);

        List<WardrobeCosmetic> cosmetics = menu.getCosmetics();
        PlayerCosmetic worn = Optional.ofNullable(store.getComponent(ref, PlayerWardrobe.getComponentType()))
                .map(w -> w.getCosmetic(menu.getSelectedSlot()))
                .orElse(null);

        int row = -1;
        for (int i = 0; i < cosmetics.size(); i++) {
            if (i % COSMETICS_PER_ROW == 0) {
                commandBuilder.append("#Cosmetics", "Wardrobe/Pages/CosmeticRow.ui");
                row++;
            }

            WardrobeCosmetic cosmetic = cosmetics.get(i);
            int col = i % COSMETICS_PER_ROW;

            commandBuilder.append("#Cosmetics[" + row + "] #CosmeticsInRow", "Wardrobe/Pages/Cosmetic.ui");
            String selector = "#Cosmetics[" + row + "] #CosmeticsInRow[" + col + "]";

            WardrobeTranslationProperties translationProperties = cosmetic.getTranslationProperties();
            Message tooltip = Message.empty();

            tooltip.insert(translationProperties.getName().bold(true));
            tooltip.insert("\n");
            Player player = store.ensureAndGetComponent(ref, Player.getComponentType());
            if (player.getGameMode() == GameMode.Creative) tooltip.insert(Message.raw("ID: " + cosmetic.getId()).color(Color.LIGHT_GRAY).italic(true));

            if (translationProperties.getDescriptionKey() != null) {
                tooltip.insert("\n");
                tooltip.insert(cosmetic.getTranslationProperties().getDescription().color(Color.LIGHT_GRAY));
            }

            if (cosmetic.getIconPath() != null)
                commandBuilder.set(selector + " #Icon.AssetPath", cosmetic.getIconPath());

            if (cosmetic.hasPermission(playerRef.getUuid())) {
                eventBuilder.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        selector + " #Button",
                        EventData.of("Cosmetic", cosmetic.getId()),
                        false
                );
            } else {
                commandBuilder.set(selector + " #Locked.Visible", true);
            }

            if (worn != null && cosmetic.getId().equals(worn.getCosmeticId())) {
                commandBuilder.set(selector + " #Selected.Visible", true);
                if (cosmetic instanceof AppearanceCosmetic a) {
                    boolean options = buildOptions(commandBuilder, eventBuilder, worn, a);
                    boolean colors = buildColors(commandBuilder, eventBuilder, worn, a);
                    if (options || colors) anchorHeight = (int) (150 * 3.5 + 10 * (3.5 - 1) + 14);
                }
            }

            if (cosmetic instanceof AppearanceCosmetic a && a.getAppearance() instanceof VariantCosmeticAppearance v && worn != null && worn.getVariantId() != null) {
                VariantCosmeticAppearance.Entry entry = v.getVariants().get(worn.getVariantId());
                if (entry != null && entry.getIconPath() != null) {
                    commandBuilder.set(selector + " #Icon.AssetPath", entry.getIconPath());
                    tooltip = Message.empty();

                    tooltip.insert(translationProperties.getName().bold(true));
                    tooltip.insert("\n");
                    if (player.getGameMode() == GameMode.Creative) tooltip.insert(Message.raw("ID: " + cosmetic.getId()).color(Color.LIGHT_GRAY).italic(true));

                    WardrobeTranslationProperties newTranslation = entry.getTranslationProperties();

                    tooltip.insert("\n");
                    tooltip.insert(Message.raw("Variant: ").color(Color.LIGHT_GRAY).italic(true));
                    tooltip.insert(newTranslation.getName().color(Color.LIGHT_GRAY).italic(true));

                    if (newTranslation.getDescriptionKey() != null) {
                        tooltip.insert("\n");
                        tooltip.insert(newTranslation.getDescription().color(Color.LIGHT_GRAY));
                    } else if (translationProperties.getDescriptionKey() != null) {
                        tooltip.insert("\n");
                        tooltip.insert(translationProperties.getDescription().color(Color.LIGHT_GRAY));
                    }
                }
            }

            commandBuilder.set(selector + " #Button.TooltipTextSpans", tooltip);
        }

        anchor.setHeight(Value.of(anchorHeight));
        commandBuilder.setObject("#Cosmetics.Anchor", anchor);
    }

    private boolean buildOptions(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, PlayerCosmetic wornCosmetic, AppearanceCosmetic cosmetic) {
        CosmeticAppearance appearance = cosmetic.getAppearance();
        String[] variants = appearance.collectVariants();
        if (variants.length == 0) return false;

        commandBuilder.set("#VariantsContainer.Visible", true);

        Map<String, VariantCosmeticAppearance.Entry> variantMap =
                appearance instanceof VariantCosmeticAppearance v ? v.getVariants() : Map.of();

        ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList<>();
        for (String variant : variants) {
            entries.add(new DropdownEntryInfo(
                    LocalizableString.fromMessageId(
                            variantMap.get(variant).getTranslationProperties().getName().getMessageId()
                    ),
                    variant
            ));
        }

        commandBuilder.set("#VariantsDropdown.Visible", true);
        commandBuilder.set("#VariantsDropdown.Entries", entries);
        commandBuilder.set("#VariantsDropdown.Value", wornCosmetic.getVariantId());

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#VariantsDropdown",
                EventData.of("@Variant", "#VariantsDropdown.Value")
        );

        return true;
    }

    private boolean buildColors(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, PlayerCosmetic wornCosmetic, AppearanceCosmetic cosmetic) {
        CosmeticAppearance appearance = cosmetic.getAppearance();

        TextureConfig textureConfig = appearance.getTextureConfig(wornCosmetic.getVariantId());

        String[] textures = textureConfig.collectVariants();
        if (textures.length == 0) return false;

        commandBuilder.set("#ColorsContainer.Visible", true);

        int row = -1;
        for (int i = 0; i < textures.length; i++) {
            String texture = textures[i];
            if (i % OPTIONS_PER_ROW == 0) {
                commandBuilder.append("#Colors", "Wardrobe/Pages/ColorOptionRow.ui");
                row++;
            }

            commandBuilder.append("#Colors[" + row + "] #ColorOptionsInRow", "Wardrobe/Pages/ColorOption.ui");
            String selector = "#Colors[" + row + "] #ColorOptionsInRow[" + (i % OPTIONS_PER_ROW) + "]";

            String[] baseColor = textureConfig instanceof VariantTextureConfig vt ? vt.getVariants().get(texture).getColors() :
                    CosmeticsModule.get().getRegistry().getGradientSets().get(((GradientTextureConfig) textureConfig).getGradientSet()).getGradients().get(texture).getBaseColor();

            // TODO: color stripes
            for (int c = 0; c < baseColor.length; c++) {
                String color = baseColor[c];
                commandBuilder.append(selector + " #Button #Colors", "Wardrobe/Pages/Color.ui");
                commandBuilder.set(selector + " #Button #Colors[" + c + "].Background", color);
            }

            if (texture.equals(wornCosmetic.getTextureId()))
                commandBuilder.set(selector + " #Button #SelectedHighlight.Visible", true);

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #Button",
                    EventData.of("Texture", texture),
                    false
            );
        }

        return true;
    }

    public static class PageEventData {
        public static final BuilderCodec<PageEventData> CODEC = BuilderCodec.builder(PageEventData.class, PageEventData::new)
                .append(new KeyedCodec<>("@SearchQuery", Codec.STRING), (e, v) -> e.searchQuery = v, e -> e.searchQuery).add()
                .append(new KeyedCodec<>("Categories", Codec.STRING), (e, v) -> e.category = v, e -> e.category).add()
                .append(new KeyedCodec<>("Slots", Codec.STRING), (e, v) -> e.slot = v, e -> e.slot).add()
                .append(new KeyedCodec<>("Cosmetic", Codec.STRING), (e, v) -> e.cosmetic = v, e -> e.cosmetic).add()
                .append(new KeyedCodec<>("@Variant", Codec.STRING), (e, v) -> e.variant = v, e -> e.variant).add()
                .append(new KeyedCodec<>("Texture", Codec.STRING), (e, v) -> e.texture = v, e -> e.texture).add()
                .append(new KeyedCodec<>("Action", new EnumCodec<>(MenuAction.class)), (e, v) -> e.action = v, e -> e.action).add()
                .build();

        private String searchQuery;
        private String category;
        private String slot;
        private String cosmetic;
        private String variant;
        private String texture;
        private MenuAction action;
    }

    enum MenuAction {
        Reset, Discard, Save;

        private final EventData eventData;

        MenuAction() {
            this.eventData = EventData.of("Action", this.name());
        }

        public EventData getEvent() {
            return eventData;
        }
    }
}