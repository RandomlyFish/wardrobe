package dev.hardaway.wardrobe.impl.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
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
import dev.hardaway.wardrobe.api.cosmetic.AppearanceCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCategory;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.cosmetic.apperance.CosmeticAppearance;
import dev.hardaway.wardrobe.api.cosmetic.apperance.TextureConfig;
import dev.hardaway.wardrobe.api.menu.WardrobeMenu;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.asset.cosmetic.appearance.VariantCosmeticAppearance;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.GradientTextureConfig;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.VariantTextureConfig;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        buildCategories(commandBuilder, eventBuilder);
        buildSlots(commandBuilder, eventBuilder);
        buildCosmetics(commandBuilder, eventBuilder, ref, store);

        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchField", EventData.of("@SearchQuery", "#SearchField.Value"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ResetAvatar", EventData.of("Action", "Reset"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#DiscardChanges", EventData.of("Action", "Discard"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SaveChanges", EventData.of("Action", "Save"), false);

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
            buildCategories(commandBuilder, eventBuilder);
            buildSlots(commandBuilder, eventBuilder);
            buildCosmetics(commandBuilder, eventBuilder, ref, store);
        }

        if (data.slot != null) {
            menu.selectSlot(data.slot);
            buildSlots(commandBuilder, eventBuilder);
            buildCosmetics(commandBuilder, eventBuilder, ref, store);
        }

        if (data.cosmetic != null || data.variant != null || data.texture != null) {
            PlayerWardrobe wardrobe = store.ensureAndGetComponent(ref, PlayerWardrobe.getComponentType());
            if (menu.selectCosmetic(wardrobe,
                    data.cosmetic != null ? WardrobeCosmetic.getAssetMap().getAsset(data.cosmetic) : null,
                    data.variant,
                    data.texture)) {
                buildCosmetics(commandBuilder, eventBuilder, ref, store);
                shouldClose = false;
            }
        }

        if ("Reset".equals(data.action)) {
            if (store.removeComponentIfExists(ref, PlayerWardrobe.getComponentType())) {
                buildCosmetics(commandBuilder, eventBuilder, ref, store);
                shouldClose = baseWardrobe == null;
            }
        }

        if ("Discard".equals(data.action)) {
            if (baseWardrobe != null)
                store.putComponent(ref, PlayerWardrobe.getComponentType(), (PlayerWardrobeComponent) baseWardrobe);
            else store.removeComponentIfExists(ref, PlayerWardrobe.getComponentType());

            shouldClose = true;
            close();
        }

        if ("Save".equals(data.action)) {
            shouldClose = true;
            close();
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

    private void buildCategories(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder) {
        commandBuilder.clear("#Categories");

        List<? extends WardrobeCategory> categories = menu.getCategories();

        for (int i = 0; i < categories.size(); i++) {
            WardrobeCategory category = categories.get(i);
            commandBuilder.append("#Categories", "Wardrobe/Pages/Tab.ui");

            String selector = "#Categories[" + i + "] #Button";
            commandBuilder.set(selector + " #Icon.AssetPath", category.getIconPath());

            if (category.getId().equals(menu.getSelectedCategory())) {
                commandBuilder.set(selector + " #Selected #Icon.AssetPath", category.getSelectedIconPath());
                commandBuilder.set(selector + " #Selected.Visible", true);
                commandBuilder.set("#CategoryName.Text", category.getTranslationProperties().getName());
            }

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector,
                    EventData.of("Category", category.getId()),
                    false
            );
        }
    }

    private void buildSlots(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder) {
        commandBuilder.clear("#Slots");

        List<WardrobeCosmeticSlot> slots = menu.getSlots();
        for (int i = 0; i < slots.size(); i++) {
            WardrobeCosmeticSlot slot = slots.get(i);
            commandBuilder.append("#Slots", "Wardrobe/Pages/Tab.ui");

            String selector = "#Slots[" + i + "] #Button";
            commandBuilder.set(selector + " #Icon.AssetPath", slot.getIconPath());

            if (slot.getId().equals(menu.getSelectedSlot())) {
                commandBuilder.set(selector + " #Selected #Icon.AssetPath", slot.getSelectedIconPath());
                commandBuilder.set(selector + " #Selected.Visible", true);
                commandBuilder.set("#SubCategoryName.Text", slot.getTranslationProperties().getName());
            }

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector,
                    EventData.of("Slot", slot.getId()),
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

            commandBuilder.set(selector + " #Button.TooltipText", cosmetic.getTranslationProperties().getName());
            if (cosmetic.getIconPath() != null)
                commandBuilder.set(selector + " #Button #Icon.AssetPath", cosmetic.getIconPath());

            if (cosmetic.hasPermission(playerRef.getUuid())) {
                eventBuilder.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        selector + " #Button",
                        EventData.of("Cosmetic", cosmetic.getId()),
                        false
                );
            } else {
                commandBuilder.set(selector + " #Button #Locked.Visible", true);
            }

            if (worn != null && cosmetic.getId().equals(worn.getCosmeticId())) {
                commandBuilder.set(selector + " #Button #Selected.Visible", true);
                if (cosmetic instanceof AppearanceCosmetic a && (buildOptions(commandBuilder, eventBuilder, worn, a) || buildColors(commandBuilder, eventBuilder, worn, a))) {
                    anchorHeight = (int) (150 * 3.5 + 10 * (3.5 - 1) + 14);
                }
            }

            if (cosmetic instanceof AppearanceCosmetic a && a.getAppearance() instanceof VariantCosmeticAppearance v && worn != null && worn.getVariantId() != null) {
                VariantCosmeticAppearance.Entry entry = v.getVariants().get(worn.getVariantId());
                if (entry != null && entry.getIconPath() != null) commandBuilder.set(selector + " #Button #Icon.AssetPath", entry.getIconPath());
            }
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
            commandBuilder.set(selector + " #Button #Colors.Background", baseColor[0]);

            if (texture.equals(wornCosmetic.getTextureId())) commandBuilder.set(selector + " #Button #SelectedHighlight.Visible", true);

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
                .append(new KeyedCodec<>("Category", Codec.STRING), (e, v) -> e.category = v, e -> e.category).add()
                .append(new KeyedCodec<>("Slot", Codec.STRING), (e, v) -> e.slot = v, e -> e.slot).add()
                .append(new KeyedCodec<>("Cosmetic", Codec.STRING), (e, v) -> e.cosmetic = v, e -> e.cosmetic).add()
                .append(new KeyedCodec<>("@Variant", Codec.STRING), (e, v) -> e.variant = v, e -> e.variant).add()
                .append(new KeyedCodec<>("Texture", Codec.STRING), (e, v) -> e.texture = v, e -> e.texture).add()
                .append(new KeyedCodec<>("Action", Codec.STRING), (e, v) -> e.action = v, e -> e.action).add()
                .build();

        private String searchQuery;
        private String category;
        private String slot;
        private String cosmetic;
        private String variant;
        private String texture;
        private String action;
    }
}