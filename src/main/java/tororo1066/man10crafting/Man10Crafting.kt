package tororo1066.man10crafting

import org.bukkit.Bukkit
import org.bukkit.Keyed
import tororo1066.man10crafting.data.RecipeData
import tororo1066.tororopluginapi.SConfig
import tororo1066.tororopluginapi.SInput
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.SStr
import tororo1066.tororopluginapi.lang.SLang
import java.io.File

class Man10Crafting: SJavaPlugin() {

    companion object{
        lateinit var plugin: Man10Crafting
        val recipes = HashMap<String,RecipeData>()
        lateinit var sConfig: SConfig
        lateinit var sInput: SInput
        lateinit var sLang: SLang
        val prefix = SStr("§c[§bMan10Crafting§c]§r").toString()
    }

    override fun onLoad() {
        plugin = this
        sLang = SLang(this)
    }

    override fun onStart() {
        saveDefaultConfig()
        sConfig = SConfig(this)
        sInput = SInput(this)

        Bukkit.recipeIterator().forEach {
            if (it !is Keyed)return@forEach
            if (it.key.key == "man10crafting"){
                Bukkit.removeRecipe(it.key)
            }
        }
        val folder = File("${dataFolder.path}/recipes/")
        if (!folder.exists()) folder.mkdirs()
        folder.listFiles()?.forEach {
            (it.listFiles()?:return@forEach).forEach { file ->
                val data = RecipeData.loadFromYml(it.nameWithoutExtension,file.nameWithoutExtension)
                data.category = it.nameWithoutExtension
                data.register()
            }
        }
    }
}