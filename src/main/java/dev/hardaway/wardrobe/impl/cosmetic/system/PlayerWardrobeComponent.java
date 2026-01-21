package dev.hardaway.wardrobe.impl.cosmetic.system;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeGroup;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlayerWardrobeComponent implements PlayerWardrobe, Component<EntityStore> {

    public static final BuilderCodec<PlayerWardrobeComponent> CODEC = BuilderCodec.builder(PlayerWardrobeComponent.class, PlayerWardrobeComponent::new)
            .append(new KeyedCodec<>("Cosmetics", new MapCodec<>(CosmeticSaveData.CODEC, HashMap::new, false), true), (t, value) -> t.cosmetics = value, t -> t.cosmetics).add()
            .build();

    private Map<String, CosmeticSaveData> cosmetics;
    private boolean dirty;

    public PlayerWardrobeComponent() {
        this(new HashMap<>());
    }

    protected PlayerWardrobeComponent(Map<String, CosmeticSaveData> cosmetics) {
        this.cosmetics = cosmetics;
        this.dirty = true;
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

    @Nullable
    @Override
    public PlayerCosmetic getCosmetic(WardrobeGroup group) {
        return cosmetics.get(group.getId());
    }

    @Override
    public void setCosmetic(WardrobeGroup group, @Nullable PlayerCosmetic cosmetic) {
        this.cosmetics.compute(group.getId(), (_, _) -> cosmetic == null ? null : new CosmeticSaveData(cosmetic.getCosmetic().getId(), cosmetic.getTextureId()));
    }

    @Override
    public void clearCosmetics() {
        this.cosmetics.clear();
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new PlayerWardrobeComponent(this.cosmetics);
    }
}
