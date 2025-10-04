package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.CategoryDisplayItem
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem
import java.util.concurrent.CompletableFuture

class CategoryDisplayItemEditMenu: LargeSInventory("CategoryDisplayItemEditMenu") {
    init {
        registerClickSound()
        setOnClick {
            it.isCancelled = true
        }
    }

    override fun renderMenu(p: Player): Boolean {
        val items = arrayListOf<SInventoryItem>()

        Man10Crafting.categoryDisplayItems.entries.forEach {
            val item = SInventoryItem(it.value.itemStack.clone())
                .apply {
                    if (!it.value.respectItemStackName) {
                        setDisplayName(it.key)
                    }
                }
                .addLore("§cシフト右クリックで削除")
                .setCanClick(false)
                .setClickEvent { e ->
                    if (e.click == ClickType.SHIFT_RIGHT){
                        val config = Man10Crafting.plugin.config
                        config.set("categoryDisplayItems.${it.key}",null)

                        CompletableFuture.runAsync {
                            Man10Crafting.plugin.saveConfig()
                        }.thenRun {
                            Man10Crafting.categoryDisplayItems.remove(it.key)
                            allRenderMenu(p)
                        }

                        return@setClickEvent
                    }
                }
            items.add(item)
        }

        setResourceItems(items)
        return true
    }

    override fun afterRenderMenu() {
        super.afterRenderMenu()
        setItem(51, createInputItem(SItem(Material.EMERALD_BLOCK).setDisplayName("§a追加"), String::class.java, "§aアイテムを手に持ってカテゴリ名を入力", invOpenCancel = true, action = { str, p ->
            val item = p.inventory.itemInMainHand.clone()

            Man10Crafting.sInput.sendInputCUI(p, Boolean::class.java, "§aアイテムの名前を尊重するか(true/false)", action = { respectItemStackName ->
                val section = Man10Crafting.plugin.config.getConfigurationSection("categoryDisplayItems")?:Man10Crafting.plugin.config.createSection("categoryDisplayItems")
                val displaySection = section.createSection(str)
                displaySection.set("itemStack",item)
                displaySection.set("respectItemStackName",respectItemStackName)
                CompletableFuture.runAsync {
                    Man10Crafting.plugin.saveConfig()
                }.thenRun {
                    Man10Crafting.categoryDisplayItems[str] = CategoryDisplayItem(item, respectItemStackName)
                    open(p)
                }
            }, onCancel = { open(p) })
        }))
    }
}