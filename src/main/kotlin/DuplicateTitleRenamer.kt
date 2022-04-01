class DuplicateTitleRenamer {

    private val songTitleCounter = mutableMapOf<String,Int>()

    fun process(song: Song):Song {
        val title = song.metadata["title"]!!
        val counter = (songTitleCounter[title]?:0);
        songTitleCounter[title] = counter + 1
        return if (counter == 0) {
            song
        }else{
            val newMetadata = song.metadata.set("title", title+" "+makeAlternateString(counter))
            song.copy(metadata = newMetadata)
        }
    }

    private fun makeAlternateString(counter: Int) =
         if (counter <= 1){
            "(Alt)"
        }else{
             "(Alt $counter)"
        }

}
