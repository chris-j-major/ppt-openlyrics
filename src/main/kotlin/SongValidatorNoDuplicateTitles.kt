open class SongValidatorNoDuplicateTitles():SongValidator() {

    private val names = mutableSetOf<String>()

    override fun check(song: Song, callback:(issue:String)->Unit) {
        super.check(song, callback)
        song.metadata["title"]?.let { title ->
            if (names.contains(title)) {
                callback("Title (\"$title\") has already been used")
            } else {
                names.add(title)
            }
        }
    }
}
