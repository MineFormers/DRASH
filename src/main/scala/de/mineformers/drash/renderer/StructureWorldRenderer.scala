package de.mineformers.drash.renderer

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.client.event.{RenderWorldEvent, RenderWorldLastEvent}
import org.lwjgl.opengl.{GL14, GL11}
import net.minecraft.client.Minecraft
import de.mineformers.drash.DRASH
import net.minecraft.client.renderer.culling.Frustrum
import net.minecraft.entity.Entity
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent
import de.mineformers.core.util.renderer.ShaderSystem
import org.lwjgl.opengl.GL20._

/**
 * StructureWorldRenderer
 *
 * @author PaleoCrafter
 */
class StructureWorldRenderer {
  final val vertex =
    """#version 120
      |
      |void main() {
      | gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
      | gl_TexCoord[0] = gl_MultiTexCoord0;
      |}
    """.stripMargin
  final val fragment =
    """#version 120
      |uniform float alpha; uniform sampler2D tex;
      |
      |void main() {
      | gl_FragColor = texture2D(tex, vec2(gl_TexCoord[0])) * vec4(1.0, 1.0, 1.0, alpha);
      |}
    """.stripMargin
  val shaders = new ShaderSystem((vertex, GL_VERTEX_SHADER), (fragment, GL_FRAGMENT_SHADER))

  @SubscribeEvent
  def onRenderLast(event: RenderWorldLastEvent): Unit = {
    val active = DRASH.environment.getActive(Minecraft.getMinecraft.theWorld.provider.dimensionId)
    if (active != null) {
      val x = active.pos.x
      val y = active.pos.y
      val z = active.pos.z
      val frustrum = new Frustrum()
      val cam = Minecraft.getMinecraft.renderViewEntity
      val camX: Double = cam.lastTickPosX + (cam.posX - cam.lastTickPosX) * event.partialTicks
      val camY: Double = cam.lastTickPosY + (cam.posY - cam.lastTickPosY) * event.partialTicks
      val camZ: Double = cam.lastTickPosZ + (cam.posZ - cam.lastTickPosZ) * event.partialTicks
      frustrum.setPosition(camX, camY, camZ)
      if (frustrum.isBoundingBoxInFrustum(active.bounds)) {
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F)
        translateToWorldCoords(Minecraft.getMinecraft.renderViewEntity, event.partialTicks)
        GL11.glTranslatef(x, y, z)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        shaders.activate()
        shaders.setUniform1i("tex", 0)
        shaders.setUniform1f("alpha", 0.5F)
        val list = active.chunkRenderers
        for (pass <- 0 until 2)
          list foreach {
            _.render(pass)
          }
        shaders.deactivate()
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glColor4f(1F, 1F, 1F, 1F)
        GL11.glPopMatrix()
      }
    }
  }

  @SubscribeEvent
  def onRenderBlock(event: RenderWorldEvent.Pre): Unit = {
    test("test")
  }

  def test(string: String): Unit = {
    println(string)
  }

  @SubscribeEvent
  def onTick(event: ClientTickEvent): Unit = {
    if (Minecraft.getMinecraft.theWorld != null) {
      val active = DRASH.environment.getActive(Minecraft.getMinecraft.theWorld.provider.dimensionId)
      if (active != null) {
        active.structure.update(active)
      }
    }
  }

  def translateToWorldCoords(entity: Entity, frame: Float): Unit = {
    val interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * frame
    val interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * frame
    val interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * frame

    GL11.glTranslated(-interpPosX, -interpPosY, -interpPosZ)
  }
}
