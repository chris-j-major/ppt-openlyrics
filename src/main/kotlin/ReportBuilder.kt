import java.io.File

class ReportBuilder(private val basePath: File, private val output: File):SongTargets {

    private val rows = mutableListOf<ReportRow>(
        ReportRow("file" , "result" , "title" , "author",
        "verses","order")
    )

    data class ReportRow( val filePart:String , val result:String , val title:String ="", val author:String ="",
                          val verses:String="" , val order:String ="" ){
        override fun toString(): String {
            return listOf(quote(filePart),quote(result),quote(title),quote(author),verses,order).joinToString(",")
        }

        private fun quote(s: String): String = "\""+s.replace("\"","''")+"\""
    }

    fun recordSuccess(file: File, song: Song) {
        rows.add( ReportRow( trimFile(file) , "ok" ,
            song.metadata["title"]?:"",
            song.metadata["author"]?:"" , song.verses.size.toString() , song.songOrder ?: "-" ))
    }

    private fun trimFile(file: File) = file.toRelativeString(basePath)

    fun recordFailure(file: File, e: Exception) {
        rows.add( ReportRow( trimFile(file) , e.toString() ))
    }
    fun recordDuplicate(file: File, song: Song) {
        rows.add( ReportRow( trimFile(file) , "duplicate",song.metadata["title"]?:""))
    }

    fun toCsv(file: File) {
        file.writeText( rows.joinToString("\n") { x -> x.toString() })
    }

    override fun process(source: File, song: Song) = recordSuccess(source,song)

    override fun complete() {
        this.toCsv(output)
    }

}
