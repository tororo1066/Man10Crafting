package tororo1066.man10crafting

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import tororo1066.man10crafting.data.LegacyRecipeData
import tororo1066.man10crafting.data.LegacyRecipeData.Type.*
import tororo1066.man10crafting.ingredient.ItemStackIngredient
import tororo1066.man10crafting.recipe.crafting.ShapedCraftingRecipe
import tororo1066.man10crafting.recipe.crafting.ShapelessCraftingRecipe
import tororo1066.man10crafting.recipe.crafting.StackableShapedCraftingRecipe
import tororo1066.man10crafting.recipe.furnace.BlastingCraftingRecipe
import tororo1066.man10crafting.recipe.furnace.FurnaceCraftingRecipe
import tororo1066.man10crafting.recipe.furnace.SmokingCraftingRecipe
import tororo1066.man10crafting.recipe.options.CommandExecutable
import tororo1066.man10crafting.recipe.options.Permissible
import tororo1066.man10crafting.recipe.paper.BrewingCraftingRecipe
import tororo1066.man10crafting.recipe.smithing.SmithingTransformCraftingRecipe
import tororo1066.man10crafting.recipe.smithing.SmithingTrimCraftingRecipe
import tororo1066.man10crafting.recipe.stonecutting.StonecuttingCraftingRecipe
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File

object LegacyConverter {

    fun startConvert(p: Player) {
        object : BukkitRunnable() {
            override fun run() {
                val oldRecipes = File(SJavaPlugin.plugin.dataFolder, "old_recipes")
                if (!oldRecipes.exists()) {
                    p.sendMessage("${Man10Crafting.prefix}§c旧レシピデータが存在しません old_recipesディレクトリに旧レシピデータを配置してください")
                    cancel()
                    return
                }
                val categories = oldRecipes.listFiles() ?: emptyArray()
                if (categories.isEmpty()) {
                    p.sendMessage("${Man10Crafting.prefix}§c旧レシピデータが存在しません old_recipesディレクトリに旧レシピデータを配置してください")
                    cancel()
                    return
                }
                var convertedCount = 0
                categories.forEach { categoryFolder ->
                    if (!categoryFolder.isDirectory) return@forEach
                    val category = categoryFolder.name
                    val files = categoryFolder.listFiles() ?: return@forEach
                    files.forEach { file ->
                        if (!file.isFile) return@forEach
                        val name = file.nameWithoutExtension
                        val legacyData = LegacyRecipeData.loadFromYml(file, category, name)
                        if (!legacyData.loaded) return@forEach

                        fun itemStackIngredient(itemStack: ItemStack) = ItemStackIngredient()
                            .apply { this.itemStacks.add(itemStack) }

                        val newData = when (legacyData.type) {
                            SHAPED -> {
                                (if (legacyData.stackRecipe) StackableShapedCraftingRecipe() else ShapedCraftingRecipe())
                                    .apply {
                                        legacyData.materials.forEach { (char, itemStack) ->
                                            this.ingredients[char] = itemStackIngredient(itemStack)
                                        }
                                        this.shape = legacyData.shape
                                    }
                            }
                            SHAPELESS -> {
                                ShapelessCraftingRecipe().apply {
                                    legacyData.shapelessMaterials.forEach { itemStack ->
                                        this.ingredients.add(itemStackIngredient(itemStack))
                                    }
                                }
                            }
                            FURNACE, SMOKING, BLASTING -> {
                                val data = when (legacyData.type) {
                                    FURNACE -> FurnaceCraftingRecipe()
                                    SMOKING -> SmokingCraftingRecipe()
                                    BLASTING -> BlastingCraftingRecipe()
                                    else -> throw IllegalStateException()
                                }
                                data.apply {
                                    this.input = itemStackIngredient(legacyData.singleMaterial)
                                    this.experience = legacyData.furnaceExp
                                    this.cookingTime = legacyData.furnaceTime
                                }
                            }
                            SMITHING -> {
                                (if (legacyData.smithingTransform) SmithingTransformCraftingRecipe() else SmithingTrimCraftingRecipe())
                                    .apply {
                                        this.template = legacyData.smithingAdditionalMaterial?.let { itemStackIngredient(it) }
                                        this.base = itemStackIngredient(legacyData.singleMaterial)
                                        this.addition = itemStackIngredient(legacyData.smithingMaterial)
                                        this.copyDataComponents = legacyData.smithingCopyNbt
                                    }
                            }
                            STONECUTTING -> {
                                StonecuttingCraftingRecipe().apply {
                                    this.input = itemStackIngredient(legacyData.singleMaterial)
                                }
                            }
                            BREWING -> {
                                BrewingCraftingRecipe().apply {
                                    this.ingredient = itemStackIngredient(legacyData.singleMaterial)
                                    this.input = itemStackIngredient(legacyData.potionInput)
                                    this.brewingTime = legacyData.brewingTime
                                }
                            }
                        }

                        if (newData !is SmithingTrimCraftingRecipe) {
                            newData.result = legacyData.result
                        }
                        newData.category = legacyData.category.lowercase()
                        newData.key = legacyData.key.lowercase()
                        newData.index = legacyData.index
                        newData.registerIndex = legacyData.registerIndex
                        newData.enabled = legacyData.enabled
                        newData.hidden = legacyData.hidden
                        if (newData is Permissible) {
                            newData.permission = legacyData.permission.ifEmpty { null }
                        }
                        if (newData is CommandExecutable) {
                            newData.commands = listOf(legacyData.command).filter { it.isNotEmpty() }
                        }

                        val success = newData.save().get()
                        if (success) {
                            object : BukkitRunnable() {
                                override fun run() {
                                    newData.register()
                                }
                            }.runTask(Man10Crafting.plugin)
                            Man10Crafting.recipes[newData.namespacedKey] = newData
                            convertedCount++
                        }
                    }
                }

                p.sendMessage("${Man10Crafting.prefix}§a変換が完了しました。 $convertedCount 件のレシピを変換しました。")
                cancel()
            }
        }.runTaskAsynchronously(Man10Crafting.plugin)
    }
}