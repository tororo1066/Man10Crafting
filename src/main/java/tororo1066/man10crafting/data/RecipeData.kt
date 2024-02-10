package tororo1066.man10crafting.data

import io.papermc.paper.potion.PotionMix
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.*
import tororo1066.man10crafting.Man10Crafting
import tororo1066.tororopluginapi.SStr
import java.io.File

class RecipeData {

    lateinit var type: Type
    var namespace = ""
    var category = ""
    val materials = HashMap<Char,ItemStack>()
    val shapelessMaterials = ArrayList<ItemStack>()
    val shape = ArrayList<String>()
    lateinit var result: ItemStack
    lateinit var singleMaterial: ItemStack
    var furnaceExp = 0f
    var furnaceTime = 0

    lateinit var smithingMaterial: ItemStack
    var smithingAdditionalMaterial: ItemStack? = null
    var smithingTransform = true
    var smithingCopyNbt = false

    lateinit var potionInput: ItemStack

    var permission = ""
    var enabled = true

    private lateinit var recipe: Recipe

    private lateinit var potionRecipe: PotionMix
    var brewingTime = -1

    var returnBottle = true
    var command = ""
    var index = Int.MAX_VALUE

    companion object{
        fun loadFromYml(category: String, id: String): RecipeData {
            val yml = Man10Crafting.sConfig.getConfig("recipes/${category}/$id")?:return RecipeData()
            val recipe = RecipeData()
            recipe.namespace = id
            recipe.category = category
            recipe.type = Type.valueOf(yml.getString("type","shaped")!!.uppercase())
            when(recipe.type){
                Type.SHAPED->{
                    val section = yml.getConfigurationSection("material")!!
                    section.getKeys(false).forEach {
                        recipe.materials[it.toCharArray()[0]] = section.getItemStack(it)!!
                    }
                    recipe.shape.addAll(yml.getStringList("shape"))
                }
                Type.SHAPELESS->{
                    @Suppress("UNCHECKED_CAST")
                    recipe.shapelessMaterials.addAll(yml.get("material") as List<ItemStack>)
                }
                Type.FURNACE, Type.BLASTING, Type.SMOKING->{
                    recipe.singleMaterial = yml.getItemStack("material")!!
                    recipe.furnaceExp = yml.getInt("furnace.exp").toFloat()
                    recipe.furnaceTime = yml.getInt("furnace.time")
                }
                Type.SMITHING->{
                    recipe.singleMaterial = yml.getItemStack("material")!!
                    recipe.smithingMaterial = yml.getItemStack("smithMaterial")!!
                    recipe.smithingAdditionalMaterial = yml.getItemStack("additionalMaterial")
                    recipe.smithingTransform = yml.getBoolean("transform",true)
                    recipe.smithingCopyNbt = yml.getBoolean("copyNbt",false)
                }
                Type.STONECUTTING->{
                    recipe.singleMaterial = yml.getItemStack("material")!!
                }
                Type.BREWING->{
                    recipe.potionInput = yml.getItemStack("input")!!
                    recipe.singleMaterial = yml.getItemStack("material")!!
                    recipe.brewingTime = yml.getInt("brewingTime",-1)
                }
            }
            recipe.result = yml.getItemStack("result")!!
            recipe.permission = yml.getString("permission","")!!
            recipe.enabled = yml.getBoolean("enabled",true)
            recipe.returnBottle = yml.getBoolean("returnBottle")
            recipe.command = yml.getString("command","")!!
            recipe.index = yml.getInt("index",Int.MAX_VALUE)

            return recipe
        }
    }

    fun create(){
        val namespace = NamespacedKey(Man10Crafting.plugin,namespace)
        val recipe: Recipe = when(type){
            Type.SHAPED->{
                val shaped = ShapedRecipe(namespace,result)
                shaped.shape(shape[0],shape[1],shape[2])
                materials.forEach {
                    shaped.setIngredient(it.key,it.value)
                }
                shaped
            }
            Type.SHAPELESS->{
                val shapeless = ShapelessRecipe(namespace,result)
                shapelessMaterials.forEach {
                    shapeless.addIngredient(it)
                }
                shapeless
            }
            Type.FURNACE->{
                FurnaceRecipe(namespace,result,RecipeChoice.ExactChoice(singleMaterial),furnaceExp,furnaceTime)
            }
            Type.SMOKING->{
                SmokingRecipe(namespace,result,RecipeChoice.ExactChoice(singleMaterial),furnaceExp,furnaceTime)
            }
            Type.BLASTING->{
                BlastingRecipe(namespace,result,RecipeChoice.ExactChoice(singleMaterial),furnaceExp,furnaceTime)
            }
            Type.SMITHING->{
                Man10Crafting.smithUtil.create(namespace,singleMaterial,smithingMaterial,smithingAdditionalMaterial,result,smithingTransform,smithingCopyNbt)
            }
            Type.STONECUTTING->{
                StonecuttingRecipe(namespace,result,RecipeChoice.ExactChoice(singleMaterial))
            }

            Type.BREWING->{
                potionRecipe = PotionMix(namespace,result,RecipeChoice.ExactChoice(potionInput),RecipeChoice.ExactChoice(singleMaterial))
                return
            }
        }
        this.recipe = recipe
    }

