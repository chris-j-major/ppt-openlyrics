import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

open class SongValidator {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun assert(song: Song,message:String) {
        this.check(song){ issue->
            logger.error(issue)
            logger.error(message)
            exitProcess(5)
        }
    }

    fun log(song: Song) {
        this.check(song){ issue->
            logger.warn(issue)
        }
    }


    protected open fun check(song: Song, callback:(issue:String)->Unit) {
        val title = song.metadata["title"]
        if (title==null){
            callback("Title is not specified")
        }else if (title.length < 4){
            callback("Title is very short \"${title}\"")
        }
        val author = song.metadata["author"]
        if (author!==null &&author.length < 4){
            callback("Author is specified but very short \"${author}\"")
        }
    }

}
