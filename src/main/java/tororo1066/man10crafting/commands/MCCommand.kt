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
    val getCraftAmount = command().addNeedPermission("mcraft.op").addArg(SCommandArg("log")).addArg(SCommandArg("item")).addArg(
        SCommandArg(Man10Crafting.recipes.keys)).setNormalExecutor {
        val recipe = Man10Crafting.recipes[it.args[2]]!!
        val sum = SJavaPlugin.mysql.asyncQuery("select sum(amount) from craft_log where recipe = '${it.args[2]}'")
        sum.forEach { rs ->
            it.sender.sendPlainMessage(Man10Crafting.prefix + recipe.result.itemMeta.displayName + "§aの合計作成回数:§6" + (rs.getNullableObject("sum(amount)") as? BigDecimal))
        }
    }

    @SCommandBody
    val getPlayerCraftAmount = command().addNeedPermission("mcraft.op").addArg(SCommandArg("log")).addArg(SCommandArg("player")).addArg(
        SCommandArg(SCommandArgType.ONLINE_PLAYER)).addArg(SCommandArg(Man10Crafting.recipes.keys)).setNormalExecutor {
        val p = Bukkit.getPlayer(it.args[2])!!
        val recipe = Man10Crafting.recipes[it.args[3]]!!
        val sum = SJavaPlugin.mysql.asyncQuery("select sum(amount) from craft_log where recipe = '${it.args[3]}' and uuid = '${p.uniqueId}'")
        sum.forEach { rs ->
            it.sender.sendPlainMessage(Man10Crafting.prefix + "§f§l" + p.name + "§aの" + recipe.result.itemMeta.displayName + "§aの合計作成回数:§6" + rs.getNullableObject("sum(amount)") as? BigDecimal)
        }
    }

    init {
        registerSLangCommand(Man10Crafting.plugin,"mcraft.op")
    }
}