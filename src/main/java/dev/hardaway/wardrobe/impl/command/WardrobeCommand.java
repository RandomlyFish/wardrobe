package dev.hardaway.wardrobe.impl.command;


import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;
import dev.hardaway.wardrobe.impl.ui.WardrobePage;

import javax.annotation.Nonnull;


public class WardrobeCommand extends AbstractPlayerCommand {

    private final ComponentType<EntityStore, PlayerWardrobeComponent> playerWardrobeComponentType;

    public WardrobeCommand(ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType) {
        super("wardrobe", "Customise your avatar's cosmetics");
        this.setPermissionGroup(GameMode.Adventure);
        this.playerWardrobeComponentType = wardrobeComponentType;
        this.addSubCommand(new WardrobeRemoveCommand(wardrobeComponentType));
        this.addSubCommand(new WardrobeWearCommand(wardrobeComponentType));
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player != null)
            player.getPageManager().openCustomPage(ref, store, new WardrobePage(playerRef, CustomPageLifetime.CanDismiss, this.playerWardrobeComponentType));
    }
}