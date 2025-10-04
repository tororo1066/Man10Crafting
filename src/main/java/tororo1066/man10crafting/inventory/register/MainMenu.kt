package tororo1066.man10crafting.inventory.register

import org.bukkit.Material
import org.bukkit.entity.Player
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.inventory.register.crafting.ShapedCraftingRegister
import tororo1066.man10crafting.inventory.register.crafting.ShapelessCraftingRegister
import tororo1066.man10crafting.inventory.register.furnace.BlastingRegister
import tororo1066.man10crafting.inventory.register.furnace.CampfireRegister
import tororo1066.man10crafting.inventory.register.furnace.FurnaceRegister
import tororo1066.man10crafting.inventory.register.furnace.SmokingRegister
import tororo1066.man10crafting.inventory.register.smithing.SmithingTransformRegister
import tororo1066.man10crafting.inventory.register.smithing.SmithingTrimRegister
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem

class MainMenu: SInventory(SJavaPlugin.plugin, "§cレシピ登録メニュー", 5) {

    init {
        registerClickSound()
    }

    //0  1  2  3  4  5  6  7  8
    //9  c  11 f  13 s  15 sc 17
    //18 19 20 21 22 23 24 25 26
    //27 28 29 30 31 32 33 34 35
    //36 37 38 39 40 41 42 e  ca
    // c: crafting, f: furnace, s: smithing, sc: stonecutting, e: edit menu, ca: category menu

    override fun renderMenu(p: Player): Boolean {
        fillItem(Items.backgroundLightBlue())

        setItem(
            10,
            SInventoryItem(Material.CRAFTING_TABLE)
                .setDisplayName("§a作業台レシピ")
                .setCanClick(false)
                .setClickEvent {
                    val inv = object : SInventory(SJavaPlugin.plugin, "§a作業台レシピ", 1) {
                        override fun renderMenu(p: Player): Boolean {
                            fillItem(Items.backgroundLightBlue())
                            setItem(
                                2,
                                SInventoryItem(Material.EMERALD_BLOCK)
                                    .setDisplayName("§b定型レシピ")
                                    .setCanClick(false)
                                    .setClickEvent {
                                        val inv = ShapedCraftingRegister()
                                        moveChildInventory(inv, p)
                                    }
                            )

                            setItem(
                                6,
                                SInventoryItem(Material.LAPIS_BLOCK)
                                    .setDisplayName("§d不定形レシピ")
                                    .setCanClick(false)
                                    .setClickEvent {
                                        val inv = ShapelessCraftingRegister()
                                        moveChildInventory(inv, p)
                                    }
                            )
                            return true
                        }
                    }
                    moveChildInventory(inv, p)
                }
        )

        setItem(
            12,
            SInventoryItem(Material.FURNACE)
                .setDisplayName("§cかまどレシピ")
                .setCanClick(false)
                .setClickEvent {
                    val inv = object : SInventory(SJavaPlugin.plugin, "§cかまどレシピ", 1) {
                        override fun renderMenu(p: Player): Boolean {
                            fillItem(Items.backgroundLightBlue())
                            setItem(
                                1,
                                SInventoryItem(Material.BLAST_FURNACE)
                                    .setDisplayName("§c溶鉱炉レシピ")
                                    .setCanClick(false)
                                    .setClickEvent {
                                        val inv = BlastingRegister()
                                        moveChildInventory(inv, p)
                                    }
                            )

                            setItem(
                                3,
                                SInventoryItem(Material.FURNACE)
                                    .setDisplayName("§6かまどレシピ")
                                    .setCanClick(false)
                                    .setClickEvent {
                                        val inv = FurnaceRegister()
                                        moveChildInventory(inv, p)
                                    }
                            )

                            setItem(
                                5,
                                SInventoryItem(Material.SMOKER)
                                    .setDisplayName("§e燻製器レシピ")
                                    .setCanClick(false)
                                    .setClickEvent {
                                        val inv = SmokingRegister()
                                        moveChildInventory(inv, p)
                                    }
                            )

                            setItem(
                                7,
                                SInventoryItem(Material.CAMPFIRE)
                                    .setDisplayName("§a焚き火レシピ")
                                    .setCanClick(false)
                                    .setClickEvent {
                                        val inv = CampfireRegister()
                                        moveChildInventory(inv, p)
                                    }
                            )
                            return true
                        }
                    }

                    moveChildInventory(inv, p)
                }
        )

        setItem(
            14,
            SInventoryItem(Material.SMITHING_TABLE)
                .setDisplayName("§d鍛冶台レシピ")
                .setCanClick(false)
                .setClickEvent {
                    val inv = object : SInventory(SJavaPlugin.plugin, "§d鍛冶台レシピ", 1) {
                        override fun renderMenu(p: Player): Boolean {
                            fillItem(Items.backgroundLightBlue())
                            setItem(
                                2,
                                SInventoryItem(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                                    .setDisplayName("§c変換レシピ")
                                    .setCanClick(false)
                                    .setClickEvent {
                                        val inv = SmithingTransformRegister()
                                        moveChildInventory(inv, p)
                                    }
                            )

                            setItem(
                                6,
                                SInventoryItem(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE)
                                    .setDisplayName("§a装飾レシピ")
                                    .setCanClick(false)
                                    .setClickEvent {
                                        val inv = SmithingTrimRegister()
                                        moveChildInventory(inv, p)
                                    }
                            )
                            return true
                        }
                    }

                    moveChildInventory(inv, p)
                }
        )

        setItem(16,
            SInventoryItem(Material.STONECUTTER)
                .setDisplayName("§7石切台レシピ")
                .setCanClick(false)
                .setClickEvent {
                    val inv = StonecuttingRegister()
                    moveChildInventory(inv, p)
                }
        )

        setItem(28,
            SInventoryItem(Material.BREWING_STAND)
                .setDisplayName("§5醸造台レシピ")
                .setCanClick(false)
                .setClickEvent {
                    val inv = BrewingRegister()
                    moveChildInventory(inv, p)
                }
        )

        setItem(43, SInventoryItem(Material.WRITABLE_BOOK)
            .setDisplayName("§eレシピ編集メニューを開く")
            .setCanClick(false)
            .setClickEvent {
                if (Man10Crafting.recipes.isEmpty()){
                    p.sendMessage("§cレシピが一つも登録されていません")
                    return@setClickEvent
                }
                val inv = EditMenu()
                moveChildInventory(inv, p)
            }
        )

        setItem(44, SInventoryItem(Material.BOOK)
            .setDisplayName("§bカテゴリの表示アイテムを設定する")
            .setCanClick(false)
            .setClickEvent {
                val inv = CategoryDisplayItemEditMenu()
                moveChildInventory(inv, p)
            }
        )

        return true
    }
}