package de.mineformers.drash.renderer

import de.mineformers.drash.structure.StructureWorld
import net.minecraft.util.AxisAlignedBB
import StructureChunkRenderer._
import de.mineformers.core.util.world.BlockPos
import org.lwjgl.opengl.{GL14, GL11}
import net.minecraft.client.renderer.{Tessellator, OpenGlHelper}
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import de.mineformers.drash.DRASH
import net.minecraft.init.Blocks

/**
 * StructureChunkRenderer
 *
 * @author PaleoCrafter
 */
class StructureChunkRenderer(val structure: StructureWorld, baseX: Int, baseY: Int, baseZ: Int) {
  val bounds = AxisAlignedBB.getBoundingBox(baseX * ChunkWidth, baseY * ChunkHeight, baseZ * ChunkLength, (baseX + 1) * ChunkWidth, (baseY + 1) * ChunkHeight, (baseZ + 1) * ChunkLength)
  val centered = BlockPos(((baseX + 0.5) * ChunkWidth).toInt, ((baseY + 0.5) * ChunkHeight).toInt, ((baseZ + 0.5) * ChunkLength).toInt)
  var tiles = (for ((pos, tile) <- structure.tiles if pos.containedBy(bounds)) yield tile).toList
  val glList = GL11.glGenLists(2)
  var update = true
  val mc = Minecraft.getMinecraft

  def dispose(): Unit = {
    tiles = null
    GL11.glDeleteLists(glList, 2)
  }

  def updateList(): Unit = {
    if (update) {
      this.update = false
      val minX = bounds.minX.toInt
      val maxX = bounds.maxX.toInt min structure.width
      val minY = bounds.minY.toInt
      val maxY = bounds.maxY.toInt min structure.height
      val minZ = bounds.minZ.toInt
      val maxZ = bounds.maxZ.toInt min structure.length
      this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture)
      for (pass <- 0 until 2) {
        GL11.glNewList(glList + pass, GL11.GL_COMPILE)
        val renderBlocks = structure.renderer
        val ambient = mc.gameSettings.ambientOcclusion
        mc.gameSettings.ambientOcclusion = 0
        renderBlocks.renderAllFaces = true
        Tessellator.instance.startDrawingQuads()
        for (y <- minY until maxY; x <- minX until maxX; z <- minZ until maxZ) {
          try {
            val block = structure.getBlock(x, y, z)
            val pos = structure.pos
            if (mc.theWorld.getBlock(pos.x + x, pos.y + y, pos.z + z) == Blocks.air || mc.theWorld.getBlock(pos.x + x, pos.y + y, pos.z + z) == null)
              if (block != null && block.canRenderInPass(pass)) {
                renderBlocks.renderBlockByRenderType(block, x, y, z)
              }
          } catch {
            case e: Exception =>
              DRASH.Log.error("Failed to render block", e)
          }
        }
        Tessellator.instance.draw()
        mc.gameSettings.ambientOcclusion = ambient
        GL11.glEndList()
      }
    }
  }

  def render(pass: Int): Unit = {
    GL11.glDisable(GL11.GL_LIGHTING)
    this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture)
    this.updateList()
    GL11.glCallList(glList + pass)

    renderTiles(pass)

    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

    GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F)
  }

  def renderTiles(pass: Int): Unit = {
    if (pass != 0)
      return
    try {
      for (tile <- tiles) {
        val x = tile.xCoord
        val y = tile.yCoord
        val z = tile.zCoord
        val renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tile)
        if (mc.theWorld.getBlock(baseX + x, baseY + y, baseZ + z) == Blocks.air)
          if (renderer != null) {
            try {
              renderer.renderTileEntityAt(tile, x, y, z, 0)
              OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit)
              GL11.glDisable(GL11.GL_TEXTURE_2D)
              OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            } catch {
              case e: Exception =>
                DRASH.Log.error("Failed to render a tile entity!", e)
            }
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1F)
          }
      }
    } catch {
      case e: Exception =>
        DRASH.Log.error("Failed to render tiles", e)
    }
  }
}

object StructureChunkRenderer {
  final val ChunkWidth = 16
  final val ChunkHeight = 16
  final val ChunkLength = 16
}
