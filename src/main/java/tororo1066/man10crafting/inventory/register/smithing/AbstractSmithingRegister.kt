package tororo1066.man10crafting.inventory.register.smithing

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.ingredient.ItemStackIngredient
import tororo1066.man10crafting.inventory.register.AbstractRegister
import tororo1066.man10crafting.inventory.register.Items
import tororo1066.man10crafting.inventory.register.Items.emptyIngredientItem
import tororo1066.man10crafting.inventory.register.Items.optionsItem
import tororo1066.man10crafting.inventory.register.Items.setAdvancedModeItem
import tororo1066.man10crafting.recipe.smithing.AbstractSmithingRecipe
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.sInventory.SInventoryItem

abstract class AbstractSmithingRegister<T: AbstractSmithingRecipe>(
    name: String,
    currentData: T,
    isEdit: Boolean
): AbstractRegister<T>(
    name,
    currentData,
    isEdit
) {

    var templateIngredient: Pair<AbstractIngredient, Int>? = null
    var baseIngredient: Pair<AbstractIngredient, Int>? = null
    var additionIngredient: Pair<AbstractIngredient, Int>? = null

    override val task = object : BukkitRunnable() {
        override fun run() {
            if (!advancedMode) return
            listOf(templateIngredient, baseIngredient, additionIngredient).forEachIndexed { index, ingredient ->
                if (ingredient == null) return@forEachIndexed
                val nextIndex = ingredient.first.getNextIndex(ingredient.second)
                when(index) {
                    0 -> templateIngredient = ingredient.first to nextIndex
                    1 -> baseIngredient = ingredient.first to nextIndex
                    2 -> additionIngredient = ingredient.first to nextIndex
                }
                val slot = 19 + index
                setItem(slot, editItem(ingredient.first, nextIndex) { p, newIngredient ->
                    when(index) {
                        0 -> templateIngredient = if (newIngredient == null) null else newIngredient to 0
                        1 -> baseIngredient = if (newIngredient == null) null else newIngredient to 0
                        2 -> additionIngredient = if (newIngredient == null) null else newIngredient to 0
                    }
                    allRenderMenu(p)
                })
            }
        }
    }.runTaskTimer(SJavaPlugin.plugin, 20, 20)

    private fun editItem(ingredient: AbstractIngredient, index: Int, onSelect: (player: Player, newIngredient: AbstractIngredient?) -> Unit) =
        ingredient.editItem(this, index) { player, newIngredient ->
            onSelect(player, newIngredient)
            allRenderMenu(player)
        }

    override fun readInput() {
        val template = inv.getItem(19)
        val base = inv.getItem(20)
        val addition = inv.getItem(21)
        templateIngredient = if (template != null) {
            itemStackToIngredient(template)
        } else {
            null
        }
        baseIngredient = if (base != null) {
            itemStackToIngredient(base)
        } else {
            null
        }
        additionIngredient = if (addition != null) {
            itemStackToIngredient(addition)
        } else {
            null
        }
    }

    override fun prepareSave(): Boolean {
        if (templateIngredient == null && baseIngredient == null && additionIngredient == null) {
            return false
        }
        templateIngredient?.first?.let {
            if (!it.isValid()) return false
        }
        baseIngredient?.first?.let {
            if (!it.isValid()) return false
        }
        additionIngredient?.first?.let {
            if (!it.isValid()) return false
        }

        currentData.template = templateIngredient?.first
        currentData.base = baseIngredient?.first
        currentData.addition = additionIngredient?.first

        return true
    }

    //0  1  2  3  o  5  6  7  8
    //9  10 11 12 13 14 15 16 17
    //18 t  b  a  ad 23 24 r  26
    //27 28 29 30 31 32 33 34 35
    //36 37 38 39 40 41 42 43 s
    //o: optionSlots
    //t: template
    //b: base
    //a: addition
    //ad: advanced mode
    //r: result
    //s: save

    init {
        fillItem(Items.backgroundLightBlue())
        setItem(
            10,
            SInventoryItem(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
            .setDisplayName("§aテンプレート")
            .setCanClick(false)
        )
        setItem(
            11,
            SInventoryItem(Material.DIAMOND_PICKAXE)
                .setDisplayName("§aベース")
                .setCanClick(false)
        )
        setItem(
            12,
            SInventoryItem(Material.NETHERITE_INGOT)
                .setDisplayName("§a追加素材")
                .setCanClick(false)
        )
        removeItems(listOf(19,20,21))

        this.currentData.template?.let {
            templateIngredient = it to 0
        }
        this.currentData.base?.let {
            baseIngredient = it to 0
        }
        this.currentData.addition?.let {
            additionIngredient = it to 0
        }
    }

    override fun renderMenu(p: Player): Boolean {
        val onlySupportedAdvanced = onlySupportedAdvanced(
            listOfNotNull(
                templateIngredient?.first,
                baseIngredient?.first,
                additionIngredient?.first
            )
        )
        if (onlySupportedAdvanced) advancedMode = true

        setAdvancedModeItem(22, onlySupportedAdvanced)

        if (advancedMode) {
            fun setIngredientItem(slot: Int, ingredientPair: Pair<AbstractIngredient, Int>?, onSelect: (AbstractIngredient?) -> Unit) {
                if (ingredientPair == null) {
                    setItem(slot, emptyIngredientItem { ingredient ->
                        onSelect(ingredient)
                        allRenderMenu(p)
                    })
                } else {
                    setItem(slot, editItem(ingredientPair.first, ingredientPair.second) { player, newIngredient ->
                        onSelect(newIngredient)
                        allRenderMenu(player)
                    })
                }
            }
            setIngredientItem(19, templateIngredient) { ingredient ->
                templateIngredient = if (ingredient == null) null else ingredient to 0
            }
            setIngredientItem(20, baseIngredient) { ingredient ->
                baseIngredient = if (ingredient == null) null else ingredient to 0
            }
            setIngredientItem(21, additionIngredient) { ingredient ->
                additionIngredient = if (ingredient == null) null else ingredient to 0
            }
        } else {
            fun itemStackOrNull(ingredientPair: Pair<AbstractIngredient, Int>?): ItemStack? {
                val ingredient = ingredientPair?.first ?: return null
                if (ingredient !is ItemStackIngredient || ingredient.itemStacks.size != 1) return null
                return ingredient.itemStacks.first().clone()
            }

            removeItems(listOf(19,20,21))
            itemStackOrNull(templateIngredient)?.let {
                inv.setItem(19, it)
            } ?: run {
                templateIngredient = null
            }
            itemStackOrNull(baseIngredient)?.let {
                inv.setItem(20, it)
            } ?: run {
                baseIngredient = null
            }
            itemStackOrNull(additionIngredient)?.let {
                inv.setItem(21, it)
            } ?: run {
                additionIngredient = null
            }
        }

        setItem(4, optionsItem(currentData))

        setItem(44, saveItem())

        return true
    }
}