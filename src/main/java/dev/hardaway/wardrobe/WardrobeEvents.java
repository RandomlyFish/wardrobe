package dev.hardaway.wardrobe;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin;
import com.hypixel.hytale.builtin.asseteditor.AssetPath;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.builtin.asseteditor.assettypehandler.AssetStoreTypeHandler;
import com.hypixel.hytale.builtin.asseteditor.assettypehandler.AssetTypeHandler;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorSelectAssetEvent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Model;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPreviewCameraSettings;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUpdateModelPreview;
import com.hypixel.hytale.server.core.asset.type.item.config.AssetIconProperties;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.appearance.Appearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.AppearanceCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticCategoryAsset;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticSlotAsset;
import dev.hardaway.wardrobe.impl.cosmetic.appearance.VariantAppearance;
import dev.hardaway.wardrobe.impl.cosmetic.texture.VariantTextureConfig;
import dev.hardaway.wardrobe.impl.player.PlayerWardrobeComponent;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class WardrobeEvents {

    protected static void registerEvents(JavaPlugin plugin) {
        plugin.getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, WardrobeEvents::onInventoryChange);
        plugin.getEventRegistry().register(LoadedAssetsEvent.class, CosmeticAsset.class, WardrobeEvents::onCosmeticsUpdated);
        plugin.getEventRegistry().register(LoadedAssetsEvent.class, CosmeticSlotAsset.class, WardrobeEvents::onSlotsUpdated);
        plugin.getEventRegistry().register(LoadedAssetsEvent.class, CosmeticCategoryAsset.class, WardrobeEvents::onCategoriesUpdated);
        plugin.getEventRegistry().registerGlobal(AssetEditorSelectAssetEvent.class, WardrobeEvents::onSelectAsset);
        plugin.getEventRegistry().register(LoadedAssetsEvent.class, CosmeticAsset.class, WardrobeEvents::onCosmeticAssetLoaded);
    }

    private static void onInventoryChange(LivingEntityInventoryChangeEvent event) {
        if (!Objects.equals(event.getEntity().getInventory().getArmor(), event.getItemContainer()))
            return;

        Ref<EntityStore> ref = event.getEntity().getReference();
        World world = event.getEntity().getWorld();
        if (ref != null && world != null) {
            Store<EntityStore> store = world.getEntityStore().getStore();

            PlayerWardrobe wardrobe = store.getComponent(ref, PlayerWardrobeComponent.getComponentType());
            if (wardrobe != null) wardrobe.rebuild();
        }
    }

    private static void onCosmeticsUpdated(LoadedAssetsEvent<String, CosmeticAsset, DefaultAssetMap<String, CosmeticAsset>> event) {
        WardrobeUtil.rebuildAllWardrobes();
    }

    private static void onSlotsUpdated(LoadedAssetsEvent<String, CosmeticSlotAsset, DefaultAssetMap<String, CosmeticSlotAsset>> event) {
        WardrobeUtil.rebuildAllWardrobes();
    }

    private static void onCategoriesUpdated(LoadedAssetsEvent<String, CosmeticCategoryAsset, DefaultAssetMap<String, CosmeticCategoryAsset>> event) {
        WardrobeUtil.rebuildAllWardrobes();
    }

    private static void onSelectAsset(@Nonnull AssetEditorSelectAssetEvent event) {
        String assetType = event.getAssetType();
        if (CosmeticAsset.class.getSimpleName().equals(assetType)) {
            String key = ModelAsset.getAssetStore().decodeFilePathKey(event.getAssetFilePath().path());
            CosmeticAsset cosmetic = CosmeticAsset.getAssetMap().getAsset(key);
            sendCosmeticPacket(cosmetic, event.getEditorClient().getPacketHandler(), event.getAssetFilePath());
        }
    }

    private static void onCosmeticAssetLoaded(@Nonnull LoadedAssetsEvent<String, CosmeticAsset, ?> event) {
        if (!event.isInitial()) {
            Map<EditorClient, AssetPath> clientOpenAssetPathMapping = AssetEditorPlugin.get().getClientOpenAssetPathMapping();
            if (!clientOpenAssetPathMapping.isEmpty()) {
                AssetUpdateQuery.RebuildCache rebuildCache = event.getQuery().getRebuildCache();
                if (rebuildCache.isBlockTextures() || rebuildCache.isModelTextures() || rebuildCache.isItemIcons() || rebuildCache.isModels()) {
                    for (CosmeticAsset cosmetic : event.getLoadedAssets().values()) {
                        for (Map.Entry<EditorClient, AssetPath> editor : clientOpenAssetPathMapping.entrySet()) {
                            Path path = editor.getValue().path();
                            if (!path.toString().isEmpty()) {
                                AssetTypeHandler assetType = AssetEditorPlugin.get().getAssetTypeRegistry().getAssetTypeHandlerForPath(path);
                                if (assetType instanceof AssetStoreTypeHandler && ((AssetStoreTypeHandler) assetType).getAssetStore().getAssetClass().equals(ModelAsset.class)) {
                                    String id = ModelAsset.getAssetStore().decodeFilePathKey(path);
                                    if (cosmetic.getId().equals(id)) {
                                        sendCosmeticPacket(cosmetic, editor.getKey().getPacketHandler(), editor.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void sendCosmeticPacket(CosmeticAsset cosmetic, PacketHandler packetHandler, AssetPath assetPath) {
        if (!(cosmetic instanceof AppearanceCosmetic appearanceCosmetic)) return;

        com.hypixel.hytale.server.core.asset.type.item.config.AssetIconProperties iconProperties = cosmetic.getProperties().getIconProperties();
        AssetIconProperties defaultIconProperties = new AssetIconProperties(0.58823F, new Vector2f(0.0F, -60F), new Vector3f(22.5F, 45.0F, 22.5F));
        if (iconProperties == null) {
            iconProperties = defaultIconProperties;
        }

        AssetEditorPreviewCameraSettings camera = new AssetEditorPreviewCameraSettings();
        camera.modelScale = iconProperties.getScale();
        Vector2f translation = iconProperties.getTranslation() != null ? iconProperties.getTranslation() : defaultIconProperties.getTranslation();
        camera.cameraPosition = new Vector3f(-translation.x, -translation.y, 0.0F);
        Vector3f rotation = iconProperties.getRotation() != null ? iconProperties.getRotation() : defaultIconProperties.getRotation();
        camera.cameraOrientation = new Vector3f((float) (-Math.toRadians(rotation.x)), (float) (-Math.toRadians(rotation.y)), (float) (-Math.toRadians(rotation.z)));

        String option = null;
        String variant = null;

        Appearance appearance = appearanceCosmetic.getAppearance();
        if (appearance instanceof VariantAppearance variantAppearance) {
            option = variantAppearance.collectVariants()[0];
        }

        TextureConfig textureConfig = appearance.getTextureConfig(option);
        if (textureConfig instanceof VariantTextureConfig variantTextureConfig) {
            variant = variantTextureConfig.collectVariants()[0];
        }

        String model = appearance.getModel(option);
        String texture = textureConfig.getTexture(variant);

        Model packet = new Model();
        packet.path = model;
        packet.texture = texture;
        packetHandler.writeNoCache(new AssetEditorUpdateModelPreview(assetPath.toPacket(), packet, null, camera));
    }
}
