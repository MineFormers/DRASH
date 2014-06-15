package de.mineformers.drash.api

import de.mineformers.drash.api.structure.Structure
import de.mineformers.drash.structure.Structures
import net.minecraft.util.ResourceLocation

/**
 * StructureRegistry
 *
 * @author PaleoCrafter
 */
object StructureRegistry {
  private val instance = new Structures

  def get(name: String): Structure = {
    instance.apply(name)
  }

  def add(name: String, structure: Structure) {
    instance.add(name, structure)
  }

  def add(name: String, resource: ResourceLocation) {
    instance.load(name, resource)
  }
}