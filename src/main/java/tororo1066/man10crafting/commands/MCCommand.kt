package tororo1066.man10crafting.commands

import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.inventory.player.RecipeMenu
import tororo1066.man10crafting.inventory.register.MainMenu
import tororo1066.tororopluginapi.annotation.SCommandBody
import tororo1066.tororopluginapi.sCommand.SCommand
import tororo1066.tororopluginapi.sCommand.SCommandArg

class MCCommand: SCommand("mcraft") {

    @SCommandBody
    val recipes = command().addNeedPermission("mcraft.user").setPlayerExecutor {
        RecipeMenu(it.sender).open(it.sender)
    }

    @SCommandBody
    val register = command().addNeedPermission("mcraft.op").addArg(SCommandArg().addAllowString("register")).setPlayerExecutor { MainMenu().open(it.sender) }

    init {
        registerSLangCommand(Man10Crafting.plugin,"mcraft.op")
    }
}