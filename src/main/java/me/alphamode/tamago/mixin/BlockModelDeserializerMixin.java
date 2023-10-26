package me.alphamode.tamago.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.fabricators_of_create.porting_lib.core.PortingLib;
import io.github.fabricators_of_create.porting_lib.models.geometry.GeometryLoaderManager;
import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryLoader;
import io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

@Mixin(BlockModel.Deserializer.class)
public abstract class BlockModelDeserializerMixin {

    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;", at = @At("RETURN"))
    public void modelLoading(JsonElement element, Type type, JsonDeserializationContext deserializationContext, CallbackInfoReturnable<BlockModel> cir) {
        BlockModel model = cir.getReturnValue();
        JsonObject jsonobject = element.getAsJsonObject();
        if (model.getCustomGeometry() != null)
            return;
        IUnbakedGeometry<?> geometry = deserializeGeometry(deserializationContext, jsonobject);

        List<BlockElement> elements = model.getElements();
        if (geometry != null) {
            elements.clear();
            model.setCustomGeometry(geometry);
        }
    }

    @Unique
    @Nullable
    private static IUnbakedGeometry<?> deserializeGeometry(JsonDeserializationContext deserializationContext, JsonObject object) throws JsonParseException {
        if (!object.has("porting_lib:loader") )
            return null;

        ResourceLocation name = new ResourceLocation(GsonHelper.getAsString(object, "porting_lib:loader"));
        IGeometryLoader<?> loader = GeometryLoaderManager.get(name);
        if (loader == null) {
            if (!GeometryLoaderManager.KNOWN_MISSING_LOADERS.contains(name)) {
                GeometryLoaderManager.KNOWN_MISSING_LOADERS.add(name);
                PortingLib.LOGGER.warn(String.format(Locale.ENGLISH, "Model loader '%s' not found. Registered loaders: %s", name, GeometryLoaderManager.getLoaderList()));
                PortingLib.LOGGER.warn("Falling back to vanilla logic.");
            }
            return null;
        }

        return loader.read(object, deserializationContext);
    }
}