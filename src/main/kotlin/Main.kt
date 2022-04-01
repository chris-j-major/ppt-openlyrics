import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun main(args: Array<String>) {

    val logger: Logger = LoggerFactory.getLogger("")

    println("Program arguments: ${args.joinToString()}")

    val extractor = Extractor()

    val target = File("C:\\Users\\chris\\Song library-output\\")
    val source = File("C:\\Users\\chris\\Song library\\")

    if (!target.isDirectory){
        target.mkdirs()
    }

    val validator = SongValidatorNoDuplicateTitles()
    val processor = SongChorusCombiner()
    val statCounter = StatCounter()
    val report = ReportBuilder(source)

    forAllFiles(source){ file:File ->
        if (file.extension == "ppt") {
            try {
                extractor.extract(file)?.let { initial_song ->
                    val song = processor.process(initial_song)
                    validator.raise(song,"while processing $file")
                    val name = file.nameWithoutExtension
                    val firstCharacter = name.first().toString()
                    val path = target.resolve( firstCharacter )
                    if (!path.isDirectory){
                        path.mkdirs()
                    }
                    val outputName = path.resolve(file.nameWithoutExtension + ".xml")
                    song.toXml(outputName)
                    statCounter.count(song)
                    report.recordSuccess(file,song)
                    //logger.info("Converted ${file.name} to ${outputName.name}  \"${song.metadata["title"]}\"")
                }
            }catch(e:Exception){
                report.recordFailure(file,e)
                logger.error("Failed to converted $file to $e")
                e.printStackTrace()
            }
        }
    }
    logger.info(statCounter.toString())
    report.toCsv(target.resolve("report.csv"))
}

fun forAllFiles(start: File, function: (File) -> Unit) {
    if (start.isDirectory){
        start.listFiles()?.forEach{
            if (it!=null) forAllFiles(it,function)
        }
    }else if(start.isFile){
        function(start)
    }
}
