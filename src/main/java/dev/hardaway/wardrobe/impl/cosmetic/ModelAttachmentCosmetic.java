package dev.hardaway.wardrobe.impl.cosmetic;

import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;
import com.hypixel.hytale.server.core.inventory.ItemStack;
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
import dev.hardaway.wardrobe.impl.cosmetic.builtin.HytaleCosmetic;
import dev.hardaway.wardrobe.impl.cosmetic.texture.GradientTextureConfig;
import dev.hardaway.wardrobe.impl.cosmetic.texture.VariantTextureConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModelAttachmentCosmetic extends CosmeticAsset implements AppearanceCosmetic {

    public static final BuilderCodec<ModelAttachmentCosmetic> CODEC = AssetBuilderCodec.builder(ModelAttachmentCosmetic.class, ModelAttachmentCosmetic::new, CosmeticAsset.ABSTRACT_CODEC)
            .appendInherited(new KeyedCodec<>("OverlapCosmeticSlots", Codec.STRING_ARRAY),
                    (c, value) -> c.overlapCosmeticSlotIds = value,
                    c -> c.overlapCosmeticSlotIds,
                    (c, p) -> c.overlapCosmeticSlotIds = p.overlapCosmeticSlotIds
            ).add()

            .appendInherited(new KeyedCodec<>("Appearance", Appearance.CODEC, true),
                    (c, value) -> c.appearance = value,
                    c -> c.appearance,
                    (c, p) -> c.appearance = p.appearance
            )
            .addValidator(Validators.nonNull())
            .add()

            .appendInherited(new KeyedCodec<>("OverlapAppearance", Appearance.CODEC),
                    (c, value) -> c.overlapAppearance = value,
                    c -> c.appearance,
                    (c, p) -> c.overlapAppearance = p.overlapAppearance
            ).add()

            .appendInherited(new KeyedCodec<>("ArmorAppearance", Appearance.CODEC, false),
                    (t, value) -> t.armorAppearance = value,
                    t -> t.armorAppearance,
                    (c, p) -> c.armorAppearance = p.armorAppearance
            ).add()

            .build();

    private String[] overlapCosmeticSlotIds = new String[0];
    private Appearance appearance;
    private @Nullable Appearance overlapAppearance;
    private @Nullable Appearance armorAppearance;

    protected ModelAttachmentCosmetic() {
    }

    public ModelAttachmentCosmetic(String id, String[] hiddenCosmeticSlots, String cosmeticSlotId, WardrobeProperties properties, Appearance appearance, @Nullable Appearance overlapAppearance, @Nullable Appearance armorAppearance) {
        super(id, cosmeticSlotId, hiddenCosmeticSlots, properties);
        this.appearance = appearance;
        this.overlapAppearance = overlapAppearance;
        this.armorAppearance = armorAppearance;
    }

    public String[] getOverlapCosmeticSlotIds() {
        return overlapCosmeticSlotIds;
    }

    @Override
    public Appearance getAppearance() {
        return appearance;
    }

    @Nullable
    public Appearance getOverlapAppearance() {
        return overlapAppearance;
    }

    @Nullable
    public Appearance getArmorAppearance() {
        return armorAppearance;
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

    @Override
    public void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic) {
        super.applyCosmetic(context, slot, playerCosmetic);
        Appearance appearance = this.getAppearance();

        if (slot.getArmorSlot() != null && this.getArmorAppearance() != null) {
            boolean shouldHide = switch (slot.getArmorSlot()) {
                case Head -> context.getPlayerSettings().hideHelmet();
                case Chest -> context.getPlayerSettings().hideCuirass();
                case Hands -> context.getPlayerSettings().hideGauntlets();
                case Legs -> context.getPlayerSettings().hidePants();
            };

            if (!shouldHide) {
                ItemStack armor = context.getPlayer().getInventory().getArmor().getItemStack((short) slot.getArmorSlot().getValue());
                if (armor != null) appearance = this.getArmorAppearance();
            }
        } else if (this.getOverlapAppearance() != null) {
            boolean overlapFound = false;
            for (dev.hardaway.wardrobe.api.cosmetic.Cosmetic cosmetic : context.getCosmeticMap().values()) {
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

        TextureConfig textureConfig = appearance.getTextureConfig(playerCosmetic.getOptionId());
        context.addAttachment(slot.getId(), new ModelAttachment(
                appearance.getModel(playerCosmetic.getOptionId()),
                textureConfig.getTexture(playerCosmetic.getVariantId()),
                textureConfig.getGradientSet(),
                textureConfig.getGradientSet() != null ? playerCosmetic.getVariantId() : null,
                1.0
        ));
    }

}
