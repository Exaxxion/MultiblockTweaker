package eutros.multiblocktweaker.crafttweaker.construction;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import eutros.multiblocktweaker.MultiblockTweaker;
import eutros.multiblocktweaker.crafttweaker.CustomMultiblock;
import eutros.multiblocktweaker.crafttweaker.gtwrap.interfaces.IBlockPattern;
import eutros.multiblocktweaker.crafttweaker.gtwrap.interfaces.IICubeRenderer;
import eutros.multiblocktweaker.crafttweaker.gtwrap.interfaces.IMultiblockShapeInfo;
import eutros.multiblocktweaker.gregtech.cuberenderer.SidedCubeRenderer;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Builder, or Multiblock Builder, is used to define a custom {@link CustomMultiblock}.
 * <p>
 * To get started, call {@link #start(String, int)}.
 *
 * @zenClass mods.gregtech.multiblock.Builder
 */
@ZenClass("mods.gregtech.multiblock.Builder")
@ZenRegister
public class MultiblockBuilder {

    public ResourceLocation loc;
    public int metaId;
    public float zoom = 1.0f;
    private final Hash.Strategy<ItemStack> strategy = ItemStackHashStrategy.comparingAllButCount();
    public Map<ItemStack, List<ITextComponent>> tooltipMap = new Object2ObjectOpenCustomHashMap<>(strategy);
    public boolean clearTooltips = false;
    public gregtech.api.render.ICubeRenderer texture = null;
    public RecipeMap<?> recipeMap = null;
    public BlockPattern pattern = null;

    @NotNull
    public List<MultiblockShapeInfo> designs = new ArrayList<>();

    private MultiblockBuilder(ResourceLocation loc, int metaId) {
        this.loc = loc;
        this.metaId = metaId;
    }

    /**
     * Create a multiblock builder from a resource location.
     *
     * @param location The resource location of the multiblock.
     *                 Used for getting the recipe map again.
     *                 If no namespace is defined, it defaults to this mod's mod id.
     * @param metaId   The metadata the resulting multiblock will be registered as.
     * @return A multiblock builder instance, that should be used to set the properties of the multiblock.
     */
    @ZenMethod
    public static MultiblockBuilder start(@NotNull String location, int metaId) {
        ResourceLocation loc = new ResourceLocation(location);
        if (loc.getNamespace().equals("minecraft")) {
            loc = new ResourceLocation(MultiblockTweaker.MOD_ID, loc.getPath());
        }
        return new MultiblockBuilder(loc, metaId);
    }

    /**
     * Compulsory, set the pattern for the multiblock.
     *
     * @param pattern An {@link IBlockPattern} defining the multiblock's construction.
     * @return This builder, for convenience.
     */
    @ZenMethod
    public MultiblockBuilder withPattern(@NotNull IBlockPattern pattern) {
        this.pattern = pattern.getInternal();
        return this;
    }

    /**
     * Compulsory, set the recipe map for the multiblock.
     *
     * @param map The map to use.
     * @return This builder, for convenience.
     */
    @ZenMethod
    public MultiblockBuilder withRecipeMap(@NotNull RecipeMap<?> map) {
        this.recipeMap = map;
        return this;
    }

    /**
     * Set the texture for the multiblock.
     * This will be used for the controller, and all the inputs/outputs when the structure forms.
     * <p>
     * If this is not defined, it will be defined as the most common block out of all designs.
     * If there are no designs defined either, {@link #build()} will fail.
     *
     * @param texture The texture to use.
     * @return This builder, for convenience.
     */
    @ZenMethod
    public MultiblockBuilder withTexture(@NotNull IICubeRenderer texture) {
        this.texture = texture.getInternal();
        return this;
    }

    /**
     * Set the default zoom level for the multiblock.
     * This is optional, and if not defined will default to 1.0f
     *
     * @param zoom The default zoom level of the multiblock on the JEI preview page
     * @return This builder, for convenience.
     */
    @ZenMethod
    public MultiblockBuilder withZoom(float zoom) {
        this.zoom = zoom;
        return this;
    }

    /**
     * Add a tooltip to a specific {@link net.minecraft.item.ItemStack} in a Multiblock Structure.
     * Multiple tooltips can be added to the same {@link net.minecraft.item.ItemStack}.
     * This is optional, and if not defined will not add any tooltips.
     *
     * @param itemStack - The ItemStack form of the block to add the tooltip to.
     * @param tooltip - The localization key for the tooltip to be added.
     * @return This builder, for convenience.
     */
    @ZenMethod
    public MultiblockBuilder withPartTooltip(IItemStack itemStack, crafttweaker.api.text.ITextComponent tooltip) {

        ItemStack actualStack = CraftTweakerMC.getItemStack(itemStack);
        ITextComponent tooltipText = CraftTweakerMC.getITextComponent(tooltip);

        tooltipMap.computeIfAbsent(actualStack, $ -> new ArrayList<>()).add(tooltipText);

        return this;
    }

    /**
     * A Helper method to clear pre-existing tooltips from Multiblocks.
     * This is optional, and if not called will default false.
     *
     * @param clear - If existing tooltips should be cleared.
     * @return This builder, for convenience.
     */
    @ZenMethod
    public MultiblockBuilder clearTooltips(boolean clear) {
        this.clearTooltips = clear;
        return this;
    }

    /**
     * Add a design to be shown in JEI or structure previews. Can be called multiple times.
     * <p>
     * If none are defined, the multiblock won't show in JEI.
     *
     * @param designs The designs to add and show in JEI.
     * @return This builder, for convenience.
     */
    @ZenMethod
    public MultiblockBuilder addDesign(@NotNull IMultiblockShapeInfo... designs) {
        for (IMultiblockShapeInfo info : designs) {
            this.designs.add(info.getInternal());
        }
        return this;
    }

    /**
     * Construct the {@link CustomMultiblock} using the defined features.
     * <p>
     * Will fail if {@link #withPattern(IBlockPattern)} or {@link #withRecipeMap(RecipeMap)} wasn't called,
     * or if neither {@link #withTexture(IICubeRenderer)} nor {@link #addDesign(IMultiblockShapeInfo...)} was called.
     *
     * @return The built {@link CustomMultiblock}.
     */
    @ZenMethod
    @Nullable
    public CustomMultiblock build() {
        if (pattern == null) {
            CraftTweakerAPI.logError(String.format("No pattern defined for multiblock \"%s\"", loc));
            return null;
        }
        if (recipeMap == null) {
            CraftTweakerAPI.logError(String.format("No recipeMap defined for multiblock \"%s\"", loc));
            return null;
        }
        if (texture == null) {
            if (designs.isEmpty()) {
                CraftTweakerAPI.logError(String.format("No texture defined for multiblock \"%s\", and there are no defined designs.", loc));
                return null;
            } else {
                //noinspection Convert2MethodRef blah blah different semantics
                Optional<ICubeRenderer> tex = designs.stream() // get the mode block and turn it into an ICubeRenderer
                        .map(design -> design.getBlocks())
                        .flatMap(Arrays::stream)
                        .flatMap(Arrays::stream)
                        .flatMap(Arrays::stream) // flatten the 3D array
                        .map(BlockInfo::getBlockState)
                        .filter(s -> s.getRenderType() != EnumBlockRenderType.INVISIBLE)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                        .entrySet()
                        .stream()
                        .max(Map.Entry.comparingByValue()) // get the mode
                        .map(Map.Entry::getKey)
                        .map(SidedCubeRenderer::new);

                if (!tex.isPresent()) {
                    CraftTweakerAPI.logWarning(String.format("No texture defined for multiblock \"%s\", and couldn't resolve texture from defined designs.", loc));
                    return null;
                }

                texture = tex.get();
            }
        }
        if (designs.isEmpty()) {
            CraftTweakerAPI.logWarning(String.format("No designs defined for multiblock \"%s\". It will not show up in JEI.", loc));
        }

        return new CustomMultiblock(this);
    }

    /**
     * Convenience method, equivalent to {@code build().register()}
     * <p>
     *
     * @return The built {@link CustomMultiblock}.
     * @see #build()
     * @see CustomMultiblock#register()
     */
    @Nullable
    @ZenMethod
    public CustomMultiblock buildAndRegister() {
        return Optional.ofNullable(build()).map(CustomMultiblock::register).orElse(null);
    }

}
