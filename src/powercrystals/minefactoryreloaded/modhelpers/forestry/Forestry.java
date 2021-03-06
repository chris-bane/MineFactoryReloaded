package powercrystals.minefactoryreloaded.modhelpers.forestry;

import static cpw.mods.fml.common.registry.GameRegistry.*;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.CustomProperty;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import powercrystals.minefactoryreloaded.farmables.drinkhandlers.DrinkHandlerBiofuel;
import powercrystals.minefactoryreloaded.farmables.fertilizables.FertilizerStandard;
import powercrystals.minefactoryreloaded.farmables.harvestables.HarvestableWood;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityUnifier;

@Mod(modid = "MineFactoryReloaded|CompatForestry", name = "MFR Compat: Forestry", version = MineFactoryReloadedCore.version, dependencies = "after:MineFactoryReloaded;after:Forestry",
customProperties = @CustomProperty(k = "cofhversion", v = "true"))
public class Forestry
{
	private static final String name = "Forestry";
	@EventHandler
	public static void load(FMLInitializationEvent e)
	{
		if(!Loader.isModLoaded(name))
		{
			return;
		}
		try
		{
			initForestry();
		}
		catch(Exception x)
		{
			x.printStackTrace();
		}
	}

	private static void initForestry()
	{
		Item item = findItem(name, "fertilizerCompound");
		if (item != null)
			MFRRegistry.registerFertilizer(new FertilizerStandard(item, 0));
		else
			MineFactoryReloadedCore.log().error("Forestry fertilizer null!");

		item = findItem(name, "fertilizerBio");
		if (item != null)
			MFRRegistry.registerFertilizer(new FertilizerStandard(item, 0));
		else
			MineFactoryReloadedCore.log().error("Forestry compost null!");

		item = findItem(name, "peat");
		if (item != null)
			MFRRegistry.registerSludgeDrop(10, new ItemStack(item));
		else
			MineFactoryReloadedCore.log().error("Forestry peat null!");

		item = findItem(name, "ash");
		if (item != null)
			MFRRegistry.registerSludgeDrop(1, new ItemStack(item));
		else
			MineFactoryReloadedCore.log().error("Forestry ash null!");

		item = findItem(name, "decayingWheat");
		if (item != null)
			MFRRegistry.registerSludgeDrop(20, new ItemStack(item));
		else
			MineFactoryReloadedCore.log().error("Forestry wheat null!");

		item = findItem(name, "sapling");
		Block block = findBlock(name, "saplingGE");
		if (item != null && block != null) {
			ForestrySapling sapling = new ForestrySapling(item, block);
			MFRRegistry.registerPlantable(sapling);
			MFRRegistry.registerFertilizable(sapling);
		} else
			MineFactoryReloadedCore.log().error("Forestry sapling/block null!");

		block = findBlock(name, "soil");
		if (block != null) {
			ForestryBogEarth bog = new ForestryBogEarth(block);
			MFRRegistry.registerPlantable(bog);
			MFRRegistry.registerFertilizable(bog);
			MFRRegistry.registerHarvestable(bog);
			MFRRegistry.registerFruit(bog);
		} else
			MineFactoryReloadedCore.log().error("Forestry bog earth null!");

		for (int i = 1; true; ++i) {
			block = findBlock(name, "log" + i);
			if (block == null) {
				if (i > 1)
					MineFactoryReloadedCore.log().info("Forestry logs null at " + i + ".");
				else
					MineFactoryReloadedCore.log().error("Forestry logs null!");
				break;
			}
			MFRRegistry.registerHarvestable(new HarvestableWood(block));
			MFRRegistry.registerFruitLogBlock(block);
		}

		block = findBlock(name, "leaves");
		if (block != null) {
			ForestryLeaf leaf = new ForestryLeaf(block);
			MFRRegistry.registerFertilizable(leaf);
			MFRRegistry.registerHarvestable(leaf);
			MFRRegistry.registerFruit(leaf);
		} else
			MineFactoryReloadedCore.log().error("Forestry leaves null!");

		block = findBlock(name, "pods");
		item = findItem(name, "grafterProven");
		if (block != null) {
			ForestryPod pod = new ForestryPod(block, item);
			MFRRegistry.registerFertilizable(pod);
			MFRRegistry.registerHarvestable(pod);
			MFRRegistry.registerFruit(pod);
		} else
			MineFactoryReloadedCore.log().error("Forestry pods null!");
	}

	@EventHandler
	public static void postInit(FMLPostInitializationEvent e)
	{
		MFRRegistry.registerLiquidDrinkHandler("bioethanol", new DrinkHandlerBiofuel());

		if (!Loader.isModLoaded(name))
		{
			return;
		}

		TileEntityUnifier.updateUnifierLiquids();
	}
}
