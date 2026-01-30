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
import dev.hardaway.wardrobe.impl.asset.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;

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
        PlayerWardrobe wardrobeComponent = store.getComponent(ref, PlayerWardrobeComponent.getComponentType());
        if (wardrobeComponent == null) {
            context.sendMessage(Message.raw("No Wardrobe data found"));
            return;
        }

        WardrobeCosmetic cosmetic = this.cosmeticArg.get(context);
        for (Map.Entry<String, PlayerCosmetic> cosmeticEntry : wardrobeComponent.getCosmeticMap().entrySet()) {
            if (Objects.equals(cosmeticEntry.getValue().getCosmeticId(), cosmetic.getId())) {
                wardrobeComponent.removeCosmetic(cosmeticEntry.getKey());
                context.sendMessage(Message.join(Message.raw("Removed '"), cosmetic.getTranslationProperties().getName(), Message.raw("' from your avatar")));
            }
        }
    }
}
