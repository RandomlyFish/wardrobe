package dev.hardaway.wardrobe.impl.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;

import javax.annotation.Nonnull;

public class UpdateWardrobeSystem extends RefSystem<EntityStore> {

    @Override
    public Query<EntityStore> getQuery() {
        return PlayerWardrobe.getComponentType();
    }

    @Override
    public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        PlayerWardrobe wardrobe = store.getComponent(ref, PlayerWardrobe.getComponentType());
        wardrobe.rebuild();
    }

    @Override
    public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    }
}