import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


data class Song( val metadata:SongMetaData, val verses:List<Verse>, val songOrder:String? = null){
    fun toXml(output: File) {

        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val dom = db.newDocument()

        val songElem = dom.createElement("song")
        songElem.setAttribute("xmlns", "http://openlyrics.info/namespace/2009/song")
        songElem.setAttribute("createdIn", "Chris's Magic convertor")
        songElem.setAttribute("modifiedDate", SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(Date()))

        songElem.appendChild( this.addMetadata( dom ) )
        songElem.appendChild( this.addVerses( dom ) )

        dom.appendChild(songElem)

        val tr = TransformerFactory.newInstance().newTransformer()
        tr.setOutputProperty(OutputKeys.INDENT, "yes")
        tr.setOutputProperty(OutputKeys.METHOD, "xml")
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        tr.transform(
            DOMSource(dom),
            StreamResult( FileWriter(output) )
        )
    }

    private fun addVerses(dom: Document):Element {
        val lyricsElem = dom.createElement("lyrics")

        verses.forEach { lyricsElem.appendChild( it.toXmlElement(dom) ) }


        return lyricsElem

    }

    private fun addMetadata(dom: Document):Element {
        val propertiesElem = dom.createElement("properties")

        val titlesElem = dom.createElement("titles")
        propertiesElem.appendChild(titlesElem)
        val titleElem = dom.createElement("title")
        titlesElem.appendChild(titleElem)
        titleElem.textContent = this.metadata.getWithDefault("title","untitled")

        metadata["ref"]?.let {
            val commentsElem = dom.createElement("comments")
            propertiesElem.appendChild(commentsElem)
            val commentElem = dom.createElement("comment")
            commentsElem.appendChild(commentElem)
            commentElem.textContent = it
        }

        metadata["author"]?.let {
            val commentsElem = dom.createElement("authors")
            propertiesElem.appendChild(commentsElem)
            val commentElem = dom.createElement("author")
            commentsElem.appendChild(commentElem)
            commentElem.textContent = it
        }

        metadata["copyright"]?.let {
            val copyrightElem = dom.createElement("copyright")
            propertiesElem.appendChild(copyrightElem)
            copyrightElem.textContent = it
        }

        songOrder?.let{
            val verseOrderElem = dom.createElement("verseOrder")
            propertiesElem.appendChild(verseOrderElem)
            verseOrderElem.textContent = songOrder
        }



        return propertiesElem
    }

}