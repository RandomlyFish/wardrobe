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
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.asset.CosmeticSlotAsset;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;

import javax.annotation.Nonnull;

public class WardrobeClearCommand extends AbstractPlayerCommand {

    private final RequiredArg<CosmeticSlotAsset> cosmeticSlotArg;

    public WardrobeClearCommand() {
        super("clear", "server.commands.wardrobe.clear.description");
        this.setPermissionGroup(GameMode.Adventure);
        this.cosmeticSlotArg = this.withRequiredArg("slot", "The cosmetic slot to remove", WardrobeCommand.COSMETIC_SLOT_ARGUMENT_TYPE);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        PlayerWardrobe wardrobeComponent = store.getComponent(ref, PlayerWardrobeComponent.getComponentType());
        if (wardrobeComponent == null) {
            context.sendMessage(Message.raw("No Wardrobe data found"));
            return;
        }

        WardrobeCosmeticSlot cosmeticSlot = this.cosmeticSlotArg.get(context);
        PlayerCosmetic cosmeticData = wardrobeComponent.getCosmetic(cosmeticSlot);
        if (cosmeticData == null) {
            context.sendMessage(Message.join(Message.raw("No cosmetics found in the '"), cosmeticSlot.getTranslationProperties().getName(), Message.raw("' slot")));
            return;
        }


        wardrobeComponent.removeCosmetic(cosmeticSlot.getId());
        context.sendMessage(Message.join(Message.raw("Cleared cosmetic from the '"), cosmeticSlot.getTranslationProperties().getName(), Message.raw("' slot")));
    }
}
