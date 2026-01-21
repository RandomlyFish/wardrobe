package dev.hardaway.wardrobe.impl.asset.cosmetic;//package dev.hardaway.wardrobe.impl.asset;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeGroup;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.impl.asset.CosmeticGroupAsset;
import dev.hardaway.wardrobe.impl.asset.cosmetic.texture.TextureConfig;

import javax.annotation.Nullable;
import java.util.Objects;

/// / Fallback
// if wearing armor
// use HelmetFallback
// if head acccssory covering
// do not apply
// if head accessory half covering
// use accessory fallback
public class HaircutCosmetic extends ModelAttachmentCosmetic {

    public static final BuilderCodec<HaircutCosmetic> CODEC = BuilderCodec.builder(HaircutCosmetic.class, HaircutCosmetic::new, ModelAttachmentCosmetic.CODEC)
            .append(new KeyedCodec<>("HelmFallbackModel", Codec.STRING),
                    (t, value) -> t.helmFallbackModel = value,
                    t -> t.helmFallbackModel
            ).add()
            .append(new KeyedCodec<>("HelmFallbackTextureConfig", TextureConfig.CODEC),
                    (t, value) -> t.helmFallbackTextureConfig = value,
                    t -> t.helmFallbackTextureConfig
            ).add()
            .append(new KeyedCodec<>("HatFallbackModel", Codec.STRING),
                    (t, value) -> t.hatFallbackModel = value,
                    t -> t.hatFallbackModel
            ).add()
            .append(new KeyedCodec<>("HatFallbackTextureConfig", TextureConfig.CODEC),
                    (t, value) -> t.hatFallbackTextureConfig = value,
                    t -> t.hatFallbackTextureConfig
            ).add()
            .build();

    private @Nullable String helmFallbackModel;
    private @Nullable TextureConfig helmFallbackTextureConfig;
    private @Nullable String hatFallbackModel;
    private @Nullable TextureConfig hatFallbackTextureConfig;

    protected HaircutCosmetic() {
    }

    public HaircutCosmetic(String id, String nameKey, String group, String icon, @Nullable String permissionNode, String model, TextureConfig textureConfig, @Nullable String helmFallbackModel, @Nullable TextureConfig helmFallbackTextureConfig, @Nullable String hatFallbackModel, @Nullable TextureConfig hatFallbackTextureConfig) {
        super(id, nameKey, group, icon, permissionNode, model, textureConfig);
        this.helmFallbackModel = helmFallbackModel;
        this.helmFallbackTextureConfig = helmFallbackTextureConfig;
        this.hatFallbackModel = hatFallbackModel;
        this.hatFallbackTextureConfig = hatFallbackTextureConfig;
    }

    @Nullable
    public String getHelmFallbackModel() {
        return helmFallbackModel;
    }

    @Nullable
    public TextureConfig getHelmFallbackTextureConfig() {
        return helmFallbackTextureConfig;
    }

    @Nullable
    public String getHatFallbackModel() {
        return hatFallbackModel;
    }

    @Nullable
    public TextureConfig getHatFallbackTextureConfig() {
        return hatFallbackTextureConfig;
    }

    @Override
    public void applyCosmetic(WardrobeContext context, WardrobeGroup group, PlayerCosmetic playerCosmetic) {
        TextureConfig textureConfig = this.getTextureConfig();

        String model = this.getModel();
        String texture = textureConfig.getTexture(playerCosmetic.getTextureId());
        String gradientSet = textureConfig.getGradientSet();

        // TODO: make this data driven or something
        CosmeticGroupAsset headAccessoryGroup = CosmeticGroupAsset.getAssetMap().getAsset("HeadAccessory");
        if (headAccessoryGroup != null) {
            PlayerCosmetic headCosmetic = context.getCosmetic(headAccessoryGroup);
            if (headCosmetic != null) {
                WardrobeCosmetic headAsset = headCosmetic.getCosmetic();
                if (headAsset instanceof HeadAccessoryCosmetic headAccessory) {
                    switch (headAccessory.getHatStyle()) {
                        case DEFAULT -> {
                        } // Continue as normal
                        case HALF -> {
                            if (this.hatFallbackModel != null)
                                model = this.hatFallbackModel;
                            if (this.hatFallbackTextureConfig != null) {
                                texture = this.hatFallbackTextureConfig.getTexture(playerCosmetic.getTextureId());
                                gradientSet = this.hatFallbackTextureConfig.getGradientSet();
                            }
                        }
                        case COVERING -> {
                            return; // Don't apply haircut if the hat is covering
                        }
                    }
                }
            }
        }

        Player player = context.getPlayer();
        if (group.getHytaleCosmeticType() != null) {
            ItemContainer armorContainer = player.getInventory().getArmor();
            for (short i = 0; i < armorContainer.getCapacity(); i++) {
                ItemStack stack = armorContainer.getItemStack(i);
                if (stack == null || stack.isEmpty()) continue;

                ItemArmor armor = stack.getItem().getArmor();
                if (armor == null)
                    return;

                if (Objects.equals(armor.getArmorSlot(), ItemArmorSlot.Head)) {
                    if (this.helmFallbackModel != null)
                        model = this.helmFallbackModel;
                    if (this.helmFallbackTextureConfig != null) {
                        texture = this.helmFallbackTextureConfig.getTexture(playerCosmetic.getTextureId());
                        gradientSet = this.helmFallbackTextureConfig.getGradientSet();
                    }
                    break;
                }
            }
        }

        context.addAttachment(group, new ModelAttachment(
                model,
                texture,
                gradientSet,
                gradientSet != null ? playerCosmetic.getTextureId() : null,
                1.0
        ));
    }
}
