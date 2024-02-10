package tororo1066.man10crafting

import smithcrafting.base.SmithBase
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

    companion object {
        lateinit var plugin: Man10Crafting
        val recipes = HashMap<String,RecipeData>()
        lateinit var sConfig: SConfig
        lateinit var sInput: SInput
        lateinit var sLang: SLang
        lateinit var smithUtil: SmithBase
        var enabledAutoCraft = true
        val prefix = SStr("&c[&bMan10Crafting&c]&r").toString()
    }

    fun reloadPluginConfig(){
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

        enabledAutoCraft = config.getBoolean("enabledAutoCraft",true)

        MCCommand()
    }

    override fun onStart() {
        saveDefaultConfig()
        plugin = this
        sLang = SLang(this)
        sConfig = SConfig(this)
        sInput = SInput(this)
        smithUtil = SmithBase.getInstance()

        mysql.callbackExecute("CREATE TABLE `craft_log` (\n" +
                "\t`id` INT(10) NOT NULL AUTO_INCREMENT,\n" +
                "\t`type` VARCHAR(10) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`uuid` VARCHAR(36) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`mcid` VARCHAR(16) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`location` TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`recipe` TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`amount` INT(10) NULL DEFAULT NULL,\n" +
                "\t`date` DATETIME NULL DEFAULT NULL,\n" +
                "\tPRIMARY KEY (`id`) USING BTREE\n" +
                ")" +
                ";\n") {}

        reloadPluginConfig()
    }
}