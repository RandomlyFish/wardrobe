package dev.hardaway.wardrobe;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.impl.command.TestCommand;
import dev.hardaway.wardrobe.impl.command.WardrobeCommand;
import dev.hardaway.wardrobe.impl.cosmetic.asset.CosmeticAsset;
import dev.hardaway.wardrobe.impl.cosmetic.asset.CosmeticCategory;
import dev.hardaway.wardrobe.impl.cosmetic.asset.CosmeticGroup;
import dev.hardaway.wardrobe.impl.cosmetic.asset.HaircutCosmetic;
import dev.hardaway.wardrobe.impl.cosmetic.asset.HeadAccessoryCosmetic;
import dev.hardaway.wardrobe.impl.cosmetic.asset.ModelAttachmentCosmetic;
import dev.hardaway.wardrobe.impl.cosmetic.asset.PlayerModelCosmetic;
import dev.hardaway.wardrobe.impl.cosmetic.asset.texture.GradientTextureConfig;
import dev.hardaway.wardrobe.impl.cosmetic.asset.texture.StaticTextureConfig;
import dev.hardaway.wardrobe.impl.cosmetic.asset.texture.TextureConfig;
import dev.hardaway.wardrobe.impl.cosmetic.asset.texture.VariantTextureConfig;
import dev.hardaway.wardrobe.impl.cosmetic.system.PlayerWardrobeComponent;
import dev.hardaway.wardrobe.impl.cosmetic.system.PlayerWardrobeSystem;
import dev.hardaway.wardrobe.impl.cosmetic.system.ResetPlayerModelSystem;
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
                .register(Priority.NORMAL, "PlayerModel", PlayerModelCosmetic.class, PlayerModelCosmetic.CODEC)
                .register(Priority.NORMAL, "Haircut", HaircutCosmetic.class, HaircutCosmetic.CODEC)
                .register(Priority.NORMAL, "HeadAccessory", HeadAccessoryCosmetic.class, HeadAccessoryCosmetic.CODEC);

        AssetRegistry.register(HytaleAssetStore.builder(CosmeticCategory.class, new DefaultAssetMap<>())
                .setPath("Wardrobe/Categories")
                .setCodec(CosmeticCategory.CODEC)
                .setKeyFunction(CosmeticCategory::getId)
                .build()
        );
        AssetRegistry.register(HytaleAssetStore.builder(CosmeticGroup.class, new DefaultAssetMap<>())
                .setPath("Wardrobe/Groups")
                .setCodec(CosmeticGroup.CODEC)
                .setKeyFunction(CosmeticGroup::getId)
                .loadsAfter(CosmeticCategory.class)
                .build()
        );
        AssetRegistry.register(HytaleAssetStore.builder(CosmeticAsset.class, new DefaultAssetMap<>())
                .setPath("Wardrobe/Cosmetics")
                .setCodec(CosmeticAsset.CODEC)
                .setKeyFunction(CosmeticAsset::getId)
                .loadsAfter(ModelAsset.class, CosmeticGroup.class)
                .build()
        );


        this.playerWardrobeComponentType = this.getEntityStoreRegistry().registerComponent(
                PlayerWardrobeComponent.class,
                "PlayerWardrobe",
                PlayerWardrobeComponent.CODEC
        );

        this.getEntityStoreRegistry().registerSystem(new ResetPlayerModelSystem(this.playerWardrobeComponentType));
        this.getEntityStoreRegistry().registerSystem(new PlayerWardrobeSystem(this.playerWardrobeComponentType));

        OpenCustomUIInteraction.registerCustomPageSupplier(this, WardrobePage.class, "AvatarCustomisation", (_, _, playerRef, _) ->
                new WardrobePage(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, this.playerWardrobeComponentType)
        );

        this.getCommandRegistry().registerCommand(new TestCommand(this.playerWardrobeComponentType));
        this.getCommandRegistry().registerCommand(new WardrobeCommand(this.playerWardrobeComponentType));

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