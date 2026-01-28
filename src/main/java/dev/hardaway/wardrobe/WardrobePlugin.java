package dev.hardaway.wardrobe;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Notification;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.appearance.CosmeticAppearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.asset.CosmeticCategoryAsset;
import dev.hardaway.wardrobe.impl.asset.CosmeticSlotAsset;
import dev.hardaway.wardrobe.impl.asset.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.asset.cosmetic.ModelAttachmentCosmetic;
import dev.hardaway.wardrobe.impl.asset.cosmetic.PlayerModelCosmetic;
import dev.hardaway.wardrobe.impl.asset.cosmetic.appearance.ModelCosmeticAppearance;
import dev.hardaway.wardrobe.impl.asset.cosmetic.appearance.VariantCosmeticAppearance;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.GradientTextureConfig;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.StaticTextureConfig;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.VariantTextureConfig;
import dev.hardaway.wardrobe.impl.command.WardrobeCommand;
import dev.hardaway.wardrobe.impl.system.PlayerSettingsRebuildSystem;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;
import dev.hardaway.wardrobe.impl.system.WardrobeSystems;
import dev.hardaway.wardrobe.impl.ui.WardrobePage;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class WardrobePlugin extends JavaPlugin {

    private static WardrobePlugin instance;
    private ComponentType<EntityStore, PlayerWardrobeComponent> playerWardrobeComponentType;

    public WardrobePlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static WardrobePlugin get() {
        return instance;
    }

    @Override
    protected void setup() {
        this.getCodecRegistry(TextureConfig.CODEC)
                .register(Priority.DEFAULT, "Static", StaticTextureConfig.class, StaticTextureConfig.CODEC)
                .register(Priority.NORMAL, "Gradient", GradientTextureConfig.class, GradientTextureConfig.CODEC)
                .register(Priority.NORMAL, "Variant", VariantTextureConfig.class, VariantTextureConfig.CODEC);

        this.getCodecRegistry(CosmeticAsset.CODEC)
                .register(Priority.DEFAULT, "ModelAttachment", ModelAttachmentCosmetic.class, ModelAttachmentCosmetic.CODEC)
                .register(Priority.NORMAL, "PlayerModel", PlayerModelCosmetic.class, PlayerModelCosmetic.CODEC);

        this.getCodecRegistry(CosmeticAppearance.CODEC)
                .register(Priority.DEFAULT, "Model", ModelCosmeticAppearance.class, ModelCosmeticAppearance.CODEC)
                .register(Priority.NORMAL, "Variant", VariantCosmeticAppearance.class, VariantCosmeticAppearance.CODEC);

        AssetRegistry.register(HytaleAssetStore.builder(CosmeticCategoryAsset.class, new DefaultAssetMap<>())
                .setPath("Wardrobe/Categories")
                .setCodec(CosmeticCategoryAsset.CODEC)
                .setKeyFunction(CosmeticCategoryAsset::getId)
                .build()
        );
        AssetRegistry.register(HytaleAssetStore.builder(CosmeticSlotAsset.class, new DefaultAssetMap<>())
                .setPath("Wardrobe/Slots")
                .setCodec(CosmeticSlotAsset.CODEC)
                .setKeyFunction(CosmeticSlotAsset::getId)
                .loadsAfter(CosmeticCategoryAsset.class)
                .build()
        );
        AssetRegistry.register(HytaleAssetStore.builder(CosmeticAsset.class, new DefaultAssetMap<>())
                .setPath("Wardrobe/Cosmetics")
                .setCodec(CosmeticAsset.CODEC)
                .setKeyFunction(CosmeticAsset::getId)
                .loadsAfter(ModelAsset.class, CosmeticSlotAsset.class)
                .build()
        );


        this.playerWardrobeComponentType = this.getEntityStoreRegistry().registerComponent(
                PlayerWardrobeComponent.class,
                "PlayerWardrobe",
                PlayerWardrobeComponent.CODEC
        );

        // TODO: groups
        this.getEntityStoreRegistry().registerSystem(new WardrobeSystems.Tick());
        this.getEntityStoreRegistry().registerSystem(new WardrobeSystems.EntityAdded());
        this.getEntityStoreRegistry().registerSystem(new WardrobeSystems.WardrobeChanged());
        this.getEntityStoreRegistry().registerSystem(new PlayerSettingsRebuildSystem());
        this.getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, WardrobePlugin::onPlayerAddToWorld);
        this.getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, WardrobePlugin::onInventoryChange);
        this.getEventRegistry().register(LoadedAssetsEvent.class, CosmeticAsset.class, WardrobePlugin::onCosmeticsUpdated);
        this.getEventRegistry().register(LoadedAssetsEvent.class, CosmeticSlotAsset.class, WardrobePlugin::onSlotsUpdated);
        this.getEventRegistry().register(LoadedAssetsEvent.class, CosmeticCategoryAsset.class, WardrobePlugin::onCategoriesUpdated);

        OpenCustomUIInteraction.registerCustomPageSupplier(this, WardrobePage.class, "Wardrobe", (_, _, playerRef, _) ->
                new WardrobePage(playerRef, CustomPageLifetime.CanDismiss)
        );

        this.getCommandRegistry().registerCommand(new WardrobeCommand());

    }

    public ComponentType<EntityStore, PlayerWardrobeComponent> getPlayerWardrobeComponentType() {
        return playerWardrobeComponentType;
    }

    public static <T extends JsonAssetWithMap<String, DefaultAssetMap<String, T>>> Supplier<AssetStore<String, T, DefaultAssetMap<String, T>>> createAssetStore(Class<T> clazz) {
        return new Supplier<>() {
            AssetStore<String, T, DefaultAssetMap<String, T>> value;

            @Override
            public AssetStore<String, T, DefaultAssetMap<String, T>> get() {
                if (value == null)
                    value = AssetRegistry.getAssetStore(clazz);
                return value;
            }
        };
    }

    // TODO: player wardrobe data validation before this
    private static void onPlayerAddToWorld(AddPlayerToWorldEvent event) {
        PlayerWardrobe wardrobe = event.getHolder().getComponent(PlayerWardrobeComponent.getComponentType());
        if (wardrobe != null) wardrobe.rebuild();
    }

    private static void onInventoryChange(LivingEntityInventoryChangeEvent event) {
        Ref<EntityStore> ref = event.getEntity().getReference();
        World world = event.getEntity().getWorld();
        if (ref != null && world != null) {
            Store<EntityStore> store = world.getEntityStore().getStore();
            PlayerWardrobe wardrobe = store.getComponent(ref, PlayerWardrobeComponent.getComponentType());
            if (wardrobe != null) wardrobe.rebuild();
        }
    }

    private static void onCosmeticsUpdated(LoadedAssetsEvent<String, CosmeticAsset, DefaultAssetMap<String, CosmeticAsset>> event) {
        rebuildAllWardrobes();
    }

    private static void onSlotsUpdated(LoadedAssetsEvent<String, CosmeticSlotAsset, DefaultAssetMap<String, CosmeticSlotAsset>> event) {
        rebuildAllWardrobes();
    }

    private static void onCategoriesUpdated(LoadedAssetsEvent<String, CosmeticCategoryAsset, DefaultAssetMap<String, CosmeticCategoryAsset>> event) {
        rebuildAllWardrobes();
    }

    public static void rebuildAllWardrobes() {
        Universe universe = Universe.get();
        universe.getPlayers().forEach(playerRef -> {
            World world = universe.getWorld(playerRef.getWorldUuid());
            world.execute(() -> {
                Store<EntityStore> store = world.getEntityStore().getStore();

                PlayerWardrobe wardrobe = store.getComponent(playerRef.getReference(), PlayerWardrobeComponent.getComponentType());
                if (wardrobe != null) wardrobe.rebuild();
            });
        });
    }

    public static void sendError(PlayerRef playerRef) {
        Notification notification = new Notification();
        notification.style = NotificationStyle.Warning;
        notification.message = Message.raw("Error rebuilding wardrobe").getFormattedMessage();
        notification.secondaryMessage = Message.raw("Some cosmetics were invalid").getFormattedMessage();
        notification.icon = "Icons/AssetNotifications/IconAlert.png";
        playerRef.getPacketHandler().writeNoCache(notification);
    }
}