package tororo1066.man10crafting.listeners

import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.*
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import tororo1066.man10crafting.Man10Crafting
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.annotation.SEventHandler
import tororo1066.tororopluginapi.utils.LocType
import tororo1066.tororopluginapi.utils.toLocString
import kotlin.math.min

class CraftListener {

    @SEventHandler
    fun event(e: PrepareItemCraftEvent){
        val eventRecipe = e.recipe?:return
        if (eventRecipe !is Keyed)return
        val namespacedKey = eventRecipe.key
        if (namespacedKey.namespace != "man10crafting")return
        val recipe = Man10Crafting.recipes[namespacedKey.key]?:return
        if (!recipe.checkNeed(e.view.player as Player)) {
            e.inventory.result = ItemStack(Material.AIR)
        }
    }

    @SEventHandler(EventPriority.LOWEST)
    fun event(e: CraftItemEvent){
        val eventRecipe = e.recipe
        if (eventRecipe !is Keyed)return
        val namespacedKey = eventRecipe.key
        if (namespacedKey.namespace != "man10crafting")return
        val recipe = Man10Crafting.recipes[namespacedKey.key]?:return
        if (!recipe.checkNeed(e.whoClicked as Player)){
            e.isCancelled = true
            e.inventory.result = ItemStack(Material.AIR)
        }
        if (e.isCancelled)return
        if (recipe.returnBottle) {
            (1..9).forEach {
                val item = e.inventory.getItem(it)?:return@forEach
                    if (item.type == Material.POTION){
                    Bukkit.getScheduler().runTaskLater(Man10Crafting.plugin, Runnable {
                        e.inventory.setItem(it, ItemStack(Material.GLASS_BOTTLE))
                    },0)
                }
            }
        }
        if (recipe.command.isNotBlank()){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),recipe.command.replace("<name>",e.whoClicked.name).replace("<uuid>",e.whoClicked.uniqueId.toString()))
        }
        var amount = 1
        if (e.isShiftClick){
            amount = e.inventory.maxStackSize
            for (matrix in e.inventory.matrix){
                if (matrix == null || matrix.type.isAir)continue
                val matrixAmount = matrix.amount
                if (matrixAmount < amount) amount = matrixAmount
            }
        }
        SJavaPlugin.mysql.callbackExecute("insert into craft_log (type,uuid,mcid,location,recipe,amount) values('${recipe.type.name}','${e.whoClicked.uniqueId}','${e.whoClicked.name}','${e.inventory.location?.toLocString(LocType.WORLD_BLOCK_SPACE)}','${recipe.namespace}',${amount})") {}
    }

    @SEventHandler(EventPriority.LOWEST)
    fun event(e: FurnaceSmeltEvent){
        val eventRecipe = e.recipe?:return
        if (eventRecipe.key.namespace != "man10crafting")return
        val recipe = Man10Crafting.recipes[eventRecipe.key.key]?:return
        if (!recipe.enabled) {
            e.isCancelled = true
            return
        }

        SJavaPlugin.mysql.callbackExecute("insert into craft_log (type,uuid,mcid,location,recipe,amount) values('${recipe.type.name}','none','none','${e.block.location.toLocString(LocType.WORLD_BLOCK_SPACE)}','${recipe.namespace}',1)") {}
    }

    @SEventHandler(EventPriority.LOWEST)
    fun event(e: PrepareSmithingEvent){
        val eventRecipe = e.inventory.recipe?:return
        if (eventRecipe !is Keyed)return
        val namespacedKey = eventRecipe.key
        if (namespacedKey.namespace != "man10crafting")return
        val recipe = Man10Crafting.recipes[namespacedKey.key]?:return
        if (!recipe.checkNeed(e.view.player as Player)){
            e.result = ItemStack(Material.AIR)
            return
        }
        e.result = recipe.result
    }

    @SEventHandler(EventPriority.LOWEST)
    fun event(e: SmithItemEvent){
        val eventRecipe = e.inventory.recipe?:return
        if (eventRecipe !is Keyed)return
        val namespacedKey = eventRecipe.key
        if (namespacedKey.namespace != "man10crafting")return
        val recipe = Man10Crafting.recipes[namespacedKey.key]?:return
        if (!recipe.checkNeed(e.whoClicked as Player)){
            e.isCancelled = true
            return
        }
        if (!e.isCancelled){
            e.inventory.result = recipe.result
        }

        SJavaPlugin.mysql.callbackExecute("insert into craft_log (type,uuid,mcid,location,recipe,amount) values('${recipe.type.name}','${e.whoClicked.uniqueId}','${e.whoClicked.name}','${e.inventory.location?.toLocString(LocType.WORLD_BLOCK_SPACE)}','${recipe.namespace}',${min(e.inventory.inputEquipment?.amount?:0,e.inventory.inputMineral?.amount?:0)})") {}
    }
}