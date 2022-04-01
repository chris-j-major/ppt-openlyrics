class DuplicateSkipper {

    private val seenSongs = HashSet<String>()

    fun isThisANewSong(song: Song): Boolean {
        val songContent = song.convertToFlatContent()
        return if (seenSongs.contains(songContent)){
            false
        }else{
            seenSongs.add(songContent)
            true
        }
    }

}
