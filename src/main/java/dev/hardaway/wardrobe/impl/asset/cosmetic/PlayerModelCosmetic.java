package dev.hardaway.wardrobe.impl.asset.cosmetic;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import dev.hardaway.wardrobe.api.WardrobeTranslationProperties;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeVisibility;
import dev.hardaway.wardrobe.api.cosmetic.appearance.AppearanceCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.appearance.CosmeticAppearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.cosmetic.variant.CosmeticColorEntry;
import dev.hardaway.wardrobe.api.cosmetic.variant.CosmeticVariantEntry;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.impl.asset.cosmetic.appearance.VariantCosmeticAppearance;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.GradientTextureConfig;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.VariantTextureConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerModelCosmetic extends CosmeticAsset implements AppearanceCosmetic {

    public static final BuilderCodec<PlayerModelCosmetic> CODEC = BuilderCodec.builder(PlayerModelCosmetic.class, PlayerModelCosmetic::new, CosmeticAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Appearance", CosmeticAppearance.CODEC, true),
                    (t, value) -> t.appearance = value,
                    t -> t.appearance
            ).add()
            .build();

    private CosmeticAppearance appearance;

    private PlayerModelCosmetic() {
    }

    public PlayerModelCosmetic(String id, WardrobeTranslationProperties translationProperties, WardrobeVisibility wardrobeVisibility, String cosmeticSlotId, String iconPath, String permissionNode, CosmeticAppearance appearance) {
        super(id, translationProperties, wardrobeVisibility, cosmeticSlotId, iconPath, permissionNode);
        this.appearance = appearance;
    }

    @Override
    public CosmeticAppearance getAppearance() {
        return appearance;
    }

    @Override
    public void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic) {
        super.applyCosmetic(context, slot, playerCosmetic);
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(this.getAppearance().getModel(playerCosmetic.getVariantId()));
        TextureConfig textureConfig = this.getAppearance().getTextureConfig(playerCosmetic.getVariantId());

        Model model = Model.createUnitScaleModel(modelAsset);
        context.setPlayerModel(new Model(
                model.getModelAssetId(),
                model.getScale(),
                model.getRandomAttachmentIds(),
                model.getAttachments(),
                model.getBoundingBox(),
                model.getModel(),
                textureConfig.getTexture(playerCosmetic.getTextureId()),
                textureConfig.getGradientSet(),
                textureConfig.getGradientSet() != null ? playerCosmetic.getTextureId() : null,
                model.getEyeHeight(),
                model.getCrouchOffset(),
                model.getAnimationSetMap(),
                model.getCamera(),
                model.getLight(),
                model.getParticles(),
                model.getTrails(),
                model.getPhysicsValues(),
                model.getDetailBoxes(),
                model.getPhobia(),
                model.getPhobiaModelAssetId()
        ));
    }

    // TODO: create after decoding
    @Override
    public Map<String, CosmeticVariantEntry> getVariantEntries() {
        String[] variants = this.appearance.collectVariants();
        if (variants.length == 0) return Map.of();

        if (this.appearance instanceof VariantCosmeticAppearance v) {
            Map<String, CosmeticVariantEntry> entries = new LinkedHashMap<>();
            for (String variantId : variants) {
                VariantCosmeticAppearance.Entry entry = v.getVariants().get(variantId);
                entries.put(variantId, new CosmeticVariantEntry(
                        variantId,
                        entry.getTranslationProperties(),
                        entry.getIconPath()
                ));
            }
            return entries;
        }

        return Map.of();
    }

    @Override
    public List<CosmeticColorEntry> getColorEntries(@Nullable String variantId) {
        TextureConfig textureConfig = this.appearance.getTextureConfig(variantId);
        String[] textures = textureConfig.collectVariants();
        if (textures.length == 0) return List.of();

        List<CosmeticColorEntry> entries = new ArrayList<>();
        for (String textureId : textures) {
            String[] colors;
            if (textureConfig instanceof VariantTextureConfig vt) {
                colors = vt.getVariants().get(textureId).getColors();
            } else if (textureConfig instanceof GradientTextureConfig gt) {
                colors = CosmeticsModule.get().getRegistry().getGradientSets()
                        .get(gt.getGradientSet()).getGradients().get(textureId).getBaseColor();
            } else {
                continue;
            }
            entries.add(new CosmeticColorEntry(textureId, colors));
        }
        return entries;
    }
}
