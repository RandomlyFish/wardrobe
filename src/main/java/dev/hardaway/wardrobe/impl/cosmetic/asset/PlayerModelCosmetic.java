package dev.hardaway.wardrobe.impl.cosmetic.asset;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeGroup;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.impl.cosmetic.asset.texture.TextureConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerModelCosmetic extends CosmeticAsset {

    public static final BuilderCodec<PlayerModelCosmetic> CODEC = BuilderCodec.builder(PlayerModelCosmetic.class, PlayerModelCosmetic::new, CosmeticAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("ModelAsset", Codec.STRING, true),
                    (t, value) -> t.modelAsset = value,
                    t -> t.modelAsset
            ).add()
            .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC, true),
                    (t, value) -> t.textureConfig = value,
                    t -> t.textureConfig
            ).add()
            .build();

    private String modelAsset;
    private TextureConfig textureConfig;

    private PlayerModelCosmetic() {
    }

    // Required Groups (underwear, face, mouth, ears, eyes)
    public PlayerModelCosmetic(String id, String nameKey, String group, String icon, @Nullable String permissionNode, String modelAsset, TextureConfig textureConfig) {
        super(id, nameKey, group, icon, permissionNode);
        this.modelAsset = modelAsset;
        this.textureConfig = textureConfig;
    }

    @Nonnull
    public String getModelAsset() {
        return modelAsset;
    }

    @Nonnull
    public TextureConfig getTextureConfig() {
        return textureConfig;
    }

    @Override
    public void applyCosmetic(WardrobeContext context, WardrobeGroup group, PlayerCosmetic playerCosmetic) {
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(this.getModelAsset());
        Model model = Model.createUnitScaleModel(modelAsset);
        context.setPlayerModel(new Model(
                model.getModelAssetId(),
                model.getScale(),
                model.getRandomAttachmentIds(),
                model.getAttachments(),
                model.getBoundingBox(),
                model.getModel(),
                this.getTextureConfig().getTexture(playerCosmetic.getTextureId()),
                this.getTextureConfig().getGradientSet(),
                this.getTextureConfig().getGradientSet() != null ? playerCosmetic.getTextureId() : null,
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
        // TODO: warn if the model asset has attachments? they will be removed!
    }

}
