package dev.hardaway.wardrobe;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.cosmetic.asset.CosmeticAsset;
import dev.hardaway.wardrobe.cosmetic.asset.config.DefaultTextureConfig;
import dev.hardaway.wardrobe.cosmetic.asset.config.GradientTextureConfig;
import dev.hardaway.wardrobe.cosmetic.asset.config.TextureConfig;
import dev.hardaway.wardrobe.cosmetic.asset.config.VariantTextureConfig;
import dev.hardaway.wardrobe.command.TestCommand;
import dev.hardaway.wardrobe.command.WardrobeCommand;
import dev.hardaway.wardrobe.cosmetic.system.component.PlayerWardrobeComponent;
import dev.hardaway.wardrobe.cosmetic.system.PlayerWardrobeSystem;
import dev.hardaway.wardrobe.cosmetic.system.SetupPlayerWardrobeSystem;
import dev.hardaway.wardrobe.cosmetic.asset.category.CosmeticCategory;
import dev.hardaway.wardrobe.cosmetic.asset.category.CosmeticGroup;
import dev.hardaway.wardrobe.ui.WardrobePage;

import javax.annotation.Nonnull;

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
                .register(Priority.DEFAULT, "Default", DefaultTextureConfig.class, DefaultTextureConfig.CODEC)
                .register("Gradient", GradientTextureConfig.class, GradientTextureConfig.CODEC)
                .register("Variant", VariantTextureConfig.class, VariantTextureConfig.CODEC);

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
                .loadsAfter(CosmeticGroup.class)
                .build()
        );


        this.playerWardrobeComponentType = this.getEntityStoreRegistry().registerComponent(
                PlayerWardrobeComponent.class,
                "PlayerWardrobe",
                PlayerWardrobeComponent.CODEC
        );

        this.getEntityStoreRegistry().registerSystem(new SetupPlayerWardrobeSystem(this.playerWardrobeComponentType));
        this.getEntityStoreRegistry().registerSystem(new PlayerWardrobeSystem(this.playerWardrobeComponentType));

        OpenCustomUIInteraction.registerCustomPageSupplier(this, WardrobePage.class, "AvatarCustomisation", (_, _, playerRef, _) ->
                new WardrobePage(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction)
        );

        this.getCommandRegistry().registerCommand(new TestCommand(this.playerWardrobeComponentType));
        this.getCommandRegistry().registerCommand(new WardrobeCommand());

    }
}