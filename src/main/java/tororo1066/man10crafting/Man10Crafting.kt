package tororo1066.man10crafting

import org.bukkit.Bukkit
import org.bukkit.Keyed
import tororo1066.man10crafting.commands.MCCommand
import tororo1066.man10crafting.data.RecipeData
import tororo1066.tororopluginapi.SInput
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.SStr
import tororo1066.tororopluginapi.config.SConfig
import tororo1066.tororopluginapi.lang.SLang
import java.io.File

class Man10Crafting: SJavaPlugin(UseOption.MySQL) {

    companion object{
        lateinit var plugin: Man10Crafting
        val recipes = HashMap<String,RecipeData>()
        lateinit var sConfig: SConfig
        lateinit var sInput: SInput
        lateinit var sLang: SLang
        val prefix = SStr("&c[&bMan10Crafting&c]&r").toString()
    }

    override fun onStart() {
        saveDefaultConfig()
        plugin = this
        sLang = SLang(this)
        sConfig = SConfig(this)
        sInput = SInput(this)

        mysql.callbackExecute("CREATE TABLE IF NOT EXISTS `craft_log` (\n" +
                "\t`id` INT(10) NOT NULL AUTO_INCREMENT,\n" +
                "\t`type` VARCHAR(10) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`uuid` VARCHAR(36) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`mcid` VARCHAR(16) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`location` TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`recipe` TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`amount` INT(10) NULL DEFAULT NULL,\n" +
                "\tPRIMARY KEY (`id`) USING BTREE\n" +
                ")\n" +
                ";\n") {}

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
                data.register()
            }
        }

        MCCommand()
    }
}