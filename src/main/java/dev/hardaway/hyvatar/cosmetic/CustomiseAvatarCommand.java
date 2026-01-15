package dev.hardaway.hyvatar.cosmetic;


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
import dev.hardaway.hyvatar.ui.AvatarCustomisationPage;

import javax.annotation.Nonnull;


public class CustomiseAvatarCommand extends AbstractPlayerCommand {

    public CustomiseAvatarCommand() {
        super("avatar", "Customise the player avatar model and cosmetics");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player != null) player.getPageManager().openCustomPage(ref, store, new AvatarCustomisationPage(playerRef, CustomPageLifetime.CanDismiss));
    }
}