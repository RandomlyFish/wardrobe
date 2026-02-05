package dev.hardaway.wardrobe;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.appearance.Appearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.impl.command.WardrobeCommand;
import dev.hardaway.wardrobe.impl.cosmetic.*;
import dev.hardaway.wardrobe.impl.cosmetic.appearance.ModelAppearance;
import dev.hardaway.wardrobe.impl.cosmetic.appearance.VariantAppearance;
import dev.hardaway.wardrobe.impl.cosmetic.texture.GradientTextureConfig;
import dev.hardaway.wardrobe.impl.cosmetic.texture.StaticTextureConfig;
import dev.hardaway.wardrobe.impl.cosmetic.texture.VariantTextureConfig;
import dev.hardaway.wardrobe.impl.menu.WardrobePage;
import dev.hardaway.wardrobe.impl.player.PlayerWardrobeComponent;
import dev.hardaway.wardrobe.impl.player.PlayerWardrobeSystems;

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
        OpenCustomUIInteraction.registerCustomPageSupplier(this, WardrobePage.class, "Wardrobe", (ref, componentAccessor, playerRef, context) -> {
            PlayerWardrobeComponent wardrobe = context.getCommandBuffer().ensureAndGetComponent(ref, PlayerWardrobeComponent.getComponentType());
            BlockPosition position = context.getTargetBlock();
            int rotationIndex = position == null ? 0 : context.getCommandBuffer().getExternalData().getWorld().getBlockRotationIndex(position.x, position.y, position.z);
            return new WardrobePage(playerRef, wardrobe, position == null ? null : new Position(position.x, position.y, position.z), rotationIndex);
        });

        this.getCodecRegistry(TextureConfig.CODEC)
                .register(Priority.DEFAULT, "Static", StaticTextureConfig.class, StaticTextureConfig.CODEC)
                .register(Priority.NORMAL, "Gradient", GradientTextureConfig.class, GradientTextureConfig.CODEC)
                .register(Priority.NORMAL, "Variant", VariantTextureConfig.class, VariantTextureConfig.CODEC);

        this.getCodecRegistry(CosmeticAsset.CODEC)
                .register(Priority.DEFAULT, "ModelAttachment", ModelAttachmentCosmetic.class, ModelAttachmentCosmetic.CODEC)
                .register(Priority.NORMAL, "PlayerModel", PlayerModelCosmetic.class, PlayerModelCosmetic.CODEC);

        this.getCodecRegistry(Appearance.CODEC)
                .register(Priority.DEFAULT, "Model", ModelAppearance.class, ModelAppearance.CODEC)
                .register(Priority.NORMAL, "Variant", VariantAppearance.class, VariantAppearance.CODEC);

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
        this.getEntityStoreRegistry().registerSystem(new PlayerWardrobeSystems.Tick());
        this.getEntityStoreRegistry().registerSystem(new PlayerWardrobeSystems.EntityAdded());
        this.getEntityStoreRegistry().registerSystem(new PlayerWardrobeSystems.WardrobeChanged());
        this.getEntityStoreRegistry().registerSystem(new PlayerWardrobeSystems.ArmorVisibilityChanged());

        WardrobeEvents.registerEvents(this);

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
}