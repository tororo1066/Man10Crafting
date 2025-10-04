package tororo1066.man10crafting.customFurnace

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.CookingRecipe
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import tororo1066.man10crafting.customFurnace.fuel.ICustomFurnaceFuel
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.sItem.SItem

class CustomFurnaceInstance {
    lateinit var customFurnace: CustomFurnace
    lateinit var location: Location
    /** 使用中の燃料 */
    var currentFuel: ICustomFurnaceFuel? = null
    /** 素材スロット */
    var input: ItemStack? = null
    /** 完成品スロット */
    var output: ItemStack? = null
    /** 燃料スロット */
    var fuel: ItemStack? = null
    /** 進捗 tick */
    var progress: Int = 0
    /** 燃焼残り tick */
    var remainingFuel: Int = 0
    /** かまどに保持されている経験値 */
    var storedExp: Float = 0.0f
    var recipe: Recipe? = null
    /** かまどのインベントリ */
    val inventory by lazy {
        FurnaceInventoryHolder(this).inventory
    }

    var debugSlot = 0

    val queue = ArrayDeque<(CustomFurnaceInstance) -> Unit>()

    fun addQueue(func: (CustomFurnaceInstance) -> Unit) {
        queue.add(func)
    }

    fun updateRecipe() {
        if (input == null) {
            recipe = null
            return
        }
        val newRecipe = CustomFurnaceManager.instance.getRecipe(input!!, currentFuel)
        if (newRecipe != null && newRecipe != recipe) {
            recipe = newRecipe
            progress = 0
        }
    }

    private fun updateProgressView() {
        val cookingTime = when(recipe) {
            is CookingRecipe<*> -> (recipe as CookingRecipe<*>).cookingTime
            is CustomFurnaceRecipe -> (recipe as CustomFurnaceRecipe).time
            else -> 200
        }
        var customModelData = if (progress == 0) customFurnace.progressStartCustomModelData else customFurnace.progressStartCustomModelData + 1 + (progress.toDouble() / cookingTime * customFurnace.progressStep).toInt()
        customModelData = customModelData.coerceAtMost(customFurnace.progressStep + customFurnace.progressStartCustomModelData)
        Bukkit.getScheduler().runTask(SJavaPlugin.plugin, Runnable {
            inventory.setItem(customFurnace.progressSlot, SItem(customFurnace.progressMaterial).setDisplayName(" ")
                .setCustomModelData(customModelData).build())
        })
    }

    fun tick() {
        //デバッグ
        Bukkit.getScheduler().runTask(SJavaPlugin.plugin, Runnable {
            inventory.setItem(debugSlot, SItem(Material.DIAMOND).setDisplayName("Debug")
                .addLore("input: ${input?.type} x${input?.amount}", "output: ${output?.type} x${output?.amount}", "fuel: ${fuel?.type} x${fuel?.amount}", "progress: $progress", "remainingFuel: $remainingFuel", "storedExp: $storedExp", "recipe: ${recipe?.javaClass?.simpleName}", "currentFuel: ${currentFuel?.javaClass?.simpleName}").build())
        })

        queue.forEach {
            it.invoke(this)
        }
        if (queue.isNotEmpty()) updateRecipe()
        queue.clear()

        //燃料処理
        if (currentFuel != null) {
            if (remainingFuel > 0) {
                remainingFuel--
                if (remainingFuel <= 0) {
                    currentFuel = null
                }
            }
        }

        if (remainingFuel <= 0) {
            if (fuel != null) {
                val fuelItem = fuel!!
                val fuelType = CustomFurnaceManager.instance.getFuel(fuelItem)
                if (fuelType != null) {
                    if (input != null) {
                        val recipe = CustomFurnaceManager.instance.getRecipe(input!!, fuelType)
                        if (recipe != null) {
                            currentFuel = fuelType
                            remainingFuel = fuelType.burnTime
                            fuelItem.amount--
                            if (fuelItem.amount <= 0) {
                                fuel = null
                            }
                            Bukkit.getScheduler().runTask(SJavaPlugin.plugin, Runnable {
                                inventory.setItem(customFurnace.fuelSlot, fuel)
                            })
                        }
                    }
                }
            }
            updateRecipe()
        }

        //レシピ処理
        if (recipe != null && currentFuel != null) {
            val currentRecipe = recipe!!
            if (output != null && !output!!.isSimilar(currentRecipe.result)) {
                progress = 0
                updateProgressView()
            } else {
                when(currentRecipe) {
                    is CookingRecipe<*> -> {
                        if (progress >= currentRecipe.cookingTime) {
                            if (output == null) {
                                output = currentRecipe.result.clone()
                            } else {
                                output!!.amount++
                            }
                            Bukkit.getScheduler().runTask(SJavaPlugin.plugin, Runnable {
                                inventory.setItem(customFurnace.outputSlot, output)
                            })
                            storedExp += currentRecipe.experience
                            input!!.amount--
                            if (input!!.amount <= 0) {
                                input = null
                                progress = 0
                            } else {
                                progress = 1
                            }
                            Bukkit.getScheduler().runTask(SJavaPlugin.plugin, Runnable {
                                inventory.setItem(customFurnace.inputSlot, input)
                            })
                            updateProgressView()
                            updateRecipe()
                        } else {
                            progress++
                            updateProgressView()
                        }
                    }
                    is CustomFurnaceRecipe -> {
                        if (progress >= currentRecipe.time) {
                            if (output == null) {
                                output = currentRecipe.output.clone()
                            } else {
                                output!!.amount++
                            }
                            Bukkit.getScheduler().runTask(SJavaPlugin.plugin, Runnable {
                                inventory.setItem(customFurnace.outputSlot, output)
                            })
                            storedExp += currentRecipe.exp
                            input!!.amount--
                            if (input!!.amount <= 0) {
                                input = null
                                progress = 0
                            } else {
                                progress = 1
                            }
                            Bukkit.getScheduler().runTask(SJavaPlugin.plugin, Runnable {
                                inventory.setItem(customFurnace.inputSlot, input)
                            })
                            updateProgressView()
                            updateRecipe()
                        } else {
                            progress++
                            updateProgressView()
                        }
                    }
                }
            }
        } else {
            progress = 0
            updateProgressView()
        }
    }

    class FurnaceInventoryHolder(val instance: CustomFurnaceInstance): InventoryHolder {
        override fun getInventory(): Inventory {
            val format = instance.customFurnace.inventoryFormat
            val inv = Bukkit.createInventory(this, format.size * 9, Component.text(instance.customFurnace.displayName))
            format.forEachIndexed { listIndex, str ->
                str.forEachIndexed second@ { charIndex, c ->
                    val index = charIndex + listIndex * 18
                    if (index % 2 == 1) return@second
                    if (c == ' ') return@second
                    val slot = index / 2
                    when (c) {
                        'i' -> inv.setItem(slot, instance.input)
                        'o' -> inv.setItem(slot, instance.output)
                        'f' -> inv.setItem(slot, instance.fuel)
                        'v' -> inv.setItem(slot, SItem(instance.customFurnace.overlayMaterial).setDisplayName(" ").setCustomModelData(instance.customFurnace.overlayCustomModelData).build())
                        '.' -> inv.setItem(slot, SItem(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ").build())
                    }
                }
            }

            return inv
        }
    }
}