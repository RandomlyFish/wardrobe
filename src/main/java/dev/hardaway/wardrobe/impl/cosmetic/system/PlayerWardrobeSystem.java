package dev.hardaway.wardrobe.impl.cosmetic.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticRegistry;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkin;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeGroup;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.impl.cosmetic.asset.CosmeticGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PlayerWardrobeSystem extends EntityTickingSystem<EntityStore> {
    private final ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType;

    public PlayerWardrobeSystem(ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType) {
        this.wardrobeComponentType = wardrobeComponentType;
    }

    // TODO: rebuild on asset reload and equipment update
    @Override
    public void tick(float v, int i, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        PlayerWardrobeComponent wardrobeComponent = chunk.getComponent(i, this.wardrobeComponentType);
        if (wardrobeComponent == null || !wardrobeComponent.consumeDirty()) // TODO: use events instead of ticking
            return;

        if (wardrobeComponent.getCosmetics().isEmpty()) {
            Ref<EntityStore> ref = chunk.getReferenceTo(i);
            commandBuffer.tryRemoveComponent(ref, this.wardrobeComponentType);
            return;
        }

        Player player = chunk.getComponent(i, Player.getComponentType());
        PlayerSkinComponent skinComponent = chunk.getComponent(i, PlayerSkinComponent.getComponentType());
        Model playerModel = CosmeticsModule.get().createModel(skinComponent.getPlayerSkin()); // TODO: recreate skin
        PlayerWardrobeContext context = new PlayerWardrobeContext(
                player,
                wardrobeComponent,
                playerModel,
                new HashMap<>(),
                new HashSet<>()
        );

        PlayerSkin skin = skinFromProtocol(skinComponent.getPlayerSkin());
        for (WardrobeGroup group : CosmeticGroup.getAssetMap().getAssetMap().values()) {
            PlayerCosmetic playerCosmetic = wardrobeComponent.getCosmetic(group);
            if (playerCosmetic != null) {
                WardrobeCosmetic cosmetic = playerCosmetic.getCosmetic();
                if (cosmetic != null) {
                    cosmetic.applyCosmetic(context, group, playerCosmetic);
                }
            } else if (group.getHytaleCosmeticType() != null) {
                PlayerWardrobeSystem.applyHytaleCosmetic(context, group, skin);
            }
        }

        Model contextModel = context.getPlayerModel();

        Map<WardrobeGroup, ModelAttachment> attachmentsMap = context.getModelAttachments();
        ModelAttachment[] attachments = new ModelAttachment[attachmentsMap.size()];
        int currentIndex = 0;
        for (Map.Entry<WardrobeGroup, ModelAttachment> entry : attachmentsMap.entrySet()) {
            if (context.groupsToHide.contains(entry.getKey()) || entry.getValue() == null)
                continue;

            attachments[currentIndex++] = entry.getValue();
        }

        Model model = new Model(
                "Wardrobe_" + player.getDisplayName() + "_" + contextModel.getModelAssetId(),
                contextModel.getScale(),
                contextModel.getRandomAttachmentIds(),
                attachments, // Skin attachments
                contextModel.getBoundingBox(),
                contextModel.getModel(), // Model
                contextModel.getTexture(), // Skin texture
                contextModel.getGradientSet(), // Skin gradient set
                contextModel.getGradientId(), // Skin gradient id
                contextModel.getEyeHeight(),
                contextModel.getCrouchOffset(),
                contextModel.getAnimationSetMap(),
                contextModel.getCamera(),
                contextModel.getLight(),
                contextModel.getParticles(),
                contextModel.getTrails(),
                contextModel.getPhysicsValues(),
                contextModel.getDetailBoxes(),
                contextModel.getPhobia(),
                contextModel.getPhobiaModelAssetId()
        );
        chunk.setComponent(i, ModelComponent.getComponentType(), new ModelComponent(model));
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return this.wardrobeComponentType;
    }


    public static PlayerSkin skinFromProtocol(com.hypixel.hytale.protocol.PlayerSkin protocolPlayerSkin) {
        return new PlayerSkin(protocolPlayerSkin.bodyCharacteristic, protocolPlayerSkin.underwear, protocolPlayerSkin.face, protocolPlayerSkin.ears, protocolPlayerSkin.mouth, protocolPlayerSkin.eyes, protocolPlayerSkin.facialHair, protocolPlayerSkin.haircut, protocolPlayerSkin.eyebrows, protocolPlayerSkin.pants, protocolPlayerSkin.overpants, protocolPlayerSkin.undertop, protocolPlayerSkin.overtop, protocolPlayerSkin.shoes, protocolPlayerSkin.headAccessory, protocolPlayerSkin.faceAccessory, protocolPlayerSkin.earAccessory, protocolPlayerSkin.skinFeature, protocolPlayerSkin.gloves, protocolPlayerSkin.cape);
    }

    // TODO: support hytale generic hair
    // TODO: support hytale headaccessorytype
    public static void applyHytaleCosmetic(WardrobeContext context, WardrobeGroup group, PlayerSkin skin) {
        PlayerSkin.PlayerSkinPartId skinPartId = switch (group.getHytaleCosmeticType()) {
            case EMOTES, GRADIENT_SETS, EYE_COLORS, SKIN_TONES, BODY_CHARACTERISTICS -> null;
            case UNDERWEAR -> skin.getUnderwear();
            case EYEBROWS -> skin.getEyebrows();
            case EYES -> skin.getEyes();
            case FACIAL_HAIR -> skin.getFacialHair();
            case PANTS -> skin.getPants();
            case OVERPANTS -> skin.getOverpants();
            case UNDERTOPS -> skin.getUndertop();
            case OVERTOPS -> skin.getOvertop();
            case HAIRCUTS -> skin.getHaircut();
            case SHOES -> skin.getShoes();
            case HEAD_ACCESSORY -> skin.getHeadAccessory();
            case FACE_ACCESSORY -> skin.getFaceAccessory();
            case EAR_ACCESSORY -> skin.getEarAccessory();
            case GLOVES -> skin.getGloves();
            case CAPES -> skin.getCape();
            case SKIN_FEATURES -> skin.getSkinFeature();
            case EARS -> new PlayerSkin.PlayerSkinPartId(skin.getEars(), skin.getBodyCharacteristic().textureId, null);
            case FACE -> new PlayerSkin.PlayerSkinPartId(skin.getFace(), skin.getBodyCharacteristic().textureId, null);
            case MOUTHS ->
                    new PlayerSkin.PlayerSkinPartId(skin.getMouth(), skin.getBodyCharacteristic().textureId, null);
            case null -> null;
        };

        if (skinPartId == null)
            return;

        CosmeticRegistry cosmeticRegistry = CosmeticsModule.get().getRegistry();
        PlayerSkinPart skinPart = (PlayerSkinPart) cosmeticRegistry.getByType(group.getHytaleCosmeticType()).get(skinPartId.assetId);

        String model;
        String texture;
        @Nullable String gradientSet = null;
        @Nullable String gradientId = null;

        if (skinPart.getVariants() != null && skinPartId.variantId != null) {
            PlayerSkinPart.Variant variant = skinPart.getVariants().get(skinPartId.variantId);
            model = variant.getModel();

            if (variant.getTextures() != null) {
                texture = variant.getTextures().get(skinPartId.textureId).getTexture();
            } else {
                texture = variant.getGreyscaleTexture();
                gradientSet = skinPart.getGradientSet();
                gradientId = skinPartId.getTextureId();
            }
        } else {
            model = skinPart.getModel();
            if (skinPart.getTextures() != null) {
                texture = skinPart.getTextures().get(skinPartId.textureId).getTexture();
            } else {
                texture = skinPart.getGreyscaleTexture();
                gradientSet = skinPart.getGradientSet();
                gradientId = skinPartId.getTextureId();
            }
        }

        context.addAttachment(group, new ModelAttachment(
                model,
                texture,
                gradientSet,
                gradientId,
                1.0
        ));
    }
}