    fun saveConfig(){
        if (Man10Crafting.sConfig.exists("recipes/${namespace}")){
            File("${Man10Crafting.plugin.dataFolder.path}/recipes/${category}/${namespace}.yml").delete()
        }
        val file = File("${Man10Crafting.plugin.dataFolder.path}/recipes/${category}/${namespace}.yml")
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        file.createNewFile()
        val config = Man10Crafting.sConfig.getConfig("recipes/${category}/${namespace}")!!

        config.set("type",type.name.lowercase())

        when(type){
            Type.SHAPED->{
                config.set("material",materials)
                config.set("shape",shape)
            }
            Type.SHAPELESS->{
                config.set("material",shapelessMaterials)
            }
            Type.FURNACE,Type.SMOKING,Type.BLASTING->{
                config.set("material",singleMaterial)
                config.set("furnace.exp",furnaceExp.toInt())
                config.set("furnace.time",furnaceTime)
            }
            Type.SMITHING->{
                config.set("material",singleMaterial)
                config.set("smithMaterial",smithingMaterial)
                config.set("additionalMaterial",smithingAdditionalMaterial)
                config.set("transform",smithingTransform)
                config.set("copyNbt",smithingCopyNbt)
            }
            Type.STONECUTTING->{
                config.set("material",singleMaterial)
            }
            Type.BREWING->{
                config.set("input",potionInput)
                config.set("material",singleMaterial)
                if (brewingTime != -1) {
                    config.set("brewingTime", brewingTime)
                }
            }
        }

        if (permission.isNotBlank()){
            config.set("permission",permission)
        }

        if (returnBottle){
            config.set("returnBottle",true)
        }

        if (command.isNotBlank()){
            config.set("command",command)
        }

        if (index != Int.MAX_VALUE){
            config.set("index",index)
        }

        config.set("enabled",enabled)

        config.set("result",result)

        Man10Crafting.sConfig.saveConfig(config,"recipes/${category}/${namespace}")
    }

    fun register(){
        if (type == Type.BREWING && !SStr.isPaper()) {
            Man10Crafting.plugin.logger.warning("Brewing recipe is not supported in not paper server")
            return
        }
        if (!::recipe.isInitialized || (type == Type.BREWING && !::potionRecipe.isInitialized)){
            create()
        }
        if (type == Type.BREWING) {
            val namespacedKey = potionRecipe.key
            Bukkit.getPotionBrewer().removePotionMix(namespacedKey)
            Bukkit.getPotionBrewer().addPotionMix(potionRecipe)
            Man10Crafting.recipes[namespace] = this
            return
        }
        val namespacedKey = NamespacedKey(Man10Crafting.plugin,namespace)
        val defaultRecipe = Bukkit.getRecipe(namespacedKey)
        if (defaultRecipe != null){
            Bukkit.removeRecipe(namespacedKey)
        }
        Bukkit.addRecipe(recipe)
        Man10Crafting.recipes[namespace] = this
    }

    fun checkNeed(p: Player): Boolean{
        if (!enabled) return false
        if (permission.isNotBlank() && !p.hasPermission(permission))return false

        return true
    }


    enum class Type(val material: Material){
        SHAPED(Material.CRAFTING_TABLE),
        SHAPELESS(Material.CRAFTING_TABLE),
        FURNACE(Material.FURNACE),
        SMOKING(Material.SMOKER),
        BLASTING(Material.BLAST_FURNACE),
        SMITHING(Material.SMITHING_TABLE),
        STONECUTTING(Material.STONECUTTER),
        BREWING(Material.BREWING_STAND)
    }
}