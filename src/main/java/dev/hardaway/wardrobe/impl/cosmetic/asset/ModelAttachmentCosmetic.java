package dev.hardaway.wardrobe.impl.cosmetic.asset;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.Cosmetic;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeGroup;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.impl.cosmetic.asset.texture.TextureConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// HiddenCosmeticGroups[] - array of CosmeticGroups that will be hidden when this cosmetic is worn
public class ModelAttachmentCosmetic extends CosmeticAsset {

    public static final BuilderCodec<ModelAttachmentCosmetic> CODEC = BuilderCodec.builder(ModelAttachmentCosmetic.class, ModelAttachmentCosmetic::new, CosmeticAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Model", Codec.STRING, true),
                    (t, value) -> t.model = value,
                    t -> t.model
            ).add()
            .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC, true),
                    (t, value) -> t.textureConfig = value,
                    t -> t.textureConfig
            ).add()
            .build();

    private String model;
    private TextureConfig textureConfig;

    protected ModelAttachmentCosmetic() {
    }

    public ModelAttachmentCosmetic(String id, String nameKey, String group, String icon, @Nullable String permissionNode, String model, TextureConfig textureConfig) {
        super(id, nameKey, group, icon, permissionNode);
        this.model = model;
        this.textureConfig = textureConfig;
    }

    @Nonnull
    public String getModel() {
        return model;
    }

    @Nonnull
    public TextureConfig getTextureConfig() {
        return textureConfig;
    }

    @Override
    public void applyCosmetic(WardrobeContext context, WardrobeGroup group, PlayerCosmetic playerCosmetic) {
        TextureConfig textureConfig = this.getTextureConfig();

        Player player = context.getPlayer();
        if (group.getHytaleCosmeticType() != null) {
            ItemContainer armorContainer = player.getInventory().getArmor();
            for (short i = 0; i < armorContainer.getCapacity(); i++) {
                ItemStack stack = armorContainer.getItemStack(i);
                if (stack == null || stack.isEmpty()) continue;

                ItemArmor armor = stack.getItem().getArmor();
                if (armor == null)
                    return;

                com.hypixel.hytale.protocol.ItemArmor protocolArmor = armor.toPacket();
                if (protocolArmor.cosmeticsToHide == null)
                    return;

                for (Cosmetic cosmetic : protocolArmor.cosmeticsToHide) {
                    CosmeticType type = protocolCosmeticToCosmeticType(cosmetic);
                    if (type == null)
                        continue;

                    if (group.getHytaleCosmeticType().equals(type))
                        return; // Cosmetic is hidden
                }
            }
        }

        context.addAttachment(group, new ModelAttachment(
                this.getModel(),
                textureConfig.getTexture(playerCosmetic.getTextureId()),
                textureConfig.getGradientSet(),
                textureConfig.getGradientSet() != null ? playerCosmetic.getTextureId() : null,
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
