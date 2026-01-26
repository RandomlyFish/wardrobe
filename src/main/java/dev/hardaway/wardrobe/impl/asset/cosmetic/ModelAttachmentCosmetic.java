package dev.hardaway.wardrobe.impl.asset.cosmetic;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import dev.hardaway.wardrobe.api.WardrobeTranslationProperties;
import dev.hardaway.wardrobe.api.cosmetic.Cosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeVisibility;
import dev.hardaway.wardrobe.api.cosmetic.appearance.AppearanceCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.appearance.CosmeticAppearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.impl.asset.cosmetic.builtin.HytaleCosmetic;

import javax.annotation.Nullable;

public class ModelAttachmentCosmetic extends CosmeticAsset implements AppearanceCosmetic {

    public static final BuilderCodec<ModelAttachmentCosmetic> CODEC = BuilderCodec.builder(ModelAttachmentCosmetic.class, ModelAttachmentCosmetic::new, CosmeticAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("OverlapCosmeticSlots", Codec.STRING_ARRAY),
                    (t, value) -> t.overlapCosmeticSlotIds = value,
                    t -> t.overlapCosmeticSlotIds
            ).add()
            .append(new KeyedCodec<>("Appearance", CosmeticAppearance.CODEC, true),
                    (t, value) -> t.appearance = value,
                    t -> t.appearance
            ).add()
            .append(new KeyedCodec<>("OverlapAppearance", CosmeticAppearance.CODEC),
                    (t, value) -> t.overlapAppearance = value,
                    t -> t.appearance
            ).add()
            .append(new KeyedCodec<>("ArmorAppearance", CosmeticAppearance.CODEC, false),
                    (t, value) -> t.armorAppearance = value,
                    t -> t.armorAppearance
            ).add()
            .build();

    private String[] overlapCosmeticSlotIds = new String[0];
    private CosmeticAppearance appearance;
    private @Nullable CosmeticAppearance overlapAppearance;
    private @Nullable CosmeticAppearance armorAppearance;

    protected ModelAttachmentCosmetic() {
    }

    public ModelAttachmentCosmetic(String id, WardrobeTranslationProperties translationProperties, WardrobeVisibility wardrobeVisibility, String cosmeticSlotId, String iconPath, String permissionNode, CosmeticAppearance appearance, @Nullable CosmeticAppearance overlapAppearance, @Nullable CosmeticAppearance armorAppearance) {
        super(id, translationProperties, wardrobeVisibility, cosmeticSlotId, iconPath, permissionNode);
        this.appearance = appearance;
        this.overlapAppearance = overlapAppearance;
        this.armorAppearance = armorAppearance;
    }

    public String[] getOverlapCosmeticSlotIds() {
        return overlapCosmeticSlotIds;
    }

    @Override
    public CosmeticAppearance getAppearance() {
        return appearance;
    }

    @Nullable
    public CosmeticAppearance getOverlapAppearance() {
        return overlapAppearance;
    }

    @Nullable
    public CosmeticAppearance getArmorAppearance() {
        return armorAppearance;
    }

    @Override
    public String[] getHiddenCosmeticSlotIds() {
        return new String[0];
    }

    @Override
    public void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic) {
        super.applyCosmetic(context, slot, playerCosmetic);
        CosmeticAppearance appearance = this.getAppearance();

        if (slot.getArmorSlot() != null && this.getArmorAppearance() != null) {
//            boolean shouldHide = switch (slot.getArmorSlot()) {
//                case Head -> context.getPlayerSettings().hideHelmet();
//                case Chest -> context.getPlayerSettings().hideCuirass();
//                case Hands -> context.getPlayerSettings().hideGauntlets();
//                case Legs -> context.getPlayerSettings().hidePants();
//            };
//
//            if (!shouldHide) {
//                ItemStack armor = context.getPlayer().getInventory().getArmor().getItemStack((short) slot.getArmorSlot().getValue());
//                if (armor != null) appearance = this.getArmorAppearance();
//            }
        } else if (this.getOverlapAppearance() != null) {
            boolean overlapFound = false;
            for (Cosmetic cosmetic : context.getCosmeticMap().values()) {
                if (overlapFound)
                    break;

                if (cosmetic instanceof HytaleCosmetic hytaleCosmetic) {
                    if (hytaleCosmetic.getPart().getHeadAccessoryType() == PlayerSkinPart.HeadAccessoryType.HalfCovering) {
                        overlapFound = true;
                        break;
                    }
                }
                if (cosmetic instanceof ModelAttachmentCosmetic modelAttachmentCosmetic) {
                    for (String overlapSlot : modelAttachmentCosmetic.overlapCosmeticSlotIds) {
                        if (slot.getId().equals(overlapSlot)) {
                            overlapFound = true;
                            break;
                        }
                    }
                }
            }

            if (overlapFound) {
                appearance = this.getOverlapAppearance();
            }
        }

        TextureConfig textureConfig = appearance.getTextureConfig(playerCosmetic.getVariantId());
        context.addAttachment(slot.getId(), new ModelAttachment(
                appearance.getModel(playerCosmetic.getVariantId()),
                textureConfig.getTexture(playerCosmetic.getTextureId()),
                textureConfig.getGradientSet(),
                textureConfig.getGradientSet() != null ? playerCosmetic.getTextureId() : null,
                1.0
        ));
    }

}
