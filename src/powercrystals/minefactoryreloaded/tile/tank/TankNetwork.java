package powercrystals.minefactoryreloaded.tile.tank;

import static powercrystals.minefactoryreloaded.tile.tank.TileEntityTank.CAPACITY;

import cofh.core.util.fluid.FluidTankAdv;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.position.BlockPosition;

import java.util.LinkedHashSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TankNetwork
{
	private LinkedHashSet<TileEntityTank> nodeSet;
	private TileEntityTank master;
	FluidTankAdv storage = new FluidTankAdv(0);

	protected TankNetwork() {
		storage.setCapacity(0);
	}


	public TankNetwork(TileEntityTank base) { this();
		nodeSet = new LinkedHashSet<TileEntityTank>();
		addNode(base);
	}

	public int getNodeShare(TileEntityTank cond) {
		int size = nodeSet.size();
		if (size <= 1)
			return storage.getCapacity();
		int amt = 0;
		if (master == cond) amt = storage.getFluidAmount() % size;
		return amt + storage.getFluidAmount() / size;
	}

	public void addNode(TileEntityTank cond) {
		if (nodeSet.add(cond))
			if (!nodeAdded(cond))
				return;
	}

	public void removeNode(TileEntityTank cond, boolean simulate) {
		nodeSet.remove(cond);
		if (!nodeSet.isEmpty()) {
			int share = simulate ? getNodeShare(cond) :
					(cond.fluidForGrid != null ? cond.fluidForGrid.amount : 0);
			if (simulate || nodeSet.remove(cond)) {
				cond.fluidForGrid = storage.drain(share, !simulate);
				if (!simulate)
					nodeRemoved(cond);
			}
		}
		rebalanceGrid();
	}

	public void markSweep() {
		destroyGrid();
		if (nodeSet.isEmpty())
			return;
		TileEntityTank main = nodeSet.iterator().next();
		LinkedHashSet<TileEntityTank> oldSet = nodeSet;
		nodeSet = new LinkedHashSet<TileEntityTank>(Math.min(oldSet.size() / 6, 5));
		rebalanceGrid();

		LinkedHashSet<TileEntityTank> toCheck = new LinkedHashSet<TileEntityTank>();
		LinkedHashSet<TileEntityTank> checked = new LinkedHashSet<TileEntityTank>();
		BlockPosition bp = new BlockPosition(0,0,0);
		ForgeDirection[] dir = ForgeDirection.VALID_DIRECTIONS;
		toCheck.add(main);
		checked.add(main);
		while (!toCheck.isEmpty()) {
			main = toCheck.iterator().next();
			addNode(main);
			World world = main.getWorldObj();
			for (int i = 6; i --> 0; ) {
				bp.x = main.xCoord; bp.y = main.yCoord; bp.z = main.zCoord;
				bp.step(dir[i]);
				if (world.blockExists(bp.x, bp.y, bp.z)) {
					TileEntity te = bp.getTileEntity(world);
					if (te instanceof TileEntityTank) {
						if (main.isInterfacing(dir[i^1]) && !checked.contains(te))
							toCheck.add((TileEntityTank)te);
						checked.add((TileEntityTank)te);
					}
				}
			}
			toCheck.remove(main);
			oldSet.remove(main);
		}
		if (!oldSet.isEmpty()) {
			TankNetwork newGrid = new TankNetwork();
			newGrid.nodeSet = oldSet;
			newGrid.markSweep();
		}
	}

	public void destroyGrid() {
		master = null;
		for (TileEntityTank curCond : nodeSet)
			destroyNode(curCond);
	}

	public void destroyNode(TileEntityTank cond) {
		cond.fluidForGrid = storage.drain(getNodeShare(cond), false);
		cond.grid = null;
	}

	public boolean canMergeGrid(TankNetwork grid) {
		if (grid == null) return false;
		return FluidHelper.isFluidEqual(grid.storage.getFluid(), storage.getFluid());
	}

	public void mergeGrid(TankNetwork grid) {
		if (grid == this) return;
		if (storage.getFluid() == null && grid.storage.getFluid() != null) {
			grid.mergeGrid(this);
			return;
		}
		grid.destroyGrid();

		for (TileEntityTank cond : grid.nodeSet)
			addNode(cond);

		grid.nodeSet.clear();
	}

	public void nodeRemoved(TileEntityTank cond) {
		if (cond == master) {
			if (nodeSet.isEmpty()) {
				master = null;
			} else {
				master = nodeSet.iterator().next();
			}
		}
		if (cond.interfaceCount() > 1)
			markSweep(); // TODO: tick handler?
	}

	public boolean nodeAdded(TileEntityTank cond) {
		if (cond.grid != null) {
			if (cond.grid != this) {
				nodeSet.remove(cond);
				if (canMergeGrid(cond.grid)) {
					mergeGrid(cond.grid);
				} else
					return false;
			} else
				return false;
		} else if (cond.fluidForGrid != null) {
			if (!FluidHelper.isFluidEqualOrNull(cond.fluidForGrid, storage.getFluid())) {
				nodeSet.remove(cond);
				return false;
			} else
				cond.grid = this;
		} else {
			cond.grid = this;
		}
		rebalanceGrid();
		if (master == null) {
			master = cond;
		}
		storage.fill(cond.fluidForGrid, true);
		cond.fluidForGrid = storage.drain(0, false);
		return true;
	}

	public void rebalanceGrid() {
		storage.setCapacity(nodeSet.size() * CAPACITY);
	}

	public int getSize() {
		return nodeSet.size();
	}

	@Override
	public String toString() {
		return "TankNetwork@" + Integer.toString(hashCode()) + "; master:" + master;
	}
}
