package tororo1066.man10crafting.commands

import org.bukkit.Bukkit
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.inventory.player.RecipeMenu
import tororo1066.man10crafting.inventory.register.MainMenu
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.annotation.SCommandBody
import tororo1066.tororopluginapi.sCommand.SCommand
import tororo1066.tororopluginapi.sCommand.SCommandArg
import tororo1066.tororopluginapi.sCommand.SCommandArgType
import java.math.BigDecimal

class MCCommand: SCommand("mcraft") {

    @SCommandBody
    val recipes = command().addNeedPermission("mcraft.user").setPlayerExecutor {
        RecipeMenu(it.sender).open(it.sender)
    }

    @SCommandBody
    val register = command().addNeedPermission("mcraft.op").addArg(SCommandArg().addAllowString("register")).setPlayerExecutor { MainMenu().open(it.sender) }

    @SCommandBody
    val getResultItem = command().addNeedPermission("mcraft.op")
            .addArg(SCommandArg("giveResultItem"))
            .addArg(SCommandArg(Man10Crafting.recipes.keys))
            .setPlayerExecutor {
                val recipe = Man10Crafting.recipes[it.args[1]]!!
                it.sender.inventory.addItem(recipe.result)
                it.sender.sendPlainMessage(Man10Crafting.prefix + "§aレシピの結果アイテムを付与しました")
            }

    @SCommandBody
    val getCraftAmount = command().addNeedPermission("mcraft.op").addArg(SCommandArg("log")).addArg(SCommandArg("item")).addArg(
        SCommandArg(Man10Crafting.recipes.keys)).setNormalExecutor {
        val recipe = Man10Crafting.recipes[it.args[2]]!!
        SJavaPlugin.mysql.callbackQuery("select sum(amount) from craft_log where recipe = '${it.args[2]}'") { sum ->
            sum.forEach { rs ->
                it.sender.sendPlainMessage(Man10Crafting.prefix + recipe.result.itemMeta.displayName + "§aの合計作成回数:§6" + (rs.getNullableObject("sum(amount)") as? BigDecimal))
            }
        }

    }

    @SCommandBody
    val getPlayerCraftAmount = command().addNeedPermission("mcraft.op").addArg(SCommandArg("log")).addArg(SCommandArg("player")).addArg(
        SCommandArg(SCommandArgType.ONLINE_PLAYER)).addArg(SCommandArg(Man10Crafting.recipes.keys)).setNormalExecutor {
        val p = Bukkit.getPlayer(it.args[2])!!
        val recipe = Man10Crafting.recipes[it.args[3]]!!
        SJavaPlugin.mysql.callbackQuery("select sum(amount) from craft_log where recipe = '${it.args[3]}' and uuid = '${p.uniqueId}'") { sum ->
            sum.forEach { rs ->
                it.sender.sendPlainMessage(Man10Crafting.prefix + "§f§l" + p.name + "§aの" + recipe.result.itemMeta.displayName + "§aの合計作成回数:§6" + rs.getNullableObject("sum(amount)") as? BigDecimal)
            }
        }

    }

    @SCommandBody
    val enabledAutoCraft = command().addNeedPermission("mcraft.op").addArg(SCommandArg("enabledAutoCraft"))
            .addArg(SCommandArg(SCommandArgType.BOOLEAN)).setNormalExecutor {
                Man10Crafting.enabledAutoCraft = it.args[1].toBoolean()
                Man10Crafting.plugin.config.set("enabledAutoCraft",Man10Crafting.enabledAutoCraft)
                Man10Crafting.plugin.saveConfig()
                it.sender.sendPlainMessage(Man10Crafting.prefix + "§a自動クラフトを${if (Man10Crafting.enabledAutoCraft) "有効" else "無効"}にしました")
    }

    @SCommandBody
    val reload = command().addNeedPermission("mcraft.op").addArg(SCommandArg("reload"))
        .setNormalExecutor {
            Man10Crafting.plugin.reloadPluginConfig()
            it.sender.sendPlainMessage(Man10Crafting.prefix + "§a再読み込みしました")
        }

    init {
        registerSLangCommand(Man10Crafting.plugin,"mcraft.op")
    }
}