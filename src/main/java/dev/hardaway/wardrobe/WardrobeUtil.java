package dev.hardaway.wardrobe;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.cosmetics.*;
import dev.hardaway.wardrobe.cosmetic.CosmeticAssetData;

import javax.annotation.Nullable;
import java.util.function.Supplier;

// TODO: split this up
public class WardrobeUtil {

    // Extra Codecs
    public static final EnumCodec<PlayerSkinPart.HeadAccessoryType> HEAD_ACCESSORY_TYPE_CODEC = new EnumCodec<>(PlayerSkinPart.HeadAccessoryType.class, EnumCodec.EnumStyle.LEGACY);
    public static final EnumCodec<PlayerSkinPartType> SKIN_PART_TYPE_CODEC = new EnumCodec<>(PlayerSkinPartType.class, EnumCodec.EnumStyle.LEGACY);

    public static <T extends JsonAssetWithMap<String, DefaultAssetMap<String, T>>> Supplier<AssetStore<String, T, DefaultAssetMap<String, T>>> createAssetStore(Class<T> clazz) {
        return WardrobeUtil.memoize(() -> AssetRegistry.getAssetStore(clazz));
    }

    public static <Z> Supplier<Z> memoize(Supplier<Z> supplier) {
        return new Supplier<>() {
            Z value;

            @Override
            public Z get() {
                if (value == null)
                    value = supplier.get();
                return value;
            }
        };
    }

    public static PlayerSkin skinFromProtocol(com.hypixel.hytale.protocol.PlayerSkin protocolPlayerSkin) {
        return new PlayerSkin(protocolPlayerSkin.bodyCharacteristic, protocolPlayerSkin.underwear, protocolPlayerSkin.face, protocolPlayerSkin.ears, protocolPlayerSkin.mouth, protocolPlayerSkin.eyes, protocolPlayerSkin.facialHair, protocolPlayerSkin.haircut, protocolPlayerSkin.eyebrows, protocolPlayerSkin.pants, protocolPlayerSkin.overpants, protocolPlayerSkin.undertop, protocolPlayerSkin.overtop, protocolPlayerSkin.shoes, protocolPlayerSkin.headAccessory, protocolPlayerSkin.faceAccessory, protocolPlayerSkin.earAccessory, protocolPlayerSkin.skinFeature, protocolPlayerSkin.gloves, protocolPlayerSkin.cape);
    }

    public static CosmeticAssetData createCosmeticData(CosmeticType type, PlayerSkin skin) {
        PlayerSkin.PlayerSkinPartId skinPartId = switch (type) {
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
        };

        if (skinPartId == null)
            return null;

        CosmeticRegistry cosmeticRegistry = CosmeticsModule.get().getRegistry();
        PlayerSkinPart skinPart = (PlayerSkinPart) cosmeticRegistry.getByType(type).get(skinPartId.assetId);

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

        return new CosmeticAssetData(
                model,
                texture,
                gradientSet,
                gradientId
        );
    }

}
