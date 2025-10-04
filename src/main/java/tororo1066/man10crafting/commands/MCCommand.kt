package tororo1066.man10crafting.commands

import org.bukkit.NamespacedKey
import tororo1066.man10crafting.LegacyConverter
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.inventory.player.RecipeCategoryList
import tororo1066.man10crafting.inventory.register.MainMenu
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.annotation.SCommandBody
import tororo1066.tororopluginapi.sCommand.SCommand
import tororo1066.tororopluginapi.sCommand.SCommandArg
import tororo1066.tororopluginapi.sCommand.SCommandArgType

class MCCommand: SCommand("mcraft") {

    @SCommandBody
    val recipes = command().addNeedPermission("mcraft.user").setPlayerExecutor {
        RecipeCategoryList().open(it.sender)
    }

    @SCommandBody
    val register = command().addNeedPermission("mcraft.op").addArg(SCommandArg().addAllowString("register")).setPlayerExecutor { MainMenu().open(it.sender) }

    @SCommandBody
    val getResultItem = command().addNeedPermission("mcraft.op")
            .addArg(SCommandArg("giveResultItem"))
            .addArg(SCommandArg(Man10Crafting.recipes.keys.map { it.key }))
            .setPlayerExecutor {
                val recipe = Man10Crafting.recipes[NamespacedKey(SJavaPlugin.plugin, it.args[1])]
                if (recipe == null) {
                    it.sender.sendPlainMessage(Man10Crafting.prefix + "§cそのようなレシピは存在しません")
                    return@setPlayerExecutor
                }
                it.sender.inventory.addItem(recipe.result)
                it.sender.sendPlainMessage(Man10Crafting.prefix + "§aレシピの結果アイテムを付与しました")
            }

//    @SCommandBody
//    val getCraftAmount = command().addNeedPermission("mcraft.op").addArg(SCommandArg("log")).addArg(SCommandArg("item")).addArg(
//        SCommandArg(Man10Crafting.old_recipes.keys)).setNormalExecutor {
//        val recipe = Man10Crafting.old_recipes[it.args[2]]!!
//        Man10Crafting.sDatabase.backGroundQuery("select sum(amount) from craft_log where recipe = '${it.args[2]}'") { sum ->
//            sum.forEach { rs ->
//                it.sender.sendPlainMessage(Man10Crafting.prefix + recipe.result.itemMeta.displayName + "§aの合計作成回数:§6" + (rs.getNullableObject("sum(amount)") as? BigDecimal))
//            }
//        }
//
//    }
//
//    @SCommandBody
//    val getPlayerCraftAmount = command().addNeedPermission("mcraft.op").addArg(SCommandArg("log")).addArg(SCommandArg("player")).addArg(
//        SCommandArg(SCommandArgType.ONLINE_PLAYER)).addArg(SCommandArg(Man10Crafting.old_recipes.keys)).setNormalExecutor {
//        val p = Bukkit.getPlayer(it.args[2])!!
//        val recipe = Man10Crafting.old_recipes[it.args[3]]!!
//        Man10Crafting.sDatabase.backGroundQuery("select sum(amount) from craft_log where recipe = '${it.args[3]}' and uuid = '${p.uniqueId}'") { sum ->
//            sum.forEach { rs ->
//                it.sender.sendPlainMessage(Man10Crafting.prefix + "§f§l" + p.name + "§aの" + recipe.result.itemMeta.displayName + "§aの合計作成回数:§6" + rs.getNullableObject("sum(amount)") as? BigDecimal)
//            }
//        }
//
//    }

    @SCommandBody
    val enabledAutoCraft = command().addNeedPermission("mcraft.op").addArg(SCommandArg("enabledAutoCraft"))
            .addArg(SCommandArg(SCommandArgType.BOOLEAN)).setNormalExecutor {
                Man10Crafting.enabledAutoCraft = it.args[1].toBoolean()
                Man10Crafting.plugin.config.set("enabledAutoCraft",Man10Crafting.enabledAutoCraft)
                Man10Crafting.plugin.saveConfig()
                it.sender.sendPlainMessage(Man10Crafting.prefix + "§a自動クラフトを${if (Man10Crafting.enabledAutoCraft) "有効" else "無効"}にしました")
    }

