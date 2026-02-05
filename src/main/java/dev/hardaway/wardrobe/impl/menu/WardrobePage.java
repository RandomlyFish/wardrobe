package dev.hardaway.wardrobe.impl.menu;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.WardrobeUtil;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.menu.WardrobeTab;
import dev.hardaway.wardrobe.api.menu.variant.CosmeticOptionEntry;
import dev.hardaway.wardrobe.api.menu.variant.CosmeticVariantEntry;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.property.WardrobeTranslationProperties;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticSlotAsset;
import dev.hardaway.wardrobe.impl.player.PlayerWardrobeComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class WardrobePage extends InteractiveCustomUIPage<WardrobePage.PageEventData> {

    private static final int COSMETICS_PER_ROW = 5;
    private static final int VARIANTS_PER_ROW = 13;

    private final WardrobeMenu menu;
    private final Position position;
    private final int rotationIndex;
    private final ServerCameraSettings cameraSettings;
    private boolean shouldClose = true;

    public WardrobePage(@Nonnull PlayerRef playerRef, PlayerWardrobeComponent wardrobe, @Nullable Position position, int rotationIndex) {
        super(playerRef, CustomPageLifetime.CanDismiss, PageEventData.CODEC);
        this.menu = new WardrobeMenu(playerRef.getUuid(), wardrobe);
        this.position = position;
        if (this.position != null) {
            this.position.x += 0.5;
            this.position.y += 1.5;
            this.position.z += 0.5;
        }

        this.rotationIndex = rotationIndex+2;
        this.cameraSettings = new ServerCameraSettings();
        cameraSettings.isFirstPerson = false;
        cameraSettings.positionLerpSpeed = 0.2F;
        cameraSettings.rotationType = RotationType.Custom;
        cameraSettings.rotationLerpSpeed = 1;
        cameraSettings.mouseInputType = MouseInputType.LookAtPlane;
        cameraSettings.displayCursor = true;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Wardrobe/Pages/Wardrobe.ui");

        buildWardrobeTabs(commandBuilder, eventBuilder, ref, store);

        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchField", EventData.of("@SearchQuery", "#SearchField.Value"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ResetAvatar", MenuAction.Reset.getEvent(), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Discard", MenuAction.Discard.getEvent(), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Save", MenuAction.Save.getEvent(), false);

        if (position != null) {
            commandBuilder.set("#Mirror.Visible", true);
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Camera", MenuAction.Camera.getEvent(), false);
        }

        changeCamera(ref, store);
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
            buildWardrobeTabs(commandBuilder, eventBuilder, ref, store);
        }

        if (data.slot != null) {
            menu.selectSlot(data.slot);
            buildTabs(commandBuilder, eventBuilder, "Slots", s -> s.equals(menu.getSelectedSlot()), menu.getSlots().toArray(WardrobeTab[]::new));
            buildCheckbox(commandBuilder, eventBuilder);
            buildCosmetics(commandBuilder, eventBuilder, ref, store);
        }

        if (data.cosmetic != null || data.option != null || data.variant != null) {
            menu.selectCosmetic(
                    data.cosmetic != null ? CosmeticAsset.getAssetMap().getAsset(data.cosmetic) : null,
                    data.option, data.variant
            );
            buildCosmetics(commandBuilder, eventBuilder, ref, store);
        }


        if (data.hideType != null) {
            menu.toggleCosmeticType();
            buildCheckbox(commandBuilder, eventBuilder);
        }

        switch (data.action) {
            case null -> {
            } // Do nothing
            case Reset -> {
                menu.getWardrobe().clearCosmetics();
                menu.getWardrobe().getHiddenCosmeticTypes().clear(); // TODO: replace this with proper api method
                menu.getWardrobe().rebuild();
                buildCosmetics(commandBuilder, eventBuilder, ref, store);
            }
            case Discard -> {
                store.putComponent(ref, PlayerWardrobeComponent.getComponentType(), menu.getBaseWardrobe());

                shouldClose = true;
                close();
            }
            case Save -> {
                shouldClose = true;
                close();
            }
            case Camera -> changeCamera(ref, store);
        }

        shouldClose = menu.getWardrobe().equals(menu.getBaseWardrobe());
        sendUpdate(commandBuilder, eventBuilder, false);
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        if (!shouldClose) {
            shouldClose = true;
            store.getExternalData().getWorld().execute(() -> {
                Player player = store.ensureAndGetComponent(ref, Player.getComponentType());
                player.getPageManager().openCustomPage(ref, store, new WardrobeDismissPage(playerRef, CustomPageLifetime.CantClose, menu.getBaseWardrobe()));
            });
            return;
        }

        super.onDismiss(ref, store);
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.FirstPerson, false, null));
    }

    private void changeCamera(Ref<EntityStore> ref, Store<EntityStore> store) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        float rotation;

        if (cameraSettings.displayCursor && position != null) {
            rotation = (float) ((Math.PI / 2) * rotationIndex);

            cameraSettings.displayCursor = false;
            cameraSettings.positionType = PositionType.Custom;
            cameraSettings.position = position;

            store.getExternalData().getWorld().execute(() -> {
                transform.getRotation().setY((float) (rotation + Math.PI));
                Teleport teleport = Teleport.createForPlayer(transform.getTransform());
                store.addComponent(ref, Teleport.getComponentType(), teleport);
            });
        } else {
            rotation = transform.getRotation().y;

            cameraSettings.displayCursor = true;
            cameraSettings.positionType = PositionType.AttachedToPlusOffset;

            cameraSettings.eyeOffset = true;
            cameraSettings.distance = 3F;
            cameraSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast;
            cameraSettings.planeNormal = new Vector3f((float) Math.sin(rotation), -2f, (float) Math.cos(rotation));
        }

        cameraSettings.rotation = new Direction(rotation, 0, 0);

        playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, false, cameraSettings)
        );
    }

    private void buildWardrobeTabs(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Ref<EntityStore> ref, Store<EntityStore> store) {
        buildTabs(commandBuilder, eventBuilder, "Categories", s -> s.equals(menu.getSelectedCategory()), menu.getCategories().toArray(WardrobeTab[]::new));
        buildTabs(commandBuilder, eventBuilder, "Slots", s -> s.equals(menu.getSelectedSlot()), menu.getSlots().toArray(WardrobeTab[]::new));
        buildCheckbox(commandBuilder, eventBuilder);
        buildCosmetics(commandBuilder, eventBuilder, ref, store);
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
                commandBuilder.set(tabSelector + "Name.Text", tab.getProperties().getTranslationProperties().getName());
            }

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector,
                    EventData.of(tabGroup, tab.getId()),
                    false
            );
        }
    }

    private void buildCheckbox(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder) {
        CosmeticSlotAsset slot = CosmeticSlotAsset.getAssetMap().getAsset(menu.getSelectedSlot());
        if (slot != null && slot.getHytaleCosmeticType() != null && WardrobeUtil.canBeHidden(slot.getHytaleCosmeticType())) {
            commandBuilder.set("#HideType.Visible", true);
            commandBuilder.set("#HideType.Value", menu.getWardrobe().getHiddenCosmeticTypes().contains(slot.getHytaleCosmeticType()));
            eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#HideType", EventData.of("@HideType", "#HideType.Value"));
        } else {
            commandBuilder.set("#HideType.Visible", false);
        }
    }

    private void buildCosmetics(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Ref<EntityStore> ref, Store<EntityStore> store) {
        commandBuilder.clear("#Cosmetics");
        commandBuilder.clear("#Variants");

        commandBuilder.set("#VariantsContainer.Visible", false);
        commandBuilder.set("#OptionsContainer.Visible", false);
        commandBuilder.set("#OptionsDropdown.Visible", false);

        Anchor anchor = new Anchor();
        anchor.setTop(Value.of(10));
        int anchorHeight = (int) (150 * 4.8 + 10 * (4.8 - 1) + 14);

        List<WardrobeCosmetic> cosmetics = menu.getCosmetics();
        PlayerCosmetic worn = menu.getWardrobe().getCosmetic(menu.getSelectedSlot());

        int row = -1;
        for (int i = 0; i < cosmetics.size(); i++) {
            if (i % COSMETICS_PER_ROW == 0) {
                commandBuilder.append("#Cosmetics", "Wardrobe/Pages/Row.ui");
                row++;
            }

            WardrobeCosmetic cosmetic = cosmetics.get(i);
            int col = i % COSMETICS_PER_ROW;

            commandBuilder.append("#Cosmetics[" + row + "] #Row", "Wardrobe/Pages/Cosmetic.ui");
            String selector = "#Cosmetics[" + row + "] #Row[" + col + "]";

            WardrobeTranslationProperties translationProperties = cosmetic.getProperties().getTranslationProperties();
            Message tooltip = Message.empty();
            tooltip.insert(translationProperties.getName().bold(true));

            Player player = store.ensureAndGetComponent(ref, Player.getComponentType());
            if (player.getGameMode() == GameMode.Creative) {
                tooltip.insert("\n");
                tooltip.insert(Message.raw("ID: " + cosmetic.getId()).color(Color.LIGHT_GRAY).italic(true));
            }

            if (!translationProperties.getDescriptionKey().isEmpty()) {
                tooltip.insert("\n");
                tooltip.insert(cosmetic.getProperties().getTranslationProperties().getDescription().color(Color.LIGHT_GRAY));
            }

            if (cosmetic.getIconPath() != null)
                commandBuilder.set(selector + " #Icon.AssetPath", cosmetic.getIconPath());

            if (cosmetic.getProperties().hasPermission(playerRef.getUuid())) {
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
                boolean options = buildOptions(commandBuilder, eventBuilder, worn, cosmetic);
                boolean colors = buildVariants(commandBuilder, eventBuilder, worn, cosmetic);
                if (options || colors) anchorHeight = (int) (150 * 3.5 + 10 * (3.5 - 1) + 14);
            }

            if (worn != null && worn.getOptionId() != null) {
                CosmeticOptionEntry entry = cosmetic.getOptionEntries().get(worn.getOptionId());
                if (entry != null) {
                    String variantIconPath = entry.icon();
                    if (variantIconPath != null) {
                        commandBuilder.set(selector + " #Icon.AssetPath", variantIconPath);
                        tooltip = Message.empty();
                        tooltip.insert(translationProperties.getName().bold(true));

                        if (player.getGameMode() == GameMode.Creative) {
                            tooltip.insert("\n");
                            tooltip.insert(Message.raw("ID: " + cosmetic.getId()).color(Color.LIGHT_GRAY).italic(true));
                        }

                        WardrobeTranslationProperties newTranslation = entry.properties().getTranslationProperties();

                        if (newTranslation != null) {
                            tooltip.insert("\n");
                            tooltip.insert(Message.raw("Option: ").color(Color.LIGHT_GRAY).italic(true));
                            tooltip.insert(newTranslation.getName().color(Color.LIGHT_GRAY).italic(true));

                            if (!newTranslation.getDescriptionKey().isEmpty()) {
                                tooltip.insert("\n");
                                tooltip.insert(newTranslation.getDescription().color(Color.LIGHT_GRAY));
                            } else if (!translationProperties.getDescriptionKey().isEmpty()) {
                                tooltip.insert("\n");
                                tooltip.insert(translationProperties.getDescription().color(Color.LIGHT_GRAY));
                            }
                        }
                    }
                }
            }

            if (tooltip.getChildren().size() > 1) commandBuilder.set(selector + " #Button.TooltipTextSpans", tooltip);
            else commandBuilder.set(selector + " #Button.TooltipText", tooltip.getChildren().getFirst());
        }

        anchor.setHeight(Value.of(anchorHeight));
        commandBuilder.setObject("#Cosmetics.Anchor", anchor);
    }

    private boolean buildOptions(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, PlayerCosmetic wornCosmetic, WardrobeCosmetic cosmetic) {
        Map<String, CosmeticOptionEntry> optionEntries = cosmetic.getOptionEntries();
        if (optionEntries.isEmpty()) return false;

        commandBuilder.set("#OptionsContainer.Visible", true);

        ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList<>();
        for (CosmeticOptionEntry option : optionEntries.values()) {
            entries.add(new DropdownEntryInfo(
                    LocalizableString.fromMessageId(
                            option.properties().getTranslationProperties().getName().getMessageId()
                    ),
                    option.id()
            ));
        }

        commandBuilder.set("#OptionsDropdown.Visible", true);
        commandBuilder.set("#OptionsDropdown.Entries", entries);
        commandBuilder.set("#OptionsDropdown.Value", wornCosmetic.getOptionId());

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#OptionsDropdown",
                EventData.of("@Variant", "#OptionsDropdown.Value")
        );

        return true;
    }

    private boolean buildVariants(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, PlayerCosmetic wornCosmetic, WardrobeCosmetic cosmetic) {
        List<CosmeticVariantEntry> variantEntries = cosmetic.getVariantEntries(wornCosmetic.getOptionId());
        if (variantEntries.isEmpty()) return false;

        commandBuilder.set("#VariantsContainer.Visible", true);

        int row = -1;
        for (int i = 0; i < variantEntries.size(); i++) {
            CosmeticVariantEntry variant = variantEntries.get(i);
            if (i % VARIANTS_PER_ROW == 0) {
                commandBuilder.append("#Variants", "Wardrobe/Pages/Row.ui");
                row++;
            }

            commandBuilder.append("#Variants[" + row + "] #Row", "Wardrobe/Pages/Variant.ui");
            String selector = "#Variants[" + row + "] #Row[" + (i % VARIANTS_PER_ROW) + "]";

            if (variant.properties().getTranslationProperties() != null) commandBuilder.set(selector + " #Button.TooltipText", variant.properties().getTranslationProperties().getName());
            if (variant.icon() != null) commandBuilder.set(selector + " #Button #Icon.AssetPath", variant.icon());

            String[] colors = variant.colors();

            for (int c = 0; c < colors.length; c++) {
                String color = colors[c];
                commandBuilder.append(selector + " #Button #Colors", "Wardrobe/Pages/Color.ui");
                commandBuilder.set(selector + " #Button #Colors[" + c + "].Background", color);
            }

            if (variant.id().equals(wornCosmetic.getVariantId()))
                commandBuilder.set(selector + " #Button #SelectedHighlight.Visible", true);

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #Button",
                    EventData.of("Variant", variant.id()),
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
                .append(new KeyedCodec<>("@Variant", Codec.STRING), (e, v) -> e.option = v, e -> e.option).add()
                .append(new KeyedCodec<>("Variant", Codec.STRING), (e, v) -> e.variant = v, e -> e.variant).add()
                .append(new KeyedCodec<>("@HideType", Codec.BOOLEAN), (e, v) -> e.hideType = v, e -> e.hideType).add()
                .append(new KeyedCodec<>("Action", new EnumCodec<>(MenuAction.class)), (e, v) -> e.action = v, e -> e.action).add()
                .build();

        private String searchQuery;
        private String category;
        private String slot;
        private String cosmetic;
        private String option;
        private String variant;
        private Boolean hideType;
        private MenuAction action;
    }

    public enum MenuAction {
        Reset, Discard, Save, Camera;

        private final EventData eventData;

        MenuAction() {
            this.eventData = EventData.of("Action", name());
        }

        public EventData getEvent() {
            return eventData;
        }
    }
}