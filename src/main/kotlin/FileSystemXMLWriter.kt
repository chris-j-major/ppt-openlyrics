
import java.io.File
import java.io.FileWriter
import java.util.*

class FileSystemXMLWriter(private val target: File, private val characters_to_form_path:Int=1 ) : SongTargets {

    override fun process(source: File, song: Song) {
        val name = source.nameWithoutExtension
        val outputName = recursiveFormPath( target , name ).resolve( "$name.xml")
        song.toXml(FileWriter( outputName))
    }

    private fun recursiveFormPath(target: File, name: String, counter:Int=0):File {
        return if (counter >= characters_to_form_path){
            target
        }else{
            val firstCharacter = name.first().toString().lowercase(Locale.getDefault())
            val path = target.resolve(firstCharacter)
            if (!path.isDirectory) {
                path.mkdirs()
            }
            recursiveFormPath(path,name.substring(1),counter+1)
        }
    }

    override fun complete() {

    }

}
