package dev.hardaway.wardrobe.impl.cosmetic;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.cosmetic.appearance.Appearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.AppearanceCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;
import dev.hardaway.wardrobe.api.menu.variant.CosmeticOptionEntry;
import dev.hardaway.wardrobe.api.menu.variant.CosmeticVariantEntry;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.property.WardrobeProperties;
import dev.hardaway.wardrobe.api.property.WardrobeTranslationProperties;
import dev.hardaway.wardrobe.api.property.WardrobeVisibility;
import dev.hardaway.wardrobe.impl.cosmetic.appearance.VariantAppearance;
import dev.hardaway.wardrobe.impl.cosmetic.texture.GradientTextureConfig;
import dev.hardaway.wardrobe.impl.cosmetic.texture.VariantTextureConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerModelCosmetic extends CosmeticAsset implements AppearanceCosmetic {

    public static final BuilderCodec<PlayerModelCosmetic> CODEC = BuilderCodec.builder(PlayerModelCosmetic.class, PlayerModelCosmetic::new, CosmeticAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Appearance", Appearance.CODEC, true),
                    (t, value) -> t.appearance = value,
                    t -> t.appearance
            )
            .addValidator(Validators.nonNull())
            .add()
            .build();

    private Appearance appearance;

    private PlayerModelCosmetic() {
    }

    public PlayerModelCosmetic(String id, String[] hiddenCosmeticSlots, String cosmeticSlotId, WardrobeProperties properties, Appearance appearance) {
        super(id, cosmeticSlotId, hiddenCosmeticSlots, properties);
        this.appearance = appearance;
    }

    @Override
    public Appearance getAppearance() {
        return appearance;
    }

    @Override
    public void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic) {
        super.applyCosmetic(context, slot, playerCosmetic);
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(this.getAppearance().getModel(playerCosmetic.getOptionId()));
        TextureConfig textureConfig = this.getAppearance().getTextureConfig(playerCosmetic.getOptionId());

        Model model = Model.createUnitScaleModel(modelAsset);
        context.setPlayerModel(new Model(
                model.getModelAssetId(),
                model.getScale(),
                model.getRandomAttachmentIds(),
                model.getAttachments(),
                model.getBoundingBox(),
                model.getModel(),
                textureConfig.getTexture(playerCosmetic.getVariantId()),
                textureConfig.getGradientSet(),
                textureConfig.getGradientSet() != null ? playerCosmetic.getVariantId() : null,
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
    public Map<String, CosmeticOptionEntry> getOptionEntries() {
        String[] variants = this.appearance.collectVariants();
        if (variants.length == 0) return Map.of();

        if (this.appearance instanceof VariantAppearance v) {
            Map<String, CosmeticOptionEntry> entries = new LinkedHashMap<>();
            for (String variantId : variants) {
                VariantAppearance.Entry entry = v.getVariants().get(variantId);
                entries.put(variantId, new CosmeticOptionEntry(
                        variantId,
                        entry.getProperties()
                ));
            }
            return entries;
        }

        return Map.of();
    }

    @Override
    public List<CosmeticVariantEntry> getVariantEntries(@Nullable String variantId) {
        TextureConfig textureConfig = this.appearance.getTextureConfig(variantId);
        String[] textures = textureConfig.collectVariants();
        if (textures.length == 0) return List.of();

        List<CosmeticVariantEntry> entries = new ArrayList<>();
        for (String textureId : textures) {
            WardrobeProperties properties;
            String[] colors;
            if (textureConfig instanceof VariantTextureConfig vt) {
                properties = vt.getVariants().get(textureId).getProperties();
                colors = vt.getVariants().get(textureId).getColors();
            } else if (textureConfig instanceof GradientTextureConfig gt) {
                properties = new WardrobeProperties(new WardrobeTranslationProperties(textureId, ""), WardrobeVisibility.ALWAYS, null, null);
                colors = CosmeticsModule.get().getRegistry().getGradientSets()
                        .get(gt.getGradientSet()).getGradients().get(textureId).getBaseColor();
            } else {
                continue;
            }
            entries.add(new CosmeticVariantEntry(textureId, properties, colors));
        }
        return entries;
    }
}
