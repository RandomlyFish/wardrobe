package dev.hardaway.wardrobe.api.property.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;

import javax.annotation.Nonnull;

@Deprecated(forRemoval = true)
public class AppearanceModelValidator implements Validator<String> {
    public static final AppearanceModelValidator INSTANCE = new AppearanceModelValidator();

    private AppearanceModelValidator() {
    }

    public void accept(@Nonnull String string, @Nonnull ValidationResults results) {
        ValidationResults attachmentResults = new ValidationResults(results.getExtraInfo());
        ValidationResults modelResults = new ValidationResults(results.getExtraInfo());

        CommonAssetValidator.MODEL_CHARACTER_ATTACHMENT.accept(string, attachmentResults);
        ModelAsset.VALIDATOR_CACHE.getValidator().accept(string, modelResults);

        if (attachmentResults.hasFailed() && modelResults.hasFailed()) {
            results.fail("Model must be an attachment model or a model asset");
        }
    }

    @Override
    public void updateSchema(SchemaContext schemaContext, Schema schema) {
        CommonAssetValidator.MODEL_CHARACTER_ATTACHMENT.updateSchema(schemaContext, schema); // TODO: properly split this class so two schemas can exist
    }
}

