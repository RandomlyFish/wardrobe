package dev.hardaway.wardrobe.impl.asset.cosmetic;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import dev.hardaway.wardrobe.api.WardrobeTranslationProperties;
import dev.hardaway.wardrobe.api.cosmetic.AppearanceCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeVisibility;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.CosmeticAppearance;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.TextureConfig;

public class ModelAttachmentCosmetic extends CosmeticAsset implements AppearanceCosmetic {

    public static final BuilderCodec<ModelAttachmentCosmetic> CODEC = BuilderCodec.builder(ModelAttachmentCosmetic.class, ModelAttachmentCosmetic::new, CosmeticAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Appearance", CosmeticAppearance.CODEC, true),
                    (t, value) -> t.appearance = value,
                    t -> t.appearance
            ).add()
            .build();

    private CosmeticAppearance appearance;

    protected ModelAttachmentCosmetic() {
    }

    public ModelAttachmentCosmetic(String id, WardrobeTranslationProperties translationProperties, WardrobeVisibility wardrobeVisibility, String cosmeticSlotId, String iconPath, String permissionNode, CosmeticAppearance appearance) {
        super(id, translationProperties, wardrobeVisibility, cosmeticSlotId, iconPath, permissionNode);
        this.appearance = appearance;
    }

    @Override
    public CosmeticAppearance getAppearance() {
        return appearance;
    }

    @Override
    public void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic) {
        TextureConfig textureConfig = this.getAppearance().getTextureConfig(playerCosmetic.getVariantId());
        context.addAttachment(slot, new ModelAttachment(
                this.getAppearance().getModel(playerCosmetic.getVariantId()),
                textureConfig.getTexture(playerCosmetic.getTextureId()),
                textureConfig.getGradientSet(),
                textureConfig.getGradientSet() != null ? playerCosmetic.getTextureId() : null,
                1.0
        ));
    }

}
