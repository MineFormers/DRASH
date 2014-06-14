package de.mineformers.drash

import cpw.mods.fml.common.{FMLCommonHandler, SidedProxy, Mod}
import cpw.mods.fml.common.event.{FMLPostInitializationEvent, FMLInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.Mod.EventHandler
import org.apache.logging.log4j.LogManager
import de.mineformers.drash.api.StructureRegistry
import de.mineformers.core.block.BaseBlock
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.block.material.Material
import net.minecraft.world.World
import net.minecraft.entity.player.EntityPlayer
import de.mineformers.core.util.ResourceUtils.Resource
import de.mineformers.drash.api.structure.{Structure, StructureHelper, ModMaticFile}
import net.minecraft.nbt.{CompressedStreamTools, NBTTagCompound}
import java.io.FileOutputStream
import de.mineformers.core.registry.SharedBlockRegistry
import net.minecraft.util.AxisAlignedBB
import de.mineformers.drash.structure.StructureWorld
import cpw.mods.fml.relauncher.{Side, SideOnly}
import scala.collection.mutable
import de.mineformers.core.util.world.BlockPos
import net.minecraftforge.common.MinecraftForge
import de.mineformers.drash.renderer.StructureWorldRenderer
import de.mineformers.drash.recipe.RecipeTraverser
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard
import net.minecraft.client.Minecraft
import de.mineformers.drash.gui.FindRecipeFrame
import cpw.mods.fml.client.registry.ClientRegistry
import net.minecraftforge.oredict.OreDictionary

/**
 * DRASH
 *
 * @author PaleoCrafter
 */
@Mod(modid = DRASH.ModId, name = DRASH.ModName, modLanguage = "scala")
object DRASH {
  final val ModId = "drash"
  final val ModName = "DRASH"
  final val Log = LogManager.getLogger(ModId)

  @SidedProxy(serverSide = Environment.ServerClass, clientSide = Environment.ClientClass)
  var environment: Environment = _
  var executedServerSide = false

  @EventHandler
  def preInit(e: FMLPreInitializationEvent): Unit = {
    if (e.getSide.isServer) {
      executedServerSide = true
      Log.warn(ModName + " is not allowed to be run server side! Please remove it")
    }
    val b = new TestBlock
    SharedBlockRegistry.add("test12356", b)
    OreDictionary.registerOre("plankWood", b)
  }

  @EventHandler
  def init(e: FMLInitializationEvent): Unit = {
    environment.init()
  }

  @EventHandler
  def postInit(e: FMLPostInitializationEvent): Unit = {
    environment.postInit()
  }
}

class TestBlock extends BaseBlock("test", "test", CreativeTabs.tabBlock, Material.rock) {
  override def onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer, p_149727_6_ : Int, p_149727_7_ : Float, p_149727_8_ : Float, p_149727_9_ : Float): Boolean = {
    if (!world.isRemote) {
      if (!player.isSneaking) {
        val struct = StructureRegistry.get("test")
        StructureHelper.pasteStructure(world, x + 1, y, z + 1, struct, true, true)
      } else {
        val struct = StructureHelper.createFromRegion(world, AxisAlignedBB.getBoundingBox(x + 1, y, z + 1, x + 3, y + 1, z + 3), true)
        val tag = new NBTTagCompound
        ModMaticFile.write(struct, tag)
        CompressedStreamTools.writeCompressed(tag, new FileOutputStream("test.modmatic"))
      }
    }
    true
  }
}

trait Environment {
  @SideOnly(Side.CLIENT)
  val activeStructures = mutable.Map.empty[Int, StructureWorld]

  @SideOnly(Side.CLIENT)
  def getActive(dimension: Int): StructureWorld = activeStructures.getOrElse(dimension, null)

  @SideOnly(Side.CLIENT)
  def setActive(dimension: Int, pos: BlockPos, structure: Structure): Unit = {
    remActive(dimension)
    activeStructures += dimension -> new StructureWorld(structure, pos)
  }

  @SideOnly(Side.CLIENT)
  def remActive(dimension: Int): Unit = {
    val active = getActive(dimension)
    if (active != null) {
      active.dispose()
      activeStructures.remove(dimension)
    }
  }

  def init(): Unit = ()

  def postInit(): Unit = ()
}

object Environment {
  final val ServerClass = "de.mineformers.drash.Environment$Server"
  final val ClientClass = "de.mineformers.drash.Environment$Client"

  class Server extends Environment

  class Client extends Environment {
    private val keyFind: KeyBinding = new KeyBinding("key.calcRecipe", Keyboard.KEY_F, "key.categories.misc")

    override def init(): Unit = {
      ClientRegistry.registerKeyBinding(keyFind)
      StructureRegistry.add("test", Resource("drash", "test.modmatic"))
      MinecraftForge.EVENT_BUS.register(new StructureWorldRenderer)
      FMLCommonHandler.instance().bus().register(this)
      this.setActive(0, BlockPos(0, 5, 0), StructureRegistry.get("test"))
    }

    override def postInit(): Unit = {
      RecipeTraverser.loadVanilla()
      RecipeTraverser.load()
    }

    @SubscribeEvent
    def onKey(event: KeyInputEvent): Unit = {
      if (keyFind.isPressed && Minecraft.getMinecraft.currentScreen == null) {
        val frame = new FindRecipeFrame()
        frame.proxy = frame.createProxy
        Minecraft.getMinecraft.displayGuiScreen(frame.proxy)
      }
    }
  }

}

