package tororo1066.man10crafting.ingredient

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import tororo1066.man10crafting.inventory.register.Items
import tororo1066.man10crafting.inventory.register.SelectIngredientMenu
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.defaultMenus.SingleItemInventory
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem
import java.util.function.Consumer

open class MaterialIngredient: AbstractIngredient() {

    val materials = HashMap<Material, Int>()
    val tags = HashMap<Tag<Material>, Int>()
    override val displayItems: List<ItemStack>
        get() = materials.map { ItemStack(it.key, it.value) } + tags.flatMap { it.key.values.map { mat -> ItemStack(mat, it.value) } }

    override fun createBukkitRecipeChoice(): RecipeChoice {
        val materialList = materials.keys.toMutableList()
        tags.forEach { materialList.addAll(it.key.values) }
        return RecipeChoice.MaterialChoice(materialList)
    }

    override fun validate(itemStack: ItemStack): Boolean {
        return getAmount(itemStack) != null
    }

    override fun getAmount(itemStack: ItemStack): Int? {
        val matAmount = materials[itemStack.type]
        if (matAmount != null) {
            if (itemStack.amount >= matAmount) return matAmount
        }
        for (tag in tags.keys) {
            if (tag.isTagged(itemStack.type)) {
                val tagAmount = tags[tag] ?: continue
                if (itemStack.amount >= tagAmount) return tagAmount
            }
        }
        return null
    }

    override fun isSimilar(itemStack: ItemStack): Boolean {
        if (materials.containsKey(itemStack.type)) return true
        for (tag in tags.keys) {
            if (tag.isTagged(itemStack.type)) return true
        }
        return false
    }

