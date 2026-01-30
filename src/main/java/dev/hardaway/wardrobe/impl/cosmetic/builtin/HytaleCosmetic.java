package dev.hardaway.wardrobe.impl.cosmetic.builtin;

import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;
import dev.hardaway.wardrobe.api.cosmetic.Cosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public class HytaleCosmetic implements Cosmetic {

    private final CosmeticType type;
    private final PlayerSkinPart part;
    protected final Map<String, PlayerSkinPart.Variant> variantMap;

    public HytaleCosmetic(CosmeticType type, PlayerSkinPart part) {
        this.type = type;
        this.part = part;

        if (this.part.getVariants() != null) {
            this.variantMap = Collections.unmodifiableMap(this.part.getVariants());
        } else {
            this.variantMap = Collections.emptyMap();
        }
    }

    @Override
    public String getId() {
        return "Hytale:" + this.type.name() + "." + part.getId();
    }

    public CosmeticType getType() {
        return type;
    }

    public PlayerSkinPart getPart() {
        return part;
    }

    protected ModelAttachment createAttachment(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic) {
        String model;
        String texture;
        @Nullable String gradientSet = null;
        @Nullable String gradientId = null;

        if (!this.variantMap.isEmpty()) {
            PlayerSkinPart.Variant variant = this.variantMap.get(playerCosmetic.getVariantId());
            model = variant.getModel();

            if (variant.getTextures() != null) {
                texture = variant.getTextures().get(playerCosmetic.getTextureId()).getTexture();
            } else {
                texture = variant.getGreyscaleTexture();
                gradientSet = this.part.getGradientSet();
                gradientId = playerCosmetic.getTextureId();
            }
        } else {
            model = this.part.getModel();

            if (this.part.getTextures() != null) {
                texture = this.part.getTextures().get(playerCosmetic.getTextureId()).getTexture();
            } else {
                texture = this.part.getGreyscaleTexture();
                gradientSet = this.part.getGradientSet();
                gradientId = playerCosmetic.getTextureId();
            }
        }

        if (this.part.getHeadAccessoryType() == PlayerSkinPart.HeadAccessoryType.FullyCovering) {
            context.hideSlots("Haircut");
        }

        return new ModelAttachment(
                model,
                texture,
                gradientSet,
                gradientId,
                1.0
        );
    }

    @Override
    public void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic) {
        context.addAttachment(slot.getId(), this.createAttachment(context, slot, playerCosmetic));
    }

    @Override
    public String getPermissionNode() {
        return "";
    }
}
