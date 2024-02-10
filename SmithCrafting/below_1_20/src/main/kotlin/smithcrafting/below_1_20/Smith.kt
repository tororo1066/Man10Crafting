package smithcrafting.below_1_20

import smithcrafting.base.SmithBase
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.*
import org.bukkit.plugin.java.JavaPlugin
import tororo1066.tororopluginapi.SInput
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

class Smith: SmithBase {

    override fun create(
        namespacedKey: NamespacedKey,
        material: ItemStack,
        smithingMaterial: ItemStack,
        additionalMaterial: ItemStack?,
        result: ItemStack,
        transform: Boolean,
        copyNbt: Boolean
    ): Recipe {
        val recipe = SmithingRecipe(
            namespacedKey,
            result,
            RecipeChoice.ExactChoice(material),
            RecipeChoice.ExactChoice(smithingMaterial),
            copyNbt
        )
        return recipe
    }

    override fun registerInventory(
        plugin: JavaPlugin,
        sInput: SInput,
        onSave: (SmithBase.SaveData) -> Boolean
    ): SInventory {
        return SmithInventory(plugin,sInput,null,onSave)
    }

    override fun editInventory(
        plugin: JavaPlugin,
        sInput: SInput,
        data: SmithBase.SaveData,
        onSave: (SmithBase.SaveData) -> Boolean
    ): SInventory {
        val inv = object : SmithInventory(plugin,sInput,data,onSave) {
            override fun renderMenu(): Boolean {
                super.renderMenu()
                setItem(19,data.singleMaterial)
                setItem(22,data.smithingMaterial)
                setItem(25,data.result)

                setItem(43, SInventoryItem(Material.WRITABLE_BOOK).setDisplayName("§a上書き保存").setCanClick(false).setClickEvent {
                    save(it.whoClicked as Player,data.namespace,data.category,data.index,data.copyNbt,data.transform,onSave)
                    it.whoClicked.closeInventory()
                })
                return true
            }
        }

        return inv
    }

    override fun viewInventory(
        plugin: JavaPlugin,
        p: Player,
        data: SmithBase.SaveData,
        moveOtherRecipeItem: (p: Player, inv: SInventory, item: ItemStack) -> SInventoryItem
    ): SInventory {
        val recipeInfoMenu = object : SInventory(plugin,data.result.itemMeta.displayName + "§l§7のレシピ",6) {
            override fun renderMenu(): Boolean {
                setOnClick {
                    it.isCancelled = true
                }
                val white = SInventoryItem(Material.WHITE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false)
                val gray = SInventoryItem(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false)

                setItems(0..8, white)
                setItems(36..53, white)
                setItems(9..35, gray)

                setItem(19,moveOtherRecipeItem(p,this,data.singleMaterial))
                setItem(21,moveOtherRecipeItem(p,this,data.smithingMaterial))
                setItem(23,SInventoryItem(Material.SMITHING_TABLE).setCanClick(false))
                setItem(25,moveOtherRecipeItem(p,this,data.result))
                return true
            }
        }
        return recipeInfoMenu
    }

    private open class SmithInventory(plugin: JavaPlugin, val sInput: SInput, data: SmithBase.SaveData?, val onSave: (SmithBase.SaveData) -> Boolean): SInventory(plugin,"§5Smithing",5) {
        var index = data?.index?:Int.MAX_VALUE
        var copyNbt = data?.copyNbt?:false
        var transform = data?.transform?:false

        override fun renderMenu(): Boolean {
            setItems(0..44, SInventoryItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false))
            val whitePane = SInventoryItem(Material.WHITE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false)
            setItems(9..35,whitePane)
            removeItems(listOf(19,22,25))

            setItem(4, SInventoryItem(Material.LIME_CONCRETE).setDisplayName("§aオプション").setCanClick(false).setClickEvent {
                val optionMenu = object : SInventory(plugin,"§aオプション",3) {
                    override fun renderMenu(): Boolean {
                        setItems(0..26,
                            SInventoryItem(SItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ")).setCanClick(false))
                        setItem(10,createInputItem(SItem(Material.BOOK).setDisplayName("§6レシピブックで表示される優先度").addLore("§a優先度:${index}").addLore("§e数値が低いほど前に出てきます"),Int::class.java,"§a数値を入力してください") { int, p ->
                            index = int
                            p.sendMessage("§b" + int + "に変更しました")
                        })
                        setItem(11, SInventoryItem(Material.PAPER).setDisplayName("§dNBTを保持する")
                            .addLore("§a現在の値: $copyNbt").setCanClick(false).setClickEvent { _ ->
                                copyNbt = !copyNbt
                                allRenderMenu()
                            })
                        return true
                    }
                }
                moveChildInventory(optionMenu,it.whoClicked as Player)
            })

            setItem(44,createInputItem(
                SItem(Material.WRITABLE_BOOK).setDisplayName("§a保存"),String::class.java,"カテゴリー名を入力してください",
                invOpenCancel = true) { category, p ->
                sInput.sendInputCUI(p,String::class.java,"内部名を入力してください") { str ->
                    save(p,str,category,index,copyNbt,transform,onSave)
                }
            })
            return true
        }
    }

    companion object {
        private fun SInventory.save(p: Player, namespace: String, category: String, index: Int, copyNbt: Boolean, transform: Boolean, onSave: (SmithBase.SaveData) -> Boolean) {
            val material = getItem(19)
            val smithingMaterial = getItem(22)
            val result = getItem(25)
            if (material == null || smithingMaterial == null || result == null){
                p.sendMessage("§c素材、または完成品のアイテムが存在しません")
                open(p)
                return
            }
            if (onSave(
                    SmithBase.SaveData(
                        namespace,
                        category,
                        index,
                        material,
                        smithingMaterial,
                        null,
                        result,
                        copyNbt,
                        transform
                    )
                )){
                p.sendMessage("§a保存に成功しました")
            } else {
                p.sendMessage("§c素材、または完成品のアイテムが存在しません")
                open(p)
            }
        }
    }
}