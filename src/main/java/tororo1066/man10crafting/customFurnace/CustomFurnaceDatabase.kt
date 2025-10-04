package tororo1066.man10crafting.customFurnace

import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.database.SDBCondition
import tororo1066.tororopluginapi.database.SDBVariable
import tororo1066.tororopluginapi.database.SDatabase
import tororo1066.tororopluginapi.sItem.SItem
import java.io.File
import java.util.concurrent.CompletableFuture

class CustomFurnaceDatabase {
    val sDatabase by lazy {
        val file = File(SJavaPlugin.plugin.dataFolder.path + "/customFurnace/config.yml")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        SDatabase.newInstance(SJavaPlugin.plugin, file.path, null)
    }
    val serverName = SJavaPlugin.plugin.config.getString("database.serverName") ?: "undefined"
    val actualTableName = "${serverName}_$TABLE_NAME"

    companion object {
        const val TABLE_NAME = "custom_furnace"
    }

    fun createTable(): CompletableFuture<Boolean> {
        return sDatabase.asyncCreateTable(actualTableName, mapOf(
            "id" to SDBVariable(SDBVariable.Int, autoIncrement = true),
            "world" to SDBVariable(SDBVariable.TinyText),
            "x" to SDBVariable(SDBVariable.Int),
            "y" to SDBVariable(SDBVariable.Int),
            "z" to SDBVariable(SDBVariable.Int),
            "furnace" to SDBVariable(SDBVariable.TinyText),
            "input" to SDBVariable(SDBVariable.Text),
            "output" to SDBVariable(SDBVariable.Text),
            "fuel" to SDBVariable(SDBVariable.Text),
            "storedExp" to SDBVariable(SDBVariable.Float),
            "progress" to SDBVariable(SDBVariable.Int),
            "remainingFuel" to SDBVariable(SDBVariable.Int),
            "currentFuel" to SDBVariable(SDBVariable.Text),
        ))
    }

    fun loadByChunk(chunk: Chunk): CompletableFuture<List<CustomFurnaceInstance>> {
        return sDatabase.asyncSelect(
            actualTableName,
            SDBCondition().equal("world", chunk.world.name).and(SDBCondition().between("x", chunk.x * 16, chunk.x * 16 + 15)).and(SDBCondition().between("z", chunk.z * 16, chunk.z * 16 + 15))
        ).thenApplyAsync { result ->
            return@thenApplyAsync result.mapNotNull {
                val location = Location(Bukkit.getWorld(it.getString("world")), it.getInt("x").toDouble(), it.getInt("y").toDouble(), it.getInt("z").toDouble())
                val furnace = CustomFurnaceManager.instance.getCustomFurnace(it.getString("furnace")) ?: return@mapNotNull null
                val input = it.getNullableString("input")?.let { let -> SItem.fromBase64(let)?.build() }
                val output = it.getNullableString("output")?.let { let -> SItem.fromBase64(let)?.build() }
                val fuel = it.getNullableString("fuel")?.let { let -> SItem.fromBase64(let)?.build() }
                val storedExp = it.getFloat("storedExp")
                val progress = it.getInt("progress")
                val remainingFuel = it.getInt("remainingFuel")
                val currentFuel = it.getNullableString("currentFuel")?.let { let -> SItem.fromBase64(let)?.build() }
                CustomFurnaceInstance().apply {
                    this.customFurnace = furnace
                    this.location = location
                    this.input = input
                    this.output = output
                    this.fuel = fuel
                    this.storedExp = storedExp
                    this.progress = progress
                    this.remainingFuel = remainingFuel
                    this.currentFuel = currentFuel?.let { let -> CustomFurnaceManager.instance.getFuel(let) }
                }
            }
        }
    }

    fun insert(furnace: CustomFurnaceInstance): CompletableFuture<Boolean> {
        return sDatabase.asyncInsert(actualTableName, mapOf(
            "world" to furnace.location.world.name,
            "x" to furnace.location.blockX,
            "y" to furnace.location.blockY,
            "z" to furnace.location.blockZ,
            "furnace" to furnace.customFurnace.internalName,
            "input" to furnace.input?.let { SItem(it).toBase64() },
            "output" to furnace.output?.let { SItem(it).toBase64() },
            "fuel" to furnace.fuel?.let { SItem(it).toBase64() },
            "storedExp" to furnace.storedExp,
            "progress" to furnace.progress,
            "remainingFuel" to furnace.remainingFuel,
            "currentFuel" to furnace.currentFuel?.let { SItem(it.fuelStack).toBase64() },
        ))
    }
}