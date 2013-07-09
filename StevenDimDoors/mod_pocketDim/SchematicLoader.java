package StevenDimDoors.mod_pocketDim;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockComparator;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockStairs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.ChestGenHooks;
import StevenDimDoors.mod_pocketDim.helpers.DungeonHelper;
import StevenDimDoors.mod_pocketDim.helpers.dimHelper;
import StevenDimDoors.mod_pocketDim.helpers.yCoordHelper;
import StevenDimDoors.mod_pocketDim.ticking.MobObelisk;

public class SchematicLoader 
{
	private final static int EAST_DOOR_METADATA = 0;
	private final static int NORTH_DOOR_METADATA = 3;

	private DDProperties properties = DDProperties.instance();
	
	public SchematicLoader() { }

	public int transformMetadata(int metadata, int orientation, int blockID)
	{
		if (DungeonHelper.instance().metadataFlipList.contains(blockID))
		{
			switch (orientation)
			{
			case 0:

				if(Block.blocksList[blockID] instanceof BlockStairs)
				{

					switch (metadata)
					{
					case 0:
						metadata = 2;
						break;
					case 1:
						metadata = 3;
						break;
					case 2:
						metadata = 1;
						break;
					case 3:
						metadata = 0;
						break;
					case 7:
						metadata = 4;
						break;
					case 6:
						metadata = 5;
						break;
					case 5:
						metadata = 7;
						break;
					case 4:
						metadata = 6;
						break;

					}
				}

				else if(blockID== Block.chest.blockID||blockID== Block.chestTrapped.blockID||blockID== Block.ladder.blockID)
				{
					switch (metadata)
					{

					case 2:
						metadata = 5;
						break;
					case 3:
						metadata = 4;
						break;					
					case 4:
						metadata = 2;
						break;
					case 5:
						metadata = 3;
						break;
					}

				}
				else if (blockID==Block.vine.blockID)
				{
					switch (metadata)
					{

					case 1:
						metadata = 2;
						break;
					case 2:
						metadata = 4;
						break;					
					case 4:
						metadata = 8;
						break;
					case 8:
						metadata = 1;
						break;
					}
				}
				else if(blockID== Block.lever.blockID||blockID== Block.stoneButton.blockID||blockID== Block.woodenButton.blockID||blockID== Block.torchWood.blockID||blockID== Block.torchRedstoneIdle.blockID||blockID== Block.torchRedstoneActive.blockID)
				{
					switch (metadata)
					{
					case 12:
						metadata = 9;
						break;
					case 11:
						metadata = 10;
						break;
					case 10:
						metadata = 12;
						break;
					case 9:
						metadata = 11;
						break;					
					case 2:
						metadata = 4;
						break;
					case 3:
						metadata = 2;
						break;
					case 1:
						metadata = 3;
						break;
					case 4:
						metadata = 1;
						break;
					}
				}
				else if(blockID== Block.pistonBase.blockID||blockID==Block.pistonExtension.blockID||blockID==Block.pistonStickyBase.blockID||blockID==Block.dispenser.blockID||blockID==Block.dropper.blockID)
				{
					switch (metadata)
					{
					case 4:
						metadata = 2;
						break;
					case 5:
						metadata = 3;
						break;
					case 13:
						metadata = 11;
						break;
					case 12:
						metadata = 10;
						break;
					case 3:
						metadata = 4;
						break;
					case 2:
						metadata = 5;
						break;
					case 11:
						metadata = 12;
						break;
					case 10:
						metadata = 13;
						break;
					}
				}
				else if(Block.blocksList[blockID] instanceof BlockRedstoneRepeater ||Block.blocksList[blockID] instanceof BlockDoor ||blockID== Block.tripWireSource.blockID||Block.blocksList[blockID] instanceof BlockComparator)
				{
					switch (metadata)
					{
					case 0:
						metadata = 1;
						break;
					case 1:
						metadata = 2;
						break;
					case 2:
						metadata = 3;
						break;
					case 3:
						metadata = 0;
						break;
					case 4:
						metadata = 5;
						break;
					case 5:
						metadata = 6;
						break;
					case 6:
						metadata = 7;
						break;
					case 7:
						metadata = 4;
						break;
					case 8:
						metadata = 9;
						break;
					case 9:
						metadata = 10;
						break;
					case 10:
						metadata = 11;
						break;
					case 11:
						metadata = 8;
						break;
					case 12:
						metadata = 13;
						break;
					case 13:
						metadata = 14;
						break;
					case 14:
						metadata = 15;
						break;
					case 15:
						metadata = 12;
						break;
					}
				}
				break;
			case 1:
				if(Block.blocksList[blockID] instanceof BlockStairs)
				{
					switch (metadata)
					{
					case 0:
						metadata = 1;
						break;
					case 1:
						metadata = 0;
						break;
					case 2:
						metadata = 3;
						break;
					case 3:
						metadata = 2;
						break;
					case 7:
						metadata = 6;
						break;
					case 6:
						metadata = 7;
						break;
					case 5:
						metadata = 4;
						break;
					case 4:
						metadata = 5;
						break;
					}
				}

				else if(blockID== Block.chest.blockID||blockID== Block.chestTrapped.blockID||blockID==Block.ladder.blockID)
				{
					switch (metadata)
					{
					case 2:
						metadata = 3;
						break;
					case 3:
						metadata = 2;
						break;					
					case 4:
						metadata = 5;
						break;
					case 5:
						metadata = 4;
						break;
					}

				}

				else	if(blockID==Block.vine.blockID)
				{
					switch (metadata)
					{

					case 1:
						metadata = 4;
						break;
					case 2:
						metadata = 8;
						break;					
					case 4:
						metadata = 1;
						break;
					case 8:
						metadata = 2;
						break;
					}
				}




				else if(blockID== Block.lever.blockID||blockID== Block.torchWood.blockID||blockID== Block.torchRedstoneIdle.blockID||blockID== Block.torchRedstoneActive.blockID)
				{
					switch (metadata)
					{
					case 12:
						metadata = 11;
						break;
					case 11:
						metadata = 12;
						break;
					case 10:
						metadata = 9;
						break;
					case 9:
						metadata = 10;
						break;					
					case 2:
						metadata = 1;
						break;
					case 3:
						metadata = 4;
						break;
					case 1:
						metadata = 2;
						break;
					case 4:
						metadata = 3;

						break;

					}

				}

				else	if(blockID== Block.pistonBase.blockID||blockID==Block.pistonStickyBase.blockID||blockID==Block.dispenser.blockID||blockID==Block.dropper.blockID)
				{
					switch (metadata)
					{
					case 4:
						metadata = 5;
						break;
					case 5:
						metadata = 4;
						break;
					case 13:
						metadata = 12;
						break;
					case 12:
						metadata = 13;
						break;
					case 3:
						metadata = 2;
						break;
					case 2:
						metadata = 3;
						break;
					case 11:
						metadata = 10;
						break;
					case 10:
						metadata = 11;
						break;

					}



				}

				else	if(Block.blocksList[blockID] instanceof BlockRedstoneRepeater ||Block.blocksList[blockID] instanceof BlockDoor ||blockID== Block.tripWireSource.blockID||Block.blocksList[blockID] instanceof BlockComparator)
				{
					switch (metadata)
					{
					case 0:
						metadata = 2;
						break;
					case 1:
						metadata = 3;
						break;
					case 2:
						metadata = 0;
						break;
					case 3:
						metadata = 1;
						break;
					case 4:
						metadata = 6;
						break;
					case 5:
						metadata = 7;
						break;
					case 6:
						metadata = 4;
						break;
					case 7:
						metadata = 5;
						break;
					case 8:
						metadata = 10;
						break;
					case 9:
						metadata = 11;
						break;
					case 10:
						metadata = 8;
						break;
					case 11:
						metadata = 9;
						break;
					case 12:
						metadata = 14;
						break;
					case 13:
						metadata = 15;
						break;
					case 14:
						metadata = 12;
						break;
					case 15:
						metadata = 13;
						break;


					}



				}

				break;
			case 2:

				if(Block.blocksList[blockID] instanceof BlockStairs)
				{

					switch (metadata)
					{
					case 2:
						metadata = 0;
						break;
					case 3:
						metadata = 1;
						break;
					case 1:
						metadata = 2;
						break;
					case 0:
						metadata = 3;
						break;
					case 4:
						metadata = 7;
						break;
					case 5:
						metadata = 6;
						break;
					case 7:
						metadata = 5;
						break;
					case 6:
						metadata = 4;
						break;

					}
				}

				else	if(blockID== Block.chest.blockID||blockID== Block.chestTrapped.blockID||blockID==Block.ladder.blockID)
				{
					switch (metadata)
					{

					case 2:
						metadata = 4;
						break;
					case 3:
						metadata = 5;
						break;					
					case 4:
						metadata = 3;
						break;
					case 5:
						metadata = 2;
						break;



					}

				}

				else	if(blockID==Block.vine.blockID)
				{
					switch (metadata)
					{

					case 1:
						metadata = 8;
						break;
					case 2:
						metadata = 1;
						break;					
					case 4:
						metadata = 2;
						break;
					case 8:
						metadata = 4;
						break;
					}
				}




				else	if(blockID== Block.lever.blockID||blockID== Block.torchWood.blockID||blockID== Block.torchRedstoneIdle.blockID||blockID== Block.torchRedstoneActive.blockID)
				{
					switch (metadata)
					{
					case 9:
						metadata = 12;
						break;
					case 10:
						metadata = 11;
						break;
					case 12:
						metadata = 10;
						break;
					case 11:
						metadata = 9;
						break;					
					case 4:
						metadata = 2;
						break;
					case 2:
						metadata = 3;
						break;
					case 3:
						metadata = 1;
						break;
					case 1:
						metadata = 4;

						break;

					}

				}

				else	if(blockID== Block.pistonBase.blockID||blockID==Block.pistonStickyBase.blockID||blockID==Block.dispenser.blockID||blockID==Block.dropper.blockID)

				{
					switch (metadata)
					{
					case 2:
						metadata = 4;
						break;
					case 3:
						metadata = 5;
						break;
					case 11:
						metadata = 13;
						break;
					case 10:
						metadata = 12;
						break;
					case 4:
						metadata = 3;
						break;
					case 5:
						metadata = 2;
						break;
					case 12:
						metadata = 11;
						break;
					case 13:
						metadata = 10;
						break;


					}



				}

				else	if(Block.blocksList[blockID] instanceof BlockRedstoneRepeater ||Block.blocksList[blockID] instanceof BlockDoor ||blockID== Block.tripWireSource.blockID||Block.blocksList[blockID] instanceof BlockComparator)
				{
					switch (metadata)
					{
					case 1:
						metadata = 0;
						break;
					case 2:
						metadata = 1;
						break;
					case 3:
						metadata = 2;
						break;
					case 0:
						metadata = 3;
						break;
					case 5:
						metadata = 4;
						break;
					case 6:
						metadata = 5;
						break;
					case 7:
						metadata = 6;
						break;
					case 4:
						metadata = 7;
						break;
					case 9:
						metadata = 8;
						break;
					case 10:
						metadata = 9;
						break;
					case 11:
						metadata = 10;
						break;
					case 8:
						metadata = 11;
						break;
					case 13:
						metadata = 12;
						break;
					case 14:
						metadata = 13;
						break;
					case 15:
						metadata = 14;
						break;
					case 12:
						metadata = 15;
						break;


					}



				}






				break;
			case 3:
				/**
				 * this is the default case- never need to change anything here
				 * 
				 */




				break;

			}


		}
		return metadata;
	}

