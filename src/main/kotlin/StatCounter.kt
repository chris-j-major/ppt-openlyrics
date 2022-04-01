import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class StatCounter:SongTargets {
    val logger: Logger = LoggerFactory.getLogger("")

    private var total_songs = 0
    private var total_verses = 0
    private var real_verses = 0
    private var real_choruses =0

    override fun process(source: File, song: Song) {
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

    override fun complete() {
        logger.info( this.toString() )
    }

    override fun toString() = "$total_songs Songs, $total_verses verses (${real_verses}/${real_choruses} choruses)"
}