package tororo1066.man10crafting

import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import tororo1066.man10crafting.data.RecipeData
import tororo1066.tororopluginapi.SConfig
import tororo1066.tororopluginapi.SJavaPlugin
import java.io.File

class Man10Crafting: SJavaPlugin() {

    companion object{
        lateinit var plugin: Man10Crafting
        val recipes = HashMap<String,RecipeData>()
        lateinit var sConfig: SConfig
    }

    override fun onStart() {
        saveDefaultConfig()
        plugin = this
        sConfig = SConfig(this)
        Bukkit.recipeIterator().forEach {
            if (it !is Keyed)return@forEach
            if (it.key.key == "man10crafting"){
                Bukkit.removeRecipe(it.key)
            }
        }

        val folder = File("${dataFolder.path}/recipes/")
        if (!folder.exists()) folder.mkdirs()
        folder.listFiles()?.forEach {
            val data = RecipeData.loadFromYml(it.nameWithoutExtension)
            data.register()
            recipes[it.nameWithoutExtension] = data
        }
        val testRecipe = ShapedRecipe(NamespacedKey(this,"tororotest"), ItemStack(Material.DIAMOND))
        testRecipe.shape("e  "," e ","  e")
        testRecipe.setIngredient('e', ItemStack(Material.EMERALD))
        Bukkit.addRecipe(testRecipe)
    }
}