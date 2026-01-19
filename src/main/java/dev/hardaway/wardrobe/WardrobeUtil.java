package dev.hardaway.wardrobe;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;

import java.util.function.Supplier;

// TODO: split this up
public class WardrobeUtil {

    public static <T extends JsonAssetWithMap<String, DefaultAssetMap<String, T>>> Supplier<AssetStore<String, T, DefaultAssetMap<String, T>>> createAssetStore(Class<T> clazz) {
        return WardrobeUtil.memoize(() -> AssetRegistry.getAssetStore(clazz));
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
