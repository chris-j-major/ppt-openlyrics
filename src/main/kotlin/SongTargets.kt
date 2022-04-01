import java.io.File

interface SongTargets {
    fun process(source: File, song:Song)
    fun complete()
}
