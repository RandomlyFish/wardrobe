package dev.hardaway.wardrobe.impl.command;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;

import javax.annotation.Nonnull;

public class WardrobeResetCommand extends AbstractPlayerCommand {

    private final ComponentType<EntityStore, PlayerWardrobeComponent> playerWardrobeComponentType;

    public WardrobeResetCommand(ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType) {
        super("reset", "Reset all Wardrobe related data");
        this.setPermissionGroup(GameMode.Adventure);
        this.playerWardrobeComponentType = wardrobeComponentType;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        store.removeComponentIfExists(ref, playerWardrobeComponentType);
        context.sendMessage(Message.raw("Wardrobe data reset"));
    }
}
