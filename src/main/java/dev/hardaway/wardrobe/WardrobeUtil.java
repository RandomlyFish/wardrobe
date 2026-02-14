package dev.hardaway.wardrobe;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.Notification;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.cosmetics.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.impl.cosmetic.builtin.HytaleBodyCharacteristicCosmetic;
import dev.hardaway.wardrobe.impl.cosmetic.builtin.HytaleCosmetic;
import dev.hardaway.wardrobe.impl.cosmetic.builtin.HytaleHaircutCosmetic;
import dev.hardaway.wardrobe.impl.player.PlayerWardrobeComponent;

import javax.annotation.Nullable;

public class WardrobeUtil {

    public static void rebuildAllWardrobes() {
        Universe universe = Universe.get();
        universe.getPlayers().forEach(playerRef -> {
            World world = universe.getWorld(playerRef.getWorldUuid());
            world.execute(() -> {
                Store<EntityStore> store = world.getEntityStore().getStore();

                PlayerWardrobeComponent wardrobe = store.getComponent(playerRef.getReference(), PlayerWardrobeComponent.getComponentType());
                if (wardrobe != null) wardrobe.rebuild();
                // TODO: close or update all wardrobe pages
            });
        });
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
        if (skinPart == null) return null;

        return switch (type) {
            case HAIRCUTS -> new HytaleHaircutCosmetic(type, skinPart);
            case BODY_CHARACTERISTICS -> new HytaleBodyCharacteristicCosmetic(type, skinPart);
            default -> new HytaleCosmetic(type, skinPart);
        };
    }

    @Nullable
    public static PlayerSkin.PlayerSkinPartId getSkinPartForType(CosmeticType type, PlayerSkin skin) {
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
            case EARS -> new PlayerSkin.PlayerSkinPartId(skin.getEars(), skin.getBodyCharacteristic().textureId, null);
            case FACE -> new PlayerSkin.PlayerSkinPartId(skin.getFace(), skin.getBodyCharacteristic().textureId, null);
            case MOUTHS ->
                    new PlayerSkin.PlayerSkinPartId(skin.getMouth(), skin.getBodyCharacteristic().textureId, null);
            case null -> null;
        };
    }

    @Nullable
    public static CosmeticType protocolCosmeticToCosmeticType(com.hypixel.hytale.protocol.Cosmetic cosmetic) {
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

    public static boolean canBeHidden(CosmeticType type) {
        return switch (type) {
            case EMOTES, GRADIENT_SETS, EYE_COLORS, SKIN_TONES, SKIN_FEATURES, EYES, UNDERWEAR, BODY_CHARACTERISTICS,
                 FACE, MOUTHS, EARS -> false;
            case HAIRCUTS, EYEBROWS, FACIAL_HAIR, HEAD_ACCESSORY, FACE_ACCESSORY, EAR_ACCESSORY, UNDERTOPS, OVERTOPS,
                 GLOVES, PANTS, OVERPANTS, SHOES, CAPES -> true;
        };
    }

    public static void sendError(PlayerRef playerRef) {
        Notification notification = new Notification();
        notification.style = NotificationStyle.Warning;
        notification.message = Message.raw("Error rebuilding wardrobe").getFormattedMessage();
        notification.secondaryMessage = Message.raw("Some cosmetics were invalid").getFormattedMessage();
        notification.icon = "Icons/AssetNotifications/IconAlert.png";
        playerRef.getPacketHandler().writeNoCache(notification);
    }
}
