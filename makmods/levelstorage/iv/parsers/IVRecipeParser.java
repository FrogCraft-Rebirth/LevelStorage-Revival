package makmods.levelstorage.iv.parsers;

import java.util.List;

import com.google.common.collect.Lists;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IMachineRecipeManager.RecipeIoContainer;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeInputItemStack;
import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import makmods.levelstorage.LSConfig;
import makmods.levelstorage.iv.IVEntry;
import makmods.levelstorage.iv.IVItemStackEntry;
import makmods.levelstorage.iv.IVRegistry;
import makmods.levelstorage.iv.parsers.recipe.IWrappedRecipeCompound;
import makmods.levelstorage.iv.parsers.recipe.ItemStackRecipeCompound;
import makmods.levelstorage.iv.parsers.recipe.OreDictRecipeCompound;
import makmods.levelstorage.iv.parsers.recipe.RecipeHelper;
import makmods.levelstorage.logic.util.LogHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;

public class IVRecipeParser implements IRecipeParser {

	public static int PASSES = LSConfig.dynamicIVRegistryMappingDepth;

	public int assignCrafting(ItemStack is) {
		List<IRecipe> recipesFor = RecipeHelper.getRecipesFor(is);
		if (recipesFor.size() == 0)
			return IVRegistry.NOT_FOUND;
		List<Integer> candidates = Lists.newArrayList();
		for (IRecipe recipe : recipesFor) {
			int fIV = 0;
			List<IWrappedRecipeCompound> iss = RecipeHelper
					.getKnownCompounds(recipe);
			if (iss == null)
				continue;
			boolean hasUnknown = false;
			for (IWrappedRecipeCompound rc : iss) {
				if (rc instanceof ItemStackRecipeCompound) {
					ItemStackRecipeCompound isrc = (ItemStackRecipeCompound) rc;
					int value = IVRegistry.getValue(isrc.getStack());
					if (value != IVRegistry.NOT_FOUND)
						fIV += value;
					else
						hasUnknown = true;
				} else {
					OreDictRecipeCompound odrc = (OreDictRecipeCompound) rc;
					int value = IVRegistry.getValue(odrc.getName());
					if (value != IVRegistry.NOT_FOUND)
						fIV += value;
					else
						hasUnknown = true;
				}
			}
			if (recipe.getRecipeOutput().stackSize > 0)
				fIV /= recipe.getRecipeOutput().stackSize;
			if (fIV > 0 && !hasUnknown)
				candidates.add(fIV);
		}
		int toReleased = Integer.MAX_VALUE;
		for (Integer candidate : candidates)
			if (candidate < toReleased && (candidate != IVRegistry.NOT_FOUND))
				toReleased = candidate;
		if (toReleased == Integer.MAX_VALUE) {
			return IVRegistry.NOT_FOUND;
		} else {
			String oreDict = RecipeHelper.resolveOreDict(is);
			if (oreDict != null) {
				int odIV = IVRegistry.getValue(oreDict);

				if (odIV == IVRegistry.NOT_FOUND) {
					IVRegistry.instance.assignOreDict_dynamic(oreDict,
							toReleased);
				} else if (toReleased < odIV && toReleased > 0) {
					IVRegistry.instance.removeIV(oreDict);
					IVRegistry.instance.assignOreDict_dynamic(oreDict,
							toReleased);
				}
			}
			int isIV = IVRegistry.getValue(is);
			if (isIV == IVRegistry.NOT_FOUND) {
				IVRegistry.instance.assignItemStack_dynamic(is, toReleased);
			} else if (toReleased < isIV && toReleased > 0) {
				IVRegistry.instance.removeIV(is);
				IVRegistry.instance.assignItemStack_dynamic(is, toReleased);
			}
			return toReleased;
		}
	}

	public void assignIC2Machine(IMachineRecipeManager manager) {
		Iterable<RecipeIoContainer> recipes = manager.getRecipes();
		for (RecipeIoContainer entry : recipes) {
			IRecipeInput input = entry.input;
			RecipeOutput output = entry.output;

			int inputValue = input instanceof RecipeInputItemStack ? IVRegistry
					.getValue(((RecipeInputItemStack) input).input)
					: IVRegistry.getValue(((RecipeInputOreDict) input).input);
			if (inputValue == IVRegistry.NOT_FOUND)
				continue;
			int iv = inputValue / output.items.get(0).stackSize;
			int outputIV = IVRegistry.getValue(output.items.get(0));
			if (outputIV == IVRegistry.NOT_FOUND)
				IVRegistry.instance.assignItemStack_dynamic(
						output.items.get(0), iv);
			else if (iv < outputIV && iv > 0) {
				IVRegistry.instance.removeIV(output.items.get(0));
				IVRegistry.instance.assignItemStack_dynamic(
						output.items.get(0), iv);
			}
		}
	}

	public void assignFurnace(FurnaceRecipes recipes) {
		List<IVEntry> copied = IVRegistry.instance.copyRegistry();

		for (IVEntry entry : copied) {
			if (!(entry instanceof IVItemStackEntry))
				continue;
			IVItemStackEntry e = (IVItemStackEntry) entry;

			ItemStack outputSmelting = recipes.getSmeltingResult(e.getStack());
			if (outputSmelting != null) {
				int iv = entry.getValue() / outputSmelting.stackSize;
				int alreadyExistingValue = IVRegistry.getValue(outputSmelting);
				if (alreadyExistingValue == IVRegistry.NOT_FOUND)
					IVRegistry.instance.assignItemStack_dynamic(
							outputSmelting.copy(), iv);
				else if (iv < alreadyExistingValue && iv > 0) {
					IVRegistry.instance.removeIV(outputSmelting);
					IVRegistry.instance.assignItemStack_dynamic(
							outputSmelting.copy(), iv);
				}
			}
		}
	}

	@Override
	public void parse() {
		List<IRecipe> recipes = RecipeHelper.getAllRecipes();
		int iterator = 0;
		int totalMS = 0;
		IVRegistry.clearCache();
		for (int i = 0; i < PASSES; i++) {
			IVRegistry.clearCache();
			assignFurnace(FurnaceRecipes.instance());
			IVRegistry.clearCache();
			assignIC2Machine(Recipes.macerator);
			IVRegistry.clearCache();
			assignIC2Machine(Recipes.extractor);
			IVRegistry.clearCache();
			assignIC2Machine(Recipes.compressor);
			IVRegistry.clearCache();
			assignIC2Machine(Recipes.metalformerRolling);
			IVRegistry.clearCache();
			assignIC2Machine(Recipes.metalformerExtruding);
			IVRegistry.clearCache();
			assignIC2Machine(Recipes.metalformerCutting);
			IVRegistry.clearCache();
			for (IRecipe recipe : recipes) {
				ItemStack result = recipe.getRecipeOutput();
				if (result != null) {
					iterator++;
					long ms = System.currentTimeMillis();
					assignCrafting(result);
					totalMS += System.currentTimeMillis() - ms;
				}
			}
			IVRegistry.clearCache();
		}
		LogHelper.info("Total recipes iterated: " + iterator);
		LogHelper.info("Total time consumed: " + totalMS);
		if (iterator > 0)
			LogHelper.info("Average ms per recipe: " + totalMS / iterator);
	}

}
