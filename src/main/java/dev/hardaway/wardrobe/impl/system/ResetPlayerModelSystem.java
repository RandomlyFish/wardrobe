package dev.hardaway.wardrobe.impl.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResetPlayerModelSystem extends RefChangeSystem<EntityStore, PlayerWardrobeComponent> {

    private static final Query<EntityStore> QUERY = Query.and(Player.getComponentType(), PlayerSkinComponent.getComponentType(), ModelComponent.getComponentType());

    private final ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType;

    public ResetPlayerModelSystem(ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType) {
        this.wardrobeComponentType = wardrobeComponentType;
    }

    @Nonnull
    @Override
    public ComponentType<EntityStore, PlayerWardrobeComponent> componentType() {
        return wardrobeComponentType;
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull PlayerWardrobeComponent playerWardrobeComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    }

    @Override
    public void onComponentSet(@Nonnull Ref<EntityStore> ref, @Nullable PlayerWardrobeComponent playerWardrobeComponent, @Nonnull PlayerWardrobeComponent t1, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    }

    @Override
    public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull PlayerWardrobeComponent playerWardrobeComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        PlayerSkinComponent playerSkinComponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
        if (playerSkinComponent != null) {
            playerSkinComponent.setNetworkOutdated();
            Model newModel = CosmeticsModule.get().createModel(playerSkinComponent.getPlayerSkin());
            if (newModel != null) {
                commandBuffer.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
            }
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }
}