    @SCommandBody
    val disabledVanillaCraftWithCustomModelDataItem = command().addNeedPermission("mcraft.op").addArg(SCommandArg("disabledVanillaCraftWithCustomModelDataItem"))
            .addArg(SCommandArg(SCommandArgType.BOOLEAN)).setNormalExecutor {
                Man10Crafting.disabledVanillaCraftWithCustomModelDataItem = it.args[1].toBoolean()
                Man10Crafting.plugin.config.set("disabledVanillaCraftWithCustomModelDataItem",Man10Crafting.disabledVanillaCraftWithCustomModelDataItem)
                Man10Crafting.plugin.saveConfig()
                it.sender.sendPlainMessage(Man10Crafting.prefix + "§aカスタムモデルデータ付きのアイテムを用いたバニラレシピのクラフトを${if (Man10Crafting.disabledVanillaCraftWithCustomModelDataItem) "無効" else "有効"}にしました")
            }

    @SCommandBody
    val disabledCustomModelDataItemAsVanillaItem = command().addNeedPermission("mcraft.op").addArg(SCommandArg("disabledCustomModelDataItemAsVanillaItem"))
            .addArg(SCommandArg(SCommandArgType.BOOLEAN)).setNormalExecutor {
                Man10Crafting.disabledCustomModelDataItemAsVanillaItem = it.args[1].toBoolean()
                Man10Crafting.plugin.config.set("disabledCustomModelDataItemAsVanillaItem",Man10Crafting.disabledCustomModelDataItemAsVanillaItem)
                Man10Crafting.plugin.saveConfig()
                it.sender.sendPlainMessage(Man10Crafting.prefix + "§aカスタムモデルデータ付きのアイテムをバニラアイテムとして扱うを${if (Man10Crafting.disabledCustomModelDataItemAsVanillaItem) "無効" else "有効"}にしました")
        }

    @SCommandBody
    val reload = command().addNeedPermission("mcraft.op").addArg(SCommandArg("reload"))
        .setNormalExecutor {
            Man10Crafting.plugin.reloadPluginConfig()
            it.sender.sendPlainMessage(Man10Crafting.prefix + "§a再読み込みしました")
        }

    @SCommandBody
    val convert = command().addNeedPermission("mcraft.op").addArg(SCommandArg("convert"))
        .setPlayerExecutor {
            LegacyConverter.startConvert(it.sender)
        }

//    @SCommandBody
//    val test = command().addNeedPermission("mcraft.op").addArg(SCommandArg("test")).setPlayerExecutor {
//        val item = it.sender.inventory.itemInMainHand
//        val item2 = it.sender.inventory.itemInOffHand
//        val data = CustomFurnaceRecipe()
//        data.input = item
//        data.output = item2
//        data.exp = 1.0f
//        data.time = 200
//        data.saveToYml(Man10Crafting.plugin.dataFolder.resolve("customFurnace").resolve("recipes").resolve("test.yml"))
//    }
//
//    @SCommandBody
//    val test2 = command().addNeedPermission("mcraft.op")
//        .addArg(SCommandArg("test2"))
//        .addArg(SCommandArg(SCommandArgType.STRING).addAlias("furnace"))
//        .setPlayerExecutor {
//            val furnace = CustomFurnaceInstance()
//            val location = it.sender.location.toBlockLocation()
//            furnace.location = location
//            furnace.customFurnace = CustomFurnaceManager.instance.getCustomFurnace(it.args[1])!!
//            CustomFurnaceManager.instance.furnaces[location] = furnace
//            it.sender.openInventory(furnace.inventory)
//        }

    init {
        registerSLangCommand(Man10Crafting.plugin,"mcraft.op")
    }
}