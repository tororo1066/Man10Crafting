package tororo1066.man10crafting.listeners

import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.block.BrewingStand
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BrewingStartEvent
import org.bukkit.event.inventory.*
import org.bukkit.inventory.*
import tororo1066.man10crafting.Man10Crafting
import tororo1066.man10crafting.data.RecipeData
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
        if (e.isCancelled)return
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
        SJavaPlugin.mysql.callbackExecute("insert into craft_log (type,uuid,mcid,location,recipe,amount,date) values('${recipe.type.name}','${e.whoClicked.uniqueId}','${e.whoClicked.name}','${e.inventory.location?.toLocString(LocType.WORLD_BLOCK_SPACE)}','${recipe.namespace}',${amount},now())") {}
    }

    @SEventHandler(EventPriority.LOWEST)
    fun event(e: FurnaceSmeltEvent){
        if (e.isCancelled)return
        val eventRecipe = e.recipe?:return
        if (eventRecipe.key.namespace != "man10crafting")return
        val recipe = Man10Crafting.recipes[eventRecipe.key.key]?:return
        if (!recipe.enabled) {
            e.isCancelled = true
            return
        }

        SJavaPlugin.mysql.callbackExecute("insert into craft_log (type,uuid,mcid,location,recipe,amount,date) values('${recipe.type.name}','none','none','${e.block.location.toLocString(LocType.WORLD_BLOCK_SPACE)}','${recipe.namespace}',1,now())") {}
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
        if (e.isCancelled)return
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

        SJavaPlugin.mysql.callbackExecute("insert into craft_log (type,uuid,mcid,location,recipe,amount,date) values('${recipe.type.name}','${e.whoClicked.uniqueId}','${e.whoClicked.name}','${e.inventory.location?.toLocString(LocType.WORLD_BLOCK_SPACE)}','${recipe.namespace}',${min(e.inventory.inputEquipment?.amount?:0,e.inventory.inputMineral?.amount?:0)},now())") {}
    }

    @SEventHandler(EventPriority.LOWEST)
    fun event(e: InventoryClickEvent){
        if (e.isCancelled)return
        val item = e.currentItem?:return
        if (item.amount <= item.maxStackSize)return
        val inv = e.clickedInventory?:return
        when(inv){
            is CraftingInventory ->{
                if (e.slot !in 1..9)return
                e.isCancelled = true
            }

            is FurnaceInventory ->{
                if (e.slot != 0)return
                e.isCancelled = true
            }

            is SmithingInventory ->{
                if (e.slot !in 0..1)return
                e.isCancelled = true
            }

            is StonecutterInventory ->{
                if (e.slot != 0)return
                e.isCancelled = true
            }
        }
    }

    @SEventHandler(EventPriority.LOWEST)
    fun event(e: BrewingStartEvent){
        val ingredient = e.source
        val stand = e.block.state as BrewingStand
        var maxBrewingTime = -1
        (0..2).forEach { index ->
            val item = stand.inventory.getItem(index)?:return@forEach
            val recipe = Man10Crafting.recipes.entries.find {
                it.value.type == RecipeData.Type.BREWING && it.value.potionInput == item && it.value.singleMaterial == ingredient
            }?.value?:return@forEach
            maxBrewingTime = maxBrewingTime.coerceAtLeast(recipe.brewingTime)
        }
        if (maxBrewingTime != -1){
            e.totalBrewTime = maxBrewingTime
        }
    }

    @SEventHandler(EventPriority.LOWEST)
    fun event(e: BrewEvent){
        if (e.isCancelled)return

        (0..2).forEach { index ->
            val item = e.contents.getItem(index)?:return@forEach
            val recipe = Man10Crafting.recipes.entries.find {
                it.value.type == RecipeData.Type.BREWING && it.value.potionInput == item && it.value.singleMaterial == e.contents.ingredient
            }?.value?:return@forEach
            SJavaPlugin.mysql.callbackExecute("insert into craft_log (type,uuid,mcid,location,recipe,amount,date) values('${recipe.type.name}','none','none','${e.contents.location?.toLocString(LocType.WORLD_BLOCK_SPACE)}','${recipe.namespace}',1,now())") {}
        }
    }
}
