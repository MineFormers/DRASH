package de.mineformers.drash.recipe

import net.minecraft.item.{ItemStack, Item}
import net.minecraft.item.crafting._
import net.minecraft.block.Block
import net.minecraftforge.oredict.{ShapelessOreRecipe, ShapedOreRecipe}

/**
 * vanilla
 *
 * @author PaleoCrafter
 */
class ShapedRecipeHandler extends RecipeHandler[ShapedRecipe] {
  override def build(): List[ShapedRecipe] = {
    var list: List[ShapedRecipe] = Nil
    import scala.collection.JavaConversions._
    for (recipe <- CraftingManager.getInstance().getRecipeList.asInstanceOf[java.util.List[IRecipe]])
      recipe match {
        case shaped: ShapedRecipes =>
          list +:= ShapedRecipe(shaped.getRecipeOutput, (shaped.recipeItems map { s => if (s != null) MultiStack(s) else null
          }).toList)
        case shapedOre: ShapedOreRecipe =>
          list +:= ShapedRecipe(shapedOre.getRecipeOutput, (shapedOre.getInput map {
            case list: java.util.List[_] => MultiStack(list.toArray(Array[ItemStack]()))
            case stack: ItemStack => MultiStack(stack)
            case arr: Array[ItemStack] => MultiStack(arr)
            case arr: Array[Block] => MultiStack(arr.map(new ItemStack(_)).toArray)
            case arr: Array[Item] => MultiStack(arr.map(new ItemStack(_)).toArray)
            case b: Block => MultiStack(new ItemStack(b))
            case i: Item => MultiStack(new ItemStack(i))
            case _ => null
          }).toList)
        case _ =>
      }
    list
  }
}

case class ShapedRecipe(result: ItemStack, override val ingredients: List[MultiStack]) extends Recipe

class ShapelessRecipeHandler extends RecipeHandler[ShapelessRecipe] {
  override def build(): List[ShapelessRecipe] = {
    var list: List[ShapelessRecipe] = Nil
    import scala.collection.JavaConversions._
    for (recipe <- CraftingManager.getInstance().getRecipeList.asInstanceOf[java.util.List[IRecipe]])
      recipe match {
        case shapeless: ShapelessRecipes =>
          list +:= ShapelessRecipe(shapeless.getRecipeOutput, (shapeless.recipeItems.asInstanceOf[java.util.List[ItemStack]] map { s => if (s != null) MultiStack(s) else null
          }).toList)
        case shapelessOre: ShapelessOreRecipe =>
          list +:= ShapelessRecipe(shapelessOre.getRecipeOutput, (shapelessOre.getInput map {
            case list: java.util.List[_] => MultiStack(list.toArray(Array[ItemStack]()))
            case stack: ItemStack => MultiStack(stack)
            case arr: Array[ItemStack] => MultiStack(arr)
            case arr: Array[Block] => MultiStack(arr.map(new ItemStack(_)).toArray)
            case arr: Array[Item] => MultiStack(arr.map(new ItemStack(_)).toArray)
            case b: Block => MultiStack(new ItemStack(b))
            case i: Item => MultiStack(new ItemStack(i))
            case _ => null
          }).toList)
        case _ =>
      }
    list
  }
}

case class ShapelessRecipe(result: ItemStack, override val ingredients: List[MultiStack]) extends Recipe

class FurnaceRecipeHandler extends RecipeHandler[FurnaceRecipe] {
  override def build(): List[FurnaceRecipe] = {
    var list: List[FurnaceRecipe] = Nil
    import scala.collection.JavaConversions._
    for((input, output) <- FurnaceRecipes.smelting().getSmeltingList.asInstanceOf[java.util.Map[ItemStack, ItemStack]]) {
      list +:= FurnaceRecipe(output, input)
    }
    list
  }
}

case class FurnaceRecipe(result: ItemStack, override val ingredient: MultiStack) extends Recipe
