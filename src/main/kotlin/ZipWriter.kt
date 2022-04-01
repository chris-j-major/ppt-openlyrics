import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipXMLWriter(target: File) : SongTargets {

    private val stream = ZipOutputStream(BufferedOutputStream(FileOutputStream(target)))

    override fun process(source: File, song: Song) {
        val outputName = source.nameWithoutExtension + ".xml"
        val entry = ZipEntry(outputName)
        stream.putNextEntry(entry)
        song.toXml(stream.writer())
    }

    override fun complete() {
        stream.close()
    }

}
