package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
import tororo1066.man10crafting.Man10Crafting
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

class OptionRegister(
    var index: Int,
    val indexFunc: (Int) -> Unit,
    var registerIndex: Int?,
    val registerIndexFunc: (Int?) -> Unit,
    var hidden: Boolean = false,
    val hiddenFunc: (Boolean) -> Unit
): SInventory(Man10Crafting.plugin,"§aオプション",3) {

    override fun renderMenu(): Boolean {
        setItems(0..26, SInventoryItem(SItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ")).setCanClick(false))
        setItem(10, createInputItem(SItem(Material.BOOK).setDisplayName("§6レシピブックの優先度")
            .addLore("§a優先度:${index}")
            .addLore("§e数値が低いほど前に表示されます"), Int::class.java, "§a数値を入力してください") { int, p ->
            index = int
            indexFunc(int)
            p.sendPlainMessage("§b" + int + "に変更しました")
        })

        setItem(11, createNullableInputItem(SItem(Material.WRITTEN_BOOK).setDisplayName("§6レシピ登録の優先度")
            .addLore("§a優先度:${registerIndex}")
            .addLore("§e数値が低いほど先に登録されます")
            .addLore("nullでレシピブックの優先度と同じになります"), Int::class.java, "§a数値を入力してください") { int, p ->
            registerIndex = int
            registerIndexFunc(int)
            p.sendPlainMessage("§b" + int + "に変更しました")
        })

        setItem(12, SInventoryItem(SItem(Material.CYAN_CONCRETE).setDisplayName("§6非表示設定"))
            .addLore("§a現在:${if (hidden) "非表示" else "表示"}").setCanClick(false)
            .setClickEvent {
                hidden = !hidden
                hiddenFunc(hidden)
                allRenderMenu()
            })
        return true
    }

    companion object {
        fun SInventory.generateItem(
            index: Int,
            indexFunc: (Int) -> Unit,
            registerIndex: Int?,
            registerIndexFunc: (Int?) -> Unit,
            hidden: Boolean,
            hiddenFunc: (Boolean) -> Unit
        ): SInventoryItem {
            return SInventoryItem(SItem(Material.LIME_CONCRETE).setDisplayName("§aオプション"))
                .setCanClick(false).setClickEvent {
                    val optionMenu = OptionRegister(
                        index,
                        indexFunc,
                        registerIndex,
                        registerIndexFunc,
                        hidden,
                        hiddenFunc
                    )
                    moveChildInventory(optionMenu, it.whoClicked as Player)
                }
        }
    }
}