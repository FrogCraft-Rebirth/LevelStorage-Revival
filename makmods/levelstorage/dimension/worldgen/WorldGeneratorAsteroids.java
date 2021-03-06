package makmods.levelstorage.dimension.worldgen;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import makmods.levelstorage.LSBlockItemList;
import makmods.levelstorage.logic.IDMeta;
import makmods.levelstorage.logic.util.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.oredict.OreDictionary;

public class WorldGeneratorAsteroids implements IWorldGenerator {

	public static final int BLOCKS_COUNT = 192;

	public static final IBlockState minableBlockId = LSBlockItemList.blockAntimatterStone.getDefaultState();
	public static final int minableBlockMeta = 0;
	public static final int veinMaxSize = 8;

	public static List<IDMeta> ores = Lists.newArrayList();

	static {
		try {
			String[] oreIDs = OreDictionary.getOreNames();
			for (String s : oreIDs) {
				try {
					if (s.startsWith("ore")) {
						List<ItemStack> stacksForGiven = OreDictionary.getOres(s);
						for (ItemStack st : stacksForGiven) {
							if (st.getItem() instanceof ItemBlock)
								ores.add(new IDMeta(st.getItem(), st.getMetadata()));
						}
					}
				} catch (Exception e) {
					LogHelper.severe("Exception when trying to dynamically allocate more ores to generate (WorldGeneratorAsteroids)");
					e.printStackTrace();
				}
			}
		} catch (Throwable e) {
			LogHelper.severe("Exception when trying to dynamically allocate more ores to generate (WorldGeneratorAsteroids)");
			e.printStackTrace();
		}
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (random.nextInt(256) == 0) {
			int epicentrumX = chunkX * 16 + random.nextInt(16);
			int epicentrumZ = chunkZ * 16 + random.nextInt(16);
			int epicentrumY = -1;
			for (int i = 256; i >= 0; i--) {
				if (!world.isAirBlock(new BlockPos(epicentrumX, i, epicentrumZ))) {
					epicentrumY = i;
					break;
				}
			}
			// we want to create this in void
			if (epicentrumY == -1) {
				epicentrumY = random.nextInt(128) + 48;
				LogHelper.info(String.format(
						"Generating asteroid with epicenter in %d:%d:%d",
						epicentrumX, epicentrumY, epicentrumZ));
				generateAsteroidBody(world, random, epicentrumX, epicentrumY,
						epicentrumZ);
				generateOres(world, random, epicentrumX, epicentrumY,
						epicentrumZ);
			}
		}
	}

	// epicentrums: par3 - x, par4 - y, par5 - z
	public void generateOres(World par1World, Random par2Random, int par3,
			int par4, int par5) {
		int tries = par2Random.nextInt(20);
		for (int t = 0; t < tries; t++) {
			int plusX = -8 + par2Random.nextInt(16);
			int plusY = -8 + par2Random.nextInt(16);
			int plusZ = -8 + par2Random.nextInt(16);
			IDMeta oreToGenerate = ores.get(par2Random.nextInt(ores.size()));
			int x = par3 + plusX;
			int y = par4 + plusY;
			int z = par5 + plusZ;
			new WorldGenMinable(Block.getBlockFromItem(oreToGenerate.getID()).getStateFromMeta(oreToGenerate.getMetadata()),
					par2Random.nextInt(veinMaxSize - 1) + 1)
					.generate(par1World, par2Random, new BlockPos(x, y, z));
		}
	}

	public boolean generateAsteroidBody(World par1World, Random par2Random,
			int par3, int par4, int par5) {
		float f = par2Random.nextFloat() * (float) Math.PI;
		double d0 = (double) ((float) (par3 + 8) + MathHelper.sin(f)
				* (float) BLOCKS_COUNT / 8.0F);
		double d1 = (double) ((float) (par3 + 8) - MathHelper.sin(f)
				* (float) BLOCKS_COUNT / 8.0F);
		double d2 = (double) ((float) (par5 + 8) + MathHelper.cos(f)
				* (float) BLOCKS_COUNT / 8.0F);
		double d3 = (double) ((float) (par5 + 8) - MathHelper.cos(f)
				* (float) BLOCKS_COUNT / 8.0F);
		double d4 = (double) (par4 + par2Random.nextInt(3) - 2);
		double d5 = (double) (par4 + par2Random.nextInt(3) - 2);

		for (int l = 0; l <= BLOCKS_COUNT; ++l) {
			double d6 = d0 + (d1 - d0) * (double) l
					/ (double) BLOCKS_COUNT;
			double d7 = d4 + (d5 - d4) * (double) l
					/ (double) BLOCKS_COUNT;
			double d8 = d2 + (d3 - d2) * (double) l
					/ (double) BLOCKS_COUNT;
			double d9 = par2Random.nextDouble() * (double) BLOCKS_COUNT
					/ 16.0D;
			double d10 = (double) (MathHelper.sin((float) l * (float) Math.PI
					/ (float) BLOCKS_COUNT) + 1.0F)
					* d9 + 1.0D;
			double d11 = (double) (MathHelper.sin((float) l * (float) Math.PI
					/ (float) BLOCKS_COUNT) + 1.0F)
					* d9 + 1.0D;
			int i1 = MathHelper.floor_double(d6 - d10 / 2.0D);
			int j1 = MathHelper.floor_double(d7 - d11 / 2.0D);
			int k1 = MathHelper.floor_double(d8 - d10 / 2.0D);
			int l1 = MathHelper.floor_double(d6 + d10 / 2.0D);
			int i2 = MathHelper.floor_double(d7 + d11 / 2.0D);
			int j2 = MathHelper.floor_double(d8 + d10 / 2.0D);

			for (int k2 = i1; k2 <= l1; ++k2) {
				double d12 = ((double) k2 + 0.5D - d6) / (d10 / 2.0D);

				if (d12 * d12 < 1.0D) {
					for (int l2 = j1; l2 <= i2; ++l2) {
						double d13 = ((double) l2 + 0.5D - d7) / (d11 / 2.0D);

						if (d12 * d12 + d13 * d13 < 1.0D) {
							for (int i3 = k1; i3 <= j2; ++i3) {
								double d14 = ((double) i3 + 0.5D - d8)
										/ (d10 / 2.0D);
								if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) {
									par1World.setBlockState(new BlockPos(k2, l2, i3),
											minableBlockId, 2);
								}
							}
						}
					}
				}
			}
		}

		return true;
	}

}
