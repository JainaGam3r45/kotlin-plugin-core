package com.jifercraft.core.config

import com.jifercraft.core.CorePlugin
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStream

class PluginConfig(private val plugin: CorePlugin) {

    private lateinit var root: YamlConfiguration
    private val configs = mutableMapOf<String, YamlConfiguration>()

    // Kotlin properties are honestly so much cleaner than getters
    val file: File get() = File(plugin.dataFolder, "config.yml")

    fun load() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        root = loadFile("config.yml")
    }

    fun section(path: String): ConfigurationSection? {
        return root.getConfigurationSection(path)
    }

    // Still getting used to operator overloading but this is pretty cool
    operator fun <T> get(path: String, default: T): T {
        @Suppress("UNCHECKED_CAST")
        return (root.get(path) as? T) ?: default
    }

    fun loadFile(name: String): YamlConfiguration {
        return configs.getOrPut(name) {
            val target = File(plugin.dataFolder, name)
            val resource = plugin.getResource(name) 
            
            if (resource != null) {
                if (!target.exists()) {
                    plugin.saveResource(name, false)
                } else {
                    // Update the file structure physically before loading the YamlConfiguration
                    // That way we don't wipe out all the user's comments when dumping
                    mergeConfig(target, resource)
                }
            } else if (!target.exists()) {
                target.createNewFile()
            }
            
            YamlConfiguration.loadConfiguration(target)
        }
    }

    fun reload(name: String? = null) {
        if (name != null) {
            configs.remove(name)
            loadFile(name)
        } else {
            val keys = configs.keys.toList()
            configs.clear()
            load()
            for (key in keys) {
                if (key != "config.yml") {
                    loadFile(key)
                }
            }
        }
    }

    fun save(name: String) {
        configs[name]?.let { cfg ->
            cfg.save(File(plugin.dataFolder, name))
        }
    }

    // hate that bukkit's yaml drops all comments.
    // doing a custom text merger instead of pulling in an external dep.
    // this was painful to write.

    private fun mergeConfig(userFile: File, defaultResource: InputStream) {
        val userLines = userFile.readLines() // kotlin file IO is actually really nice
        val defaultLines = defaultResource.bufferedReader().readLines()

        val userBlocks = parseBlocks(userLines)
        val defaultBlocks = parseBlocks(defaultLines)

        val finalBlocks = mutableListOf<Block>()
        val addedPaths = mutableSetOf<String>()

        for ((path, defaultBlock) in defaultBlocks) {
            if (path == "__trailing__") continue
            
            if (userBlocks.containsKey(path)) {
                val userBlock = userBlocks[path]!!
                val mergedHeader = mutableListOf<String>()
                
                mergedHeader.addAll(defaultBlock.header)
                
                // save custom user comments so they don't get wiped
                for (line in userBlock.header) {
                    if (line.isNotBlank() && !defaultBlock.header.contains(line)) {
                        mergedHeader.add(line)
                    }
                }

                finalBlocks.add(
                    Block(
                        path = path,
                        header = mergedHeader,
                        keyLine = userBlock.keyLine,
                        valueLines = userBlock.valueLines
                    )
                )
            } else {
                finalBlocks.add(defaultBlock)
            }
            addedPaths.add(path)
        }

        // grab anything the user added that wasn't in the default file
        for (missing in userBlocks.values) {
            if (addedPaths.contains(missing.path) || missing.path == "__trailing__") {
                continue
            }
            
            if (!missing.path.contains(".")) {
                finalBlocks.add(missing)
            } else {
                val parentPath = missing.path.substringBeforeLast('.')
                var insertIndex = -1
                // find where to tuck this back in
                for (i in finalBlocks.indices.reversed()) {
                    if (finalBlocks[i].path == parentPath || finalBlocks[i].path.startsWith("$parentPath.")) {
                        insertIndex = i
                        break
                    }
                }
                
                if (insertIndex != -1) {
                    finalBlocks.add(insertIndex + 1, missing)
                } else {
                    finalBlocks.add(missing)
                }
            }
        }

        val outputLines = mutableListOf<String>()
        for (block in finalBlocks) {
            outputLines.addAll(block.header)
            outputLines.add(block.keyLine)
            outputLines.addAll(block.valueLines)
        }

        if (userBlocks.containsKey("__trailing__")) {
            outputLines.addAll(userBlocks["__trailing__"]!!.header)
        } else if (defaultBlocks.containsKey("__trailing__")) {
            outputLines.addAll(defaultBlocks["__trailing__"]!!.header)
        }

        userFile.writeText(outputLines.joinToString("\n") + "\n")
    }

    private data class Block(
        val path: String,
        val header: MutableList<String> = mutableListOf(),
        var keyLine: String = "",
        val valueLines: MutableList<String> = mutableListOf()
    )

    private data class PathNode(val key: String, val indent: Int)

    // spent way too long stuck on this - quoted keys can have colons inside them 
    private val KEY_REGEX = "^(\\s*)((?:\"[^\"]*\")|(?:'[^']*')|[^:]+?):(?:\\s+(.*)|\\s*)$".toRegex()

    private fun parseBlocks(lines: List<String>): Map<String, Block> {
        val blocks = LinkedHashMap<String, Block>() 
        val currentPath = mutableListOf<PathNode>() 
        val pendingLines = mutableListOf<String>()

        var currentBlock: Block? = null
        var activeIndent = -1
        
        // tracking when to eat nested list lines so we don't accidentally parse them as keys
        var swallowIndentThreshold = -1

        for (line in lines) {
            val trimmed = line.trimStart()
            val currentIndent = line.length - trimmed.length

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                if (swallowIndentThreshold != -1 && currentIndent > swallowIndentThreshold && currentBlock != null) {
                    currentBlock.valueLines.add(line)
                } else {
                    pendingLines.add(line)
                }
                continue
            }

            if (swallowIndentThreshold != -1 && currentIndent > swallowIndentThreshold) {
                currentBlock?.valueLines?.add(line)
                continue
            } else {
                swallowIndentThreshold = -1
            }

            if (trimmed.startsWith("- ") || trimmed == "-") {
                if (currentBlock != null) {
                    swallowIndentThreshold = activeIndent
                    currentBlock.valueLines.add(line)
                } else {
                    pendingLines.add(line)
                }
                continue
            }

            val keyMatch = KEY_REGEX.matchEntire(line)
            if (keyMatch != null) {
                val indent = keyMatch.groupValues[1].length
                var key = keyMatch.groupValues[2].trim()
                
                if ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'"))) {
                    key = key.substring(1, key.length - 1)
                }

                val remainder = keyMatch.groupValues[3].trim()

                while (currentPath.isNotEmpty() && currentPath.last().indent >= indent) {
                    currentPath.removeAt(currentPath.size - 1)
                }
                
                currentPath.add(PathNode(key, indent))
                
                // building the dot-separated path string manully. joinToString is nice here.
                val pathString = currentPath.joinToString(".") { it.key }

                val newBlock = Block(pathString)
                newBlock.header.addAll(pendingLines)
                newBlock.keyLine = line
                blocks[pathString] = newBlock

                currentBlock = newBlock
                activeIndent = indent
                pendingLines.clear()

                // block scalars! (> or |) need to trigger the swallow so we don't break
                if (remainder == "|" || remainder == ">" || remainder == "|-" || remainder == ">-") {
                    swallowIndentThreshold = indent
                }
            } else {
                if (currentBlock != null) {
                    currentBlock.valueLines.add(line)
                } else {
                    pendingLines.add(line)
                }
            }
        }

        if (pendingLines.isNotEmpty()) {
            blocks["__trailing__"] = Block("__trailing__", header = pendingLines)
        }

        return blocks
    }
}
