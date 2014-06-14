package de.mineformers.drash.api.structure;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Structure
 *
 * @author PaleoCrafter
 */
public class Structure
{
    private List<Layer> layers;
    private List<NBTTagCompound> entities;

    public Structure()
    {
        layers = new LinkedList<>();
        entities = new ArrayList<>();
    }

    public void addLayer(Layer layer)
    {
        if (layer == null) throw new IllegalArgumentException("Layer may not be null");
        layers.add(layer);
    }

    public Layer.BlockInfo getBlock(int x, int y, int z)
    {
        Layer layer = getLayer(y);
        if(layer == null)
            return null;
        return layer.get(x, z);
    }

    public void setBlock(int x, int y, int z, Block block, int metadata, NBTTagCompound tile)
    {
        getLayer(y).set(x, z, block, metadata, tile);
    }

    public Layer getLayer(int y)
    {
        if (y >= getHeight() || y < 0)
            return null;
        return layers.get(y);
    }

    public void addEntity(NBTTagCompound tag)
    {
        entities.add(tag);
    }

    public void clear()
    {
        layers.clear();
    }

    public int getWidth()
    {
        int widest = 0;
        for (Layer layer : layers)
            if (widest < layer.getWidth())
                widest = layer.getWidth();
        return widest;
    }

    public int getLength()
    {
        int longest = 0;
        for (Layer layer : layers)
            if (longest < layer.getLength())
                longest = layer.getLength();
        return longest;
    }

    public int getHeight()
    {
        return layers.size();
    }

    public final List<Layer> getLayers()
    {
        return ImmutableList.copyOf(layers);
    }

    public final List<NBTTagCompound> getEntities()
    {
        return ImmutableList.copyOf(entities);
    }

    public Structure copy()
    {
        Structure struct = new Structure();
        for (Layer layer : layers)
            struct.addLayer(layer.copy());
        return struct;
    }

    @Override
    public String toString()
    {
        return "Structure=" + layers.toString();
    }
}
