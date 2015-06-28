package StevenDimDoors.mod_pocketDim.schematic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import StevenDimDoors.mod_pocketDim.Point3D;

/**
 * Represents an MC schematic and provides functions for loading, storing, and manipulating schematics.
 * This functionality has no dependencies to Dimensional Doors.
 */
public class Schematic {

	protected short width;
	protected short height;
	protected short length;

	protected Block[] blocks;
	protected byte[] metadata;
	protected NBTTagList tileEntities;

	protected Schematic(short width, short height, short length, String[] blockPalette, short[] blockIds, byte[] metadata, NBTTagList tileEntities)
	{
		this.width = width;
		this.height = height;
		this.length = length;
		this.metadata = metadata;
		this.tileEntities = tileEntities;

        if (blockPalette != null)
            loadBlockList(blockPalette, blockIds);
	}

    protected Schematic(short width, short height, short length, Block[] blocks, byte[] metadata, NBTTagList tileEntities) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.blocks = blocks;
        this.metadata = metadata;
        this.tileEntities = tileEntities;
    }
	
	protected Schematic(Schematic source)
	{
		//Shallow copy constructor - critical for code reuse in derived classes since
		//source's fields will be inaccessible if the derived class is in another package.
		this.width = source.width;
		this.height = source.height;
		this.length = source.length;
		this.blocks = source.blocks;
		this.metadata = source.metadata;
		this.tileEntities = source.tileEntities;
	}

    private void loadBlockList(String[] blockPalette, short[] blockIds) {
        this.blocks = new Block[blockIds.length];

        Block[] blockObjPalette = new Block[blockPalette.length];

        for (int i = 0; i < blockPalette.length; i++) {
            blockObjPalette[i] = (Block)Block.blockRegistry.getObject(blockPalette[i]);
        }

        for (int i = 0; i < blockIds.length; i++) {
            this.blocks[i] = blockObjPalette[blockIds[i]];
        }
    }

	public int calculateIndex(int x, int y, int z)
	{
		if (x < 0 || x >= width)
			throw new IndexOutOfBoundsException("x must be non-negative and less than width");
		if (y < 0 || y >= height)
			throw new IndexOutOfBoundsException("y must be non-negative and less than height");
		if (z < 0 || z >= length)
			throw new IndexOutOfBoundsException("z must be non-negative and less than length");

		return (y * width * length + z * width + x);
	}
	
	public Point3D calculatePoint(int index)
	{
		int y = index / (width * length);
		index -= y * width * length;
		int z = index / width;
		index -= z * width;
		int x = index;
		
		return new Point3D(x, y, z);
	}
	
	public int calculateIndexBelow(int index)
	{
		return index - (width * length);
	}

	public short getWidth()
	{
		return width;
	}
	
	public short getHeight()
	{
		return height;
	}
	
	public short getLength()
	{
		return length;
	}
	
	public Block getBlock(int x, int y, int z)
	{
		return (Block)Block.blockRegistry.getObject(blocks[calculateIndex(x, y, z)]);
	}

	public byte getBlockMetadata(int x, int y, int z)
	{
		return metadata[calculateIndex(x, y, z)];
	}

	public NBTTagList getTileEntities()
	{
		return (NBTTagList) tileEntities.copy();
	}

	public static Schematic readFromFile(String schematicPath) throws FileNotFoundException, InvalidSchematicException
	{
		return readFromFile(new File(schematicPath));
	}

	public static Schematic readFromFile(File schematicFile) throws FileNotFoundException, InvalidSchematicException
	{
		// There is no resource leak... readFromStream() closes the stream TWICE.
		return readFromStream(new FileInputStream(schematicFile));
	}

	public static Schematic readFromResource(String resourcePath) throws InvalidSchematicException
	{
		InputStream schematicStream = Schematic.class.getResourceAsStream(resourcePath);
		return readFromStream(schematicStream);
	}

	public static Schematic readFromStream(InputStream schematicStream) throws InvalidSchematicException
	{
		short width;
		short height;
		short length;
		int volume;
		int pairs;

		byte[] metadata = null;			//block metadata
		byte[] lowBits = null;			//first 8 bits of the block IDs
		byte[] highBits = null;			//additional 4 bits of the block IDs
		short[] blockIds = null;			//list of combined block IDs
        String[] blockPalette = null;
		NBTTagList tileEntities = null;	//storage for tile entities in NBT form
		NBTTagCompound schematicTag;	//the NBT data extracted from the schematic file
		boolean hasExtendedBlockIDs;	//indicates whether the schematic contains extended block IDs

		try
		{
			try
			{
				schematicTag = CompressedStreamTools.readCompressed(schematicStream);
				schematicStream.close(); //readCompressed() probably closes the stream anyway, but close again to be sure.
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				throw new InvalidSchematicException("The schematic could not be decoded.");
				
			}

			//load size of schematic to generate
			width = schematicTag.getShort("Width");
			height = schematicTag.getShort("Height");
			length = schematicTag.getShort("Length");
			volume = width * length * height;

			if (width < 0)
				throw new InvalidSchematicException("The schematic cannot have a negative width.");
			if (height < 0)
				throw new InvalidSchematicException("The schematic cannot have a negative height.");
			if (length < 0)
				throw new InvalidSchematicException("The schematic cannot have a negative length.");


            NBTTagList nbtPalette = schematicTag.getTagList("Palette", 8);

            if (nbtPalette.tagCount() < 1) {
                throw new InvalidSchematicException("The schematic must have a valid block palette.");
            }

            blockPalette = new String[nbtPalette.tagCount()];

            for (int i = 0; i < nbtPalette.tagCount(); i++) {
                blockPalette[i] = nbtPalette.getStringTagAt(i);
            }

			//load block info
			lowBits = schematicTag.getByteArray("Blocks");
			highBits = schematicTag.getByteArray("AddBlocks");
			metadata = schematicTag.getByteArray("Data");
			hasExtendedBlockIDs = (highBits.length != 0);

			if (volume != lowBits.length)
				throw new InvalidSchematicException("The schematic has data for fewer blocks than its dimensions indicate.");
			if (volume != metadata.length)
				throw new InvalidSchematicException("The schematic has metadata for fewer blocks than its dimensions indicate.");
			if (volume > 2 * highBits.length && hasExtendedBlockIDs)
				throw new InvalidSchematicException("The schematic has extended block IDs for fewer blocks than its dimensions indicate.");

			blockIds = new short[volume];
			if (hasExtendedBlockIDs)
			{
				//Combine the split block IDs into a single value
				pairs = volume - (volume & 1);
				int index;
				for (index = 0; index < pairs; index += 2) 
				{
                    blockIds[index] = (short) (((highBits[index >> 1] & 0x0F) << 8) + (lowBits[index] & 0xFF));
                    blockIds[index + 1] = (short) (((highBits[index >> 1] & 0xF0) << 4) + (lowBits[index + 1] & 0xFF));
				}
				if (index < volume)
				{
                    blockIds[index] = lowBits[index >> 1];
				}
			}
			else
			{
				//Copy the blockIDs
				for (int index = 0; index < volume; index++)
				{
                    blockIds[index] = (short) (lowBits[index] & 0xFF);
				}
			}

            for (int i = 0; i < blockIds.length; i++) {
                int paletteIndex = blockIds[i];
                if (paletteIndex < 0 || paletteIndex >= blockPalette.length) {
                    throw new InvalidSchematicException("Block entry referenced a non-existant palette entry.");
                }
            }

			//Get the list of tile entities
			tileEntities = schematicTag.getTagList("TileEntities", 10);
			
			Schematic result = new Schematic(width, height, length, blockPalette, blockIds, metadata, tileEntities);
			return result;
		}
		catch (InvalidSchematicException ex)
		{
			//Throw the exception again to pass it to the caller.
			throw ex;
		}
		catch (Exception ex)
		{
			throw new InvalidSchematicException("An unexpected error occurred while trying to decode the schematic.", ex);
		}
	}

	public static Schematic copyFromWorld(World world, int x, int y, int z, short width, short height, short length, boolean doCompactBounds)
	{
		if (doCompactBounds)
		{
			//Adjust the vertical bounds to reasonable values if necessary
			int worldHeight = world.getHeight();
			int fixedY = (y < 0) ? 0 : y;
			int fixedHeight = height + y - fixedY;

			if (fixedHeight + fixedY >= worldHeight)
			{
				fixedHeight = worldHeight - fixedY;
			}

			//Compact the area to be copied to remove empty borders
			CompactBoundsOperation compactor = new CompactBoundsOperation();
			compactor.apply(world, x, fixedY, z, width, fixedHeight, length);
			Point3D minCorner = compactor.getMinCorner();
			Point3D maxCorner = compactor.getMaxCorner();

			short compactWidth = (short) (maxCorner.getX() - minCorner.getX() + 1);
			short compactHeight = (short) (maxCorner.getY() - minCorner.getY() + 1);
			short compactLength = (short) (maxCorner.getZ() - minCorner.getZ() + 1);

			return copyFromWorld(world, minCorner.getX(), minCorner.getY(), minCorner.getZ(),
					compactWidth, compactHeight, compactLength);
		}
		else
		{
			return copyFromWorld(world, x, y, z, width, height, length);
		}
	}

	private static Schematic copyFromWorld(World world, int x, int y, int z, short width, short height, short length)
	{
		//Short and sweet ^_^
		WorldCopyOperation copier = new WorldCopyOperation();
		copier.apply(world, x, y, z, width, height, length);
		return new Schematic(width, height, length, copier.getBlocks(), copier.getMetadata(), copier.getTileEntities());
	}

	private static boolean encodeBlockIDs(short[] blocks, byte[] lowBits, byte[] highBits)
	{
		int index;
		int length = blocks.length - (blocks.length & 1);
		boolean hasHighBits = false;
		for (index = 0; index < length; index += 2)
		{
			highBits[index >> 1] = (byte) (((blocks[index] >> 8) & 0x0F) + ((blocks[index + 1] >> 4) & 0xF0));
			hasHighBits |= (highBits[index >> 1] != 0);
		}
		if (index < blocks.length)
		{
			highBits[index >> 1] = (byte) ((blocks[index] >> 8) & 0x0F);
			hasHighBits |= (highBits[index >> 1] != 0);
		}
		for (index = 0; index < blocks.length; index++)
		{
			lowBits[index] = (byte) (blocks[index] & 0xFF);
		}
		return hasHighBits;
	}

    private static void reduceToPalette(Block[] blocks, List<String> blockPalette, short[] blockIds) {
        for (int i = 0; i < blocks.length; i++) {
            String blockName = Block.blockRegistry.getNameForObject(blocks[i]);
            int blockIndex = blockPalette.indexOf(blockName);

            if (blockIndex < 0) {
                blockIndex = blockPalette.size();
                blockPalette.add(blockName);
            }

            blockIds[i] = (short)blockIndex;
        }
    }

	public NBTTagCompound writeToNBT()
	{
		return writeToNBT(true);
	}

	protected NBTTagCompound writeToNBT(boolean copyTileEntities)
	{
		return writeToNBT(width, height, length, blocks, metadata, tileEntities, copyTileEntities);
	}
	
	protected static NBTTagCompound writeToNBT(short width, short height, short length, Block[] blocks, byte[] metadata,
			NBTTagList tileEntities, boolean copyTileEntities)
	{
		//This is the main storage function. Schematics are really compressed NBT tags, so if we can generate
		//the tags, most of the work is done. All the other storage functions will rely on this one.

		NBTTagCompound schematicTag = new NBTTagCompound();

		schematicTag.setShort("Width", width);
		schematicTag.setShort("Length", length);
		schematicTag.setShort("Height", height);

		schematicTag.setTag("Entities", new NBTTagList());
		schematicTag.setString("Materials", "Alpha");

        List<String> blockPalette = new LinkedList<String>();
        short[] blockIds = new short[blocks.length];
        reduceToPalette(blocks, blockPalette, blockIds);

		byte[] lowBits = new byte[blocks.length];
		byte[] highBits = new byte[(blocks.length >> 1) + (blocks.length & 1)];
		boolean hasExtendedIDs = encodeBlockIDs(blockIds, lowBits, highBits);

		schematicTag.setByteArray("Blocks", lowBits);
		schematicTag.setByteArray("Data", metadata);

		if (hasExtendedIDs)
		{
			schematicTag.setByteArray("AddBlocks", highBits);
		}

		if (copyTileEntities)
		{
			//Used when the result of this function will be passed outside this class.
			//Avoids exposing the private field to external modifications.
			schematicTag.setTag("TileEntities", tileEntities.copy());
		}
		else
		{
			//Used when the result of this function is for internal use.
			//It's more efficient not to copy the tags unless it's needed.
			schematicTag.setTag("TileEntities", tileEntities);
		}
		return schematicTag;
	}

	public void writeToFile(String schematicPath) throws IOException
	{
		writeToFile(new File(schematicPath));
	}

	public void writeToFile(File schematicFile) throws IOException
	{
		FileOutputStream outputStream = new FileOutputStream(schematicFile);
		CompressedStreamTools.writeCompressed(writeToNBT(false), outputStream);
		//writeCompressed() probably closes the stream on its own - call close again just in case.
		//Closing twice will not throw an exception.
		outputStream.close();
	}
	
	public boolean applyFilter(SchematicFilter filter)
	{
		return filter.apply(this, this.blocks, this.metadata);
	}
	
	public void copyToWorld(World world, int x, int y, int z, boolean notifyClients, boolean ignoreAir)
	{
		if (notifyClients)
		{
			copyToWorld(world, x, y, z, new WorldBlockSetter(false, true, ignoreAir));
		}
		else
		{
			copyToWorld(world, x, y, z, new ChunkBlockSetter(ignoreAir));
		}
	}
	
	protected void copyToWorld(World world, int x, int y, int z, IBlockSetter blockSetter)
	{
		//This isn't implemented as a WorldOperation because it doesn't quite fit the structure of those operations.
		//It's not worth the trouble in this case.
		int index;
		int count;
		int dx, dy, dz;

		//Copy blocks and metadata into the world
		index = 0;
		for (dy = 0; dy < height; dy++)
		{
			for (dz = 0; dz < length; dz++)
			{
				for (dx = 0; dx < width; dx++)
				{
					blockSetter.setBlock(world, x + dx, y + dy, z + dz, blocks[index], metadata[index]);
					index++;
				}
			}
		}

		//Copy tile entities into the world
		count = tileEntities.tagCount();
		for (index = 0; index < count; index++)
		{
			NBTTagCompound tileTag = (NBTTagCompound) tileEntities.getCompoundTagAt(index);
			//Rewrite its location to be in world coordinates
			dx = tileTag.getInteger("x") + x;
			dy = tileTag.getInteger("y") + y;
			dz = tileTag.getInteger("z") + z;
			tileTag.setInteger("x", dx);
			tileTag.setInteger("y", dy);
			tileTag.setInteger("z", dz);
			//Load the tile entity and put it in the world
			world.setTileEntity(dx, dy, dz, TileEntity.createAndLoadEntity(tileTag));
		}
	}
}
