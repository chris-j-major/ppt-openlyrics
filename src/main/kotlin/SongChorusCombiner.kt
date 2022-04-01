class SongChorusCombiner {
    fun process(input: Song): Song {
        val verses = mutableSetOf<Verse>()
        val choruses = mutableSetOf<Verse>()
        val order = mutableListOf<String>()
        for ( v in input.verses){
            if (v.isVerse() ){
                verses.add(v)
            }else{
                choruses.add(v)
            }
            order.add(v.name)
        }
        if (choruses.isEmpty()){
            return input
        }
        // look for pairs of choruses that are identical
        val processedChoruses = mutableSetOf<Verse>()
        val replacements = mutableMapOf<String,String>()
        for (c in choruses){
            val match:Verse? = findMatchingVerse( c , processedChoruses )
            if (match==null){
                processedChoruses.add(c)
            }else{
                replacements[c.name] = match.name
            }
        }
        if (replacements.isEmpty()){
            return input
        }
        val newOrder = applyReplacements( order , replacements )
        return Song( input.metadata , verses.toList()+processedChoruses.toList(), newOrder.joinToString(" ") )
    }

    private fun findMatchingVerse(needle: Verse, haystack: MutableSet<Verse>): Verse? {
        for ( hay in haystack ){
            if ( needle.wordsMatch(hay) ) return hay
        }
        return null
    }

    private fun applyReplacements(order: List<String>, replacements: Map<String, String>): List<String> =
        order.map { replacements.getOrDefault(it,it) }

}
