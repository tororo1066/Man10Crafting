package tororo1066.man10crafting.data

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.*
import tororo1066.man10crafting.Man10Crafting
import java.io.File

class RecipeData {

    lateinit var type: Type
    var namespace = ""
    val materials = HashMap<Char,ItemStack>()
    val shapelessMaterials = ArrayList<ItemStack>()
    val shape = ArrayList<String>()
    lateinit var result: ItemStack
    lateinit var singleMaterial: ItemStack
    var furnaceExp = 0f
    var furnaceTime = 0
    lateinit var smithingMaterial: ItemStack

    var permission = ""

    private lateinit var recipe: Recipe

    companion object{
        fun loadFromYml(id: String): RecipeData {
            val yml = Man10Crafting.sConfig.getConfig("recipes/$id")?:return RecipeData()
            val recipe = RecipeData()
            recipe.namespace = id
            recipe.type = Type.valueOf(yml.getString("type","shaped")!!.uppercase())
            when(recipe.type){
                Type.SHAPED->{
                    val section = yml.getConfigurationSection("material")!!
                    section.getKeys(false).forEach {
                        Bukkit.broadcastMessage(section.getItemStack(it)!!.toString())
                        recipe.materials[it.toCharArray()[0]] = section.getItemStack(it)!!
                    }
                    yml.getStringList("shape").forEach {
                        Bukkit.broadcastMessage((it == "a  ").toString())
                        recipe.shape.add(it)
                    }

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
                }
            }
            recipe.result = yml.getItemStack("result")!!
            recipe.permission = yml.getString("permission","")!!

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
                    shaped.ingredientMap[it.key] = it.value
                    Bukkit.broadcastMessage(it.key.toString())
                }
                Bukkit.broadcastMessage(shaped.shape[0])
                Bukkit.broadcastMessage(shaped.shape[1])
                Bukkit.broadcastMessage(shaped.shape[2])
                shaped
            }
            Type.SHAPELESS->{
                val shapeless = ShapelessRecipe(namespace,result)
                shapeless.ingredientList.addAll(shapelessMaterials)
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
                SmithingRecipe(namespace,result,RecipeChoice.ExactChoice(singleMaterial),RecipeChoice.ExactChoice(smithingMaterial))
            }

        }
        this.recipe = recipe
    }

    fun saveConfig(){
        if (Man10Crafting.sConfig.exists("recipes/${namespace}")){
            File("${Man10Crafting.plugin.dataFolder.path}/recipes/${namespace}").delete()
        }
        val file = File("${Man10Crafting.plugin.dataFolder.path}/recipes/${namespace}.yml")
        file.createNewFile()
        val config = Man10Crafting.sConfig.getConfig("recipes/${namespace}")!!

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
            }
        }

        if (permission.isNotBlank()){
            config.set("permission",permission)
        }

        config.set("result",result)

        Man10Crafting.sConfig.saveConfig(config,"recipes/${namespace}")
    }

    fun register(){
        if (!::recipe.isInitialized){
            create()
        }
        Bukkit.removeRecipe(NamespacedKey(Man10Crafting.plugin,namespace))
        Bukkit.addRecipe(recipe)
        Man10Crafting.recipes[namespace] = this
    }

    fun canCraft(p: Player): Boolean{
        if (permission.isNotBlank() && !p.hasPermission(permission))return false

        return true
    }

    enum class Type{
        SHAPED,
        SHAPELESS,
        FURNACE,
        SMOKING,
        BLASTING,
        SMITHING
    }
}