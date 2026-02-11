package dev.hardaway.wardrobe.api.property.validator;

import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import dev.hardaway.wardrobe.api.property.WardrobeProperties;

public class WardrobeValidators {
    public static final CommonAssetValidator ICON = new CommonAssetValidator("png", "Icons/Cosmetics", "Icons/Wardrobe", "Icons/Models", "Icons/ItemsGenerated", "Icons/ModelsGenerated");
    public static final ArrayValidator<String> COLOR = new ArrayValidator<>(HexcodeValidator.INSTANCE);

    @Deprecated(forRemoval = true)
    public static final Validator<String> APPEARANCE_MODEL = AppearanceModelValidator.INSTANCE;

    @Deprecated(forRemoval = true)
    public static final Validator<WardrobeProperties> PROPERTIES_ICON = PropertiesIconValidator.INSTANCE;
}
