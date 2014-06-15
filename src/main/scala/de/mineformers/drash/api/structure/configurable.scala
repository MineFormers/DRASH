package de.mineformers.drash.api.structure

import scala.collection.mutable
import scala.util.control.Breaks
import de.mineformers.drash.structure.StructureWorld

/**
 * configurable
 *
 * @author PaleoCrafter
 */
class StructureConfiguration(possibleLayers: Seq[ConfigurableLayer]) {
  val amount = mutable.Map.empty[Int, Int]

  def build(): Structure = {
    val structure = new Structure
    import Breaks._
    for (y <- 0 until possibleLayers.length) {
      breakable {
        val confLayer = possibleLayers(y)
        val amount = this.amount.getOrElse(y, 0)
        if (confLayer.required && amount == 0)
          throw new IllegalArgumentException("Required layer wasn't respected")
        if (amount < confLayer.min)
          break()
        if (amount > confLayer.max && confLayer.max != -1)
          break()
        for (i <- 0 until amount)
          structure.addLayer(confLayer.copy)
      }
    }
    structure
  }
}

class ConfigurableLayer(width: Int, length: Int, val required: Boolean = false, val min: Int = 0, val max: Int = -1) extends Layer(width, length)

class MultiBlockInfo(x: Int, z: Int, val blocks: Seq[BlockEntry]) extends BlockInfo(x, z) {
  private var i = 0
  var delay = 200
  private var last: Long = 0

  override def update(world: StructureWorld, y: Int): Unit = {
    val time = System.currentTimeMillis()
    if (time - last >= 1000) {
      if (i == blocks.length - 1)
        i = 0
      else
        i = (i + 1) max 0 min (blocks.length - 1)
      last = time
      world.markBlockForUpdate(x, y, z)
    }
  }

  override def getEntry: BlockEntry = blocks(i)

  override def copy: BlockInfo = new MultiBlockInfo(x, z, blocks)
}