    override fun editItem(
        inventory: SInventory,
        index: Int,
        onSelectNewIngredient: (player: Player, newIngredient: AbstractIngredient?) -> Unit
    ): SInventoryItem {
        val item = displayItems.getOrNull(index) ?: ItemStack(Material.LAPIS_BLOCK)
        val sInventoryItem =
            SInventoryItem(item)
                .setDisplayName("§bMaterialIngredient")
                .setLore(
                    "§7アイテムの種類だけで素材かどうか判定します",
                    "§7タグを用いることも可能です",
                    "",
                    "§e左クリックで編集",
                    "§e右クリックで素材の種類を変更する"
                )
                .setCanClick(false)
                .setClickEvent { e ->
                    val p = e.whoClicked as Player
                    if (e.click == ClickType.RIGHT) {
                        val inv = SelectIngredientMenu { newIngredient ->
                            onSelectNewIngredient(p, newIngredient)
                        }
                        inventory.moveChildInventory(inv, p)
                        return@setClickEvent
                    }

                    if (e.click != ClickType.LEFT) return@setClickEvent

                    val inv = object : SInventory(SJavaPlugin.plugin, "§bMaterialIngredient", 3) {
                        override fun renderMenu(p: Player): Boolean {
                            fillItem(Items.backgroundLightBlue())
                            setItem(
                                11,
                                SInventoryItem(Material.LAPIS_BLOCK)
                                    .setDisplayName("§aMaterialを追加する")
                                    .setCanClick(false)
                                    .setClickEvent {
                                        val inv = object : LargeSInventory("§aMaterialの管理") {
                                            override fun renderMenu(p: Player): Boolean {
                                                val items = arrayListOf<SInventoryItem>()
                                                materials.forEach { (mat, amount) ->
                                                    items.add(
                                                        SInventoryItem(mat)
                                                            .setItemAmount(amount)
                                                            .setDisplayName("§a${mat.name}")
                                                            .addLore("§cシフト右クリックで削除")
                                                            .setCanClick(false)
                                                            .setClickEvent { e ->
                                                                if (e.click.isShiftClick && e.isRightClick) {
                                                                    materials.remove(mat)
                                                                    allRenderMenu(p)
                                                                }
                                                            }
                                                    )
                                                }

                                                items.add(
                                                    SInventoryItem(Material.EMERALD_BLOCK)
                                                        .setDisplayName("§a追加")
                                                        .setCanClick(false)
                                                        .setClickEvent { _ ->
                                                            val selectItemInv = SingleItemInventory(
                                                                SJavaPlugin.plugin,
                                                                "§a追加するMaterialを選択"
                                                            )
                                                            selectItemInv.onConfirm = Consumer { itemStack ->
                                                                if (itemStack.type == Material.AIR) {
                                                                    p.closeInventory()
                                                                    return@Consumer
                                                                }
                                                                materials[itemStack.type] = itemStack.amount
                                                                p.closeInventory()
                                                                renderMenu(p)
                                                            }
                                                            moveChildInventory(selectItemInv, p)
                                                        }
                                                )

                                                setResourceItems(items)
                                                return true
                                            }
                                        }
                                        moveChildInventory(inv, p)
                                    }
                            )

                            setItem(
                                15,
                                SInventoryItem(Material.WRITABLE_BOOK)
                                    .setDisplayName("§bTagを追加する")
                                    .setCanClick(false)
                                    .setClickEvent { _ ->
                                        val inv = object: LargeSInventory("§bTagの管理") {
                                            override fun renderMenu(p: Player): Boolean {
                                                val items = arrayListOf<SInventoryItem>()
                                                tags.forEach { (tag, amount) ->
                                                    items.add(
                                                        SInventoryItem(itemsTag.find { it.key == tag.key }?.values?.firstOrNull() ?: Material.BOOK)
                                                            .setItemAmount(amount)
                                                            .setDisplayName("§b${tag.key}")
                                                            .addLore("§cシフト右クリックで削除")
                                                            .setCanClick(false)
                                                            .setClickEvent { e ->
                                                                if (e.click.isShiftClick && e.isRightClick) {
                                                                    tags.remove(tag)
                                                                    allRenderMenu(p)
                                                                }
                                                            }
                                                    )
                                                }

                                                items.add(
                                                    SInventoryItem(Material.EMERALD_BLOCK)
                                                        .setDisplayName("§a追加")
                                                        .setCanClick(false)
                                                        .setClickEvent {
                                                            val tagSelectInv = object: LargeSInventory("§a追加するTagを選択") {
                                                                override fun renderMenu(p: Player): Boolean {
                                                                    val items = arrayListOf<SInventoryItem>()
                                                                    itemsTag.forEach { tag ->
                                                                        if (tags.keys.any { it.key == tag.key }) return@forEach
                                                                        items.add(
                                                                            createInputItem(
                                                                                SItem(
                                                                                    tag.values.firstOrNull()
                                                                                        ?: Material.BOOK
                                                                                )
                                                                                    .setDisplayName("§a${tag.key}")
                                                                                    .addLore("§e左クリックで選択"),
                                                                                Int::class.java,
                                                                                "§a必要な数を入力してください"
                                                                            ) { amount, p ->
                                                                                tags[tag] = amount
                                                                                p.sendPlainMessage("§b${tag.key}を追加しました")
                                                                            }
                                                                        )
                                                                    }
                                                                    setResourceItems(items)
                                                                    return true
                                                                }
                                                            }
                                                            moveChildInventory(tagSelectInv, p)
                                                        }
                                                    )

                                                setResourceItems(items)
                                                return true
                                            }
                                        }

                                        moveChildInventory(inv, p)
                                    }
                            )

                            return true
                        }
                    }

                    inventory.moveChildInventory(inv, p)
                }
        return sInventoryItem
    }

    override fun isValid(): Boolean {
        return materials.isNotEmpty() || tags.isNotEmpty()
    }

    override fun serialize(): Map<String?, Any?> {
        val map = HashMap<String?, Any?>()
        map["materials"] = materials.map { it.key.name to it.value }.toMap()
        map["tags"] = tags.map { it.key.key.toString() to it.value }.toMap()
        return map
    }

    companion object {

        private val itemsTag = Bukkit.getTags("items", Material::class.java)

        @JvmStatic
        @Suppress("unused", "UNCHECKED_CAST")
        fun deserialize(args: Map<String?, Any?>): MaterialIngredient? {
            val ingredient = MaterialIngredient()
            val mats = args["materials"] as? Map<String, Int>
            mats?.forEach { (mat, amount) ->
                ingredient.materials[Material.getMaterial(mat) ?: return@forEach] = amount
            }
            val tags = args["tags"] as? Map<String, Int>
            tags?.forEach { (tag, amount) ->
                val namespacedKey = NamespacedKey.fromString(tag) ?: return@forEach
                val tagObj = Bukkit.getTag("items", namespacedKey, Material::class.java) ?: return@forEach
                ingredient.tags[tagObj] = amount
            }

            if (ingredient.materials.isEmpty() && ingredient.tags.isEmpty()) return null

            return ingredient
        }
    }
}