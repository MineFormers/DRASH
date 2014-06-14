package de.mineformers.drash.gui

import de.mineformers.core.client.ui.component.container.{ScrollPanel, Panel, Frame}
import de.mineformers.core.client.shape2d.{Point, Size}
import de.mineformers.core.client.ui.component.decoration.{LabelItemStack, Label}
import net.minecraft.client.resources.I18n
import de.mineformers.core.client.ui.component.container.Panel.Padding
import de.mineformers.core.client.ui.component.interaction.{NumberSpinner, Button, TextBox}
import de.mineformers.core.client.ui.layout.{FlowLayout, StackLayout}
import de.mineformers.core.client.ui.reaction.ComponentEvent.{ValueChanged, ButtonPressed, ComponentClicked}
import de.mineformers.drash.recipe.{MultiStack, Recipe, RecipeTraverser}
import de.mineformers.core.client.ui.util.MouseButton
import net.minecraft.item.ItemStack
import de.mineformers.core.client.ui.reaction.Publisher
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.component.Component
import scala.collection.mutable.ListBuffer
import net.minecraftforge.oredict.OreDictionary

/**
 * FindRecipeFrame
 *
 * @author PaleoCrafter
 */
class FindRecipeFrame extends Frame(Size(216, 222)) {
  private var channel: Publisher = _

  override def init(channel: Publisher, context: Context): Unit = {
    super.init(channel, context)
    this.channel = channel
  }

  private var selectionActive = true
  this.background = "frame"
  this.padding = Padding(10)
  val selectPanel = new Panel
  selectPanel.padding = Padding.None
  val label = new Label(I18n.format("label.drash:calcRecipe"))
  selectPanel.add(label)
  val text = new TextBox("", Size(196, 12))
  text.canLoseFocus = false
  text.position += Point(0, 190)
  selectPanel.add(text)
  val scroll = new FilteredScroll
  scroll.position += Point(0, 8)
  selectPanel.add(scroll)

  add(selectPanel)

  reactions += {
    case ComponentClicked(c, button) =>
      if (selectionActive && c.enabled)
        c match {
          case s: LabelItemStack =>
            if (button == MouseButton.Left)
              switchToCalc(s.current)
          case _ =>
        }
  }

  def switchToCalc(stack: ItemStack): Unit = {
    selectionActive = false
    val calcPanel = new CalculatePanel(stack)
    calcPanel.init(channel, context)
    scroll.content.foreach(_.enabled = false)
    content = Seq(calcPanel)
    text.noFocus()
  }

  def switchToSelect(): Unit = {
    selectionActive = true
    for (c <- content(0).asInstanceOf[Panel].content) {
      c.deafTo(channel)
      c.deafTo(context)
    }
    scroll.content.foreach(_.enabled = true)
    content = Seq(selectPanel)
    text.focus()
  }

  class CalculatePanel(stack: ItemStack) extends Panel {
    padding = Padding.None
    val label = new Label(I18n.format("label.drash:calcItem", stack.getDisplayName))
    add(label)
    val buttonBack = new Button(I18n.format("label.drash:back"))
    buttonBack.position = Point(47, 182)
    val buttonClose = new Button(I18n.format("label.drash:close"))
    buttonClose.position = Point(99, 182)

    val lblAmount = new Label(I18n.format("label.drash:amount") + ":")
    lblAmount.position = Point(0, label.height + 5)

    val spinner = new NumberSpinner(60)
    spinner.position = lblAmount.position + Point(lblAmount.width + 2, -1)

    add(lblAmount)
    add(spinner)

    val recipePanel = new Panel
    recipePanel.layout = new StackLayout
    recipePanel.padding = Padding.None
    recipePanel.position = lblAmount.position + Point(0, 4 + lblAmount.height)
    recipePanel.content = build()
    recipePanel.clip = false
    add(recipePanel)
    add(buttonBack)
    add(buttonClose)

    listenTo(buttonBack, buttonClose, spinner)

    reactions += {
      case ButtonPressed(b) =>
        if (b eq buttonBack)
          switchToSelect()
        else if (b eq buttonClose)
          context.close()
      case e: ValueChanged =>
        if (e.c eq spinner)
          recipePanel.content = build()
    }

    def build(): Seq[Panel] = {
      var panels = Seq.empty[Panel]
      var alternatives = Map.empty[Recipe, Seq[(Int, MultiStack)]]
      val recipes = RecipeTraverser.getMatches(stack)
      for (recipe <- recipes) {
        val seq = ListBuffer.empty[(Int, MultiStack)]
        for (ingred <- recipe.ingredients) {
          import scala.collection.JavaConversions._
          var subs = Seq.empty[java.util.List[ItemStack]]
          if (ingred != null)
            for (stack <- ingred.stacks) {
              if (stack.getItemDamage == OreDictionary.WILDCARD_VALUE && stack.getHasSubtypes) {
                val l = new java.util.LinkedList[ItemStack]()
                stack.getItem.getSubItems(stack.getItem, null, l)
                subs +:= l
              }
            }
          val actual = if (subs.nonEmpty) MultiStack(subs.flatten) else ingred
          if (actual != null && !seq.exists(_._2 == actual))
            seq.append((1 * spinner.value, actual))
          else if (actual != null) {
            val i = seq.indexWhere(_._2 == actual)
            seq(i) = (seq(i)._1 + 1 * spinner.value, actual)
          }
        }
        alternatives += recipe -> seq.toSeq
      }
      for ((key, value) <- alternatives) {
        val panel = new Panel
        panel.padding = Padding.None
        panel.clip = false
        panel.layout = new StackLayout(2, horizontal = true)
        for ((a, m) <- value) {
          if (m != null)
            panel.add(new LabelItemStack(m.stacks, false, a))
        }
        val l = new Label(" yields")
        l.position = Point(0, 4)
        panel.add(l)
        panel.add(new LabelItemStack(Array(key.result), false, spinner.value))
        panel.size = Size(panel.contentSize.width, 16)
        panels +:= panel
      }
      panels
    }
  }

  class FilteredScroll extends ScrollPanel(Size(196, 180), _scrollHorizontal = false) {
    layout = new FlowLayout(0, 0)

    listenTo(text)

    reactions += {
      case e: ValueChanged =>
        layout.clear()
        _content.foreach(_.enabled = false)
        _content = items.filter {
          s =>
            val stack = s.stacks(0)
            stack.getDisplayName.toLowerCase.contains(text.text)
        }
        _content.foreach(_.enabled = true)
    }

    override def content: Seq[Component] = _content

    override def content_=(content: Seq[Component]): Unit = {
      _content.foreach(_.enabled = false)
      _content = items.filter(_.stacks(0).getDisplayName.toLowerCase.contains(text.text))
      _content.foreach(_.enabled = true)
    }

    val items = RecipeTraverser.labels
    var _content: Seq[Component] = items
  }

}
