package de.mineformers.drash.api.structure;

import de.mineformers.core.util.ResourceUtils;
import de.mineformers.core.util.world.BlockPos;
import de.mineformers.drash.DRASH;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import static net.minecraftforge.common.util.Constants.NBT.*;

/**
 * ModMaticFile
 *
 * @author PaleoCrafter
 */
public class ModMaticFile
{
    public static ModMaticFile load(ResourceLocation location)
    {
        try
        {
            return new ModMaticFile(CompressedStreamTools.readCompressed(ResourceUtils.locationToResource(location).toInputStream()));
        }
        catch (IOException e)
        {
            DRASH.Log().error("Error while loading modmatic file", e);
            return null;
        }
    }

    public static Structure read(NBTTagCompound tag)
    {
        Structure structure = new Structure();
        if (tag.hasKey("Blocks", TAG_LIST) && tag.hasKey("Data", TAG_BYTE_ARRAY))
        {
            Map<Short, Block> types = new HashMap<>();
            NBTTagList typeStrings = tag.getTagList("Blocks", TAG_STRING);
            for (int i = 0; i < typeStrings.tagCount(); i++)
            {
                Block block = Block.getBlockFromName(typeStrings.getStringTagAt(i));
                if (block != null)
                    types.put((short) i, block);
            }

            Map<BlockPos, NBTTagCompound> tiles = new HashMap<>();
            if (tag.hasKey("TileEntities", TAG_LIST))
            {
                NBTTagList tileList = tag.getTagList("TileEntities", TAG_COMPOUND);
                for (int i = 0; i < tileList.tagCount(); i++)
                {
                    NBTTagCompound tile = tileList.getCompoundTagAt(i);
                    tiles.put(BlockPos.apply(tile), tile);
                }
            }

            int width = tag.getInteger("Width");
            int length = tag.getInteger("Length");
            int height = tag.getInteger("Height");
            byte[] data = tag.getByteArray("Data");
            ShortBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
            for (int y = 0; y < height; y++)
            {
                Layer layer = new Layer(width, length);
                for (int x = 0; x < length; x++)
                {
                    for (int z = 0; z < width && buf.hasRemaining(); z++)
                    {
                        short dat = buf.get();
                        short id = (short) (dat >> 4);
                        byte meta = (byte) (dat & 0xF);
                        Block block = types.get(id);
                        layer.set(x, z, block, meta, tiles.get(BlockPos.apply(x, y, z)));
                    }
                }
                structure.addLayer(layer);
            }
            if (tag.hasKey("Entities", TAG_LIST))
            {
                NBTTagList entities = tag.getTagList("Entities", TAG_COMPOUND);
                for (int i = 0; i < entities.tagCount(); i++)
                {
                    NBTTagCompound entity = entities.getCompoundTagAt(i);
                    structure.addEntity(entity);
                }
            }
        }
        return structure;
    }

    public static void write(Structure structure, NBTTagCompound tag)
    {
        NBTTagList tiles = new NBTTagList();
        NBTTagList typeStrings = new NBTTagList();
        Map<Block, Short> types = new IdentityHashMap<>();
        short lastType = 0;
        for (Layer layer : structure.getLayers())
        {
            for (int x = 0; x < layer.getWidth(); x++)
            {
                for (int z = 0; z < layer.getLength(); z++)
                {
                    Layer.BlockInfo info = layer.get(x, z);
                    Block block = info != null ? info.getBlock() : Blocks.air;
                    if (!types.containsKey(block))
                    {
                        types.put(block, lastType);
                        typeStrings.appendTag(new NBTTagString(Block.blockRegistry.getNameForObject(block)));
                        lastType += 1;
                    }
                }
            }
        }

        int width = structure.getWidth();
        int length = structure.getLength();
        int height = structure.getHeight();
        tag.setInteger("Width", width);
        tag.setInteger("Length", length);
        tag.setInteger("Height", height);
        ByteBuffer bytes = ByteBuffer.wrap(new byte[2 * width * length * height]).order(ByteOrder.BIG_ENDIAN);
        ShortBuffer buf = bytes.asShortBuffer();
        int y = 0;
        for (Layer layer : structure.getLayers())
        {
            for (int x = 0; x < layer.getWidth(); x++)
            {
                for (int z = 0; z < layer.getLength(); z++)
                {
                    writeBlock(y, tiles, typeStrings, buf, types, layer.get(x, z));
                }
                for (int z = 0; z < length - layer.getLength(); z++)
                {
                    writeBlock(y, tiles, typeStrings, buf, types, null);
                }
            }
            for (int x = 0; x < width - layer.getWidth(); x++)
            {
                for (int z = 0; z < length; z++)
                {
                    writeBlock(y, tiles, typeStrings, buf, types, null);
                }
            }
            y += 1;
        }
        tag.setByteArray("Data", bytes.array());
        tag.setTag("Blocks", typeStrings);
        tag.setTag("TileEntities", tiles);
        NBTTagList entities = new NBTTagList();
        for (NBTTagCompound entity : structure.getEntities())
        {
            entities.appendTag(entity);
        }
        tag.setTag("Entities", entities);
    }

    private static void writeBlock(int y, NBTTagList tiles, NBTTagList typeStrings, ShortBuffer buf, Map<Block, Short> types, Layer.BlockInfo info)
    {
        if (info != null)
        {
            short s = (short) ((types.get(info.getBlock()) << 4) | (info.getMetadata() & 0xF));
            buf.put(s);
            NBTTagCompound tile = info.getTileEntity();
            if (tile != null)
            {
                tile.setInteger("x", info.getX());
                tile.setInteger("y", y);
                tile.setInteger("z", info.getZ());
                tiles.appendTag(tile);
            }
        }
        else
        {
            if (!types.containsKey(Blocks.air))
            {
                typeStrings.appendTag(new NBTTagString(Block.blockRegistry.getNameForObject(Blocks.air)));
                types.put(Blocks.air, (short) types.size());
            }
            writeBlock(0, tiles, typeStrings, buf, types, new Layer.BlockInfo(Blocks.air, 0, 0, (byte) 0));
        }
    }

    private final Structure structure;
    private final NBTTagCompound tag;

    public ModMaticFile(NBTTagCompound tag)
    {
        this.tag = tag;
        this.structure = read();
    }

    public Structure read()
    {
        return read(tag);
    }

    public Structure getStructure()
    {
        return structure;
    }
}
