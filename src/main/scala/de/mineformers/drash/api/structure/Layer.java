package de.mineformers.drash.api.structure;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;

/**
 * Layer
 *
 * @author PaleoCrafter
 */
public class Layer
{
    private final int width, length;
    private final BlockInfo[][] blocks;

    public Layer(int width, int length)
    {
        blocks = new BlockInfo[width][length];
        this.width = width;
        this.length = length;
    }

    public void set(int x, int z, Block block, int metadata)
    {
        set(x, z, block, metadata, null);
    }

    public void set(int x, int z, Block block, int metadata, NBTTagCompound tileEntity)
    {
        int clampedX = clampX(x);
        int clampedZ = clampZ(z);
        set(new BlockInfo(block, clampedX, clampedZ, (byte) (metadata & 0xF), tileEntity));
    }

    private void set(BlockInfo info)
    {
        blocks[info.getX()][info.getZ()] = info;
    }

    public void remove(int x, int z)
    {
        blocks[clampX(x)][clampZ(z)] = null;
    }

    public BlockInfo get(int x, int z)
    {
        if (x >= width || z >= length || x < 0 || z < 0)
            return null;
        BlockInfo info = blocks[x][z];
        if (info == null)
        {
            info = new BlockInfo(Blocks.air, x, z, (byte) 0);
            set(info);
        }
        return info;
    }

    public int getWidth()
    {
        return width;
    }

    public int getLength()
    {
        return length;
    }

    private int clampX(int x)
    {
        return Math.min(Math.max(x, 0), width - 1);
    }

    private int clampZ(int z)
    {
        return Math.min(Math.max(z, 0), length - 1);
    }

    public Layer copy()
    {
        Layer layer = new Layer(width, length);
        for (int x = 0; x < width; x++)
            for (int z = 0; z < width; z++)
                if (get(x, z) != null)
                    layer.set(get(x, z).copy());
        return layer;
    }

    @Override
    public String toString()
    {
        String s = "Layer=[";
        s += Arrays.deepToString(blocks);
        s += "]";
        return s;
    }

    public static class BlockInfo
    {
        private final Block block;
        private final int x, z;
        private final byte metadata;
        private final NBTTagCompound tile;

        public BlockInfo(Block block, int x, int z, byte metadata)
        {
            this(block, x, z, metadata, null);
        }

        public BlockInfo(Block block, int x, int z, byte metadata, NBTTagCompound tile)
        {
            this.block = block;
            this.x = x;
            this.z = z;
            this.metadata = metadata;
            this.tile = tile;
            if (tile != null)
            {
                tile.setInteger("x", x);
                tile.setInteger("z", z);
            }
        }

        public Block getBlock()
        {
            return block;
        }

        public int getX()
        {
            return x;
        }

        public int getZ()
        {
            return z;
        }

        public byte getMetadata()
        {
            return metadata;
        }

        public NBTTagCompound getTileEntity()
        {
            return tile;
        }

        public NBTTagCompound getTranslatedTileEntity(int x, int y, int z)
        {
            if (tile != null)
            {
                NBTTagCompound translated = (NBTTagCompound) tile.copy();
                translated.setInteger("x", tile.getInteger("x") + x);
                translated.setInteger("y", tile.getInteger("y") + y);
                translated.setInteger("z", tile.getInteger("z") + z);
                return translated;
            }
            return null;
        }

        public BlockInfo copy()
        {
            return new BlockInfo(block, x, z, metadata, tile);
        }

        @Override
        public String toString()
        {
            return "BlockInfo=[pos=(" + x + "," + z + "), block=" + block + ", metadata=" + metadata + "]";
        }
    }
}
