package dev.hardaway.wardrobe.impl.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ItemArmorSlot;
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
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.Cosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.asset.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.asset.cosmetic.builtin.HytaleBodyCharacteristicCosmetic;
import dev.hardaway.wardrobe.impl.asset.cosmetic.builtin.HytaleCosmetic;
import dev.hardaway.wardrobe.impl.asset.cosmetic.builtin.HytaleHaircutCosmetic;
import dev.hardaway.wardrobe.impl.asset.cosmetic.builtin.HytalePlayerCosmetic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class WardrobeSystems {
    public static class Tick extends EntityTickingSystem<EntityStore> {

        // TODO: rebuild on asset reload and equipment update
        @Override
        public void tick(float v, int i, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            PlayerWardrobeComponent wardrobeComponent = chunk.getComponent(i, PlayerWardrobeComponent.getComponentType());
            if (wardrobeComponent == null || !wardrobeComponent.consumeDirty()) // TODO: use events instead of ticking
                return;

            // If we don't have any cosmetics, stop wardrobe from handling the player
            if (wardrobeComponent.getCosmetics().isEmpty()) {
                Ref<EntityStore> ref = chunk.getReferenceTo(i);
                commandBuffer.tryRemoveComponent(ref, PlayerWardrobeComponent.getComponentType());
                return;
            }

            Model model = buildWardrobeModel(
                    chunk.getComponent(i, Player.getComponentType()),
                    chunk.getComponent(i, PlayerSettings.getComponentType()),
                    chunk.getComponent(i, PlayerSkinComponent.getComponentType()).getPlayerSkin(),
                    wardrobeComponent,
                    chunk.getComponent(i, PlayerRef.getComponentType())
            );
            chunk.setComponent(i, ModelComponent.getComponentType(), new ModelComponent(model));
        }

        @Nullable
        @Override
        public Query<EntityStore> getQuery() {
            return Query.and(PlayerWardrobe.getComponentType(), PlayerSettings.getComponentType(), PlayerSkinComponent.getComponentType());
        }

        public static Model buildWardrobeModel(Player player, PlayerSettings playerSettings, com.hypixel.hytale.protocol.PlayerSkin skin, PlayerWardrobe wardrobeComponent, PlayerRef playerRef) {
            // Build context
            Model playerModel = CosmeticsModule.get().createModel(skin); // TODO: apply body characteristics
            PlayerWardrobeContext context = new PlayerWardrobeContext(
                    player,
                    playerSettings,
                    wardrobeComponent,
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashSet<>(),
                    playerModel
            );

            // Populate the applied cosmetics map
            PlayerSkin cosmeticSkin = skinFromProtocol(skin);
            Map<String, ? extends WardrobeCosmeticSlot> slots = WardrobeCosmeticSlot.getAssetMap().getAssetMap();
            for (WardrobeCosmeticSlot group : slots.values()) {
                PlayerCosmetic playerCosmetic = wardrobeComponent.getCosmetic(group);
                if (playerCosmetic != null) {
                    Cosmetic cosmetic = CosmeticAsset.getAssetMap().getAsset(playerCosmetic.getCosmeticId()); // TODO: replace with registry
                    if (cosmetic != null) {
                        context.getCosmeticMap().put(group.getId(), cosmetic);
                    }
                } else if (group.getHytaleCosmeticType() != null) {
                    HytaleCosmetic hytaleCosmetic = Tick.createHytaleCosmetic(group.getHytaleCosmeticType(), cosmeticSkin);
                    if (hytaleCosmetic != null) {
                        context.getCosmeticMap().put(group.getId(), hytaleCosmetic);
                    }
                }
            }

            // Apply custom cosmetics
            context.getCosmeticMap().forEach((slotId, cosmetic) -> {
                WardrobeCosmeticSlot slot = slots.get(slotId); // TODO: replace with registry
                if (slot != null) {
                    // TODO: validate & warn
                    PlayerCosmetic cosmeticData = wardrobeComponent.getCosmetic(slotId);
                    if (cosmeticData == null && cosmetic instanceof HytaleCosmetic) {
                        PlayerSkin.PlayerSkinPartId part = getSkinPartForType(slot.getHytaleCosmeticType(), cosmeticSkin);
                        if (part == null)
                            return;

                        cosmeticData = new HytalePlayerCosmetic(part);
                    }

                    cosmetic.applyCosmetic(context, slot, cosmeticData);
                }
            });

            // Handle armor hiding
            Set<CosmeticType> hiddenHytaleSlots = new HashSet<>();
            ItemContainer armorContainer = player.getInventory().getArmor();
            for (short armorIndex = 0; armorIndex < armorContainer.getCapacity(); armorIndex++) {
                ItemStack stack = armorContainer.getItemStack(armorIndex);
                if (stack == null || stack.isEmpty()) continue;

                ItemArmor armor = stack.getItem().getArmor();
                if (armor == null)
                    break;

                ItemArmorSlot slot = armor.getArmorSlot();
                boolean shouldHide = switch (slot) {
                    case Head -> playerSettings.hideHelmet();
                    case Chest -> playerSettings.hideCuirass();
                    case Hands -> playerSettings.hideGauntlets();
                    case Legs -> playerSettings.hidePants();
                };

                if (shouldHide)
                    continue;

                com.hypixel.hytale.protocol.ItemArmor protocolArmor = armor.toPacket();
                if (protocolArmor.cosmeticsToHide == null)
                    break;

                for (com.hypixel.hytale.protocol.Cosmetic cosmetic : protocolArmor.cosmeticsToHide) {
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

        public static HytaleCosmetic createHytaleCosmetic(CosmeticType type, PlayerSkin skin) {
            PlayerSkin.PlayerSkinPartId skinPartId = getSkinPartForType(type, skin);
            if (skinPartId == null)
                return null;

            CosmeticRegistry cosmeticRegistry = CosmeticsModule.get().getRegistry();
            PlayerSkinPart skinPart = (PlayerSkinPart) cosmeticRegistry.getByType(type).get(skinPartId.assetId);

            return switch (type) {
                case HAIRCUTS -> new HytaleHaircutCosmetic(type, skinPart);
                case BODY_CHARACTERISTICS -> new HytaleBodyCharacteristicCosmetic(type, skinPart);
                default -> new HytaleCosmetic(type, skinPart);
            };
        }

        @Nullable
        private static PlayerSkin.PlayerSkinPartId getSkinPartForType(CosmeticType type, PlayerSkin skin) {
            return switch (type) {
                case EMOTES, GRADIENT_SETS, EYE_COLORS, SKIN_TONES -> null;
                case BODY_CHARACTERISTICS -> skin.getBodyCharacteristic();
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
                case EARS ->
                        new PlayerSkin.PlayerSkinPartId(skin.getEars(), skin.getBodyCharacteristic().textureId, null);
                case FACE ->
                        new PlayerSkin.PlayerSkinPartId(skin.getFace(), skin.getBodyCharacteristic().textureId, null);
                case MOUTHS ->
                        new PlayerSkin.PlayerSkinPartId(skin.getMouth(), skin.getBodyCharacteristic().textureId, null);
                case null -> null;
            };
        }

        @Nullable
        private static CosmeticType protocolCosmeticToCosmeticType(com.hypixel.hytale.protocol.Cosmetic cosmetic) {
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

    public static class EntityAdded extends RefSystem<EntityStore> {

        @Override
        public Query<EntityStore> getQuery() {
            return PlayerWardrobe.getComponentType();
        }

        @Override
        public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            PlayerWardrobe wardrobe = store.getComponent(ref, PlayerWardrobe.getComponentType());
            wardrobe.rebuild();
        }

        @Override
        public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        }
    }

    public static class WardrobeChanged extends RefChangeSystem<EntityStore, PlayerWardrobeComponent> {

        private static final Query<EntityStore> QUERY = Query.and(Player.getComponentType(), PlayerSkinComponent.getComponentType(), ModelComponent.getComponentType());

        @Nonnull
        @Override
        public ComponentType<EntityStore, PlayerWardrobeComponent> componentType() {
            return PlayerWardrobeComponent.getComponentType();
        }

        @Override
        public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull PlayerWardrobeComponent wardrobe, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            wardrobe.rebuild();
        }

        @Override
        public void onComponentSet(@Nonnull Ref<EntityStore> ref, @Nullable PlayerWardrobeComponent oldWardrobe, @Nonnull PlayerWardrobeComponent newWardrobe, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            newWardrobe.rebuild();
        }

        @Override
        public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull PlayerWardrobeComponent wardrobe, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            PlayerSkinComponent playerSkinComponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
            if (playerSkinComponent != null) {
                playerSkinComponent.setNetworkOutdated();
                Model newModel = CosmeticsModule.get().createModel(playerSkinComponent.getPlayerSkin());
                if (newModel != null) {
                    commandBuffer.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
                }
            }
        }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() {
            return QUERY;
        }
    }

    public static class ArmorVisibilityChanged extends RefChangeSystem<EntityStore, PlayerSettings> {

        private static final Query<EntityStore> QUERY = Query.and(Player.getComponentType(), PlayerWardrobe.getComponentType());

        @Nonnull
        @Override
        public ComponentType<EntityStore, PlayerSettings> componentType() {
            return PlayerSettings.getComponentType();
        }

        @Override
        public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull PlayerSettings settings, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        }

        @Override
        public void onComponentSet(@Nonnull Ref<EntityStore> ref, @Nullable PlayerSettings oldSettings, @Nonnull PlayerSettings newSettings, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            // If there are no armor visibility changes, then don't rebuild the wardrobe
            if (oldSettings != null && (
                    Objects.equals(oldSettings.hideHelmet(), newSettings.hideHelmet())
                            && Objects.equals(oldSettings.hideCuirass(), newSettings.hideCuirass())
                            && Objects.equals(oldSettings.hidePants(), newSettings.hidePants())
                            && Objects.equals(oldSettings.hideGauntlets(), newSettings.hideGauntlets())
            )) return;

            store.getComponent(ref, PlayerWardrobe.getComponentType()).rebuild();
        }

        @Override
        public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull PlayerSettings settings, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() {
            return QUERY;
        }
    }
}
