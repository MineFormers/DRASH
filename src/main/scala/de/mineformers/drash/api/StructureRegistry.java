package de.mineformers.drash.api;

import de.mineformers.drash.api.structure.Structure;
import de.mineformers.drash.structure.Structures;
import net.minecraft.util.ResourceLocation;

/**
 * StructureRegistry
 *
 * @author PaleoCrafter
 */
public class StructureRegistry
{
    private static Structures instance;

    private static Structures instance()
    {
        if (instance == null)
            instance = new Structures();
        return instance;
    }

    public static Structure get(String name)
    {
        return instance().apply(name);
    }

    public static void add(String name, Structure structure)
    {
        instance().add(name, structure);
    }

    public static void add(String name, ResourceLocation resource)
    {
        instance().load(name, resource);
    }
}
