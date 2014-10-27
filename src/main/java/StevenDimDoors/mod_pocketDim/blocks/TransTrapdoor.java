package StevenDimDoors.mod_pocketDim.blocks;

import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import StevenDimDoors.mod_pocketDim.mod_pocketDim;
import StevenDimDoors.mod_pocketDim.core.DDTeleporter;
import StevenDimDoors.mod_pocketDim.core.DimLink;
import StevenDimDoors.mod_pocketDim.core.LinkType;
import StevenDimDoors.mod_pocketDim.core.NewDimData;
import StevenDimDoors.mod_pocketDim.core.PocketManager;
import StevenDimDoors.mod_pocketDim.items.ItemDDKey;
import StevenDimDoors.mod_pocketDim.tileentities.TileEntityTransTrapdoor;

public class TransTrapdoor extends BlockTrapDoor implements IDimDoor, ITileEntityProvider
{

	public TransTrapdoor(Material material)
	{
		super( material);
		this.setCreativeTab(mod_pocketDim.dimDoorsCreativeTab);
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(mod_pocketDim.modid + ":" + this.getUnlocalizedName());
	}

	//Teleports the player to the exit link of that dimension, assuming it is a pocket
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		enterDimDoor(world, x, y, z, entity);
	}

	public boolean checkCanOpen(World world, int x, int y, int z)
	{
		return this.checkCanOpen(world, x, y, z, null);
	}
	
	public boolean checkCanOpen(World world, int x, int y, int z, EntityPlayer player)
	{
		DimLink link = PocketManager.getLink( x, y, z, world);
		if(link==null||player==null)
		{
			return link==null;
		}
		if(!link.getLockState())
		{
			return true;
		}
		
		for(ItemStack item : player.inventory.mainInventory)
		{
			if(item != null)
			{
				if(item.getItem() instanceof ItemDDKey)
				{
					if(link.tryToOpen(item))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
		if(this.checkCanOpen(par1World, par2, par3, par4, par5EntityPlayer))
		{
			return super.onBlockActivated(par1World, par2, par3, par4, par5EntityPlayer, par6, par7, par8, par9);
		}
		return false;
    }

	@Override
    public void func_150120_a(World par1World, int par2, int par3, int par4, boolean par5)
    {
    	if(this.checkCanOpen(par1World, par2, par3, par4))
    	{
    		super.func_150120_a(par1World, par2, par3, par4, par5);//onPoweredBlockChange
    	}
    }
	@Override
	public void enterDimDoor(World world, int x, int y, int z, Entity entity) 
	{
		if (!world.isRemote && func_150118_d(world.getBlockMetadata(x, y, z)))//isTrapDoorOpen
		{
			DimLink link = PocketManager.getLink(x, y, z, world);
			if (link != null)
			{
				DDTeleporter.traverseDimDoor(world, link, entity,this);
			}
			super.func_150120_a(world, x, y, z, false);//onPoweredBlockChange
		}
	}	

	@Override
	public void onBlockAdded(World world, int x, int y, int z) 
	{
		this.placeLink(world, x, y, z);
		world.setTileEntity(x, y, z, this.createNewTileEntity(world, 0));
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityTransTrapdoor();
	}
	
	@Override
	public void placeLink(World world, int x, int y, int z) 
	{
		if (!world.isRemote)
		{
			NewDimData dimension = PocketManager.createDimensionData(world);
			DimLink link = dimension.getLink(x, y, z);
			if (link == null && dimension.isPocketDimension())
			{
				dimension.createLink(x, y, z, LinkType.UNSAFE_EXIT,0);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		return new ItemStack(this.getDoorItem(), 1);
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(this.getDrops(), 1));
		return drops;
    }
	
	@Override
	public net.minecraft.item.Item getDoorItem()
	{
		return Item.getItemFromBlock(mod_pocketDim.transTrapdoor);
	}

	@Override
	public net.minecraft.item.Item getDrops()
	{
		return Item.getItemFromBlock(Blocks.trapdoor);
	}	
	
	public static boolean isTrapdoorSetLow(int metadata)
	{
		return (metadata & 8) == 0;
	}
	
	@Override
	public TileEntity initDoorTE(World world, int x, int y, int z)
	{
		TileEntity te = this.createNewTileEntity(world, 0);
		world.setTileEntity(x, y, z, te);
		return te;
	}

	@Override
	public boolean isDoorOnRift(World world, int x, int y, int z)
	{
		return PocketManager.getLink(x, y, z, world)!=null;
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block oldBlockID, int oldMeta)
    {
		// This function runs on the server side after a block is replaced
		// We MUST call super.breakBlock() since it involves removing tile entities
        super.breakBlock(world, x, y, z, oldBlockID, oldMeta);
        
        // Schedule rift regeneration for this block if it was replaced
        if (!world.getBlock(x, y, z).equals(oldBlockID))
        {
        	mod_pocketDim.riftRegenerator.scheduleFastRegeneration(x, y, z, world);
        }
    }
}