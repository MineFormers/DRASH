package de.mineformers.drash.api.structure;

import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

/**
 * StructureHelper
 *
 * @author PaleoCrafter
 */
public class StructureHelper
{
    public static void pasteStructure(World world, int x, int y, int z, Structure structure, boolean pasteAir, boolean spawnEntities)
    {
        for (int y1 = 0; y1 < structure.getHeight(); y1++)
        {
            Layer layer = structure.getLayer(y1);
            for (int x1 = 0; x1 < layer.getWidth(); x1++)
            {
                for (int z1 = 0; z1 < layer.getLength(); z1++)
                {
                    Layer.BlockInfo info = layer.get(x1, z1);
                    if (info != null && (pasteAir || info.getBlock() != Blocks.air))
                    {
                        world.setBlock(x + x1, y + y1, z + z1, info.getBlock(), info.getMetadata(), 2);
                        if (info.getTileEntity() != null && world.getTileEntity(x + x1, y + y1, z + z1) != null)
                        {
                            world.getTileEntity(x + x1, y + y1, z + z1).readFromNBT(info.getTranslatedTileEntity(x, y, z));
                            world.markBlockForUpdate(x + x1, y + y1, z + z1);
                        }
                    }
                }
            }
        }
        if (spawnEntities)
        {
            double[] posAdd = {x, y, z};
            for (NBTTagCompound nbt : structure.getEntities())
            {
                NBTTagCompound newTag = (NBTTagCompound) nbt.copy();
                NBTTagList pos = newTag.getTagList("Pos", Constants.NBT.TAG_DOUBLE);
                NBTTagList newPos = new NBTTagList();
                for (int i = 0; i < pos.tagCount(); i++)
                    newPos.appendTag(new NBTTagDouble(pos.func_150309_d(i) + posAdd[i]));
                newTag.setTag("Pos", newPos);
                Entity e = EntityList.createEntityFromNBT(newTag, world);
                world.spawnEntityInWorld(e);
            }
        }
    }

    public static Structure createFromRegion(World world, AxisAlignedBB bounds, boolean includeEntities)
    {
        Structure structure = new Structure();
        int minX = (int) bounds.minX;
        int minY = (int) bounds.minY;
        int minZ = (int) bounds.minZ;
        int maxX = (int) bounds.maxX;
        int maxY = (int) bounds.maxY;
        int maxZ = (int) bounds.maxZ;
        for (int y = minY; y <= maxY; y++)
        {
            Layer layer = new Layer((maxX - minX) + 1, (maxZ - minZ) + 1);
            for (int x = minX; x <= maxX; x++)
            {
                for (int z = minZ; z <= maxZ; z++)
                {
                    if (world.getBlock(x, y, z) != null)
                    {
                        Block block = world.getBlock(x, y, z);
                        int meta = world.getBlockMetadata(x, y, z);
                        TileEntity tile = world.getTileEntity(x, y, z);
                        NBTTagCompound tag = tile != null ? new NBTTagCompound() : null;
                        if (tag != null)
                        {
                            tile.writeToNBT(tag);
                            updateTileCoordinates(tag, x - minX, y - minY, z - minZ);
                        }
                        layer.set(x - minX, z - minZ, block, meta, tag);
                    }
                    else
                        layer.set(x - minX, y - minY, Blocks.air, 0);
                }
            }
            structure.addLayer(layer);
        }
        if (includeEntities)
        {
            double[] mins = {minX, minY, minZ};
            for (Object o : world.selectEntitiesWithinAABB(Entity.class, bounds, nonPlayerSelector))
            {
                NBTTagCompound tag = new NBTTagCompound();
                boolean valid = ((Entity) o).writeToNBTOptional(tag);
                if(!valid)
                    valid = ((Entity) o).writeMountToNBT(tag);
                if (valid)
                {
                    NBTTagList pos = tag.getTagList("Pos", Constants.NBT.TAG_DOUBLE);
                    NBTTagList newPos = new NBTTagList();
                    for (int i = 0; i < pos.tagCount(); i++)
                        newPos.appendTag(new NBTTagDouble(pos.func_150309_d(i) - mins[i]));
                    tag.setTag("Pos", newPos);
                    structure.addEntity(tag);
                }
            }
        }
        return structure;
    }

    public static void updateTileCoordinates(NBTTagCompound tag, int x, int y, int z)
    {
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setInteger("z", z);
    }

    private static IEntitySelector nonPlayerSelector = new IEntitySelector()
    {
        @Override
        public boolean isEntityApplicable(Entity entity)
        {
            return !(entity instanceof EntityPlayer);
        }
    };
}