	public void generateSchematic(int riftX, int riftY, int riftZ, int orientation, int destDimID, int originDimID, String schematicPath)
	{
		short width = 0;
		short height = 0;
		short length = 0;

		//list of combined blockIds
		short[] blocks = new short[0];

		//block metaData
		byte[] blockData = new byte[0];

		//first 4 bytes of the block ID
		byte[] blockId = new byte[0];

		//second 4 bytes of the block ID
		byte[] addId = new byte[0];

		NBTTagList tileEntities = null;
		NBTTagCompound[] tileEntityList;
		NBTTagList entities;

		//the wooden door leading into the pocket
		Point3D schematicEntrance = new Point3D(0,0,0);

		//the iron dim doors leading to more pockets
		ArrayList<Point3D> sideLinks = new ArrayList<Point3D>();

		//the wooden dim doors leading to the surface
		ArrayList<Point3D> exitLinks = new ArrayList<Point3D>();

		//the locations to spawn monoliths
		ArrayList<Point3D> monolithSpawns = new ArrayList<Point3D>();

		//load the schematic from disk
		try 
		{
			InputStream input;
			String fname = schematicPath ;

			if(!(new File(fname).exists()))
			{
				//get file if its in the Jar
				input = this.getClass().getResourceAsStream(fname);
			}
			else
			{
				//get file if in external library
				System.out.println(new File(fname).exists());
				input = new FileInputStream(fname);
			}
			NBTTagCompound nbtdata = CompressedStreamTools.readCompressed(input);

			//load size of schematic to generate
			width = nbtdata.getShort("Width");
			height = nbtdata.getShort("Height");
			length = nbtdata.getShort("Length");

			//load block info
			blockId = nbtdata.getByteArray("Blocks");
			blockData = nbtdata.getByteArray("Data");
			addId = nbtdata.getByteArray("AddBlocks");

			//create combined block list
			blocks = new short[blockId.length];

			//load ticking things
			tileEntities = nbtdata.getTagList("TileEntities");
			//tileEntityList = new NBTTagCompound[width*height*length];
			/**
			for(int count = 0; count<tileEntities.tagCount(); count++)
			{
				NBTTagCompound tag = (NBTTagCompound)tileEntities.tagAt(count);
				tileEntityList[tag.getInteger("y")*width*length+tag.getInteger("z")*width+tag.getInteger("x")]=tag;
			}

			entities = nbtdata.getTagList("Entities");
			tileentities = nbtdata.getTagList("TileEntities");
			 **/
			input.close();

			//combine the split block IDs into a single short[]
			for (int index = 0; index < blockId.length; index++) 
			{
				if ((index >> 1) >= addId.length) 
				{ 
					blocks[index] = (short) (blockId[index] & 0xFF);
				} 
				else 
				{
					if ((index & 1) == 0) 
					{
						blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
					} 
					else 
					{
						blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
					}
				}
			}
		}
		catch (Exception e) 
		{
			System.out.println("Error- could not find file "+schematicPath);
			e.printStackTrace();
		}

		World world;
		dimHelper.dimList.get(destDimID).hasBeenFilled = true;

		if (dimHelper.getWorld(destDimID) == null)
		{
			dimHelper.initDimension(destDimID);
		}
		world = dimHelper.getWorld(destDimID);
		
		//The following Random initialization code is based on code from ChunkProviderGenerate.
		//It makes our generation depend on the world seed.
		Random rand = new Random(world.getSeed());
        long factorA = rand.nextLong() / 2L * 2L + 1L;
        long factorB = rand.nextLong() / 2L * 2L + 1L;
        rand.setSeed((riftX >> 4) * factorA + (riftZ >> 4) * factorB ^ world.getSeed());

		//Coordinates relative to the schematic, start at 0 and increase up to max width/height/length
		int x, y, z;

		//The real point where a block will be placed
		int realX, realY, realZ;
		Point3D pocketPoint = new Point3D(0, 0, 0);
		
		//The central point of the pocket where we'll be placing the schematic
		Point3D pocketCenter = new Point3D(riftX, riftY, riftZ);
		Point3D zeroPoint = new Point3D(0, 0, 0);
		
		//The direction in which the player will enter. Will be set below. The direction is encoded using
		//the same values as door metadata. Those values are not the same as Minecraft's cardinal directions.
		int entryDirection = 0;
		
		//First loop through the schematic to load in all rift locations and Monolith spawn locations.
		//Also find the entry door, which determines the final position and orientation of the dungeon.
		for ( x = 0; x < width; ++x)
		{
			for ( y = 0; y < height; ++y) 
			{
				for ( z = 0; z < length; ++z) 
				{
					int index = y * width * length + z * width + x;
					int indexBelow = (y - 1) * width * length + z * width + x;
					int indexDoubleBelow = (y - 2) * width * length + z * width + x;
					
					int currentBlock = blocks[index];

					//NBTTagList tileEntity = tileEntities;
					//int size = tileEntity.tagCount();

					if (currentBlock == Block.doorIron.blockID && indexBelow >= 0 && blocks[indexBelow] == Block.doorIron.blockID)
					{
						sideLinks.add(new Point3D(x, y, z));
					}
					else if (currentBlock == Block.doorWood.blockID)
					{
						if (indexDoubleBelow >= 0 && blocks[indexDoubleBelow] == Block.sandStone.blockID &&
							blocks[indexBelow] == Block.doorWood.blockID)
						{
							exitLinks.add(new Point3D(x, y, z));
						}
						else if (indexBelow >= 0 && blocks[indexBelow] == Block.doorWood.blockID)
						{
							schematicEntrance = new Point3D(x, y, z);
							entryDirection = Math.abs(blockData[indexBelow] + 2) % 4;	//TODO: Write a function for extracting a door's orientation from its metadata or at least change this to use bitwise operations.
						}
					}
					else if (currentBlock == Block.endPortalFrame.blockID)
					{
						monolithSpawns.add(new Point3D(x, y, z));
					}
				}
			}
		}
		
		//Loop to actually place the blocks
		for (x = 0; x < width; x++) 
		{
			for (z = 0; z < length; z++) 
			{
				for (y = 0; y < height; y++) 
				{
					//Reinitialize pocketPoint with the current schematic coordinate system
					pocketPoint.setX(x);
					pocketPoint.setY(y);
					pocketPoint.setZ(z);
					//Transform pocketPoint into the pocket coordinate system
					transformPoint(pocketPoint, schematicEntrance, orientation - entryDirection, pocketCenter);
					//Store the transformed coordinates
					realX = pocketPoint.getX();
					realY = pocketPoint.getY();
					realZ = pocketPoint.getZ();

					int index = y * width * length + z * width + x;
					int currentBlock = blocks[index];
					int blockMetadata = blockData[index];
					NBTTagList tileEntity = tileEntities;

					//replace tagging blocks with air, and mod blocks with FoR
					if (currentBlock == Block.endPortalFrame.blockID)
					{
						currentBlock = 0;
					}
					else if ((Block.blocksList[currentBlock] == null && currentBlock != 0) ||
							(currentBlock > 158 && currentBlock != mod_pocketDim.blockDimWallPerm.blockID)) //TODO- replace 158 with max vanilla block ID
					{
						currentBlock = mod_pocketDim.blockDimWall.blockID;
					}

					//Place blocks and set metadata
					if (currentBlock > 0)
					{
						//Calculate new metadata for blocks that have orientations (e.g. doors, pistons)
						//We're using a workaround to get the desired rotation relative to the schematic's entrance
						int fixedMetadata = transformMetadata(blockMetadata, (orientation + NORTH_DOOR_METADATA - entryDirection + 16) % 4, currentBlock);

						//Convert vanilla doors to dim doors or place blocks
						if (currentBlock == Block.doorIron.blockID)
						{
							setBlockDirectly(world, realX, realY, realZ, properties.DimensionalDoorID, fixedMetadata);
						}
						else if (currentBlock == Block.doorWood.blockID)
						{
							setBlockDirectly(world, realX, realY, realZ, properties.WarpDoorID, fixedMetadata);
						}
						else
						{							
							setBlockDirectly(world, realX, realY, realZ, currentBlock, fixedMetadata);
						}

						//Fill containers (e.g. chests, dispensers)
						if(Block.blocksList[currentBlock] instanceof BlockContainer)
						{
							/**
							TileEntity tile = world.getBlockTileEntity(i+xCooe, j+yCooe, k+zCooe);
							NBTTagCompound tag = this.tileEntityList[index];
							if(tag!=null)
							{
								tile.readFromNBT(tag);
							}
							 **/

							//Fill chests
							if (world.getBlockTileEntity(realX, realY, realZ) instanceof TileEntityChest)
							{
								TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity(realX, realY, realZ);
								ChestGenHooks info = DDLoot.DungeonChestInfo;
								WeightedRandomChestContent.generateChestContents(rand, info.getItems(rand), chest, info.getCount(rand));
							}

							//Fill dispensers
							if (world.getBlockTileEntity(realX, realY, realZ) instanceof TileEntityDispenser)
							{
								TileEntityDispenser dispenser = (TileEntityDispenser) world.getBlockTileEntity(realX, realY, realZ);
								dispenser.addItem(new ItemStack(Item.arrow, 64));
							}
						}
					}
				}
			}
		}
		
		//We'll use an extra block here to restrict the scope of these variables to just inside the block
		//This is to avoid declaration conflicts with the loops below
		{
			//Set the orientation of the rift exit
			Point3D entranceRiftLocation = schematicEntrance.clone();
			transformPoint(entranceRiftLocation, schematicEntrance, orientation - entryDirection, pocketCenter);
			LinkData sideLink = dimHelper.instance.getLinkDataFromCoords(
					entranceRiftLocation.getX(),
					entranceRiftLocation.getY(),
					entranceRiftLocation.getZ(),
					world);
			sideLink.linkOrientation = world.getBlockMetadata(
					entranceRiftLocation.getX(),
					entranceRiftLocation.getY() - 1,
					entranceRiftLocation.getZ());
			System.out.println("Metadata Orientation: " + sideLink.linkOrientation);
		}
		
		//Generate the LinkData defined by the door placement, Iron Dim doors first
		for(Point3D point : sideLinks)
		{
			int depth = dimHelper.instance.getDimDepth(originDimID) + 1;
			int forwardNoise = MathHelper.getRandomIntegerInRange(rand, -50 * depth, 150 * depth);
			int sidewaysNoise = MathHelper.getRandomIntegerInRange(rand, -10 * depth, 10 * depth);

			//Transform doorLocation to the pocket coordinate system
			Point3D doorLocation = point.clone();
			transformPoint(doorLocation, schematicEntrance, orientation - entryDirection, pocketCenter);
			int blockDirection = world.getBlockMetadata(doorLocation.getX(), doorLocation.getY() - 1, doorLocation.getZ());
			
			//Rotate the link destination noise to point in the same direction as the door exit
			//and add it to the door's location. Use EAST as the reference orientation since linkDestination
			//is constructed as if pointing East.
			Point3D linkDestination = new Point3D(forwardNoise, 0, sidewaysNoise);
			transformPoint(linkDestination, zeroPoint, blockDirection - EAST_DOOR_METADATA, doorLocation);
			
			//Create the link between our current door and its intended exit in destination pocket
			LinkData sideLink = new LinkData(destDimID, 0,
					doorLocation.getX(),
					doorLocation.getY(),
					doorLocation.getZ(),
					linkDestination.getX(),
					linkDestination.getY() + 1,
					linkDestination.getZ(),
					true, blockDirection);
			dimHelper.instance.createPocket(sideLink, true, true);
		}

		//Generate linkData for wooden dim doors leading to the overworld
		for(Point3D point : exitLinks)
		{
			try
			{
				//Transform doorLocation to the pocket coordinate system.
				Point3D doorLocation = point.clone();
				transformPoint(doorLocation, schematicEntrance, orientation - entryDirection, pocketCenter);
				int blockDirection = world.getBlockMetadata(doorLocation.getX(), doorLocation.getY() - 1, doorLocation.getZ());
				Point3D linkDestination = doorLocation.clone();
				
				LinkData randomLink = dimHelper.instance.getRandomLinkData(false);
				LinkData sideLink = new LinkData(destDimID,
						dimHelper.dimList.get(originDimID).exitDimLink.destDimID,
						doorLocation.getX(),
						doorLocation.getY(),
						doorLocation.getZ(),
						linkDestination.getX(),
						linkDestination.getY() + 1,
						linkDestination.getZ(),
						true, blockDirection);

				if (sideLink.destDimID == properties.LimboDimensionID)
				{
					sideLink.destDimID = 0;
				}
				else if ((rand.nextBoolean() && randomLink != null))
				{
					sideLink.destDimID = randomLink.locDimID;
				}
				sideLink.destYCoord = yCoordHelper.getFirstUncovered(sideLink.destDimID, linkDestination.getX(), 10, linkDestination.getZ());

				if (sideLink.destYCoord < 5)
				{
					sideLink.destYCoord = 70;
				}
				sideLink.linkOrientation = world.getBlockMetadata(linkDestination.getX(), linkDestination.getY() - 1, linkDestination.getZ());

				dimHelper.instance.createLink(sideLink);
				dimHelper.instance.createLink(sideLink.destDimID , 
						sideLink.locDimID, 
						sideLink.destXCoord, 
						sideLink.destYCoord, 
						sideLink.destZCoord, 
						sideLink.locXCoord, 
						sideLink.locYCoord, 
						sideLink.locZCoord, 
						dimHelper.instance.flipDoorMetadata(sideLink.linkOrientation));

				if (world.getBlockId(linkDestination.getX(),linkDestination.getY() -3,linkDestination.getZ()) == properties.FabricBlockID)
				{
					setBlockDirectly(world,linkDestination.getX(),linkDestination.getY()-2,linkDestination.getZ(),Block.stoneBrick.blockID,0);
				}
				else
				{
					setBlockDirectly(world,linkDestination.getX(),
							linkDestination.getY() - 2,
							linkDestination.getZ(),
							world.getBlockId(linkDestination.getX(),
							linkDestination.getY() -3,
							linkDestination.getZ()),
							world.getBlockMetadata(linkDestination.getX(),
							linkDestination.getY() -3,
							linkDestination.getZ()));
				}	
			}
			catch (Exception E)
			{
				E.printStackTrace();
			}
		}

		//Spawn monoliths
		for(Point3D point : monolithSpawns)
		{
			//Transform the frame block's location to the pocket coordinate system
			Point3D frameLocation = point.clone();
			transformPoint(frameLocation, schematicEntrance, orientation - entryDirection, pocketCenter);
			
			Entity mob = new MobObelisk(world);
			mob.setLocationAndAngles(frameLocation.getX(), frameLocation.getY(), frameLocation.getZ(), 1, 1); //TODO: Why not set the angles to 0? @.@ ~SenseiKiwi
			world.spawnEntityInWorld(mob);
		}
	}
	
