package dev.hardaway.wardrobe.impl.cosmetic;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
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
import java.util.*;

public class PlayerModelCosmetic extends CosmeticAsset implements AppearanceCosmetic {

    public static final BuilderCodec<PlayerModelCosmetic> CODEC = BuilderCodec.builder(PlayerModelCosmetic.class, PlayerModelCosmetic::new, CosmeticAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Appearance", Appearance.CODEC, true),
                    (t, value) -> t.appearance = value,
                    t -> t.appearance
            )
            .addValidator(Validators.nonNull())
            .metadata(new UIPropertyTitle("Appearance")).documentation("The appearance of this Cosmetic. The Model field must be a ModelAsset id.")
            .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
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
        Appearance appearance = this.getAppearance();

        String option = playerCosmetic.getOptionId();
        if (appearance.getModel(option) == null) {
            option = appearance.collectVariants()[0];
        }



        TextureConfig textureConfig = this.getAppearance().getTextureConfig(option);

        String variant = playerCosmetic.getVariantId();
        if (textureConfig.getTexture(variant) == null) {
            variant = textureConfig.collectVariants()[0];
        }

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(this.getAppearance().getModel(option));
        Model model = Model.createScaledModel(modelAsset, this.getAppearance().getScale(option));
        context.setPlayerModel(new Model(
                model.getModelAssetId(),
                model.getScale(),
                model.getRandomAttachmentIds(),
                model.getAttachments(),
                model.getBoundingBox(),
                model.getModel(),
                textureConfig.getTexture(variant),
                textureConfig.getGradientSet(),
                textureConfig.getGradientSet() != null ? variant : null,
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
                        entry.getProperties(),
                        entry.getIcon()
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
            String icon = null;
            if (textureConfig instanceof VariantTextureConfig vt) {
                VariantTextureConfig.Entry entry = vt.getVariants().get(textureId);
                properties = entry.getProperties();
                colors = entry.getColors();
                icon = entry.getIcon();
            } else if (textureConfig instanceof GradientTextureConfig gt) {
                properties = new WardrobeProperties(new WardrobeTranslationProperties(textureId, ""), WardrobeVisibility.ALWAYS, null);
                colors = CosmeticsModule.get().getRegistry().getGradientSets()
                        .get(gt.getGradientSet()).getGradients().get(textureId).getBaseColor();
            } else {
                continue;
            }
            entries.add(new CosmeticVariantEntry(textureId, properties, colors, icon));
        }
        return entries;
    }
}
