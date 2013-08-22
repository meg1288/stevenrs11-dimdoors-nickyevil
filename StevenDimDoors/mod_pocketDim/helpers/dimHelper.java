package StevenDimDoors.mod_pocketDim.helpers;
/**
 * This class regulates all the operations involving the storage and manipulation of dimensions. It handles saving dim data, teleporting the player, and 
 * creating/registering new dimensions as well as loading old dimensions on startup
 * @Return
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet41EntityEffect;
import net.minecraft.network.packet.Packet43Experience;
import net.minecraft.network.packet.Packet9Respawn;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.DimensionManager;
import StevenDimDoors.mod_pocketDim.DDProperties;
import StevenDimDoors.mod_pocketDim.DimData;
import StevenDimDoors.mod_pocketDim.LinkData;
import StevenDimDoors.mod_pocketDim.ObjectSaveInputStream;
import StevenDimDoors.mod_pocketDim.PacketHandler;
import StevenDimDoors.mod_pocketDim.Point3D;
import StevenDimDoors.mod_pocketDim.SchematicLoader;
import StevenDimDoors.mod_pocketDim.TileEntityRift;
import StevenDimDoors.mod_pocketDim.mod_pocketDim;
import StevenDimDoors.mod_pocketDim.schematic.BlockRotator;
import StevenDimDoors.mod_pocketDim.world.LimboProvider;
import StevenDimDoors.mod_pocketDim.world.PocketProvider;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

public class dimHelper extends DimensionManager
{
	/**
	 * hashMap for the private pocket dimensions
	 */
	
	public static HashMap<String, DimData> privatePockets = new HashMap<String, DimData>();
	/**
	 * HashMap containing all the dims registered with DimDoors, sorted by dim ID. loaded on startup
	 * @Return
	 */
	public static HashMap<Integer, DimData> dimList=new HashMap<Integer, DimData>();
	public static boolean isSaving=false;
	
	/**
	 * ArrayList containing any blocks in limbo that have been placed by the player. Cycled through in the common tick manager
	 * @Return
	 */
	public static ArrayList<Point3D> blocksToDecay = new ArrayList<Point3D>();
	
	/**
	 * instance of the dimHelper
	 * @Return
	 */
	public static dimHelper instance = new dimHelper();
	
	/**
	 * HashMap for temporary storage of Link Signature damage hash values. See itemLinkSignature for more details
	 * @Return
	 */
	public HashMap<Integer, LinkData> interDimLinkList= new HashMap<Integer,LinkData>();
	
	/**
	 * ArrayList containing all link data not sorted for easy random access, used for random doors and for recreating rifts if they have a block placed over them. 
	 * See the common tick manager and the Chaos door for details on usage
	 * @Return
	 */
	//public ArrayList<LinkData> linksForRendering =new ArrayList<LinkData>();
	Random rand= new Random();
	
	public static final int DEFAULT_POCKET_SIZE = 39;
	public static final int DEFAULT_POCKET_WALL_THICKNESS = 5;
	public static final int MAX_WORLD_HEIGHT = 254;
	
	public int getDimDepth(int DimID)
	{
		if (dimList.containsKey(DimID))
		{
			return dimList.get(DimID).depth;
		}
		else return 1;
	}
	
	// GreyMaria: My god, what a mess. Here, let me clean it up a bit.
	public Entity teleportEntity(World world, Entity entity, LinkData link) //this beautiful teleport method is based off of xCompWiz's teleport function. 
	{	
		WorldServer oldWorld = (WorldServer)world;
		WorldServer newWorld;
		EntityPlayerMP player = (entity instanceof EntityPlayerMP) ? (EntityPlayerMP)entity : null;
		
		/*// SPECIAL CASE: Is our link null? If so, we've likely come from Limbo. Ensure this is the case.
		if(link == null)
		{
			if(world.provider.dimensionId == DDProperties.instance().LimboDimensionID)
			{
				link = new LinkData(0, 0, 0, 0);
				// Find destination point.
			}
		}*/
	    
		
 		// Is something riding? Handle it first.
		if(entity.riddenByEntity != null)
		{
			return this.teleportEntity(oldWorld,entity.riddenByEntity, link);
		}
		// Are we riding something? Dismount and tell the mount to go first.
		Entity cart = entity.ridingEntity;
		if (cart != null)
		{
			entity.mountEntity(null);
			cart = teleportEntity(oldWorld, cart, link);
			// We keep track of both so we can remount them on the other side.
		}
		
		// Destination doesn't exist? We need to make it.
		if(DimensionManager.getWorld(link.destDimID)==null)
		{
			DimensionManager.initDimension(link.destDimID);
		}
		
		// Determine if our destination's in another realm.
		boolean difDest = link.destDimID != link.locDimID;
		if(difDest)
		{
			newWorld = DimensionManager.getWorld(link.destDimID);
		}
		else
		{
			newWorld=(WorldServer)oldWorld;
		}
		
		// GreyMaria: What is this even accomplishing? We're doing the exact same thing at the end of this all.
		// TODO Check to see if this is actually vital.
		mod_pocketDim.teleporter.placeInPortal(entity, newWorld, link);

		if (difDest) // Are we moving our target to a new dimension?
    	{
    		if(player != null) // Are we working with a player?
    		{
    			// We need to do all this special stuff to move a player between dimensions.
    			
				// Set the new dimension and inform the client that it's moving to a new world.
	    		player.dimension = link.destDimID;
	    		player.playerNetServerHandler.sendPacketToPlayer(new Packet9Respawn(player.dimension, (byte)player.worldObj.difficultySetting, newWorld.getWorldInfo().getTerrainType(), newWorld.getHeight(), player.theItemInWorldManager.getGameType()));
	    	
	    		// GreyMaria: Used the safe player entity remover.
	    		// This should fix an apparently unreported bug where
	    		// the last non-sleeping player leaves the Overworld
	    		// for a pocket dimension, causing all sleeping players
	    		// to remain asleep instead of progressing to day.
	    		oldWorld.removePlayerEntityDangerously(player);
	    		player.isDead=false;
	    		
	    		// Creates sanity by ensuring that we're only known to exist where we're supposed to be known to exist.
	    		oldWorld.getPlayerManager().removePlayer(player);
	    		newWorld.getPlayerManager().addPlayer(player);
	    		
	    		player.theItemInWorldManager.setWorld((WorldServer)newWorld);
		    	
	    		// Synchronize with the server so the client knows what time it is and what it's holding.
		    	player.mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(player, (WorldServer)newWorld);
		    	player.mcServer.getConfigurationManager().syncPlayerInventory(player);
		    	for(Object potionEffect : player.getActivePotionEffects())
		    	{
		    		PotionEffect effect = (PotionEffect)potionEffect;
		    		player.playerNetServerHandler.sendPacketToPlayer(new Packet41EntityEffect(player.entityId, effect));
		    	}
		    	
		    	player.playerNetServerHandler.sendPacketToPlayer(new Packet43Experience(player.experience, player.experienceTotal, player.experienceLevel));
    		}  	
    		
    		// Creates sanity by removing the entity from its old location's chunk entity list, if applicable.
	        int entX = entity.chunkCoordX;
		    int entZ = entity.chunkCoordZ;
		    if ((entity.addedToChunk) && (oldWorld.getChunkProvider().chunkExists(entX, entZ)))
		    {
		    	oldWorld.getChunkFromChunkCoords(entX, entZ).removeEntity(entity);
		    	oldWorld.getChunkFromChunkCoords(entX, entZ).isModified = true;
		    }
		    // Memory concerns.
		    oldWorld.releaseEntitySkin(entity);
		    
	    	if (player == null) // Are we NOT working with a player?
	    	{
	    		NBTTagCompound entityNBT = new NBTTagCompound();
	    		entity.isDead = false;
	    		entity.addEntityID(entityNBT);
	    		entity.isDead = true;
	    		entity = EntityList.createEntityFromNBT(entityNBT, newWorld);
	    		
	    		if (entity == null)
	    		{   // TODO FIXME IMPLEMENT NULL CHECKS THAT ACTUALLY DO SOMETHING.
	    			/* 
	    			 * shit ourselves in an organized fashion, preferably
	    			 * in a neat pile instead of all over our users' games
	    			 */
	    		}
	    		
	    	}
	    	
	    	// Finally, respawn the entity in its new home.
	    	newWorld.spawnEntityInWorld(entity);
	    	entity.setWorld(newWorld);
	    }
	    entity.worldObj.updateEntityWithOptionalForce(entity, false);
	    
	    // Hey, remember me? It's time to remount.
		if (cart != null)
		{
			// Was there a player teleported? If there was, it's important that we update shit.
			if (player != null)
			{
				entity.worldObj.updateEntityWithOptionalForce(entity, true);	
			}
			entity.mountEntity(cart);
		}
		
		// Did we teleport a player? Load the chunk for them.
		if(player != null)
		{
			WorldServer.class.cast(newWorld).getChunkProvider().loadChunk(MathHelper.floor_double(entity.posX) >> 4, MathHelper.floor_double(entity.posZ) >> 4);
			
			// Tell Forge we're moving its players so everyone else knows.
			// Let's try doing this down here in case this is what's killing NEI.
			GameRegistry.onPlayerChangedDimension((EntityPlayer)entity);
			
		}
		mod_pocketDim.teleporter.placeInPortal(entity, newWorld, link);
		return entity;    
	}

	/**
	 * Primary function used to teleport the player using doors. Performs numerous null checks, and also generates the destination door/pocket if it has not done so already. 
	 * Also ensures correct orientation relative to the door using the pocketTeleporter.
	 * @param world- world the player is currently in
	 * @param linkData- the link the player is using to teleport, sends the player to its dest information. 
	 * @param player- the instance of the player to be teleported
	 * @param orientation- the orientation of the door used to teleport, determines player orientation and door placement on arrival
	 * @Return
	 */
	public void traverseDimDoor(World world,LinkData linkData, Entity entity)
	{
		DDProperties properties = DDProperties.instance();
		
		if (world.isRemote)
		{
			return;
		}
		if (linkData != null)
		{
			int destinationID=linkData.destDimID;
			
			if(dimHelper.dimList.containsKey(destinationID) && dimHelper.dimList.containsKey(world.provider.dimensionId))
			{
				this.generatePocket(linkData);
				
				if(mod_pocketDim.teleTimer==0||entity instanceof EntityPlayer)
				{
					mod_pocketDim.teleTimer=2+rand.nextInt(2);
				}
				else
				{
					return;
				}			
				if(!world.isRemote)
				{	
					entity = this.teleportEntity(world, entity, linkData);		
				}	
				entity.worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "mob.endermen.portal", 1.0F, 1.0F);
		
				int playerXCoord=MathHelper.floor_double(entity.posX);
				int playerYCoord=MathHelper.floor_double(entity.posY);
				int playerZCoord=MathHelper.floor_double(entity.posZ);
				
		    	if(!entity.worldObj.isBlockOpaqueCube(playerXCoord, playerYCoord-1,playerZCoord )&&dimHelper.instance.getDimData(linkData.locDimID).isDimRandomRift&&!linkData.hasGennedDoor)
		    	{						
		    		for(int count=0;count<20;count++)
		    		{
		    			if(!entity.worldObj.isAirBlock(playerXCoord, playerYCoord-2-count,playerZCoord))
						{
							if(Block.blocksList[entity.worldObj.getBlockId(playerXCoord, playerYCoord-2-count,playerZCoord)].blockMaterial.isLiquid())
					    	{
					    		entity.worldObj.setBlock(playerXCoord, playerYCoord-1, playerZCoord, properties.FabricBlockID);
					    		break;
					    	}
						}
		    			
		    			if(entity.worldObj.isBlockOpaqueCube(playerXCoord, playerYCoord-1-count,playerZCoord))
		    			{
		    				break;
		    			}
		    			if(count==19)
		    			{
		    				entity.worldObj.setBlock(playerXCoord, playerYCoord-1, playerZCoord, properties.FabricBlockID);
		    			}
		    		}	    																	
		    	}						
		    	
		    	this.generateDoor(world,linkData);
		
		    	
		    	if(!entity.worldObj.isAirBlock(playerXCoord,playerYCoord+1,playerZCoord))
		    	{
		    		if(Block.blocksList[entity.worldObj.getBlockId(playerXCoord,playerYCoord+1,playerZCoord)].isOpaqueCube() &&
		    				!mod_pocketDim.blockRift.isBlockImmune(entity.worldObj, playerXCoord+1,playerYCoord,playerZCoord))
		    		{
		    			entity.worldObj.setBlock(playerXCoord,playerYCoord+1,playerZCoord,0);
		    		}
		    	}
		    	if (!entity.worldObj.isAirBlock(playerXCoord,playerYCoord,playerZCoord))
		    	{
		    		if(Block.blocksList[entity.worldObj.getBlockId(playerXCoord,playerYCoord,playerZCoord)].isOpaqueCube() &&
		    				!mod_pocketDim.blockRift.isBlockImmune(entity.worldObj, playerXCoord,playerYCoord,playerZCoord))
		    		{
		    			entity.worldObj.setBlock(playerXCoord,playerYCoord,playerZCoord,0);
		    		}
		    	}
			}
		}
		return;
	}

	/**
	 * Creates a link at the location, pointing to the destination. Does NOT create a pair, so must be called twice.
	 * @param locationDimID
	 * @param destinationDimID
	 * @param locationXCoord
	 * @param locationYCoord
	 * @param locationZCoord
	 * @param destinationXCoord
	 * @param destinationYCoord
	 * @param destinationZCoord
	 
	 * @return
	 */
	public LinkData createLink( int locationDimID, int destinationDimID, int locationXCoord, int locationYCoord, int locationZCoord, int destinationXCoord, int destinationYCoord, int destinationZCoord)
	{
		if(this.getLinkDataFromCoords(locationXCoord, locationYCoord, locationZCoord, locationDimID)!=null)
		{
			return this.createLink(locationDimID, destinationDimID, locationXCoord, locationYCoord, locationZCoord, destinationXCoord, destinationYCoord, destinationZCoord, this.getLinkDataFromCoords(locationXCoord, locationYCoord, locationZCoord, locationDimID).linkOrientation);
		}
		else
		{
			return this.createLink(locationDimID, destinationDimID, locationXCoord, locationYCoord, locationZCoord, destinationXCoord, destinationYCoord, destinationZCoord, -10);
		}
	}

	
	/**
	 * Creates a link at the location, pointing to the destination. Does NOT create a pair, so must be called twice.
	 * @param locationDimID
	 * @param destinationDimID
	 * @param locationXCoord
	 * @param locationYCoord
	 * @param locationZCoord
	 * @param destinationXCoord
	 * @param destinationYCoord
	 * @param destinationZCoord
	 * @param linkOrientation
	 * @return
	 */
	public LinkData createLink( int locationDimID, int destinationDimID, int locationXCoord, int locationYCoord, int locationZCoord, int destinationXCoord, int destinationYCoord, int destinationZCoord,int linkOrientation)
	{
		LinkData linkData =new LinkData( locationDimID, destinationDimID, locationXCoord, locationYCoord, locationZCoord, destinationXCoord, destinationYCoord ,destinationZCoord,false,linkOrientation);
		return this.createLink(linkData);
	}	
	
	public LinkData createLink(LinkData link)
	{
		DDProperties properties = DDProperties.instance();
		
		if(!dimHelper.dimList.containsKey(link.locDimID))
		{
			DimData locationDimData= new DimData(link.locDimID, false, 0, link.locDimID,link.locXCoord,link.locYCoord,link.locZCoord);
			dimHelper.dimList.put(link.locDimID, locationDimData);
			link.isLocPocket=false;
		}
		if(!dimList.containsKey(link.destDimID))
		{
			dimHelper.dimList.put(link.destDimID, new DimData(link.destDimID, false, 0, link.locDimID,link.locXCoord,link.locYCoord,link.locZCoord));
		}
		DimData locationDimData=	dimHelper.instance.getDimData(link.locDimID);
		link.isLocPocket=locationDimData.isPocket;
		locationDimData.addLinkToDim(link);
		
		World world = dimHelper.getWorld(link.locDimID);
		if (world != null)
		{
			if (!mod_pocketDim.blockRift.isBlockImmune(world, link.locXCoord, link.locYCoord, link.locZCoord))
			{
				world.setBlock(link.locXCoord, link.locYCoord, link.locZCoord, properties.RiftBlockID);	
			}
		}
		//Notifies other players that a link has been created. 
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{		
			PacketHandler.onLinkCreatedPacket(link);
		}	
		return link;
	}
	
	public int getDestOrientation(LinkData link)
	{
		if(link !=null)
		{
			LinkData destLink = this.getLinkDataFromCoords(link.destXCoord, link.destYCoord, link.destZCoord, link.destDimID);
			if(destLink!=null)
			{
				return destLink.linkOrientation;
			}
			else
			{
				//System.out.println("Cant find destination link");
				return 0;
			}
		}
		else 
		{
		//	System.out.println("sending link is null");
			return 0;
		}
	}
	
	public void removeLink(LinkData link)
	{
		this.removeLink(link.locDimID, link.locXCoord, link.locYCoord, link.locZCoord);
	}
	
	/**
	 * properly deletes a link at the given coordinates. used by the rift remover. Also notifies clients of change. 
	 * @param locationDimID
	 * @param locationXCoord
	 * @param locationYCoord
	 * @param locationZCoord
	 */
	public void removeLink( int locationDimID, int locationXCoord, int locationYCoord, int locationZCoord)
	{
		if(!dimHelper.dimList.containsKey(locationDimID))
		{
			DimData locationDimData= new DimData(locationDimID, false, 0, locationDimID,locationXCoord,locationYCoord,locationZCoord);
			dimHelper.dimList.put(locationDimID, locationDimData);
		}
		LinkData link = this.getLinkDataFromCoords(locationXCoord, locationYCoord, locationZCoord, locationDimID);
		dimHelper.instance.getDimData(locationDimID).removeLinkAtCoords(link);
		//updates clients that a rift has been removed
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			PacketHandler.onLinkRemovedPacket(link);
			this.save();
		}	
	}
	public LinkData findNearestRift(World world, int x, int y, int z, int range)
	{
		return dimHelper.instance.getDimData(world).findNearestRift(world, range, x, y, z);
	}
	/**
	 * generates a door based on what door was used to teleport. Only funtions once per linking. 
	 * @param world- door 
	 * @param incLink
	 */
	public void generateDoor(World world,  LinkData incLink)
	{
		int locX = incLink.locXCoord;
		int locY = incLink.locYCoord;
		int locZ = incLink.locZCoord;
	
		int destX = incLink.destXCoord;
		int destY = incLink.destYCoord;
		int destZ = incLink.destZCoord;
		
		DDProperties properties = DDProperties.instance();
		
		if(!incLink.hasGennedDoor)
		{
			int destinationID = incLink.destDimID;

			int id =world.getBlockId(locX, locY, locZ);
			if(id==properties.WarpDoorID||id==properties.DimensionalDoorID||id==properties.TransientDoorID)
			{
				int doorTypeToPlace=id;
				if(DimensionManager.getWorld(destinationID)==null)
				{
					DimensionManager.initDimension(destinationID);
				}
				LinkData destLink =  this.getLinkDataFromCoords(destX, destY, destZ, destinationID);
				int destOrientation = 0;
				if(destLink!=null)
				{
					destOrientation = destLink.linkOrientation;
					destLink.hasGennedDoor=true;
				}
				int blockToReplace= DimensionManager.getWorld(destinationID).getBlockId(destX, destY, destZ);
				if(blockToReplace!=properties.DimensionalDoorID&&blockToReplace!=properties.WarpDoorID&&blockToReplace != properties.TransientDoorID)
				{
					DimensionManager.getWorld(destinationID).setBlock(destX, destY-1, destZ, doorTypeToPlace,destOrientation,2);
					DimensionManager.getWorld(destinationID).setBlock(destX, destY, destZ, doorTypeToPlace,8,2);
				}
				incLink.hasGennedDoor=true;
			}
		}
	}
	
	
		
	
	/**
	 * Generates the black pocket out of fabric of reality blocks. Placement of the pocket is based off of the orignial doors orientation. Kind of a clunky method, 
	 * but is necessary to maintain a one to one relationship with the overworld. Is called every teleport, but checks if the dim has been filled first and is a pocket .
	 * Also responsible for generation the random dungeons. 
	 * @param world- id of the world TO BE FILLED
	 * @param x 
	 * @param y
	 * @param z
	 * @param orientation
	 * @return
	 */
	public void generatePocket(LinkData incomingLink)
	{
		DDProperties properties = DDProperties.instance();
		try
		{
			if (DimensionManager.getWorld(incomingLink.destDimID) == null)
			{
				DimensionManager.initDimension(incomingLink.destDimID);
			}
			if (DimensionManager.getWorld(incomingLink.destDimID).provider == null)
			{
				DimensionManager.initDimension(incomingLink.destDimID);
			}
		}
		catch(Exception E)
		{
			E.printStackTrace();
			return;
		}
	//	World world = this.getWorld(incomingLink.destDimID);
		DimData data = dimHelper.instance.getDimData(incomingLink.destDimID);
		
		if(!data.hasBeenFilled&&data.isPocket&&!data.isDimRandomRift)
		{
			data.hasBeenFilled=true;
			//System.out.println("genning  pocket");
			int x = incomingLink.destXCoord;
			int y = incomingLink.destYCoord;
			int z = incomingLink.destZCoord;
			int orientation= (incomingLink.linkOrientation);
		
		int depth= this.getDimDepth(incomingLink.locDimID);
		//x=x*depth;
		//y=y*depth;
		//z=z*depth;
		y=y+13;
		
		
		if(orientation==0)
		{
			x=x+15;
			//this.getWorld(incomingLink.destDimID).provider.setSpawnPoint(x-1, y, z);
			
		}
		else if(orientation==1)
		{
			z=z+15;
			//this.getWorld(incomingLink.destDimID).provider.setSpawnPoint(x, y, z-1);

		}
		else if(orientation==2)
		{
			x=x-15;
			//this.getWorld(incomingLink.destDimID).provider.setSpawnPoint(x+1, y, z);

		}
		else if(orientation==3)
		{
			z=z-15;
			//this.getWorld(incomingLink.destDimID).provider.setSpawnPoint(x, y, z+1);

		}
			int searchRadius=19;
			 
			 if(!DimensionManager.getWorld(incomingLink.destDimID).isRemote)
			 {
				 int xCount=-searchRadius;
				 int yCount=-searchRadius;
				 int zCount=-searchRadius;
			 
				 while (xCount<=searchRadius)
				 {
					 while(yCount<=searchRadius)
					 {
						 while(zCount<=searchRadius)
						 {
							 if((Math.abs(xCount)>=15||Math.abs(yCount)>=15||Math.abs(zCount)>=15)&&DimensionManager.getWorld(incomingLink.destDimID).isAirBlock( x+xCount,  y+yCount,  z+zCount)&&((yCount+y)>0))
							 {
								 if(Math.abs(xCount)>=19||Math.abs(yCount)>=19||Math.abs(zCount)>=19)
									 {
									 	dimHelper.setBlockDirectly(DimensionManager.getWorld(incomingLink.destDimID), x+xCount,  y+yCount,  z+zCount,properties.PermaFabricBlockID,0);
									 }
								 else
								 {
									dimHelper.setBlockDirectly(DimensionManager.getWorld(incomingLink.destDimID), x+xCount,  y+yCount,  z+zCount,properties.FabricBlockID,0);
								 	if(properties.TNFREAKINGT_Enabled)
								 	{
								 		if((Math.abs(xCount)>=16||Math.abs(yCount)>=16||Math.abs(zCount)>=16) && rand.nextInt(properties.NonTntWeight + 1) == 0)
								 		{
								 			DimensionManager.getWorld(incomingLink.destDimID).setBlock( x+xCount,  y+yCount,  z+zCount,Block.tnt.blockID);
								 		}
								 	}
								 }
							 }
							 zCount++;
						 }
						 zCount=-searchRadius;
						 yCount++;
					 }
					 yCount=-searchRadius;
					 xCount++;
				 }
			 }
		}
		else if (!data.hasBeenFilled && data.isPocket && data.isDimRandomRift)
		{
			SchematicLoader.generateDungeonPocket(incomingLink, properties);
			data.hasBeenFilled=true;
		}
	}

	/**
	 * simple method called on startup to register all dims saved in the dim list. Only tries to register pocket dims, though. Also calls load()
	 * @return
	 */
	public void initPockets()
	{
		DDProperties properties = DDProperties.instance();
		mod_pocketDim.hasInitDims=true;
		this.load();
		if(!dimHelper.dimList.isEmpty())
		{
			Set<Integer> allDimIds=dimList.keySet();
			//FIXME: ...Wat. Why aren't we using a foreach loop here instead of manipulating an explicit iterator? ;-; ~SenseiKiwi
			Iterator<Integer> itr = allDimIds.iterator();
			while(itr.hasNext())
			{
				DimData dimData = (DimData) dimList.get(itr.next());
				if(dimData.isPocket)
				{
					try
					{	
						DimensionManager.getNextFreeDimId();
						registerDimension(dimData.dimID,properties.PocketProviderID);
					}
					catch (Exception e)
					{
						if(dimData.isPocket)
						{
							System.out.println("Warning- could not register dim "+dimData.depth+" . Probably caused by a version update/save data corruption/other mods. ");
						}
						else
						{
							e.printStackTrace();
						}
					}
				}							
			}			
		}
	}
	
	public boolean resetPocket(DimData dimData)
	{
		if (!dimData.isPocket || getWorld(dimData.dimID) != null)
		{
			return false;
		}
		File save = new File(getCurrentSaveRootDirectory() + "/DimensionalDoors/pocketDimID" + dimData.dimID);
		DeleteFolder.deleteFolder(save);
		dimData.hasBeenFilled = false;
		dimData.hasDoor = false;
		for(LinkData link : dimData.getLinksInDim())
		{
			link.hasGennedDoor = false;
			LinkData linkOut = this.getLinkDataFromCoords(link.destXCoord, link.destYCoord, link.destZCoord, link.destDimID);
			if (linkOut != null)
			{
				linkOut.hasGennedDoor = false;
			}
		}
		return true;
	}
	
	public boolean pruneDimension(DimData dimData, boolean deleteFolder)
	{
		
		//TODO: All the logic for checking that this is an isolated pocket should be moved in here.
		if (!dimData.isPocket || getWorld(dimData.dimID) != null)//Checks to see if the pocket is loaded or isnt actually a pocket. 
		{
			return false;
		}
		dimList.remove(dimData.dimID);
		if (deleteFolder)
		{
			File save = new File(getCurrentSaveRootDirectory() + "/DimensionalDoors/pocketDimID" + dimData.dimID);
			DeleteFolder.deleteFolder(save);
		}
		return true;
	}
	
	/**
	 * method called when the client disconnects/server stops to unregister dims. 
	 * @Return
	 */
	public void unregsisterDims()
	{
		
		if(!dimHelper.dimList.isEmpty())
		{
			Set allDimIds=dimList.keySet();
			Iterator itr =allDimIds.iterator();
			while(itr.hasNext())
			{
				DimData dimData = (DimData) dimList.get(itr.next());
				if(dimData.isPocket)
				{
					try
					{		
						DimensionManager.unregisterDimension(dimData.dimID);
					}
					catch(Exception e)
					{
						System.out.println("Dim-"+String.valueOf(dimData.dimID)+"is already unregistered, ok? Enough with it already.");
					}
					//	initDimension(dimData.dimID);
				}	
			}	
		}
	}
	
	/**
	 * Used to associate a damage value on a Rift Signature with a link pair. See LinkSignature for details. 
	 * @return
	 */
	public int createUniqueInterDimLinkKey()
	{
		int linkKey;
		Random rand= new Random();
		do
		{
			linkKey=rand.nextInt(30000);
		}
		while(this.interDimLinkList.containsKey(linkKey));
		return linkKey;
	}
	
	/**
	 * Method used to create and register a new pocket dimension. Called on door placement and rift generation. It does NOT actually generate the structure of the dim, just
	 * registers it with the dimension manager and adds the necessary links and dim info to the dimlist/linklists. 
	 * Also registers existing dims with the dimList, so link data can be stored for them. 
	 * 
	 * Handles the randomization associated with depth as well, going far enough causes the next dims exit link to be randomized. 
	 * 
	 * @param world- World currently occupied, the parent of the pocket dim to be created. 
	 * @param x 
	 * @param y
	 * @param z
	 * @param isGoingDown
	 * @param isRandomRift
	 * @param orientation- determines the orientation of the entrance link to this dim. Should be the metaData of the door occupying the rift. -1 if no door. 
	 * @return
	 */
	public LinkData createPocket(LinkData link , boolean isGoingDown, boolean isRandomRift)
	{
		DDProperties properties = DDProperties.instance();
		if(dimHelper.getWorld(0)==null)
		{
			return link;
		}
		if (dimHelper.getWorld(link.locDimID) == null)
		{
			dimHelper.initDimension(link.locDimID);
		}
		int dimensionID;
		int depth = this.getDimDepth(link.locDimID);
		dimensionID = getNextFreeDimId();
		registerDimension(dimensionID, properties.PocketProviderID);
		DimData locationDimData;
		DimData destDimData;
		
	
		
		if(dimHelper.dimList.containsKey(link.locDimID)&&!DimensionManager.getWorld(link.locDimID).isRemote) //checks to see if dim is already registered. If not, it creates a DimData entry for it later
		{
			//randomizes exit if deep enough
			locationDimData= dimList.get(DimensionManager.getWorld(link.locDimID).provider.dimensionId);
		
			if(depth>5)
			{
				if(depth>=12)
				{
					depth=11;
				}
				if(rand.nextInt(13-depth)==0)
				{
					LinkData link1=getRandomLinkData(false);		
				}
			}
			if(locationDimData.isPocket) //determines the qualites of the pocket dim being created, based on parent dim. 
			{
				if(isGoingDown)
				{
					destDimData= new DimData(dimensionID, true, locationDimData.depth+1, locationDimData.exitDimLink);
				}
				else
				{
					destDimData= new DimData(dimensionID, true, locationDimData.depth-1, locationDimData.exitDimLink);
				}
			}
			else
			{
				destDimData= new DimData(dimensionID, true, 1, link.locDimID,link.locXCoord,link.locYCoord,link.locZCoord);
			}
			
		}
		else
		{
			locationDimData= new DimData(link.locDimID, false, 0, link.locDimID,link.locXCoord,link.locYCoord,link.locZCoord);
			destDimData= new DimData(dimensionID, true, 1, link.locDimID,link.locXCoord,link.locYCoord,link.locZCoord);
		}
		destDimData.isDimRandomRift=isRandomRift;
		dimHelper.dimList.put(DimensionManager.getWorld(link.locDimID).provider.dimensionId, locationDimData);
		dimHelper.dimList.put(dimensionID, destDimData);
		
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)//sends packet to clients notifying them that a new dim has been created. 
		{
			PacketHandler.onDimCreatedPacket(destDimData);
		}
		link = this.createLink(DimensionManager.getWorld(link.locDimID).provider.dimensionId,dimensionID,link.locXCoord,link.locYCoord,link.locZCoord, link.destXCoord,constrainPocketY(link.destYCoord),link.destZCoord,link.linkOrientation); //creates and registers the two rifts that link the parent and pocket dim. 
		this.createLink(dimensionID,DimensionManager.getWorld(link.locDimID).provider.dimensionId, link.destXCoord,constrainPocketY(link.destYCoord),link.destZCoord, link.locXCoord,link.locYCoord,link.locZCoord, BlockRotator.transformMetadata(link.linkOrientation, 2, Block.doorWood.blockID));
		return link;
	}
	
	public static int constrainPocketY(int entranceDoorYPos)
	{
		
		if(entranceDoorYPos+DEFAULT_POCKET_SIZE-DEFAULT_POCKET_WALL_THICKNESS>= MAX_WORLD_HEIGHT)
		{
			return entranceDoorYPos-DEFAULT_POCKET_SIZE+DEFAULT_POCKET_WALL_THICKNESS;
		}
		if(entranceDoorYPos-1-DEFAULT_POCKET_WALL_THICKNESS<=0)
		{
			return entranceDoorYPos+DEFAULT_POCKET_WALL_THICKNESS;

		}
		else return entranceDoorYPos;
	
	}
	/**
	 * function that saves all dim data in a hashMap. Calling too often can cause Concurrent modification exceptions, so be careful.
	 * @return
	 */
	//TODO change from saving serialized objects to just saving data for compatabilies sake. 
	//TODO If saving is multithreaded as the concurrent modification exception implies, you should be synchronizing access. ~SenseiKiwi
	public void save() 
	{
		if(dimHelper.isSaving) return;
		World world = DimensionManager.getWorld(0);
		if(world==null || world.isRemote) return;
		if(DimensionManager.getCurrentSaveRootDirectory()!=null)
		{
			//System.out.println("saving");

			dimHelper.isSaving=true;
			HashMap comboSave=new HashMap();
			comboSave.put("dimList", dimHelper.dimList);
			comboSave.put("interDimLinkList", this.interDimLinkList);
			comboSave.put("blocksToDecay", dimHelper.blocksToDecay);


			
			FileOutputStream saveFile = null;
			try
			{
				//World world=FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0];
				String saveFileName=DimensionManager.getCurrentSaveRootDirectory()+"/DimensionalDoorsDataTEMP";
				saveFile = new FileOutputStream(saveFileName);
		         
		         ObjectOutputStream save = new ObjectOutputStream(saveFile);
		         save.writeObject(comboSave);
		         save.close();
		         saveFile.close();
		         
		         if(new File(DimensionManager.getCurrentSaveRootDirectory()+"/DimensionalDoorsDataOLD").exists())
		         {
		        	 new File(DimensionManager.getCurrentSaveRootDirectory()+"/DimensionalDoorsDataOLD").delete(); 
		         }
		         new File(DimensionManager.getCurrentSaveRootDirectory()+"/DimensionalDoorsData").renameTo(new File(DimensionManager.getCurrentSaveRootDirectory()+"/DimensionalDoorsDataOLD"));
		         
		        new File(saveFileName).renameTo( new File(DimensionManager.getCurrentSaveRootDirectory()+"/DimensionalDoorsData"));
			}
			catch(Exception e)
			{
				 e.printStackTrace();
				System.out.println("Could not save data-- SEVERE");
			}
			
		         
			
			
			dimHelper.isSaving=false;
		}
	}
	
	/**
	 * loads the dim data from the saved hashMap. Also handles compatabilty with old saves, see OldSaveHandler
	 * @return
	 */
	//TODO change to loading vars instead of objects
	@SuppressWarnings("unchecked")
	public void load()
	{
		boolean firstRun=false;
		System.out.println("Loading DimDoors data");
		FileInputStream saveFile = null;
	
		if(!DimensionManager.getWorld(0).isRemote&&DimensionManager.getCurrentSaveRootDirectory()!=null)
		{
	
		try
		{
			File dataStore = new File( DimensionManager.getCurrentSaveRootDirectory()+"/DimensionalDoorsData");
			
			if(!dataStore.exists())
			{
				
				
				if(!new File( DimensionManager.getCurrentSaveRootDirectory()+"/DimensionalDoorsDataOLD").exists())
				{
					firstRun=true;
				}
			}
	      
	        
	      
	         
	         
	         saveFile = new FileInputStream(dataStore);
	         ObjectSaveInputStream save = new ObjectSaveInputStream(saveFile);
	         HashMap comboSave =((HashMap) save.readObject());
	         
	         try
	         {
	        	 this.interDimLinkList = (HashMap<Integer, LinkData>) comboSave.get("interDimLinkList");
	         }
	         catch(Exception e)
	         {
	        	 System.out.println("Could not load Link Signature list. Link Sig items will loose restored locations.");
	         }
	         
	         try
	         {
	        	 dimHelper.dimList = (HashMap<Integer, DimData>) comboSave.get("dimList");
	         } 
	         catch(Exception e)
	         {
	        	 System.out.println("Could not load pocket dim list. Saves probably lost, but repairable. Move the files from indivual pocket dim files to active ones. See MC thread for details.");
	         }
	         
	        
	         
	         try
	         {
	        	 dimHelper.blocksToDecay= (ArrayList<Point3D>) comboSave.get("blocksToDecay");
	         }
	         catch(Exception e)
	         {
	        	 System.out.println("Could not load list of blocks to decay in Limbo. Probably because you updated versions, in which case this is normal. ");

	         }
	         save.close();
	         saveFile.close();
	        

  
		}
		catch(Exception e4)
		{
			try
			{
				if(!firstRun)
				{
					System.out.println("Save data damaged, trying backup...");
				}
				World world=FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0];
				File dataStore =new File( world.getSaveHandler().getMapFileFromName("idcounts").getParentFile().getParent()+"/DimensionalDoorsDataOLD");
		      

		         saveFile = new FileInputStream(dataStore);
		         ObjectSaveInputStream save = new ObjectSaveInputStream(saveFile);
		         HashMap comboSave =((HashMap)save.readObject());
		         
		         try
		         {
		        	 this.interDimLinkList=(HashMap<Integer, LinkData>) comboSave.get("interDimLinkList");
		         }
		         catch(Exception e)
		         {
		        	 System.out.println("Could not load Link Signature list. Link Sig items will loose restored locations.");
		         }
		         
		         try
		         {
		        	 dimHelper.dimList=(HashMap<Integer, DimData>) comboSave.get("dimList");
		         } 
		         catch(Exception e)
		         {
		        	 System.out.println("Could not load pocket dim list. Saves probably lost, but repairable. Move the files from indivual pocket dim files to active ones. See MC thread for details.");
		         }
		         
		        
		         
		         try
		         {
		        	 dimHelper.blocksToDecay=(ArrayList<Point3D>) comboSave.get("blocksToDecay");
		         }
		         catch(Exception e)
		         {
		        	 System.out.println("Could not load list of blocks to decay in Limbo. Probably because you updated versions, in which case this is normal. ");

		         }
		         save.close();
		         saveFile.close();
		      
			}
			catch(Exception e2)			
			{
				if(!firstRun)
				{
					e2.printStackTrace();
					System.out.println("Could not read data-- SEVERE");
				}
				
				
				
			
			}
			
		
			
		}
		}
		
	}
	
	
		
	public LinkData getRandomLinkData(boolean allowInPocket)
	{
		boolean foundRandomDest=false;
		int i=0;
		int size = dimHelper.dimList.size();
  
		while (!foundRandomDest&&size>0&&i<100)
		{
			i++;
			DimData dimData;
			ArrayList<LinkData> linksInDim = new ArrayList<LinkData>();
			for(size--;size>0;)
			{
				dimData = dimHelper.instance.getDimData((Integer)dimList.keySet().toArray()[rand.nextInt(dimList.keySet().size())]);
				if(dimData==null)
				{
					break;
				}
				linksInDim = dimData.getLinksInDim();
				if(!linksInDim.isEmpty())
				{
					break;
				}
			}
			
			if(linksInDim.isEmpty())
			{
				break;
			}
			
			LinkData link1 = (LinkData) linksInDim.get(rand.nextInt(linksInDim.size()));
			
			if(link1!=null)
			{
			
				if(!link1.isLocPocket||allowInPocket)
				{
					foundRandomDest=true;
					return link1;					 			
				}
			}
		}
		
			return null;
		
	}
	
	/**
	 * Gets a link at the destination of the link provided. Returns null if none exists. 
	 * @param link
	 * @return
	 */
	public LinkData getLinkDataAtDestination(LinkData link)
	{
		return this.getLinkDataFromCoords(link.destXCoord, link.destYCoord, link.destZCoord, link.destDimID);
	}
	/**
	 * Moves the location of the link passed in to the provided coords. Does not change the links destination coords.
	 * The boolean flag determines whether or not to update other links that point to this link- true causes all links that pointed to this link to be updated
	 * to point to the new link location.
	 * 
	 * Return true if there was an actual link to be moved.
	 * 
	 * @param link
	 * @param x
	 * @param y
	 * @param z
	 * @param dimID
	 * @param updateLinksPointingHere
	 * @return
	 */
	public boolean moveLinkDataLocation(LinkData link, int x,int y, int z, int dimID, boolean updateLinksPointingHere)
	{
		LinkData linkToMove = this.getLinkDataFromCoords(link.locXCoord, link.locYCoord, link.locZCoord, link.locDimID);
		if(linkToMove!=null)
		{
			int oldX = linkToMove.locXCoord;
			int oldY = linkToMove.locYCoord;
			int oldZ = linkToMove.locZCoord;
			int oldDimID = linkToMove.locDimID;
			
			if(updateLinksPointingHere)
			{
				ArrayList<LinkData> incomingLinks = new ArrayList<LinkData>();
				for(DimData dimData : dimHelper.dimList.values())
				{
					for(LinkData allLink : dimData.getLinksInDim())
					{
						if(this.getLinkDataAtDestination(allLink)==linkToMove)
						{
							this.moveLinkDataDestination(allLink, x, y, z, dimID, false);							
						}
					}
				}
			}
			linkToMove.locDimID=dimID;
			linkToMove.locXCoord=x;
			linkToMove.locYCoord=y;
			linkToMove.locZCoord=z;
			if(this.getLinkDataFromCoords(oldX,oldY,oldZ,oldDimID)!=null)
			{
			//	this.removeLink(this.getLinkDataFromCoords(oldX,oldY,oldZ,oldDimID));
			}
			this.createLink(linkToMove);
			LinkData linkTest = dimHelper.instance.getLinkDataFromCoords(x, y, z, dimID);
			linkTest.printLinkData();
			return true;
		}
		return false;
	}
	/**
	 * Changes the destination coords of the link passed in if it exists to the provided coords. 
	 * @param link
	 * @param x
	 * @param y
	 * @param z
	 * @param dimID
	 * @param updateLinksAtDestination
	 * @return
	 */
	public boolean moveLinkDataDestination(LinkData link, int x, int y, int z, int dimID, boolean updateLinksAtDestination)
	{
		LinkData linkToMove = this.getLinkDataFromCoords(link.locXCoord, link.locYCoord, link.locZCoord, link.locDimID);
		if(linkToMove!=null)
		{
			if(updateLinksAtDestination)
			{
				LinkData linkAtDestination = this.getLinkDataAtDestination(linkToMove);
				if(linkAtDestination!=null)
				{
					this.moveLinkDataLocation(linkAtDestination, x, y, z, dimID, false);
				}
			}
			linkToMove.destDimID=dimID;
			linkToMove.destXCoord=x;
			linkToMove.destYCoord=y;
			linkToMove.destZCoord=z;
			this.createLink(linkToMove);
			LinkData testLink = this.getLinkDataFromCoords(link.locXCoord, link.locYCoord, link.locZCoord, link.locDimID);

			return true;
		}
		return false;
	}
		
	/**
	 * gets a link based on coords and a world object
	 * @param x
	 * @param y
	 * @param z
	 * @param par1World
	 * @return
	 */
	public LinkData getLinkDataFromCoords(int x, int y, int z, World par1World) 
	{
		return this.getLinkDataFromCoords(x, y, z, par1World.provider.dimensionId);
	}
	/**
	 * gets a link based on coords and a world ID
	 * @param x
	 * @param y
	 * @param z
	 * @param worldID
	 * @return
	 */
	public LinkData getLinkDataFromCoords(int x, int y, int z, int worldID) 
	{
		if(dimHelper.dimList.containsKey(worldID))
		{
			DimData dimData=dimHelper.instance.getDimData(worldID);
			
			return dimData.findLinkAtCoords(x, y, z);
			
		}
		 
		return null;
	}
	/**
	 * function called by rift tile entities and the rift remover to find and spread between rifts. Does not actually de-register the rift data, see deleteRift for that. 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param range
	 * @param player
	 * @param item
	 * @return
	 */
	public static boolean removeRift(World world, int x, int y, int z, int range, EntityPlayer player, ItemStack item)
    {
    	DDProperties properties = DDProperties.instance();
    	
    	LinkData nearest=null;
		float distance=range+1;
		int i=-range;
		int j=-range;
		int k=-range;
		
		while (i<range)
		{
			while (j<range)
			{
				while (k<range)
				{
					if(world.getBlockId(x+i, y+j, z+k)==properties.RiftBlockID&&MathHelper.abs(i)+MathHelper.abs(j)+MathHelper.abs(k)<distance)
					{
						if(MathHelper.abs(i)+MathHelper.abs(j)+MathHelper.abs(k)!=0||range==1)
						{
							nearest=dimHelper.instance.getLinkDataFromCoords(x+i, y+j, z+k,world.provider.dimensionId);
							distance=MathHelper.abs(i)+MathHelper.abs(j)+MathHelper.abs(k);
						}
						
					}
					k++;
				}
				k=-range;
				j++;
				
			}
			j=-range;
			i++;		
			
		}
		
		
		if(nearest!=null)
		{
			if(world.getBlockTileEntity(nearest.locXCoord,nearest.locYCoord, nearest.locZCoord)!=null)
			{
				TileEntity tile = world.getBlockTileEntity(nearest.locXCoord,nearest.locYCoord, nearest.locZCoord);
				TileEntityRift tilerift = (TileEntityRift) tile;
				tilerift.shouldClose=true;
				//System.out.println("erasing rift");
				item.damageItem(1, player);
				return true;
			}

		
		}
		return false;
		
    }
				
	public static void setBlockDirectly(World world, int x, int y, int z,int id, int metadata)
	{
		int cX=x >>4;
		int cZ=z >>4;
		int cY=y >>4;
		Chunk chunk;
		int chunkX=(x % 16)< 0 ? ((x) % 16)+16 : ((x) % 16);
		int chunkY=y;
		int chunkZ=((z) % 16)< 0 ? ((z) % 16)+16 : ((z) % 16);
		
		
		//	this.chunk=new EmptyChunk(world,cX, cZ);
		try
		{
			chunk=world.getChunkFromChunkCoords(cX, cZ);
			 if (chunk.getBlockStorageArray()[cY] == null) {
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
	
	public void addDimData(DimData dimData)
	{
		dimHelper.dimList.put(dimData.dimID, dimData);
	}
	
	public void createDimData(World world)
	{
		dimHelper.dimList.put(world.provider.dimensionId, new DimData(world.provider.dimensionId, false, 0,0,world.provider.getSpawnPoint().posX,world.provider.getSpawnPoint().posY,world.provider.getSpawnPoint().posZ));
	}
	
	public DimData getDimData(World world)
	{	
		return dimHelper.instance.getDimData(world.provider.dimensionId);
	}
	
	public DimData getDimData(int dimID)
	{
		return dimHelper.dimList.get(dimID);
	}
}