	private void transformPoint(Point3D position, Point3D srcOrigin, int angle, Point3D destOrigin)
	{
		//This function receives a position (e.g. point in schematic space), translates it relative
		//to a source coordinate system (e.g. the point that will be the center of a schematic),
		//then rotates and translates it to obtain the corresponding point in a destination
		//coordinate system (e.g. the location of the entry rift in the pocket being generated).
		//The result is returned by overwriting the original position so that new object references
		//aren't needed. That way, repeated use of this function will not incur as much overhead.
		
		//Position is only overwritten at the end, so it's okay to provide it as srcOrigin or destOrigin as well.
		
		int tx = position.getX() - srcOrigin.getX();
		int ty = position.getY() - srcOrigin.getY();
		int tz = position.getZ() - srcOrigin.getZ();
		
		//"int angle" specifies a rotation consistent with Minecraft's orientation system.
		//That means each increment of 1 in angle would be a 90-degree clockwise turn.
		//Given a starting direction A and a destination direction B, the rotation would be
		//calculated by (B - A).

		//Adjust angle into the expected range
		if (angle < 0)
		{
			int correction = -(angle / 4);
			angle = angle + 4 * (correction + 1);
		}
		angle = angle % 4;
		
		//Rotations are considered in counterclockwise form because coordinate systems are
		//often assumed to be right-handed and convenient formulas are available for
		//common counterclockwise rotations.
		//Reference: http://en.wikipedia.org/wiki/Rotation_matrix#Common_rotations
		int rx;
		int rz;
		switch (angle)
		{
			case 0: //No rotation
				rx = tx;
				rz = tz;
				break;
			case 1: //90 degrees clockwise
				rx = -tz;
				rz = tx;
				break;
			case 2: //180 degrees
				rx = -tx;
				rz = -tz;
				break;
			case 3: //270 degrees clockwise
				rx = tz;
				rz = -tx;
				
				break;
			default: //This should never happen
				throw new IllegalStateException("Invalid angle value. This should never happen!");
		}
		
		position.setX( rx + destOrigin.getX() );
		position.setY( ty + destOrigin.getY() );
		position.setZ( rz + destOrigin.getZ() );
	}

