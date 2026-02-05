package dev.hardaway.wardrobe.impl.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.player.PlayerWardrobeComponent;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class WardrobeRemoveCommand extends AbstractPlayerCommand {

    private final RequiredArg<CosmeticAsset> cosmeticArg;

    public WardrobeRemoveCommand() {
        super("remove", "server.commands.wardrobe.remove.description");
        this.setPermissionGroup(GameMode.Adventure);
        this.cosmeticArg = this.withRequiredArg("cosmetic", "server.commands.wardrobe.remove.args.cosmetic.description", WardrobeCommand.COSMETIC_ARGUMENT_TYPE);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        PlayerWardrobe wardrobe = store.getComponent(ref, PlayerWardrobeComponent.getComponentType());
        if (wardrobe == null) {
            context.sendMessage(Message.raw("No Wardrobe found on your avatar"));
            return;
        }

        WardrobeCosmetic cosmetic = this.cosmeticArg.get(context);
        for (Map.Entry<String, PlayerCosmetic> cosmeticEntry : wardrobe.getCosmeticMap().entrySet()) {
            if (Objects.equals(cosmeticEntry.getValue().getCosmeticId(), cosmetic.getId())) {
                wardrobe.removeCosmetic(cosmeticEntry.getKey());
                wardrobe.rebuild();
                context.sendMessage(Message.join(Message.raw("Removed the '"), cosmetic.getProperties().getTranslationProperties().getName(), Message.raw("' cosmetic from your avatar")));
                return;
            }
        }

        context.sendMessage(Message.join(Message.raw("Failed to find the '"), cosmetic.getProperties().getTranslationProperties().getName(), Message.raw("' cosmetic on your avatar")));
    }
}
