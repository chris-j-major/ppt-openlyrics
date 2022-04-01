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
    val report = ReportBuilder(source,target.resolve("report.csv"))
    val skipDuplicates = DuplicateSkipper()
    val duplicateTitleRenamer = DuplicateTitleRenamer()

    val songOutputs = listOf<SongTargets>(
        report,
        statCounter,
        ZipXMLWriter(target.resolve("all-songs.zip")),
        FileSystemXMLWriter(target)
    )

    forAllFiles(source){ file:File ->
        if (file.extension == "ppt") {
            try {
                extractor.extract(file)?.let { initial_song ->
                    val song = processor.process(initial_song)
                    if (skipDuplicates.isThisANewSong(song) ) {
                        duplicateTitleRenamer.process(song).let { retitledsong ->
                            validator.raise(retitledsong, "while processing $file")
                            songOutputs.forEach { it.process(file,retitledsong) }
                        }
                    }else{
                        report.recordDuplicate(file, song)
                        logger.info("Skipped a duplicate song $file")
                    }
                }
            }catch(e:Exception){
                report.recordFailure(file,e)
                logger.error("Failed to converted $file to $e")
                e.printStackTrace()
            }
        }
    }
    songOutputs.forEach { it.complete() }
    logger.info(statCounter.toString())
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
