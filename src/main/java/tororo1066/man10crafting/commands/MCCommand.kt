package tororo1066.man10crafting.commands

import tororo1066.man10crafting.inventory.NormalCraftRegister
import tororo1066.tororopluginapi.annotation.SCommandBody
import tororo1066.tororopluginapi.sCommand.SCommand

class MCCommand: SCommand("mcraft") {

    @SCommandBody
    val register = command().setPlayerExecutor { NormalCraftRegister().open(it.sender) }
}