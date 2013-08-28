package makmods.levelstorage.tileentity;

import ic2.api.Direction;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.tile.IEnergyStorage;
import ic2.api.tile.IWrenchable;
import makmods.levelstorage.LevelStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

/**
 * More like an API, for basic sinks. Contains Energy Storage, Basic Energy
 * Inputs, Explosions on exceed of Max Packet Size, Wrenchable. If you intend on
 * using this, always use super.nameOfMethod()
 * 
 * @author mak326428
 * 
 */
public abstract class TileEntityBasicSink extends TileEntity implements
        IEnergyTile, IEnergySink, IWrenchable, IEnergyStorage {

	private boolean addedToENet = false;
	private int stored;
	public static final String NBT_STORED = "stored";

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readFromNBT(par1NBTTagCompound);
		stored = par1NBTTagCompound.getInteger(NBT_STORED);

	}

	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeToNBT(par1NBTTagCompound);

		par1NBTTagCompound.setInteger(NBT_STORED, stored);

	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		this.writeToNBT(tagCompound);
		return new Packet132TileEntityData(this.xCoord, this.yCoord,
		        this.zCoord, 5, tagCompound);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
		this.readFromNBT(pkt.customParam1);
	}

	@Override
	public void setStored(int energy) {
		this.stored = energy;

	}

	// IWrenchable stuff

	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side) {
		return false;
	}

	public short getFacing() {
		return (short) ForgeDirection.UNKNOWN.flag;
	}

	public void setFacing(short facing) {
		;
		// Do nothing here
	}

	public boolean wrenchCanRemove(EntityPlayer entityPlayer) {
		return true;
	}

	public float getWrenchDropRate() {
		return 0.5f;
	}

	// End of IWrenchable

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (LevelStorage.isSimulating())
			if (!addedToENet) {
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
				addedToENet = true;
			}
	}

	private void unloadFromENet() {
		if (LevelStorage.isSimulating())
			if (addedToENet) {
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
				addedToENet = false;
			}
	}

	public abstract void onUnloaded();

	@Override
	public void invalidate() {
		onUnloaded();
		unloadFromENet();
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		unloadFromENet();
		super.onChunkUnload();
	}

	public boolean isAddedToEnergyNet() {
		return addedToENet;
	}

	public abstract int getMaxInput();

	public abstract boolean explodes();

	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) {
		return true;
	}

	public int getMaxSafeInput() {
		return getMaxInput();
	}

	public int addEnergy(int amount) {

		this.stored += amount;
		return stored;
	}

	public abstract void onLoaded();

	public void validate() {
		super.validate();
		onLoaded();
	}

	public int getStored() {
		return this.stored;
	}

	public int getOutput() {
		return 0;
	}

	public boolean isTeleporterCompatible(Direction side) {
		return false;
	}

	@Override
	public int injectEnergy(Direction directionFrom, int amount) {
		if (amount > getMaxInput() && explodes()) {
			this.invalidate();
			this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
			this.worldObj.createExplosion(null, this.xCoord, this.yCoord,
			        this.zCoord, 2F, false);
		}
		if ((this.getCapacity() - this.getStored()) > amount) {
			this.addEnergy(amount);
			return 0;
		} else {
			int leftover = amount - (this.getCapacity() - this.getStored());
			this.setStored(getCapacity());
			return leftover;
		}
	}

	public int demandsEnergy() {
		return getCapacity() - getStored();
	}
}