class SongMetaData(private val metadata:Map<String,String> = mapOf()){

    fun getWithDefault(key:String,defaultValue:String) =  metadata.getOrDefault(key,defaultValue)

    fun set(key: String, value: String): SongMetaData {
        val newMap = metadata.toMutableMap()
        newMap[key] = value
        return SongMetaData(newMap)
    }

    override fun toString(): String =
        metadata.map { it.key+"="+it.value }.joinToString(", ")

    operator fun get(key: String) = metadata[key]

    fun sortedFlattenedString(): String {
       val keys = metadata.keys.sorted()
        return keys.joinToString(",") { "${it}=\"${metadata[it]}\"" }
    }
}
