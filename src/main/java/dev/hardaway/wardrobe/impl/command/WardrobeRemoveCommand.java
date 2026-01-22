package dev.hardaway.wardrobe.impl.command;

import com.hypixel.hytale.component.ComponentType;
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
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.impl.asset.CosmeticSlotAsset;
import dev.hardaway.wardrobe.impl.asset.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;

import javax.annotation.Nonnull;

public class WardrobeRemoveCommand extends AbstractPlayerCommand {

    private final ComponentType<EntityStore, PlayerWardrobeComponent> playerWardrobeComponentType;
    private final RequiredArg<String> cosmeticArg;
    private final FlagArg isGroup;

    public WardrobeRemoveCommand(ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType) {
        super("remove", "Take off cosmetics");
        this.setPermissionGroup(GameMode.Adventure);
        this.playerWardrobeComponentType = wardrobeComponentType;
        this.cosmeticArg = this.withRequiredArg("cosmetic", "The cosmetic or cosmetic group to remove", ArgTypes.STRING);
        this.isGroup = this.withFlagArg("group", "If the specified id is of a cosmetic group");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        PlayerWardrobeComponent wardrobeComponent = store.ensureAndGetComponent(ref, this.playerWardrobeComponentType);
        String id = cosmeticArg.get(context);

        String group;
        if (isGroup.provided(context)) {
            if (!CosmeticSlotAsset.getAssetMap().getAssetMap().containsKey(id)) {
                context.sendMessage(Message.raw("Failed to find cosmetic group with id \"{id}\"!").param("id", id));
                return;
            }

            group = id;
        } else {
            WardrobeCosmetic cosmetic = CosmeticAsset.getAssetMap().getAsset(id);
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
