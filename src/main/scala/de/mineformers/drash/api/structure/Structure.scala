package de.mineformers.drash.api.structure

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.block.Block
import scala.collection.mutable
import de.mineformers.drash.structure.StructureWorld
import net.minecraft.util.AxisAlignedBB

/**
 * Structure
 *
 * @author PaleoCrafter
 */
class Structure {
  private val layers = mutable.ListBuffer.empty[Layer]
  private val entities = mutable.ListBuffer.empty[NBTTagCompound]

  def addLayer(layer: Layer): Unit = {
    if (layer == null) throw new IllegalArgumentException("Layer may not be null")
    layers append layer
  }

  def getBlock(x: Int, y: Int, z: Int): BlockInfo = {
    val layer: Layer = getLayer(y)
    if (layer == null) return null
    layer.get(x, z)
  }

  def setBlock(x: Int, y: Int, z: Int, block: Block, metadata: Int, tile: NBTTagCompound): Unit = {
    getLayer(y).set(x, z, block, metadata, tile)
  }

  def getLayer(y: Int): Layer = {
    if (y >= getHeight || y < 0) return null
    layers(y)
  }

  def addEntity(tag: NBTTagCompound): Unit = {
    entities append tag
  }

  def clear(): Unit = {
    layers.clear()
  }

  def getWidth: Int = {
    var widest: Int = 0
    for (layer <- layers) if (widest < layer.width) widest = layer.width
    widest
  }

  def getLength: Int = {
    var longest: Int = 0
    for (layer <- layers) if (longest < layer.length) longest = layer.length
    longest
  }

  def getHeight: Int = layers.size

  def update(world: StructureWorld): Unit = {
    for(y <- 0 until getHeight; x <- 0 until getWidth; z <- 0 until getLength) {
      val info = getBlock(x, y, z)
      if(info != null)
        info.update(world, y)
    }
  }

  final def getLayers: List[Layer] = layers.toList

  final def getEntities: List[NBTTagCompound] = entities.toList

  def copy: Structure = {
    val struct: Structure = new Structure
    for (layer <- layers) struct.addLayer(layer.copy)
    struct
  }

  override def toString: String = "Structure=" + layers.toString
}