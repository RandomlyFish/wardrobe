package dev.hardaway.hyvatar;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.hardaway.hyvatar.cosmetic.TestCommand;
import dev.hardaway.hyvatar.ui.AvatarCustomisationPage;

import javax.annotation.Nonnull;

public class HyvatarPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HyvatarPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        OpenCustomUIInteraction.registerCustomPageSupplier(this, AvatarCustomisationPage.class, "AvatarCustomisation", (_, _, playerRef, _) ->
                new AvatarCustomisationPage(playerRef, CustomPageLifetime.CanDismiss, AvatarCustomisationPage.PageEventData.CODEC)
        );

        this.getCommandRegistry().registerCommand(new TestCommand());
    }
}