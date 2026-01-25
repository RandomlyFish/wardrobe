package dev.hardaway.wardrobe.api.menu;

import com.hypixel.hytale.common.util.StringCompareUtil;
import dev.hardaway.wardrobe.api.cosmetic.*;
import dev.hardaway.wardrobe.api.cosmetic.apperance.CosmeticAppearance;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.system.CosmeticSaveData;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import javax.annotation.Nullable;
import java.util.*;

public class WardrobeMenu {

    private final UUID playerId;

    private final List<? extends WardrobeCategory> categories;
    private final Map<String, List<WardrobeCosmeticSlot>> slotMap = new HashMap<>();
    private final Map<String, List<WardrobeCosmetic>> cosmeticMap = new HashMap<>();

    private String selectedCategory;
    private String selectedSlot;
    private String searchQuery = "";

    public WardrobeMenu(UUID playerId) {
        this.playerId = playerId;

        this.categories = WardrobeCategory.getAssetMap().getAssetMap().values().stream()
                .filter(c -> c.hasPermission(playerId))
                .sorted(Comparator.comparing(WardrobeCategory::getTabOrder))
                .toList();

        buildSlots();
        buildCosmetics();

        if (!categories.isEmpty()) {
            selectCategory(categories.getFirst().getId());
        }
    }

    private void buildSlots() {
        for (WardrobeCategory category : categories) {
            slotMap.put(category.getId(), new ArrayList<>());
        }

        WardrobeCosmeticSlot.getAssetMap().getAssetMap().values().stream()
                .filter(g -> g.hasPermission(playerId))
                .sorted(Comparator.comparing(WardrobeCosmeticSlot::getTabOrder))
                .forEach(slot -> {
                    slotMap.computeIfAbsent(slot.getCategory().getId(), k -> new ArrayList<>()).add(slot);
                    cosmeticMap.put(slot.getId(), new ArrayList<>());
                });
    }

    private void buildCosmetics() {
        WardrobeCosmetic.getAssetMap().getAssetMap().values().stream()
                .filter(c -> c.getWardrobeVisibility() != WardrobeVisibility.NEVER)
                .filter(c -> c.getWardrobeVisibility() != WardrobeVisibility.PERMISSION || c.hasPermission(playerId))
                .sorted(Comparator.comparing(WardrobeCosmetic::getId))
                .forEach(c -> cosmeticMap.computeIfAbsent(c.getCosmeticSlotId(), k -> new ArrayList<>()).add(c));
    }

    public List<? extends WardrobeCategory> getCategories() {
        return categories;
    }

    public List<WardrobeCosmeticSlot> getSlots() {
        return slotMap.getOrDefault(selectedCategory, List.of());
    }

    public List<WardrobeCosmetic> getCosmetics() {
        List<WardrobeCosmetic> cosmetics = cosmeticMap.getOrDefault(selectedSlot, List.of());

        if (searchQuery.isEmpty()) return cosmetics;

        Object2IntMap<WardrobeCosmetic> scores = new Object2IntOpenHashMap<>();
        for (WardrobeCosmetic c : cosmetics) {
            int score = StringCompareUtil.getFuzzyDistance(c.getId(), searchQuery, Locale.ENGLISH);
            if (score > 0) scores.put(c, score);
        }

        return scores.keySet().stream()
                .sorted(Comparator.comparingInt(scores::getInt).reversed())
                .toList();
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery == null ? "" : searchQuery;
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public String getSelectedSlot() {
        return selectedSlot;
    }

    public void selectCategory(String categoryId) {
        selectedCategory = categoryId;
        List<WardrobeCosmeticSlot> slots = slotMap.get(categoryId);
        selectedSlot = slots == null || slots.isEmpty() ? null : slots.getFirst().getId();
    }

    public void selectSlot(String slotId) {
        selectedSlot = slotId;
    }

    public boolean selectCosmetic(PlayerWardrobe wardrobe, @Nullable WardrobeCosmetic cosmetic, @Nullable String variant, @Nullable String texture) {
        PlayerCosmetic worn = wardrobe.getCosmetic(selectedSlot);

        if (cosmetic != null && worn != null && cosmetic.getId().equals(worn.getCosmeticId())) {
            wardrobe.removeCosmetic(selectedSlot);
            wardrobe.rebuild();
            return true;
        }

        if (cosmetic == null) {
            if (worn == null) return false;
            cosmetic = Objects.requireNonNull(WardrobeCosmetic.getAssetMap().getAsset(worn.getCosmeticId()));
        }

        if (cosmetic instanceof AppearanceCosmetic a) {
            CosmeticAppearance appearance = a.getAppearance();
            List<String> variants = List.of(appearance.collectVariants());

            if (variant == null || !variants.contains(variant)) {
                variant = worn != null && (worn.getVariantId() != null && variants.contains(worn.getVariantId()))
                        ? worn.getVariantId()
                        : variants.isEmpty() ? null : variants.getFirst();
            }

            List<String> textures = List.of(appearance.getTextureConfig(variant).collectVariants());

            if (texture == null || !textures.contains(texture)) {
                texture = worn != null && (worn.getTextureId() != null && textures.contains(worn.getTextureId()))
                        ? worn.getTextureId()
                        : textures.isEmpty() ? null : textures.getFirst();
            }
        }

        wardrobe.setCosmetic(selectedSlot, new CosmeticSaveData(cosmetic.getId(), variant, texture));
        wardrobe.rebuild();
        return true;
    }
}
