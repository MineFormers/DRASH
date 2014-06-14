package de.mineformers.drash.recipe

import net.minecraft.item.{Item, ItemStack}
import de.mineformers.core.util.ItemUtils
import net.minecraftforge.oredict.OreDictionary
import net.minecraft.block.Block
import de.mineformers.core.client.ui.component.decoration.LabelItemStack

/**
 * RecipeTraverser
 *
 * @author PaleoCrafter
 */
object RecipeTraverser {
  private var handlerClasses = List.empty[Class[_ <: RecipeHandler[_ <: Recipe]]]
  private var handlers = List.empty[RecipeHandler[_ <: Recipe]]

  def addHandler(handler: Class[_ <: RecipeHandler[_ <: Recipe]]): Unit = {
    handlerClasses +:= handler
  }

  def loadVanilla(): Unit = {
    addHandler(classOf[ShapedRecipeHandler])
    addHandler(classOf[ShapelessRecipeHandler])
    addHandler(classOf[FurnaceRecipeHandler])
  }

  def load(): Unit = {
    for (clazz <- handlerClasses) {
      handlers +:= clazz.newInstance()
    }
  }

  def getMatches(item: Item): Traversable[Recipe] = getMatches(item, OreDictionary.WILDCARD_VALUE)

  def getMatches(item: Item, damage: Int): Traversable[Recipe] = getMatches(new ItemStack(item, 1, damage))

  def getMatches(block: Block): Traversable[Recipe] = getMatches(block, OreDictionary.WILDCARD_VALUE)

  def getMatches(block: Block, damage: Int): Traversable[Recipe] = getMatches(new ItemStack(block, 1, damage))

  def getMatches(stack: ItemStack): Traversable[Recipe] = handlers flatMap (_.getMatches(stack))

  def getUses(item: Item): Traversable[Recipe] = getUses(item, OreDictionary.WILDCARD_VALUE)

  def getUses(item: Item, damage: Int): Traversable[Recipe] = getUses(new ItemStack(item, 1, damage))

  def getUses(block: Block): Traversable[Recipe] = getUses(block, OreDictionary.WILDCARD_VALUE)

  def getUses(block: Block, damage: Int): Traversable[Recipe] = getUses(new ItemStack(block, 1, damage))

  def getUses(stack: ItemStack): Traversable[Recipe] = handlers flatMap (_.getUses(stack))

  def all = handlers flatMap (_.recipes)

  lazy val items = all map (_.result) sortBy (s => (Item.getIdFromItem(s.getItem), s.getItemDamage)) filter {
    var seq = Seq.empty[ItemStack]
    s => {
      if (seq.exists(ItemUtils.stacksEqual(s, _, false)))
        false
      else {
        seq +:= s
        true
      }
    }
  }

  lazy val labels = items map (s => new LabelItemStack(Array(s), true, 0))
}

trait Recipe {
  def result: ItemStack

  def ingredients: List[MultiStack] = {
    val stack = ingredient
    if (stack != null)
      List(stack)
    else
      Nil
  }

  def ingredient: MultiStack = null

  def additionalStacks: List[MultiStack] = {
    val stack = additionalStack
    if (stack != null)
      List(stack)
    else
      Nil
  }

  def additionalStack: MultiStack = null

  override def toString: String = getClass.getSimpleName + "=[result=" + result + ", ingredients=" + ingredients + ", additional=" + additionalStacks + "]"
}

trait RecipeHandler[R <: Recipe] {
  val recipes: List[R] = build()

  def build(): List[R]

  def getMatches(result: ItemStack): Traversable[R] = recipes filter {
    s => ItemUtils.stacksEqual(result, s.result, matchNBT = false)
  }

  def getUses(stack: ItemStack): Traversable[R] = recipes filter {
    _.ingredients exists {
      s =>
        if (s != null)
          s.contains(stack)
        else stack == null
    }
  }
}

object MultiStack {
  implicit def stackToMultiStack(stack: ItemStack): MultiStack = apply(stack)

  def apply(matchNBT: Boolean, stacks: ItemStack*) = new MultiStack(stacks, matchNBT)

  def apply(stacks: ItemStack*) = new MultiStack(stacks, false)

  def apply(stack: ItemStack, matchNBT: Boolean) = new MultiStack(stack, matchNBT)

  def apply(stack: ItemStack) = new MultiStack(stack, false)

  def apply(stacks: Traversable[ItemStack], matchNBT: Boolean) = new MultiStack(stacks, matchNBT)

  def apply(stacks: Traversable[ItemStack]) = new MultiStack(stacks, false)
}

case class MultiStack(stacks: Array[ItemStack], matchNBT: Boolean) {
  def this(stack: ItemStack, matchNBT: Boolean) = this(Array(stack), matchNBT)

  def this(stacks: Traversable[ItemStack], matchNBT: Boolean) = this(stacks.toArray, matchNBT)

  def contains(stack: ItemStack): Boolean = {
    if (stack != null) {
      if (!contains(stack.getItem))
        false
      else {
        var found = false
        import scala.util.control.Breaks._
        breakable(for (is <- stacks) {
          if (is != null) {
            if (ItemUtils.stacksEqual(stack, is, matchNBT)) {
              found = true
              break()
            }
          }
        })
        found
      }
    } else
      stacks.contains(null)
  }

  def containsAll(stack: MultiStack): Boolean = {
    var valid = true
    for (s <- stack.stacks)
      if (!contains(s))
        valid = false
    valid
  }

  def contains(item: Item): Boolean = stacks.exists(_.getItem eq item)

  override def equals(obj: scala.Any): Boolean =
    obj match {
      case s: MultiStack =>
        if (s.stacks.length != stacks.length)
          false
        else
          s.containsAll(this)
      case _ => false
    }

  override def toString: String = "MultiStack=" + stacks.mkString("[", ",", "]")
}