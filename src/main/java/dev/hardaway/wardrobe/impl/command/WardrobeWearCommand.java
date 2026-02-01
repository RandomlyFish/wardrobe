package dev.hardaway.wardrobe.impl.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.appearance.Appearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.AppearanceCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.player.CosmeticSaveData;
import dev.hardaway.wardrobe.impl.player.PlayerWardrobeComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class WardrobeWearCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> cosmeticArg;
    private final OptionalArg<String> variantArg;
    private final OptionalArg<String> colorArg;

    public WardrobeWearCommand() {
        super("wear", "server.commands.wardrobe.wear.description");
        this.setPermissionGroup(GameMode.Adventure);
        this.cosmeticArg = this.withRequiredArg("cosmetic", "server.commands.wardrobe.wear.args.cosmetic.description", ArgTypes.STRING);
        this.variantArg = this.withOptionalArg("variant", "server.commands.wardrobe.wear.args.variant.description", ArgTypes.STRING);
        this.colorArg = this.withOptionalArg("color", "server.commands.wardrobe.wear.args.color.description", ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        PlayerWardrobe wardrobe = store.ensureAndGetComponent(ref, PlayerWardrobeComponent.getComponentType());

        String cosmeticId = cosmeticArg.get(context);
        WardrobeCosmetic cosmetic = CosmeticAsset.getAssetMap().getAsset(cosmeticId); // TODO: registry

        if (cosmetic == null) {
            context.sendMessage(Message.raw("Failed to find cosmetic with id \"{id}\"!").param("id", cosmeticId));
            return;
        }

        if (!cosmetic.getProperties().hasPermission(playerRef.getUuid())) {
            context.sendMessage(Message.raw("You do not have permission to use this cosmetic."));
            return;
        }

        String variant = null;
        String texture = null;

        if (cosmetic instanceof AppearanceCosmetic appearanceCosmetic) {
            Appearance appearance = appearanceCosmetic.getAppearance();
            List<String> variants = List.of(appearance.collectVariants());

            if (variantArg.provided(context)) {
                String provided = variantArg.get(context);
                if (!variants.contains(provided)) {
                    context.sendMessage(Message.raw("Failed to find variant \"{id}\" for this cosmetic!").param("id", provided));
                    return;
                }
                variant = provided;
            } else if (!variants.isEmpty()) {
                variant = variants.getFirst();
            }

            if (variant != null) {
                TextureConfig textureConfig = appearance.getTextureConfig(variant);
                List<String> textures = List.of(textureConfig.collectVariants());

                if (colorArg.provided(context)) {
                    String provided = colorArg.get(context);
                    if (!textures.contains(provided)) {
                        context.sendMessage(Message.raw("Failed to find color \"{id}\" for this cosmetic!").param("id", provided));
                        return;
                    }
                    texture = provided;
                } else if (!textures.isEmpty()) {
                    texture = textures.getFirst();
                }
            }
        }

        String slotId = cosmetic.getCosmeticSlotId();
        wardrobe.setCosmetic(slotId, new CosmeticSaveData(cosmeticId, variant, texture));
        wardrobe.rebuild();

        context.sendMessage(Message.raw("Cosmetic worn."));
    }
}