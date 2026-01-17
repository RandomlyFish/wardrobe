package dev.hardaway.wardrobe.cosmetic;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkin;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.WardrobeUtil;
import dev.hardaway.wardrobe.asset.CosmeticAsset;
import dev.hardaway.wardrobe.asset.config.TextureConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerWardrobeSystem extends EntityTickingSystem<EntityStore> {
    private final ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType;

    public PlayerWardrobeSystem(ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType) {
        this.wardrobeComponentType = wardrobeComponentType;
    }

    @Override
    public void tick(float v, int i, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        PlayerWardrobeComponent wardrobeComponent = chunk.getComponent(i, this.wardrobeComponentType);
        if (!wardrobeComponent.isDirty())
            return;

        wardrobeComponent.setDirty(false);
        Map<CosmeticType, WardrobeCosmeticData> cosmeticData = wardrobeComponent.getCosmeticData();
        PlayerSkinComponent playerSkinComponent = chunk.getComponent(i, PlayerSkinComponent.getComponentType());
        CosmeticsModule cosmeticsModule = CosmeticsModule.get();

        if (cosmeticData.isEmpty()) {
            Model newModel = cosmeticsModule.createModel(playerSkinComponent.getPlayerSkin());
            chunk.setComponent(i, ModelComponent.getComponentType(), new ModelComponent(newModel));
            playerSkinComponent.setNetworkOutdated();
            return;
        }

        List<ModelAttachment> attachmentList = new ArrayList<>();
        PlayerSkin skin = WardrobeUtil.skinFromProtocol(playerSkinComponent.getPlayerSkin());

        for (CosmeticType slot : CosmeticType.values()) {
            if (slot == CosmeticType.BODY_CHARACTERISTICS) // Skip body characteristics, we handle that separately.
                continue;

            if (cosmeticData.containsKey(slot)) {
                WardrobeCosmeticData data = cosmeticData.get(slot);
                CosmeticAsset cosmetic = CosmeticAsset.getAssetMap().getAsset(data.id());
                if (cosmetic != null) {
                    TextureConfig textureConfig = cosmetic.getTextureConfig();
                    attachmentList.add(new ModelAttachment(
                            cosmetic.getModel(),
                            textureConfig.getTexture(data.variantId()),
                            textureConfig.getGradientSet(),
                            textureConfig.getGradientSet() != null ? data.variantId() : null,
                            1.0
                    ));
                    continue;
                }
            }

            CosmeticAssetData assetData = WardrobeUtil.createCosmeticData(slot, skin);
            if (assetData != null) {
                attachmentList.add(assetData.toModelAttachment());
            }
        }


        CosmeticAssetData bodyCharacteristicsData = WardrobeUtil.createCosmeticData(CosmeticType.BODY_CHARACTERISTICS, skin);
        Model baseModel = cosmeticsModule.createModel(playerSkinComponent.getPlayerSkin());

        String baseModelName = bodyCharacteristicsData.model();
        String baseModelTexture = bodyCharacteristicsData.texture();
        String baseModelGradientSet = bodyCharacteristicsData.gradientSet();
        String baseModelGradientId = bodyCharacteristicsData.gradientId();
        if (cosmeticData.containsKey(CosmeticType.BODY_CHARACTERISTICS)) {
            WardrobeCosmeticData bodyData = cosmeticData.get(CosmeticType.BODY_CHARACTERISTICS);
            CosmeticAsset cosmetic = CosmeticAsset.getAssetMap().getAsset(bodyData.id());
            if (cosmetic != null) {
                TextureConfig textureConfig = cosmetic.getTextureConfig();
                baseModelName = cosmetic.getModel();
                baseModelTexture = textureConfig.getTexture(bodyData.variantId());
                baseModelGradientSet = textureConfig.getGradientSet();
                baseModelGradientId = textureConfig.getGradientSet() != null ? bodyData.variantId() : null;
            }
        }


        Model model = new Model(
                "Wardrobe_Player",
                baseModel.getScale(), // TODO: allow scale change
                baseModel.getRandomAttachmentIds(),
                attachmentList.toArray(ModelAttachment[]::new), // Skin attachments
                baseModel.getBoundingBox(),
                baseModelName, // Model
                baseModelTexture, // Skin texture
                baseModelGradientSet, // Skin gradient set
                baseModelGradientId, // Skin gradient id
                baseModel.getEyeHeight(),
                baseModel.getCrouchOffset(),
                baseModel.getAnimationSetMap(),
                baseModel.getCamera(),
                baseModel.getLight(),
                baseModel.getParticles(),
                baseModel.getTrails(),
                baseModel.getPhysicsValues(),
                baseModel.getDetailBoxes(),
                baseModel.getPhobia(),
                baseModel.getPhobiaModelAssetId()
        );
        chunk.setComponent(i, ModelComponent.getComponentType(), new ModelComponent(model));
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return this.wardrobeComponentType;
    }
}
