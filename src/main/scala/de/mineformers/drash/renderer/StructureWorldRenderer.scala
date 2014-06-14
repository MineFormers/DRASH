package de.mineformers.drash.renderer

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import org.lwjgl.opengl.{GL14, GL11}
import net.minecraft.client.Minecraft
import de.mineformers.drash.DRASH
import net.minecraft.client.renderer.culling.Frustrum
import net.minecraft.entity.Entity
import de.mineformers.drash.api.StructureRegistry

/**
 * StructureWorldRenderer
 *
 * @author PaleoCrafter
 */
class StructureWorldRenderer {
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
        GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA)
        GL14.glBlendColor(0, 0, 0, 0.5F)
        val list = active.chunkRenderers
        for (pass <- 0 until 3)
          list foreach {
            _.render(pass)
          }
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glColor4f(1F, 1F, 1F, 1F)
        GL11.glPopMatrix()
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
