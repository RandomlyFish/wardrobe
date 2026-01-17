package dev.hardaway.wardrobe.cosmetic;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Set;

public class SetupPlayerWardrobeSystem extends HolderSystem<EntityStore> {
    private final ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
    private final ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType;

    public SetupPlayerWardrobeSystem(ComponentType<EntityStore, PlayerWardrobeComponent> wardrobeComponentType) {
        this.wardrobeComponentType = wardrobeComponentType;
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return super.getDependencies();
    }

    @Override
    public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
        Player player = holder.getComponent(Player.getComponentType());
        if (player == null) {
            throw new UnsupportedOperationException("Cannot have null player component during Wardrobe system creation");
        } else {
            holder.ensureComponent(this.wardrobeComponentType);
        }
    }

    @Override
    public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
        PlayerWardrobeComponent component = holder.getComponent(this.wardrobeComponentType);
        if (component != null) {
            Player player = holder.getComponent(this.playerComponentType);

            World world = store.getExternalData().getWorld();
            if (world.getWorldConfig().isSavingPlayers() && player != null) {
                player.saveConfig(world, holder);
            }
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return this.playerComponentType;
    }
}