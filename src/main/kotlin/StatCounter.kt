class StatCounter {
    var total_songs = 0
    var total_verses = 0
    var real_verses = 0
    var real_choruses =0

    fun count(song: Song) {
        total_songs += 1
        total_verses += song.verses.size
        for (v in song.verses){
            if (v.name.startsWith("v")){
                real_verses ++
            }else{
                real_choruses ++
            }
        }
    }

    override fun toString() = "$total_songs Songs, $total_verses verses (${real_verses}/${real_choruses} choruses)"
}