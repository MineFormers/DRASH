package de.mineformers.drash.structure

import scala.collection.mutable
import net.minecraft.util.{AxisAlignedBB, ResourceLocation}
import de.mineformers.drash.api.structure.{Layer, ModMaticFile, Structure}
import net.minecraft.world.World
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.init.Blocks

/**
 * Structures
 *
 * @author PaleoCrafter
 */
class Structures {
  private val entries = mutable.Map.empty[String, Structure]

  def apply(key: String): Structure = entries(key)

  def add(key: String, value: Structure): Unit = {
    entries += key -> value
  }

  def load(key: String, resource: ResourceLocation): Unit = {
    add(key, ModMaticFile.load(resource).structure)
  }
}