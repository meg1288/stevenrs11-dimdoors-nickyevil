package StevenDimDoors.mod_pocketDim.ticking;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import StevenDimDoors.mod_pocketDim.DDProperties;
import StevenDimDoors.mod_pocketDim.mod_pocketDim;
import StevenDimDoors.mod_pocketDim.core.IDimLink;
import StevenDimDoors.mod_pocketDim.core.NewDimData;
import StevenDimDoors.mod_pocketDim.core.PocketManager;
import StevenDimDoors.mod_pocketDim.tileentities.TileEntityRift;
import StevenDimDoors.mod_pocketDim.util.Point4D;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class RiftRegenerator implements IRegularTickReceiver {
	
	private static final int RIFT_REGENERATION_INTERVAL = 200; //Regenerate random rifts every 200 ticks
	private static final int RIFTS_REGENERATED_PER_DIMENSION = 5;
	
	private DDProperties properties;
	
	public RiftRegenerator(IRegularTickSender sender, DDProperties properties)
	{
		sender.registerForTicking(this, RIFT_REGENERATION_INTERVAL, false);
		this.properties = properties;
	}
	
	@Override
	public void notifyTick()
	{
		regenerateRiftsInAllWorlds();
	}
	
	public static void regenerateRiftsInAllWorlds()
	{
		//Regenerate rifts that have been replaced (not permanently removed) by players
		DDProperties properties = DDProperties.instance();
		
		for (NewDimData dimension : PocketManager.getDimensions())
    	{
			if (dimension.linkCount() > 0)
			{
	    		World world = DimensionManager.getWorld(dimension.id());
	    		
	    		if (world != null)
	    		{
	    			for (int count = 0; count < RIFTS_REGENERATED_PER_DIMENSION; count++)
	    			{
	    				IDimLink link = dimension.getRandomLink();
	    				Point4D source = link.source();
	    				if (!mod_pocketDim.blockRift.isBlockImmune(world, source.getX(), source.getY(), source.getZ()))
	    				{
	    					world.setBlock(source.getX(), source.getY(), source.getZ(), properties.RiftBlockID);
	    				}
	    			}
	    		}
			}
    	}
	}
}
