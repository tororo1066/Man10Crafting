package tororo1066.man10crafting.inventory.register

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import tororo1066.man10crafting.Man10Crafting
import tororo1066.tororopluginapi.defaultMenus.CategorySInventory
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class EditMenu: CategorySInventory("§dレシピを編集する") {

    init {
        registerClickSound()
        setOnClick {
            it.isCancelled = true
        }
    }

    override fun renderMenu(p: Player): Boolean {
        val items = LinkedHashMap<String, ArrayList<SInventoryItem>>()

        Man10Crafting.recipes.values
            .sortedBy { it.index }
            .forEach { recipe ->
                val item = SInventoryItem(recipe.result)
                    .addLore(
                        "",
                        "§6状態: ${Items.booleanToString(recipe.enabled)}",
                        "§a優先度: ${recipe.index}",
                        "§d登録優先度: ${recipe.registerIndex}",
                        "§b隠す: ${Items.booleanToString(recipe.hidden)}",
                        "§7${recipe.key}",
                        "§eクリックで編集",
                        "§6シフト左クリックで有効切替",
                        "§cシフト右クリックで削除"
                    )
                    .setCanClick(false)
                    .setClickEvent { e ->
                        if (e.click == ClickType.SHIFT_LEFT) {
                            recipe.enabled = !recipe.enabled
                            if (recipe.enabled) {
                                recipe.register()
                            } else {
                                recipe.unregister()
                            }
                            recipe.save()
                            allRenderMenu(p)
                            return@setClickEvent
                        }
                        if (e.click == ClickType.SHIFT_RIGHT) {
                            Man10Crafting.recipes.remove(recipe.namespacedKey)
                            recipe.unregister()
                            recipe.delete()
                            allRenderMenu(p)
                            return@setClickEvent
                        }

                        moveChildInventory(recipe.getRegisterInventory(), p)
                    }

                val transKey = translate("categories.${recipe.category}")
                if (nowCategory.isEmpty()) nowCategory = transKey
                items.computeIfAbsent(transKey) { ArrayList() }.add(item)
            }

        setResourceItems(items)
        return true
    }
}