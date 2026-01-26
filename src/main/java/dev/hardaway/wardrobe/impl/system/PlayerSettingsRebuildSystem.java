package dev.hardaway.wardrobe.impl.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerSettingsRebuildSystem extends RefChangeSystem<EntityStore, PlayerSettings> {

    private static final Query<EntityStore> QUERY = Query.and(Player.getComponentType(), PlayerWardrobe.getComponentType());

    @Nonnull
    @Override
    public ComponentType<EntityStore, PlayerSettings> componentType() {
        return PlayerSettings.getComponentType();
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull PlayerSettings settings, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    }

    @Override
    public void onComponentSet(@Nonnull Ref<EntityStore> ref, @Nullable PlayerSettings settings, @Nonnull PlayerSettings settings2, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        store.getComponent(ref, PlayerWardrobe.getComponentType()).rebuild();
    }

    @Override
    public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull PlayerSettings settings, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }
}