import org.w3c.dom.Document
import org.w3c.dom.Element

data class Verse(val name:String, val lines:List<String> ){
    fun toXmlElement(dom: Document): Element {

        val verseElem = dom.createElement("verse")
        verseElem.setAttribute("name",name)
        for (l in lines) {
            val lineElem = dom.createElement("lines")
            verseElem.appendChild(lineElem)
            lineElem.textContent = l
        }

        return verseElem
    }

    fun isVerse(): Boolean = name.startsWith("v")

    fun wordsMatch(hay: Verse): Boolean = this.flatString() == hay.flatString()

    private fun flatString():String = lines.joinToString("\n")

}