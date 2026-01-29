package dev.hardaway.wardrobe.impl.system;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.asset.CosmeticCategoryAsset;
import dev.hardaway.wardrobe.impl.asset.CosmeticSlotAsset;
import dev.hardaway.wardrobe.impl.asset.cosmetic.CosmeticAsset;

import java.util.Objects;

public class WardrobeEvents {

    public static void registerEvents(JavaPlugin plugin) {
        plugin.getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, WardrobeEvents::onInventoryChange);
        plugin.getEventRegistry().register(LoadedAssetsEvent.class, CosmeticAsset.class, WardrobeEvents::onCosmeticsUpdated);
        plugin.getEventRegistry().register(LoadedAssetsEvent.class, CosmeticSlotAsset.class, WardrobeEvents::onSlotsUpdated);
        plugin.getEventRegistry().register(LoadedAssetsEvent.class, CosmeticCategoryAsset.class, WardrobeEvents::onCategoriesUpdated);
    }

    private static void onInventoryChange(LivingEntityInventoryChangeEvent event) {
        if (!Objects.equals(event.getEntity().getInventory().getArmor(), event.getItemContainer()))
            return;

        Ref<EntityStore> ref = event.getEntity().getReference();
        World world = event.getEntity().getWorld();
        if (ref != null && world != null) {
            Store<EntityStore> store = world.getEntityStore().getStore();

            PlayerWardrobe wardrobe = store.getComponent(ref, PlayerWardrobeComponent.getComponentType());
            if (wardrobe != null) wardrobe.rebuild();
        }
    }

    private static void onCosmeticsUpdated(LoadedAssetsEvent<String, CosmeticAsset, DefaultAssetMap<String, CosmeticAsset>> event) {
        rebuildAllWardrobes();
    }

    private static void onSlotsUpdated(LoadedAssetsEvent<String, CosmeticSlotAsset, DefaultAssetMap<String, CosmeticSlotAsset>> event) {
        rebuildAllWardrobes();
    }

    private static void onCategoriesUpdated(LoadedAssetsEvent<String, CosmeticCategoryAsset, DefaultAssetMap<String, CosmeticCategoryAsset>> event) {
        rebuildAllWardrobes();
    }

    public static void rebuildAllWardrobes() {
        Universe universe = Universe.get();
        universe.getPlayers().forEach(playerRef -> {
            World world = universe.getWorld(playerRef.getWorldUuid());
            world.execute(() -> {
                Store<EntityStore> store = world.getEntityStore().getStore();

                PlayerWardrobe wardrobe = store.getComponent(playerRef.getReference(), PlayerWardrobeComponent.getComponentType());
                if (wardrobe != null) wardrobe.rebuild();
            });
        });
    }

}
