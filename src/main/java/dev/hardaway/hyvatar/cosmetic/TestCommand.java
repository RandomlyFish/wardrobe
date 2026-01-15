package dev.hardaway.hyvatar.cosmetic;


import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.cosmetics.CosmeticRegistry;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;


public class TestCommand extends AbstractPlayerCommand {

    public TestCommand() {
        super("test", "Hyvatar test command");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Model baseModel = Model.createUnitScaleModel(ModelAsset.getAssetMap().getAsset("Player"));
        CosmeticRegistry cosmeticRegistry = CosmeticsModule.get().getRegistry();
        PlayerSkinPart popStarCape = cosmeticRegistry.getCapes().get("Cape_PopStar");
        PlayerSkinPart kingsCrown = cosmeticRegistry.getHeadAccessories().get("Head_Crown");

        ModelAttachment[] attachments = new ModelAttachment[]{
                new ModelAttachment(
                        "Characters/Body_Attachments/Faces/Player_Face_Detached.blockymodel",
                        "Characters/Body_Attachments/Faces/Faces_Detached_Textures/Face.png",
                        "Skin",
                        "02",
                        1.0
                ),
                new ModelAttachment(
                        "Characters/Body_Attachments/Ears/Ears1.blockymodel",
                        "Characters/Body_Attachments/Ears/Ears1_Textures/Ears1_Greyscale_Texture.png",
                        "Skin",
                        "02",
                        1.0
                ),
                new ModelAttachment(
                        "Characters/Body_Attachments/Mouths/Mouth1.blockymodel",
                        "Characters/Body_Attachments/Mouths/Mouth1_Textures/Default_Greyscale.png",
                        "Skin",
                        "02",
                        1.0
                ),
                new ModelAttachment(
                        "Characters/Body_Attachments/Eyes/Eyes.blockymodel",
                        "Characters/Body_Attachments/Eyes/Eyes_Textures/Greyscale_Large.png",
                        "Eyes_Gradient",
                        "Brown",
                        1.0
                ),
                new ModelAttachment(
                        popStarCape.getModel(),
                        popStarCape.getGreyscaleTexture(),
                        popStarCape.getGradientSet(),
                        "Red",
                        1.0
                ),
                new ModelAttachment(
                        kingsCrown.getModel(),
                        kingsCrown.getTextures().get("Red").getTexture(),
                        null,
                        null,
                        1.0
                )
        };


        Model model = new Model(
                baseModel.getModelAssetId(),
                baseModel.getScale(),
                baseModel.getRandomAttachmentIds(),
                attachments, // Skin attachments
                baseModel.getBoundingBox(),
                baseModel.getModel(), // Model
                baseModel.getTexture(), // Skin texture
                baseModel.getGradientSet(), // Skin gradient set
                baseModel.getGradientId(), // Skin gradient id
                baseModel.getEyeHeight(),
                baseModel.getCrouchOffset(),
                baseModel.getAnimationSetMap(),
                baseModel.getCamera(),
                baseModel.getLight(),
                baseModel.getParticles(),
                baseModel.getTrails(),
                baseModel.getPhysicsValues(),
                baseModel.getDetailBoxes(),
                baseModel.getPhobia(),
                baseModel.getPhobiaModelAssetId()
        );
        store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
        context.sendMessage(Message.raw("Changed model"));
    }
}