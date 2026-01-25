package dev.hardaway.wardrobe.impl.system;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.WardrobePlugin;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerWardrobeComponent implements PlayerWardrobe, Component<EntityStore> {

    public static final BuilderCodec<PlayerWardrobeComponent> CODEC = BuilderCodec.builder(PlayerWardrobeComponent.class, PlayerWardrobeComponent::new)
            .append(new KeyedCodec<>("Cosmetics", new MapCodec<>(CosmeticSaveData.CODEC, HashMap::new, false), true),
                    PlayerWardrobeComponent::setCosmetics,
                    t -> t.cosmetics
            ).add()
            .build();

    private Map<String, CosmeticSaveData> cosmetics;
    private Set<String> cosmeticIdSet;
    private boolean dirty;

    public PlayerWardrobeComponent() {
        this(new HashMap<>());
    }

    protected PlayerWardrobeComponent(Map<String, CosmeticSaveData> cosmetics) {
        this.setCosmetics(cosmetics);
        this.rebuild();
    }

    @Override
    public void rebuild() {
        this.dirty = true;
    }

    protected boolean consumeDirty() {
        boolean dirty = this.dirty;
        this.dirty = false;
        return dirty;
    }

    @Override
    public Collection<PlayerCosmetic> getCosmetics() {
        return Collections.unmodifiableCollection(this.cosmetics.values());
    }

    private void setCosmetics(Map<String, CosmeticSaveData> cosmeticMap) {
        this.cosmetics = cosmeticMap;
        this.cosmeticIdSet = this.cosmetics.values().stream().map(CosmeticSaveData::getCosmeticId).collect(Collectors.toSet());
    }

    @Override
    public boolean hasCosmetic(String id) {
        return cosmeticIdSet.contains(id);
    }

    @Override
    public PlayerCosmetic getCosmetic(String slot) {
        return cosmetics.get(slot);
    }

    @Override
    public void setCosmetic(String slot, PlayerCosmetic cosmetic) {
        if (cosmetic == null) {
            PlayerCosmetic lastCosmetic = this.cosmetics.remove(slot);
            if (lastCosmetic != null) {
                this.cosmeticIdSet.remove(lastCosmetic.getCosmeticId());
            }
            return;
        }

        // TODO: fix api so i dont have to do this
        CosmeticSaveData cosmeticSaveData = new CosmeticSaveData(cosmetic.getCosmeticId(), cosmetic.getVariantId(), cosmetic.getTextureId());
        PlayerCosmetic lastCosmetic = this.cosmetics.put(slot, cosmeticSaveData);
        if (lastCosmetic != null) {
            this.cosmeticIdSet.remove(lastCosmetic.getCosmeticId());
        }
        this.cosmeticIdSet.add(cosmetic.getCosmeticId());
    }

    @Override
    public void clearCosmetics() {
        this.cosmetics.clear();
    }

    @Nullable
    @Override
    public PlayerWardrobeComponent clone() {
        return new PlayerWardrobeComponent(new HashMap<>(this.cosmetics));
    }

    public static ComponentType<EntityStore, PlayerWardrobeComponent> getComponentType() {
        return WardrobePlugin.get().getPlayerWardrobeComponentType();
    }
}
