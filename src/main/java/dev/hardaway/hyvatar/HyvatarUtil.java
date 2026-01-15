package dev.hardaway.hyvatar;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPartType;

import java.util.function.Supplier;

// TODO: split this up
public class HyvatarUtil {

    // Extra Codecs
    public static final EnumCodec<PlayerSkinPart.HeadAccessoryType> HEAD_ACCESSORY_TYPE_CODEC = new EnumCodec<>(PlayerSkinPart.HeadAccessoryType.class, EnumCodec.EnumStyle.LEGACY);
    public static final EnumCodec<PlayerSkinPartType> SKIN_PART_TYPE_CODEC = new EnumCodec<>(PlayerSkinPartType.class, EnumCodec.EnumStyle.LEGACY);

    public static <T extends JsonAssetWithMap<String, DefaultAssetMap<String, T>>> Supplier<AssetStore<String, T, DefaultAssetMap<String, T>>> createAssetStore(Class<T> clazz) {
        return HyvatarUtil.memoize(() -> AssetRegistry.getAssetStore(clazz));
    }

    public static <Z> Supplier<Z> memoize(Supplier<Z> supplier) {
        return new Supplier<>() {
            Z value;

            @Override
            public Z get() {
                if (value == null)
                    value = supplier.get();
                return value;
            }
        };
    }
}
