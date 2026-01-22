package dev.hardaway.wardrobe.impl.command;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.impl.asset.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.system.CosmeticSaveData;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;

import javax.annotation.Nonnull;

public class WardrobeWearCommand extends AbstractPlayerCommand {

    private final ComponentType<EntityStore, PlayerWardrobeComponent> playerWardrobeComponentType;
    private final RequiredArg<String> cosmeticArg;
    private final OptionalArg<String> variantArg;

    public WardrobeWearCommand(ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType) {
        super("wear", "Wear a cosmetic");
        this.setPermissionGroup(GameMode.Adventure);
        this.playerWardrobeComponentType = wardrobeComponentType;
        this.cosmeticArg = this.withRequiredArg("cosmetic", "The cosmetic to apply", ArgTypes.STRING);
        this.variantArg = this.withOptionalArg("variant", "The cosmetic's variant to use", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        PlayerWardrobeComponent wardrobeComponent = store.ensureAndGetComponent(ref, this.playerWardrobeComponentType);
        String id = cosmeticArg.get(context);
        WardrobeCosmetic cosmetic = CosmeticAsset.getAssetMap().getAsset(id);
        if (cosmetic == null) {
            context.sendMessage(Message.raw("Failed to find cosmetic with id \"{id}\"!").param("id", id));
            return;
        }

//        String variant = null;
//        if (variantArg.provided(context)) {
//            String provided = variantArg.get(context);
//            if (!cosmetic.getVariants().contains(provided)) {
//                context.sendMessage(Message.raw("Failed to find a variant for this cosmetic with id \"{id}\"!").param("id", provided));
//                return;
//            } else variant = provided;
//        }
//
//        if (variant == null && !cosmetic.getVariants().isEmpty()) variant = cosmetic.getVariants().getFirst();
//
//        WardrobeCosmeticSlot cosmeticGroup = cosmetic.getCosmeticSlotId();
//
//        wardrobeComponent.setCosmetic(cosmeticGroup, new CosmeticSaveData(id, variant));
//        context.sendMessage(Message.raw("Cosmetic worn"));
    }
}