	public void generateDungeonPocket(LinkData link)
	{
		String filePath=DungeonHelper.instance().defaultBreak.schematicPath;
		if(dimHelper.dimList.containsKey(link.destDimID))
		{
			if(dimHelper.dimList.get(link.destDimID).dungeonGenerator == null)
			{
				DungeonHelper.instance().generateDungeonLink(link);
			}
			filePath = dimHelper.dimList.get(link.destDimID).dungeonGenerator.schematicPath;	
		}
		this.generateSchematic(link.destXCoord, link.destYCoord, link.destZCoord, link.linkOrientation, link.destDimID, link.locDimID, filePath);
	}


	public void setBlockDirectly(World world, int x, int y, int z,int id, int metadata)
	{
		if (Block.blocksList[id] == null)
		{
			return;
		}

		int cX = x >> 4;
		int cZ = z >> 4;
		int cY = y >> 4;
		Chunk chunk;

		//TODO: This really seems odd to me. Like it's wrong. ~SenseiKiwi
		int chunkX=(x % 16)< 0 ? ((x) % 16)+16 : ((x) % 16);
		int chunkZ=((z) % 16)< 0 ? ((z) % 16)+16 : ((z) % 16);

		try
		{
			chunk = world.getChunkFromChunkCoords(cX, cZ);
			if (chunk.getBlockStorageArray()[cY] == null) 
			{
				chunk.getBlockStorageArray()[cY] = new ExtendedBlockStorage(cY << 4, !world.provider.hasNoSky);
			}


			chunk.getBlockStorageArray()[cY].setExtBlockID(chunkX, (y) & 15, chunkZ, id);
			chunk.getBlockStorageArray()[cY].setExtBlockMetadata(chunkX, (y) & 15, chunkZ, metadata);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}