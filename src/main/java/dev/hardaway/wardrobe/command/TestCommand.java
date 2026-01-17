package dev.hardaway.wardrobe.command;


import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.cosmetic.PlayerWardrobeComponent;
import dev.hardaway.wardrobe.cosmetic.WardrobeCosmeticData;

import javax.annotation.Nonnull;


public class TestCommand extends AbstractPlayerCommand {

    private final ComponentType<EntityStore, PlayerWardrobeComponent> playerWardrobeComponentType;


    public TestCommand(ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType) {
        super("test", "Wardrobe test command");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
        this.playerWardrobeComponentType = wardrobeComponentType;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        PlayerWardrobeComponent component = store.getComponent(ref, this.playerWardrobeComponentType);
        component.setCosmetic(CosmeticType.CAPES, new WardrobeCosmeticData("Wardrobe_Cape_Test", "Red"));
        component.setCosmetic(CosmeticType.FACIAL_HAIR, new WardrobeCosmeticData("Wardrobe_FacialHair_Test", "Brown"));
        component.setCosmetic(CosmeticType.BODY_CHARACTERISTICS, new WardrobeCosmeticData("Wardrobe_BodyCharacteristic_Test"));
//        component.setCosmetic(CosmeticType.CAPES, null);
        component.setDirty(true);
        context.sendMessage(Message.raw("Changed model"));
    }
}