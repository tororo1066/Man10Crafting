package tororo1066.man10crafting.customFurnace

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.CookingRecipe
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.Recipe
import tororo1066.man10crafting.customFurnace.fuel.CustomFurnaceFuel
import tororo1066.man10crafting.customFurnace.fuel.ICustomFurnaceFuel
import tororo1066.man10crafting.customFurnace.fuel.VanillaCustomFurnaceFuel
import tororo1066.tororopluginapi.SJavaPlugin

class CustomFurnaceManager: Listener {

    companion object {
        lateinit var instance: CustomFurnaceManager
    }

    private val customFurnaces = HashMap<String, CustomFurnace>()
    private val customFuels = HashMap<String, ICustomFurnaceFuel>()
    private val builtinRecipes = HashMap<String, CookingRecipe<*>>()
    private val recipes = HashMap<String, CustomFurnaceRecipe>()
    val furnaces = HashMap<Location, CustomFurnaceInstance>()

    private val database = CustomFurnaceDatabase()

    init {
        instance = this

        val customFurnaceFolder = SJavaPlugin.plugin.dataFolder.resolve("customFurnace")
        if (!customFurnaceFolder.exists()) {
            customFurnaceFolder.mkdirs()
        }

        val furnaceFolder = customFurnaceFolder.resolve("furnaces")
        if (!furnaceFolder.exists()) {
            furnaceFolder.mkdirs()
        }

        val fuelFolder = customFurnaceFolder.resolve("fuels")
        if (!fuelFolder.exists()) {
            fuelFolder.mkdirs()
        }

        val recipeFolder = customFurnaceFolder.resolve("recipes")
        if (!recipeFolder.exists()) {
            recipeFolder.mkdirs()
        }

        Bukkit.getPluginManager().registerEvents(this, SJavaPlugin.plugin)

        furnaceFolder.listFiles()?.forEach {
            if (it.extension == "yml") {
                val customFurnace = CustomFurnace.loadFromYml(it)
                customFurnaces[customFurnace.internalName] = customFurnace
            }
        }

        fuelFolder.listFiles()?.forEach {
            if (it.extension == "yml") {
                val customFuel = CustomFurnaceFuel.loadFromYml(it)
                customFuels[customFuel.internalName] = customFuel
            }
        }

        Bukkit.recipeIterator().forEach {
            if (it is CookingRecipe<*>) {
                builtinRecipes[it.key.toString()] = it
            }
        }

        recipeFolder.listFiles()?.forEach {
            if (it.extension == "yml") {
                val customFurnaceRecipe = CustomFurnaceRecipe.loadFromYml(it)
                recipes[customFurnaceRecipe.internalName] = customFurnaceRecipe
            }
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(SJavaPlugin.plugin, Runnable {
            for (furnace in furnaces.values) {
                furnace.tick()
            }
        }, 0, 1)
    }

    fun getCustomFurnace(name: String): CustomFurnace? {
        return customFurnaces[name]
    }

    fun getRecipe(input: ItemStack, fuel: ICustomFurnaceFuel?): Recipe? {
        for (recipe in recipes.values) {
            if (recipe.checkRecipe(input, fuel)) {
                return recipe
            }
        }

        builtinRecipes.values.forEach {
            if (it.inputChoice.test(input)) {
                return it
            }
        }

        return null
    }

    fun getFuel(itemStack: ItemStack): ICustomFurnaceFuel? {
        for (fuel in customFuels.values) {
            if (fuel.fuelStack.isSimilar(itemStack)) {
                return fuel
            }
        }

        if (itemStack.type.isFuel) {
            return VanillaCustomFurnaceFuel(itemStack)
        }

        return null
    }

    fun getFuel(name: String): ICustomFurnaceFuel? {
        val fuel = customFuels[name]
        if (fuel != null) {
            return fuel
        }

        val material = Material.matchMaterial(name)
        if (material != null && material.isFuel) {
            return VanillaCustomFurnaceFuel(ItemStack(material))
        }

        return null
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.isCancelled) return
        val inventory = e.inventory
        val holder = inventory.getHolder(false) as? CustomFurnaceInstance.FurnaceInventoryHolder ?: return
        val instance = holder.instance
        val inputSlot = instance.customFurnace.inputSlot
        val outputSlot = instance.customFurnace.outputSlot
        val fuelSlot = instance.customFurnace.fuelSlot
        when (e.action) {
            InventoryAction.DROP_ALL_CURSOR,
            InventoryAction.DROP_ONE_CURSOR,
            InventoryAction.DROP_ALL_SLOT,
            InventoryAction.DROP_ONE_SLOT -> {
                if (e.clickedInventory !is PlayerInventory) {
                    e.isCancelled = true
                }
            }
            InventoryAction.NOTHING,
            InventoryAction.CLONE_STACK -> {
                return
            }
            InventoryAction.PICKUP_SOME, InventoryAction.COLLECT_TO_CURSOR, InventoryAction.UNKNOWN -> {
                e.isCancelled = true
                return
            }
            InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ONE -> {
                if (e.clickedInventory is PlayerInventory) return
                val item = e.currentItem!!
                val amount = when (e.action) {
                    InventoryAction.PICKUP_ALL -> item.amount
                    InventoryAction.PICKUP_HALF -> item.amount / 2
                    InventoryAction.PICKUP_ONE -> 1
                    else -> 0
                }
                when (e.slot) {
                    inputSlot -> {
                        instance.addQueue {
                            it.input?.let { input ->
                                input.amount -= amount
                                if (input.amount <= 0) {
                                    it.input = null
                                }
                            }
                        }
                    }
                    outputSlot -> {
                        instance.addQueue {
                            it.output?.let { output ->
                                output.amount -= amount
                                if (output.amount <= 0) {
                                    it.output = null
                                }
                            }
                        }
                    }
                    fuelSlot -> {
                        instance.addQueue {
                            it.fuel?.let { fuel ->
                                fuel.amount -= amount
                                if (fuel.amount <= 0) {
                                    it.fuel = null
                                }
                            }
                        }
                    }
                    else -> {
                        e.isCancelled = true
                    }
                }
            }
            InventoryAction.PLACE_ALL, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ONE -> {
                if (e.clickedInventory is PlayerInventory) return
                val item = e.cursor!!.clone()
                val current = e.currentItem?.clone()
                val amount = when (e.action) {
                    InventoryAction.PLACE_ALL -> item.amount
                    InventoryAction.PLACE_SOME -> current!!.maxStackSize - current.amount
                    InventoryAction.PLACE_ONE -> 1
                    else -> 0
                }
                when (e.slot) {
                    inputSlot -> {
                        instance.addQueue {
                            if (current != null && current.type != Material.AIR) {
                                it.input = current.also { current -> current.amount += amount }
                            } else {
                                it.input = item.also { item -> item.amount = amount }
                            }
                        }
                    }
                    outputSlot -> {
                        e.isCancelled = true
                    }
                    fuelSlot -> {
                        instance.addQueue {
                            if (current != null && current.type != Material.AIR) {
                                it.fuel = current.also { current -> current.amount += amount }
                            } else {
                                it.fuel = item.also { item -> item.amount = amount }
                            }
                        }
                    }
                    else -> {
                        e.isCancelled = true
                    }
                }
            }
            InventoryAction.SWAP_WITH_CURSOR -> {
                if (e.clickedInventory is PlayerInventory) return
                val item = e.cursor!!
                when (e.slot) {
                    inputSlot -> {
                        instance.addQueue {
                            it.input = item
                        }
                    }
                    outputSlot -> {
                        e.isCancelled = true
                    }
                    fuelSlot -> {
                        instance.addQueue {
                            it.fuel = item
                        }
                    }
                    else -> {
                        e.isCancelled = true
                    }
                }
            }
            InventoryAction.MOVE_TO_OTHER_INVENTORY -> {
                if (e.clickedInventory is PlayerInventory) {
                    val item = e.currentItem!!
                    e.isCancelled = true
                    var asInput = false
                    if (CustomFurnaceManager.instance.getFuel(item) != null) {
                        val targetItem = inventory.getItem(fuelSlot)
                        if (targetItem == null || targetItem.type == Material.AIR) {
                            inventory.setItem(fuelSlot, item)
                            e.currentItem = ItemStack(Material.AIR)
                            instance.addQueue {
                                it.fuel = item
                            }
                        } else {
                            if (targetItem.isSimilar(item)) {
                                val max = targetItem.maxStackSize - targetItem.amount
                                if (max > 0) {
                                    val amount = if (item.amount > max) max else item.amount
                                    targetItem.amount += amount
                                    item.amount -= amount
                                    instance.addQueue {
                                        it.fuel = targetItem
                                    }
                                } else {
                                    asInput = true
                                }
                            } else {
                                asInput = true
                            }
                        }
                    } else {
                        asInput = true
                    }

                    if (asInput) {
                        val targetItem = inventory.getItem(inputSlot)
                        if (targetItem == null || targetItem.type == Material.AIR) {
                            inventory.setItem(inputSlot, item)
                            e.currentItem = ItemStack(Material.AIR)
                            instance.addQueue {
                                it.input = item
                            }
                        } else {
                            if (targetItem.isSimilar(item)) {
                                val max = targetItem.maxStackSize - targetItem.amount
                                if (max > 0) {
                                    val amount = if (item.amount > max) max else item.amount
                                    targetItem.amount += amount
                                    item.amount -= amount
                                    instance.addQueue {
                                        it.input = targetItem
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val item = e.currentItem!!
                    val moveableAmount = getMoveableAmount(item, e.whoClicked.inventory)
                    if (moveableAmount <= 0) {
                        return
                    }
                    when (e.slot) {
                        inputSlot -> {
                            instance.addQueue {
                                it.input?.let { input ->
                                    input.amount -= moveableAmount
                                    if (input.amount <= 0) {
                                        it.input = null
                                    }
                                }
                            }
                        }
                        outputSlot -> {
                            instance.addQueue {
                                it.output?.let { output ->
                                    output.amount -= moveableAmount
                                    if (output.amount <= 0) {
                                        it.output = null
                                    }
                                }
                            }
                        }
                        fuelSlot -> {
                            instance.addQueue {
                                it.fuel?.let { fuel ->
                                    fuel.amount -= moveableAmount
                                    if (fuel.amount <= 0) {
                                        it.fuel = null
                                    }
                                }
                            }
                        }
                        else -> {
                            e.isCancelled = true
                        }
                    }
                }
            }
            InventoryAction.HOTBAR_SWAP -> {
                if (e.clickedInventory is PlayerInventory) return
                val isOffHand = e.click == ClickType.SWAP_OFFHAND
                val isInsert = e.currentItem == null || e.currentItem!!.type == Material.AIR
                val item = if (isInsert) {
                    if (isOffHand) e.whoClicked.inventory.itemInOffHand else e.whoClicked.inventory.getItem(e.hotbarButton)!!
                } else {
                    null
                }
                when (e.slot) {
                    inputSlot -> {
                        instance.addQueue {
                            it.input = item
                        }
                    }
                    outputSlot -> {
                        if (isInsert) {
                            e.isCancelled = true
                        } else {
                            instance.addQueue {
                                it.output = null
                            }
                        }
                    }
                    fuelSlot -> {
                        instance.addQueue {
                            it.fuel = item
                        }
                    }
                    else -> {
                        e.isCancelled = true
                    }
                }
            }
//            InventoryAction.HOTBAR_MOVE_AND_READD -> {
//                if (e.clickedInventory is PlayerInventory) return
//                val isOffHand = e.click == ClickType.SWAP_OFFHAND
//                val item = if (isOffHand) e.whoClicked.inventory.itemInOffHand else e.whoClicked.inventory.getItem(e.hotbarButton)!!
//                when (e.slot) {
//                    inputSlot -> {
//                        instance.addQueue {
//                            it.input = item
//                        }
//                    }
//                    outputSlot -> {
//                        e.isCancelled = true
//                    }
//                    fuelSlot -> {
//                        instance.addQueue {
//                            it.fuel = item
//                        }
//                    }
//                    else -> {
//                        e.isCancelled = true
//                    }
//                }
//            }
            else -> {
                e.isCancelled = true
            }
        }
    }

    //ItemStackの移動可能な量を取得 最大でItemStackのamountを返す
    private fun getMoveableAmount(item: ItemStack, target: Inventory): Int {
        var amount = 0
        for (i in 0 until target.size) {
            val targetItem = target.getItem(i)
            if (targetItem == null || targetItem.type == Material.AIR) {
                return item.amount
            } else if (targetItem.isSimilar(item)) {
                amount += targetItem.maxStackSize - targetItem.amount
                if (amount >= item.amount) {
                    return item.amount
                }
            }
        }
        return amount
    }

    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        if (e.isCancelled) return
        val inventory = e.inventory
        val holder = inventory.getHolder(false) as? CustomFurnaceInstance.FurnaceInventoryHolder ?: return
        val instance = holder.instance
        val inputSlot = instance.customFurnace.inputSlot
        val outputSlot = instance.customFurnace.outputSlot
        val fuelSlot = instance.customFurnace.fuelSlot
        e.newItems.forEach { (slot, item) ->
            when (slot) {
                inputSlot -> {
                    instance.addQueue {
                        it.input = item
                    }
                }
                outputSlot -> {
                    instance.addQueue {
                        it.output = item
                    }
                }
                fuelSlot -> {
                    instance.addQueue {
                        it.fuel = item
                    }
                }
                else -> {
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onLoadChunk(e: ChunkLoadEvent) {
        database.loadByChunk(e.chunk).thenAccept {
            it.forEach { furnace ->
                furnaces[furnace.location] = furnace
            }
        }
    }
}