package de.mineformers.drash.api.structure

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import de.mineformers.drash.structure.StructureWorld

/**
 * Layer
 *
 * @author PaleoCrafter
 */
class Layer(val width: Int, val length: Int) {
  private val blocks = Array.ofDim[BlockInfo](width, length)

  def set(x: Int, z: Int, block: Block, metadata: Int) {
    set(x, z, block, metadata, null)
  }

  def set(x: Int, z: Int, block: Block, metadata: Int, tileEntity: NBTTagCompound) {
    val clampedX: Int = clampX(x)
    val clampedZ: Int = clampZ(z)
    set(new SimpleBlockInfo(block, clampedX, clampedZ, (metadata & 0xF).asInstanceOf[Byte], tileEntity))
  }

  def set(info: BlockInfo) {
    blocks(info.x)(info.z) = info
  }

  def remove(x: Int, z: Int) {
    blocks(clampX(x))(clampZ(z)) = null
  }

  def get(x: Int, z: Int): BlockInfo = {
    if (x >= width || z >= length || x < 0 || z < 0) return null
    var info: BlockInfo = blocks(x)(z)
    if (info == null) {
      info = new SimpleBlockInfo(Blocks.air, x, z, 0.asInstanceOf[Byte])
      set(info)
    }
    info
  }

  private def clampX(x: Int): Int = math.min(math.max(x, 0), width - 1)

  private def clampZ(z: Int): Int = math.min(math.max(z, 0), length - 1)

  def copy: Layer = {
    val layer: Layer = new Layer(width, length)
    for (x <- 0 until width; z <- 0 until length)
      if (get(x, z) != null) layer.set(get(x, z).copy)
    layer
  }

  override def toString: String = {
    var s: String = "Layer=["
    s += blocks.mkString(",")
    s += "]"
    s
  }
}

abstract class BlockInfo(val x: Int, val z: Int) {
  def getEntry: BlockEntry

  def getBlock = getEntry.block

  def getMetadata = getEntry.metadata

  def getTileEntity = getEntry.tile

  def update(world: StructureWorld, y: Int): Unit = ()

  def getTranslatedTileEntity(x: Int, y: Int, z: Int): NBTTagCompound = {
    if (getEntry.tile != null) {
      val translated: NBTTagCompound = getEntry.tile.copy.asInstanceOf[NBTTagCompound]
      translated.setInteger("x", translated.getInteger("x") + x)
      translated.setInteger("y", translated.getInteger("y") + y)
      translated.setInteger("z", translated.getInteger("z") + z)
      translated
    } else
      null
  }

  def copy: BlockInfo

  override def toString: String = "BlockInfo=[pos=(" + x + "," + z + "), block=" + this.getEntry.block + ", metadata=" + this.getEntry.metadata + "]"
}

class SimpleBlockInfo(block: Block, x: Int, z: Int, metadata: Byte, tile: NBTTagCompound) extends BlockInfo(x, z) {
  if (tile != null) {
    tile.setInteger("x", x)
    tile.setInteger("z", z)
  }
  val getEntry = BlockEntry(block, metadata, tile)

  def this(block: Block, x: Int, z: Int, metadata: Byte) = this(block, x, z, metadata, null)

  def copy: BlockInfo = new SimpleBlockInfo(block, x, z, metadata, tile)
}

object BlockEntry {
  def apply(block: Block, metadata: Int): BlockEntry = apply(block, (metadata & 0xF).toByte, null)
}

case class BlockEntry(block: Block, metadata: Byte, tile: NBTTagCompound) {
  def this(block: Block, metadata: Byte) = this(block, metadata, null)
}