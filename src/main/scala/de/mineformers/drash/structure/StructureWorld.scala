package de.mineformers.drash.structure

import de.mineformers.drash.api.structure.Structure
import net.minecraft.world.{WorldType, WorldSettings, EnumSkyBlock, World}
import net.minecraft.world.storage.SaveHandlerMP
import net.minecraft.tileentity.TileEntity
import scala.collection.mutable
import de.mineformers.core.util.world.BlockPos
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraft.client.renderer.RenderBlocks
import de.mineformers.drash.renderer.StructureChunkRenderer
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.world.biome.BiomeGenBase
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.client.multiplayer.ChunkProviderClient
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB

/**
 * StructureWorld
 *
 * @author PaleoCrafter
 */
class StructureWorld(_structure: Structure, val pos: BlockPos) extends World(new SaveHandlerMP, "DRASH", null, StructureWorld.Settings, null) {
  private val structure = _structure.copy()
  val tiles = mutable.HashMap.empty[BlockPos, TileEntity]
  for (y <- 0 until structure.getHeight; layer = structure.getLayer(y); x <- 0 until layer.getWidth; z <- 0 until layer.getLength) {
    val info = layer.get(x, z)
    if (info != null && info.getTileEntity != null) {
      val tile = info.getBlock.createTileEntity(this, info.getMetadata)
      if (tile != null) {
        tile.setWorldObj(this)
        tile.xCoord = x
        tile.yCoord = y
        tile.zCoord = z
        tile.validate()
        tile.readFromNBT(info.getTileEntity)
        tiles.put(BlockPos(x, y, z), tile)
      }
    }
  }
  val renderer = new RenderBlocks(this)
  var chunkRenderers = createRenderChunkList()

  override def getBlock(x: Int, y: Int, z: Int): Block = {
    val info = structure.getBlock(x, y, z)
    if (info != null) info.getBlock else Blocks.air
  }

  override def getBlockMetadata(x: Int, y: Int, z: Int): Int = {
    val info = structure.getBlock(x, y, z)
    if (info != null) info.getMetadata else 0
  }

  override def getTileEntity(x: Int, y: Int, z: Int): TileEntity = tiles.getOrElse(BlockPos(x, y, z), null)

  override def isBlockNormalCubeDefault(x: Int, y: Int, z: Int, default: Boolean): Boolean = {
    val block = getBlock(x, y, z)
    if (block == null)
      false
    else if (block.isNormalCube)
      true
    else
      default
  }

  override def isAirBlock(x: Int, y: Int, z: Int): Boolean = {
    val block = getBlock(x, y, z)
    if (block == null)
      true
    else
      block.isAir(this, x, y, z)
  }

  override def getBiomeGenForCoords(x: Int, z: Int): BiomeGenBase = BiomeGenBase.jungle

  override def extendedLevelsInChunkCache(): Boolean = false

  override def blockExists(x: Int, y: Int, z: Int): Boolean = false

  override def getSkyBlockTypeBrightness(par1EnumSkyBlock: EnumSkyBlock, par2: Int, par3: Int, par4: Int): Int = 15

  override def getLightBrightness(par1: Int, par2: Int, par3: Int): Float = 1f

  override def setBlockMetadataWithNotify(x: Int, y: Int, z: Int, metadata: Int, flag: Int): Boolean = {
    val info = structure.getBlock(x, y, z)
    if (info != null) {
      structure.setBlock(x, y, z, getBlock(x, y, z), metadata, getTileTag(x, y, z))
      true
    } else false
  }

  override def isSideSolid(x: Int, y: Int, z: Int, side: ForgeDirection, default: Boolean): Boolean = {
    val block = getBlock(x, y, z)
    if (block == null)
      false
    else
      block.isSideSolid(this, x, y, z, side)
  }

  def dispose(): Unit = {
    tiles.clear()
    chunkRenderers foreach {
      _.dispose()
    }
  }

  def bounds = AxisAlignedBB.getBoundingBox(pos.x, pos.y, pos.z, pos.x + width, pos.y + height, pos.z + length)

  def width = structure.getWidth

  def length = structure.getLength

  def height = structure.getHeight

  def getTileTag(x: Int, y: Int, z: Int): NBTTagCompound = {
    val info = structure.getBlock(x, y, z)
    if (info != null)
      info.getTileEntity
    else
      null
  }

  def createRenderChunkList(): List[StructureChunkRenderer] = {
    val width = (this.width - 1) / 16 + 1
    val height = (this.height - 1) / 16 + 1
    val length = (this.length - 1) / 16 + 1
    var list = List.empty[StructureChunkRenderer]

    for (x <- 0 until width; y <- 0 until height; z <- 0 until length) {
      list :+= new StructureChunkRenderer(this, x, y, z)
    }

    list foreach {
      _.updateList()
    }

    list
  }

  override def createChunkProvider(): IChunkProvider = new ChunkProviderClient(this)

  override def getEntityByID(var1: Int): Entity = null
}

object StructureWorld {
  private final val Settings = new WorldSettings(0, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT)
}