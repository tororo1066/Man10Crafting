package tororo1066.man10crafting.recipe

import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Recipe

abstract class AbstractBukkitRecipe: AbstractRecipe() {

    abstract fun createBukkitRecipe(): Recipe

    override fun register() {
        val bukkitRecipe = createBukkitRecipe()
        Bukkit.removeRecipe(namespacedKey)
        Bukkit.addRecipe(bukkitRecipe)

        Bukkit.getOnlinePlayers().forEach { player ->
            if (accessible(player) && !hidden) {
                player.discoverRecipe(namespacedKey)
            } else {
                player.undiscoverRecipe(namespacedKey)
            }
        }
    }

    override fun unregister() {
        Bukkit.removeRecipe(namespacedKey)
        Bukkit.getOnlinePlayers().forEach { player ->
            player.undiscoverRecipe(namespacedKey)
        }
    }

    protected fun actualCraftableAmount(event: InventoryClickEvent, craftableAmount: Int): Int {
        val inventory = event.inventory
        val currentItem = event.currentItem ?: return 0

        if (event.isShiftClick) {
            var amount = craftableAmount

            val temporaryInventory = Bukkit.createInventory(null, 36)
            temporaryInventory.storageContents = inventory.storageContents.map { it?.clone() }.toTypedArray()

            val addItem = temporaryInventory.addItem(currentItem.clone().apply {
                this.amount *= amount
            })

            val remainItemAmount = addItem.values.sumOf { it.amount }
            if (remainItemAmount > 0) {
                amount -= (currentItem.amount * amount - remainItemAmount) / currentItem.amount
            }

            return if (amount > craftableAmount) craftableAmount else amount
        } else {
            val cursor = event.cursor

            if (cursor.isEmpty) return 1

            if (cursor.isSimilar(event.currentItem)) {
                val maxStackSize = cursor.maxStackSize
                if (cursor.amount + currentItem.amount > maxStackSize) return 0

                return 1
            }

            return 0
        }
    }
}