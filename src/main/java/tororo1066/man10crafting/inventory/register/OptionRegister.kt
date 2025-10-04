package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
import tororo1066.man10crafting.recipe.AbstractRecipe
import tororo1066.man10crafting.recipe.options.CommandExecutable
import tororo1066.man10crafting.recipe.options.Permissible
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.defaultMenus.PagedSInventory
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

class OptionRegister(
    val recipe: AbstractRecipe,
    val appendOptions: (inv: PagedSInventory) -> List<SInventoryItem> = { listOf() }
): PagedSInventory(SJavaPlugin.plugin,"§aオプション",3) {

    //0  1  2  3  4  5  6  7  8
    //9  o  o  o  o  o  o  o  17
    //18 l  20 21 22 23 24 r  26
    //o: optionSlots
    //l: leftSlots
    //r: rightSlots

    val optionSlots = listOf(10,11,12,13,14,15,16)

    init {
        registerClickSound()
        setOnClick {
            it.isCancelled = true
        }
    }

    override fun renderMenu(p: Player): Boolean {

        val options = mutableListOf(
            createInputItem(
                SItem(Material.BOOK)
                    .setDisplayName("§6レシピブックの優先度")
                    .addLore("§a優先度: ${recipe.index}")
                    .addLore("§e数値が低いほど前に表示されます"), Int::class.java, "§a数値を入力してください"
            ) { int, p ->
                recipe.index = int
                p.sendPlainMessage("§b" + int + "に変更しました")
            },
            createNullableInputItem(
                SItem(Material.WRITTEN_BOOK)
                    .setDisplayName("§6レシピ登録の優先度")
                    .addLore("§a優先度: ${recipe.registerIndex}")
                    .addLore("§e数値が低いほど先に登録されます")
                    .addLore("§enullでレシピブックの優先度と同じになります"), Int::class.java, "§a数値を入力してください"
            ) { int, p ->
                recipe.registerIndex = int
                p.sendPlainMessage("§b" + int + "に変更しました")
            },
            SInventoryItem(SItem(Material.CYAN_CONCRETE).setDisplayName("§6非表示設定"))
                .addLore("§a現在: ${Items.booleanToString(recipe.hidden)}").setCanClick(false)
                .setClickEvent {
                    recipe.hidden = !recipe.hidden
                    renderMenu(p)
                    renderInventory(nowPage)
                }
        )

        if (recipe is Permissible) {
            options.add(
                createNullableInputItem(
                    SItem(Material.LIGHT_BLUE_CONCRETE)
                        .setDisplayName("§6権限設定")
                        .addLore("§a権限: ${recipe.permission ?: "なし"}")
                        .addLore("§cnullで権限なしになります"), String::class.java, "§a権限を入力してください"
                ) { str, p ->
                    recipe.permission = str
                    p.sendPlainMessage("§b" + (str ?: "なし") + "に変更しました")
                }
            )
        }

        if (recipe is CommandExecutable) {
            options.add(
                SInventoryItem(SItem(Material.COMMAND_BLOCK).setDisplayName("§6コマンド設定"))
                    .addLore("§aコマンド数: ${recipe.commands.size}").setCanClick(false)
                    .setClickEvent {
                        val inv = object : LargeSInventory(SJavaPlugin.plugin, "§aコマンド設定") {
                            override fun renderMenu(p: Player): Boolean {
                                val items = arrayListOf<SInventoryItem>()
                                recipe.commands.forEach { cmd ->
                                    items.add(
                                        SInventoryItem(SItem(Material.PAPER)
                                            .setDisplayName("§6コマンド: $cmd"))
                                            .addLore("§cシフト右クリックで削除")
                                            .setCanClick(false)
                                            .setClickEvent {
                                                if (it.isRightClick && it.isShiftClick) {
                                                    recipe.commands = recipe.commands.toMutableList().also { list -> list.remove(cmd) }
                                                    p.sendPlainMessage("§bコマンドを削除しました")
                                                    allRenderMenu(p)
                                                }
                                            }
                                    )
                                }

                                items.add(
                                    createInputItem(
                                        SItem(Material.EMERALD_BLOCK)
                                            .setDisplayName("§a追加")
                                            .addLore(
                                                "§e<name>でプレイヤーの名前",
                                                "§e<uuid>でプレイヤーのUUIDに置換されます"
                                            ), String::class.java, "§aコマンドを入力してください"
                                    ) { str, p ->
                                        recipe.commands = recipe.commands.toMutableList().also { list -> list.add(str) }
                                        p.sendPlainMessage("§bコマンドを追加しました")
                                        allRenderMenu(p)
                                    }
                                )

                                setResourceItems(items)
                                return true
                            }
                        }

                        moveChildInventory(inv, p)
                    }
            )
        }

        options.addAll(appendOptions(this))

        setLeftSlots(listOf(19))
        setRightSlots(listOf(25))

        setResourceItems(arrayListOf())

        val optionPages = options.chunked(optionSlots.size)
        optionPages.forEach { optionList ->
            val inv = object : SInventory(SJavaPlugin.plugin,"§aオプション",3) {
                init {
                    fillItem(Items.backgroundLightBlue())
                    optionList.forEachIndexed { index, sInventoryItem ->
                        setItem(optionSlots[index], sInventoryItem)
                    }
                }
            }
            addPage(inv)
        }

        return true
    }
}