package tororo1066.man10crafting.inventory.register

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Material
import org.bukkit.entity.Player
import tororo1066.man10crafting.ingredient.AbstractIngredient
import tororo1066.man10crafting.recipe.AbstractRecipe
import tororo1066.tororopluginapi.defaultMenus.PagedSInventory
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

@Suppress("UnstableApiUsage")
object Items {

    fun SInventoryItem.hideTooltip(): SInventoryItem {
        this.editRaw {
            it.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
        }
        return this
    }

    fun backgroundLightBlue() = SInventoryItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).hideTooltip().setCanClick(false)
    fun backgroundWhite() = SInventoryItem(Material.WHITE_STAINED_GLASS_PANE).hideTooltip().setCanClick(false)
    fun backgroundGray() = SInventoryItem(Material.GRAY_STAINED_GLASS_PANE).hideTooltip().setCanClick(false)
    fun backgroundBlack() = SInventoryItem(Material.BLACK_STAINED_GLASS_PANE).hideTooltip().setCanClick(false)

    fun booleanToString(value: Boolean): String {
        return if (value) "§f§l[§a§l有効§f§l]" else "§f§l[§c§l無効§f§l]"
    }

    fun AbstractRegister<*>.advancedModeActiveItem() =
        SInventoryItem(Material.RED_TERRACOTTA)
            .setDisplayName("§c詳細設定モード: §f§l[§a§l有効§f§l]")
            .setLore(listOf("§7クリックで切替"))
            .setCanClick(false)
            .setClickEvent {
                advancedMode = false
                allRenderMenu(it.whoClicked as Player)
            }

    fun advancedModeActiveItemDisabled() =
        SInventoryItem(Material.RED_TERRACOTTA)
            .setDisplayName("§c詳細設定モード: §f§l[§a§l有効§f§l]")
            .setLore(listOf("§7切替不可"))
            .setCanClick(false)

    fun AbstractRegister<*>.setAdvancedModeItem(slot: Int, onlySupportedAdvanced: Boolean = false) {
        if (advancedMode) {
            if (onlySupportedAdvanced) {
                setItem(slot, advancedModeActiveItemDisabled())
            } else {
                setItem(slot, advancedModeActiveItem())
            }
        } else {
            setItem(slot, advancedModeInactiveItem())
        }

    }

    fun AbstractRegister<*>.advancedModeInactiveItem() =
        SInventoryItem(Material.GREEN_TERRACOTTA)
            .setDisplayName("§a詳細設定モード: §f§l[§c§l無効§f§l]")
            .setLore(listOf("§7クリックで切替"))
            .setCanClick(false)
            .setClickEvent {
                advancedMode = true
                readInput()
                allRenderMenu(it.whoClicked as Player)
            }

    fun SInventory.emptyIngredientItem(onSelect: (AbstractIngredient?) -> Unit) =
        SInventoryItem(Material.BARRIER)
            .setDisplayName("§c素材が設定されていません")
            .setLore(listOf("§7クリックで編集"))
            .setCanClick(false)
            .setClickEvent {
                moveChildInventory(SelectIngredientMenu(onSelect), it.whoClicked as Player)
            }

    fun SInventory.optionsItem(
        recipe: AbstractRecipe,
        appendOptions: (inv: PagedSInventory) -> List<SInventoryItem> = { listOf() }
    ) =
        SInventoryItem(Material.LIME_CONCRETE)
            .setDisplayName("§aオプション")
            .setLore(listOf("§7クリックで編集"))
            .setCanClick(false)
            .setClickEvent {
                val inv = OptionRegister(
                    recipe,
                    appendOptions
                )

                moveChildInventory(inv, it.whoClicked as Player)
            }
}