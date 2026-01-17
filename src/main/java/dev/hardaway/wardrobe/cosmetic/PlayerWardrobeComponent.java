package dev.hardaway.wardrobe.cosmetic;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * avatar components
 * - each cosmetic type has a component
 * - if cosmetic component exists: remove hypixel cosmetic
 * - if it doesnt: put hypixel cosmetic back
 * - tags to allow cosmetics to be incompatible with each other
 * <p>
 * component types
 * Eyes,
 * Ears,
 * Mouth,
 * Eyebrows,
 * Haircut,
 * FacialHair,
 * Pants,
 * Overpants,
 * Undertops,
 * Overtops,
 * Shoes,
 * HeadAccessory,
 * FaceAccessory,
 * EarAccessory,
 * Avatar, - Model and Texture
 * Gloves;
 */
public class PlayerWardrobeComponent implements Component<EntityStore> {

    public static final BuilderCodec<PlayerWardrobeComponent> CODEC = BuilderCodec.builder(PlayerWardrobeComponent.class, PlayerWardrobeComponent::new)
            .append(new KeyedCodec<>("Cosmetics", new EnumMapCodec<>(CosmeticType.class, WardrobeCosmeticData.CODEC, false), true), (t, value) -> t.cosmetics = value, t -> t.cosmetics).add()
            .build();

    private Map<CosmeticType, WardrobeCosmeticData> cosmetics;
    protected boolean dirty = true;

    public PlayerWardrobeComponent() {
        this.cosmetics = new EnumMap<>(CosmeticType.class);
    }

    protected PlayerWardrobeComponent(Map<CosmeticType, WardrobeCosmeticData> cosmetics) {
        this.cosmetics = cosmetics;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public Map<CosmeticType, WardrobeCosmeticData> getCosmetics() {
        return Collections.unmodifiableMap(this.cosmetics);
    }

    public void setCosmetic(CosmeticType slot, WardrobeCosmeticData cosmetic) {
        if (cosmetic == null) {
            this.cosmetics.remove(slot);
        } else {
            this.cosmetics.put(slot, cosmetic);
        }
        this.dirty = true;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new PlayerWardrobeComponent(this.cosmetics);
    }
}
