import org.apache.poi.hslf.usermodel.HSLFAutoShape
import org.apache.poi.hslf.usermodel.HSLFSlideShow
import org.apache.poi.hslf.usermodel.HSLFTextBox
import org.apache.poi.sl.usermodel.*
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileInputStream

class Extractor {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun extract(source: File): Song? {
        return if (source.name.endsWith("pptx")) {
            SlideExtractor(XMLSlideShow(FileInputStream(source))).extract(source)
        } else if (source.name.endsWith("ppt")) {
            SlideExtractor(HSLFSlideShow(FileInputStream(source))).extract(source)
        } else{
            logger.warn("Unable to process file \"$source\" due to unknown extension")
            null
        }
    }
}

data class ParserLine(val regex: Regex, val apply: (MatchResult,SongMetaData)->SongMetaData)

data class TextRunInfo(val text: String, val size: Double, val bold: Boolean, val italic: Boolean)

class SlideExtractor<S: Shape<S, P>,P: TextParagraph<S, P, out TextRun?>>(private val ppt: SlideShow<S,P>){
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun extract(source:File): Song? {
        val verses = mutableListOf<Verse>()

        var verseCount = 0
        var chorusCount = 0
        var details = SongMetaData()

        for (slide in ppt.slides) {
            slide.getText()?.let { text ->
                val textSize = this.getTextSizes(text)
                val textString = text.text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                if (couldBeChorus(textString, textSize)) {
                    verses.add(Verse("c${++chorusCount}", textString))
                } else if (couldBeVerse(textString, textSize)) {
                    verses.add(Verse("v${++verseCount}", textString))
                } else {
                    println("Unknown slide at ${slide.slideNumber}")
                }
            }
        }
        // try to get title/reference from first slide
        if (ppt.slides.isNotEmpty()){
            details = extractMetadataFromFirstSlide(ppt.slides.first()!!, details)
            details = extractMetadataFromLastSlide(ppt.slides.last()!!, details)
            details = details.set("source", source.name)
            if (details["title"] == "Responsive Reading") {
                details = details.set("title", "Responsive Reading " + source.nameWithoutExtension)
            }
            return Song(details, verses)
        }else{
            logger.warn("No slides found in $source")
            return null
        }
    }

    private fun getTextSizes( texts:List<TextShape<*,*>> ):List<TextRunInfo>{
        return texts.flatMap {
            getTextSizes( it )
        }
    }

    private fun getTextSizes( text:TextShape<*,*> ):List<TextRunInfo>{
        return text.textParagraphs.flatMap {
            getTextSizes( it )
        }
    }

    private fun getTextSizes( text:TextParagraph<*,*,out TextRun> ):List<TextRunInfo>{
        val output = mutableListOf<TextRunInfo>()
        var active:TextRunInfo? = null
        for (textRun in  text.textRuns ){
            val textSize = textRun.fontSize ?:  text.defaultFontSize
            if ( active != null ){
                if ( (active.bold == textRun.isBold) && (active.italic == textRun.isItalic) && (active.size == textSize)){
                    active = TextRunInfo( active.text + textRun.rawText.trim() , textSize , textRun.isBold , textRun.isItalic)
                    if ( textRun.rawText.endsWithNewLine() ){
                        output.add(active)
                        active = null
                    }
                    continue
                }else{
                    output.add(active)
                    active = null
                }
            }
            active = TextRunInfo( textRun.rawText , textSize , textRun.isBold , textRun.isItalic)
            if ( textRun.rawText.endsWithNewLine() ){
                output.add(active)
                active = null
            }
        }
        if ( active != null ){
            output.add(active)
        }
        return output.filter { it.text.isNotBlank() }
    }

    private val parserRegexps = listOf(
        ParserLine(Regex("Words and music by ([A-Za-z. ]*)", RegexOption.IGNORE_CASE) ){
                match, d -> d.set("author",match.groupValues[1].autoTrim(" Copyright").trim())},
        ParserLine(Regex("Words by ([A-Za-z. ]*)",RegexOption.IGNORE_CASE) ){
                match,d ->d.set("author",match.groupValues[1].autoTrim(" Copyright").trim())},
        ParserLine(Regex("Copyright (.*)",RegexOption.IGNORE_CASE) ){
                match,d -> d.set("copyright",match.groupValues[1].trim())},
        ParserLine(Regex("Based on (.*)",RegexOption.IGNORE_CASE) ){
                match,d -> d.set("copyright",match.groupValues[1].trim())}
    )

    private fun extractMetadataFromFirstSlide(slide: Slide<S, P>, details: SongMetaData):SongMetaData {
        var d = details
        val texts = slide.getTexts()
        val textSizes = texts.flatMap { this.getTextSizes(texts) }

        // find the top-most block
        texts.minByOrNull { this.getPosition(it)?.minY ?: 10000.0 }?.let { topmost ->
            val lines = topmost.text.split("\n").filter { it.isNotBlank() }
            if (lines.isNotEmpty()) {
                d = d.set("title", lines.first())
            }
        }

        val references = textSizes.filter { it.text.startsWith("(") &&  it.text.endsWith(")")  }
        if (references.size==1){
            d = d.set("ref",references.first().text)
        }

        return d
    }

    private fun extractMetadataFromLastSlide(slide: Slide<S, P>, details: SongMetaData):SongMetaData {
        var d = details
        val texts = slide.getTexts()

        val textSizes = texts.flatMap { this.getTextSizes(texts) }
        val mainTextSize = textSizes.maxOf { it.size }
        val smallTexts = textSizes.filter { it.size < mainTextSize }

        // find small text
        smallTexts.forEach { text ->
            if ( !text.text.contains("36671") ){
                parserRegexps.forEach { parser ->
                    parser.regex.findAll(text.text).firstOrNull()?.let { match->
                        d = parser.apply(match,d)
                    }
                }
            }
        }
        return d
    }

    private fun couldBeChorus(lines: List<String>, textSizes: List<TextRunInfo>): Boolean {
        for (text in lines){
            if (text.contains("chorus")){
                return true
            }
        }
        if ( textSizes.all { it.italic }) return true
        return false
    }

    private fun couldBeVerse(lines: List<String>, textSize: List<TextRunInfo>): Boolean {
        for (text in lines){
            if (text.isNotEmpty()){
                return true
            }
        }
        return false
    }

    private fun getPosition(text:TextShape<*, *> ): Rectangle2D? {
        return when (text) {
            is HSLFTextBox -> {
                text.anchor
            }
            is HSLFAutoShape -> {
                text.anchor
            }
            else -> {
                null
            }
        }
    }
}

private fun String.autoTrim(s: String)= this.replace(s,"")

private fun String.endsWithNewLine(): Boolean {
    if (this.endsWith("\n") ) return true
    if (this.endsWith("\r") ) return true
    return false
}

private fun <S: Shape<S, P>,P: TextParagraph<S, P, out TextRun?>> Slide<S,P>.getTexts(): List<TextShape<*,*>> {
    return this.shapes.mapNotNull {
        if(it is TextShape<*, *>) {
            it
        }else{
            null
        }
    }
}

private fun <S: Shape<S, P>,P: TextParagraph<S, P, out TextRun?>> Slide<S,P>.getText(): TextShape<*,*>? {
    val textBlocks = this.getTexts()
    return textBlocks.maxByOrNull { it.text.length }
}
