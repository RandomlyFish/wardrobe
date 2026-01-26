package dev.hardaway.wardrobe.impl.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.Cosmetic;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticRegistry;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkin;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.asset.cosmetic.ModelAttachmentCosmetic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

        // If we don't have any cosmetics, stop wardrobe from handling the player
        if (wardrobeComponent.getCosmetics().isEmpty()) {
            Ref<EntityStore> ref = chunk.getReferenceTo(i);
            commandBuffer.tryRemoveComponent(ref, this.wardrobeComponentType);
            return;
        }

        Model model = buildWardrobeModel(
                chunk.getComponent(i, Player.getComponentType()),
                chunk.getComponent(i, PlayerSkinComponent.getComponentType()).getPlayerSkin(),
                wardrobeComponent
        );
        chunk.setComponent(i, ModelComponent.getComponentType(), new ModelComponent(model));
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return this.wardrobeComponentType;
    }

    public static Model buildWardrobeModel(Player player, com.hypixel.hytale.protocol.PlayerSkin skin, PlayerWardrobe wardrobeComponent) {
        // Build context
        Model playerModel = CosmeticsModule.get().createModel(skin); // TODO: apply body characteristics
        PlayerWardrobeContext context = new PlayerWardrobeContext(
                player,
                wardrobeComponent,
                new HashMap<>(),
                new HashMap<>(),
                new HashSet<>(),
                playerModel
        );

        // Populate the appliedCosmetics map with Id -> WardrobeCosmetic for quick lookup
        for (PlayerCosmetic wornCosmetic : wardrobeComponent.getCosmetics()) {
            WardrobeCosmetic cosmetic = WardrobeCosmetic.getAssetMap().getAsset(wornCosmetic.getCosmeticId()); // TODO: replace with registry
            if (cosmetic != null) {
                context.getCosmeticMap().put(wornCosmetic.getCosmeticId(), cosmetic);
            }
        }

        // Go through each slot and apply the cosmetic if there is one, or attach the hytale cosmetic associated with the slot.
        Map<String, ? extends WardrobeCosmeticSlot> slots = WardrobeCosmeticSlot.getAssetMap().getAssetMap();
        for (WardrobeCosmeticSlot group : slots.values()) {
            PlayerCosmetic playerCosmetic = wardrobeComponent.getCosmetic(group);
            if (playerCosmetic != null) {
                WardrobeCosmetic cosmetic = context.getCosmeticMap().get(playerCosmetic.getCosmeticId());
                cosmetic.applyCosmetic(context, group, playerCosmetic);
            } else if (group.getHytaleCosmeticType() != null) {
                PlayerWardrobeSystem.applyHytaleCosmetic(context, group, skinFromProtocol(skin));
            }
        }


        // Handle armor hiding
        Set<CosmeticType> hiddenHytaleSlots = new HashSet<>();
        ItemContainer armorContainer = player.getInventory().getArmor();
        for (short armorIndex = 0; armorIndex < armorContainer.getCapacity(); armorIndex++) {
            ItemStack stack = armorContainer.getItemStack(armorIndex);
            if (stack == null || stack.isEmpty()) continue;

            ItemArmor armor = stack.getItem().getArmor();
            if (armor == null)
                break;

            com.hypixel.hytale.protocol.ItemArmor protocolArmor = armor.toPacket();
            if (protocolArmor.cosmeticsToHide == null)
                break;

            for (Cosmetic cosmetic : protocolArmor.cosmeticsToHide) {
                CosmeticType type = protocolCosmeticToCosmeticType(cosmetic);
                if (type == null)
                    continue;

                hiddenHytaleSlots.add(type);
            }
        }

        // Build the attachment set
        Set<ModelAttachment> modelAttachments = new HashSet<>();
        Map<String, ModelAttachment> attachmentsMap = context.getModelAttachments();
        for (Map.Entry<String, ModelAttachment> entry : attachmentsMap.entrySet()) {
            String slotId = entry.getKey();
            if (entry.getValue() == null || context.slotsToHide.contains(slotId)) {
                continue;
            }
            WardrobeCosmeticSlot slot = slots.get(slotId);
            if (slot != null && slot.getHytaleCosmeticType() != null && hiddenHytaleSlots.contains(slot.getHytaleCosmeticType())) {
                continue;
            }

            modelAttachments.add(entry.getValue());
        }

        Model contextModel = context.getPlayerModel();
        return new Model(
                "Wardrobe_" + player.getDisplayName() + "_" + contextModel.getModelAssetId(),
                contextModel.getScale(),
                contextModel.getRandomAttachmentIds(),
                modelAttachments.toArray(ModelAttachment[]::new), // Skin attachments
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
    }

    public static PlayerSkin skinFromProtocol(com.hypixel.hytale.protocol.PlayerSkin protocolPlayerSkin) {
        return new PlayerSkin(protocolPlayerSkin.bodyCharacteristic, protocolPlayerSkin.underwear, protocolPlayerSkin.face, protocolPlayerSkin.ears, protocolPlayerSkin.mouth, protocolPlayerSkin.eyes, protocolPlayerSkin.facialHair, protocolPlayerSkin.haircut, protocolPlayerSkin.eyebrows, protocolPlayerSkin.pants, protocolPlayerSkin.overpants, protocolPlayerSkin.undertop, protocolPlayerSkin.overtop, protocolPlayerSkin.shoes, protocolPlayerSkin.headAccessory, protocolPlayerSkin.faceAccessory, protocolPlayerSkin.earAccessory, protocolPlayerSkin.skinFeature, protocolPlayerSkin.gloves, protocolPlayerSkin.cape);
    }

    // TODO: support hytale RequiresGenericHair
    // TODO: support hytale HeadAccessoryType
    // TODO: support Overlap property
    // TODO: support HiddenCosmeticSlots
    public static void applyHytaleCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerSkin skin) {
        PlayerSkin.PlayerSkinPartId skinPartId = switch (slot.getHytaleCosmeticType()) {
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
        PlayerSkinPart skinPart = (PlayerSkinPart) cosmeticRegistry.getByType(slot.getHytaleCosmeticType()).get(skinPartId.assetId);

        if (skinPart.getHeadAccessoryType() == PlayerSkinPart.HeadAccessoryType.FullyCovering) {
            context.hideSlots("Haircut");
        } else if (skinPart.getHeadAccessoryType() == PlayerSkinPart.HeadAccessoryType.HalfCovering) {
            // TODO: tell other cosmetics that this is covering
        }

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

        // TODO: support builtin hytale cosmetics that cover up the hair
        if (skinPart.doesRequireGenericHaircut() && skinPart.getHairType() != null) {
            for (WardrobeCosmetic cosmetic : context.getCosmeticMap().values()) {
                if (cosmetic instanceof ModelAttachmentCosmetic modelAttachmentCosmetic) {
                    for (String overlapSlot : modelAttachmentCosmetic.getOverlapCosmeticSlotIds()) {
                        if (slot.getId().equals(overlapSlot)) {
                            // TODO: add proper fallbacks with registry
                            PlayerSkinPart genericHaricut = cosmeticRegistry.getHaircuts().get(String.valueOf(skinPart.getHairType()));

                            if (genericHaricut != null) {
                                model = genericHaricut.getModel();
                                texture = genericHaricut.getGreyscaleTexture();
                            }

                            break;
                        }
                    }
                }
            }
        }

        context.addAttachment(slot.getId(), new ModelAttachment(
                model,
                texture,
                gradientSet,
                gradientId,
                1.0
        ));
    }

    @Nullable
    private static CosmeticType protocolCosmeticToCosmeticType(Cosmetic cosmetic) {
        return switch (cosmetic) {
            case Haircut -> CosmeticType.HAIRCUTS;
            case FacialHair -> CosmeticType.FACIAL_HAIR;
            case Undertop -> CosmeticType.UNDERTOPS;
            case Overtop -> CosmeticType.OVERTOPS;
            case Pants -> CosmeticType.PANTS;
            case Overpants -> CosmeticType.OVERPANTS;
            case Shoes -> CosmeticType.SHOES;
            case Gloves -> CosmeticType.GLOVES;
            case Cape -> CosmeticType.CAPES;
            case HeadAccessory -> CosmeticType.HEAD_ACCESSORY;
            case FaceAccessory -> CosmeticType.FACE_ACCESSORY;
            case EarAccessory -> CosmeticType.EAR_ACCESSORY;
            case Ear -> CosmeticType.EARS;
            case null -> null;
        };
    }
}
