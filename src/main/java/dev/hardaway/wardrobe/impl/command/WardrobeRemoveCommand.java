package dev.hardaway.wardrobe.impl.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.Cosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.asset.cosmetic.CosmeticAsset;

import javax.annotation.Nonnull;

public class WardrobeRemoveCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> cosmeticArg;
    private final FlagArg isSlot;

    public WardrobeRemoveCommand() {
        super("remove", "Take off cosmetics");
        this.setPermissionGroup(GameMode.Adventure);
        this.cosmeticArg = this.withRequiredArg("cosmetic", "The cosmetic or cosmetic slot to remove", ArgTypes.STRING);
        this.isSlot = this.withFlagArg("slot", "If the specified id is of a cosmetic slot");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        PlayerWardrobe wardrobeComponent = store.ensureAndGetComponent(ref, PlayerWardrobe.getComponentType());
        String id = cosmeticArg.get(context);

        String group;
        if (isSlot.provided(context)) {
            if (!WardrobeCosmeticSlot.getAssetMap().getAssetMap().containsKey(id)) {
                context.sendMessage(Message.raw("Failed to find cosmetic slot with id \"{id}\"!").param("id", id));
                return;
            }

            group = id;
        } else {
            Cosmetic cosmetic = CosmeticAsset.getAssetMap().getAsset(id); // TODO: registry
            if (cosmetic == null) {
                context.sendMessage(Message.raw("Failed to find cosmetic with id \"{id}\"!").param("id", id));
                return;
            }

            group = cosmetic.getCosmeticSlotId();
        }

        wardrobeComponent.removeCosmetic(group);
        context.sendMessage(Message.raw("Cosmetic removed"));
    }
}
