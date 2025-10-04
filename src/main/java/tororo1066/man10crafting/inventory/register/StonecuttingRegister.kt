package tororo1066.man10crafting.inventory.register

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.ingredient.ItemStackIngredient
import tororo1066.man10crafting.inventory.register.Items.emptyIngredientItem
import tororo1066.man10crafting.inventory.register.Items.optionsItem
import tororo1066.man10crafting.inventory.register.Items.setAdvancedModeItem
import tororo1066.man10crafting.recipe.stonecutting.StonecuttingCraftingRecipe
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class StonecuttingRegister(
    currentData: StonecuttingCraftingRecipe? = null,
) : AbstractRegister<StonecuttingCraftingRecipe>(
    "§7Stonecutter",
    currentData ?: StonecuttingCraftingRecipe(),
    currentData != null
) {

    var input: Pair<AbstractIngredient, Int>? = null

    override val task = object : BukkitRunnable() {
        override fun run() {
            if (!advancedMode) return
            val ingredient = input ?: return
            val nextIndex = ingredient.first.getNextIndex(ingredient.second)
            input = ingredient.first to nextIndex
            setItem(20, editItem(ingredient.first, nextIndex))
        }
    }.runTaskTimer(SJavaPlugin.plugin, 20, 20)

    private fun editItem(ingredient: AbstractIngredient, index: Int) =
        ingredient.editItem(this, index) { p, newIngredient ->
            input = if (newIngredient == null) null else Pair(newIngredient, 0)
            allRenderMenu(p)
        }

    override fun readInput() {
        val item = inv.getItem(20)
        input = if (item != null) {
            itemStackToIngredient(item)
        } else {
            null
        }
    }

    override fun prepareSave(): Boolean {
        val input = input ?: return false
        if (!input.first.isValid()) return false
        val result = inv.getItem(24) ?: return false

        currentData.input = input.first
        currentData.result = result
        return true
    }

    //0  1  2  3  p  5  6  7  8
    //9  10 11 12 a  14 15 16 17
    //18 19 i  21 22 23 o  25 26
    //27 28 29 30 31 32 33 34 35
    //36 37 38 39 40 41 42 43 s
    //p: options
    //a: advanced mode
    //i: input
    //o: output
    //s: save

    init {
        fillItem(Items.backgroundLightBlue())
        setItems(listOf(11,15,19,21,23,25,29,33), Items.backgroundWhite())
        setItem(22, SInventoryItem(Material.STONECUTTER).editRaw {
            @Suppress("UnstableApiUsage")
            it.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
        })
        removeItems(listOf(20,24))

        this.currentData.getInputOrNull()?.let {
            input = Pair(it, 0)
        }

        val result = this.currentData.getResultOrNull()
        if (result != null) {
            inv.setItem(24, result.clone())
        }
    }

    override fun renderMenu(p: Player): Boolean {

        val onlySupportedAdvanced = input?.let { input -> onlySupportedAdvanced(input.first) } ?: false
        if (onlySupportedAdvanced) advancedMode = true

        setAdvancedModeItem(13, onlySupportedAdvanced)

        if (advancedMode) {
            input?.let { input ->
                setItem(20, editItem(input.first, input.second))
            } ?: run {
                setItem(20, emptyIngredientItem { ingredient ->
                    if (ingredient != null) {
                        this.input = Pair(ingredient, 0)
                        allRenderMenu(p)
                    }
                })
            }
        } else {
            removeItem(20)
            val ingredient = input?.first
            if (ingredient !is ItemStackIngredient || ingredient.itemStacks.size != 1) {
                input = null
            } else {
                inv.setItem(20, ingredient.itemStacks.first().clone())
            }
        }

        setItem(4, optionsItem(this.currentData))

        setItem(44, saveItem())

        return true
    }
}