package com.example.data

data class CrosswordWord(
    val id: Int,
    val word: String, // Uppercase
    val clue: String,
    val isAcross: Boolean,
    val row: Int,
    val col: Int,
    val length: Int
)

data class Level(
    val id: Int,
    val title: String,
    val words: List<CrosswordWord>,
    val gridWidth: Int,
    val gridHeight: Int
)

object LevelsData {
    val levels = listOf(
        Level(
            id = 1,
            title = "Aesthetic Basics",
            gridWidth = 5,
            gridHeight = 5,
            words = listOf(
                CrosswordWord(1, "FLASH", "A sudden brief burst of bright light.", true, 0, 0, 5),
                CrosswordWord(2, "LIGHT", "The natural agent that stimulates sight.", false, 0, 1, 5),
                CrosswordWord(3, "AGENT", "A person who acts on behalf of another.", true, 2, 0, 5),
                CrosswordWord(4, "SINCE", "From a particular time in the past.", false, 0, 3, 5),
                CrosswordWord(5, "THEM", "Used to refer to two or more people or things.", true, 4, 1, 4)
            )
        ),
        Level(
            id = 2,
            title = "Intellectual Journey",
            gridWidth = 6,
            gridHeight = 5,
            words = listOf(
                CrosswordWord(1, "BRAIN", "An organ of soft nervous tissue in the skull.", true, 0, 1, 5),
                CrosswordWord(2, "REALM", "A kingdom or field of activity or interest.", false, 0, 2, 5),
                CrosswordWord(3, "SPACE", "A continuous area or expanse which is free.", true, 2, 0, 5),
                CrosswordWord(4, "CLEAN", "Free from dirt, marks, or stains.", false, 2, 3, 5),
                CrosswordWord(5, "SMELL", "The faculty of perceiving odors or scents.", true, 4, 1, 5)
            )
        ),
        Level(
            id = 3,
            title = "Advanced Horizons",
            gridWidth = 6,
            gridHeight = 5,
            words = listOf(
                CrosswordWord(1, "CLIMB", "Go upward with gradual or continuous progress.", true, 0, 0, 5),
                CrosswordWord(2, "LEVEL", "A position on a scale of amount, quantity, or progress.", false, 0, 1, 5),
                CrosswordWord(3, "VOCAL", "Relating to the human voice or singing.", true, 2, 1, 5),
                CrosswordWord(4, "MACRO", "Large-scale; overall; relating to a complete system.", false, 0, 3, 5),
                CrosswordWord(5, "GLOOM", "A state of partial or total darkness.", true, 4, 0, 5)
            )
        ),
        Level(
            id = 4,
            title = "Cryptic Echoes",
            gridWidth = 7,
            gridHeight = 6,
            words = listOf(
                CrosswordWord(1, "PUZZLE", "A game, toy, or problem designed to test ingenuity.", true, 0, 0, 6),
                CrosswordWord(2, "ZEALOT", "A person who is fanatical and uncompromising in pursuit of ideals.", false, 0, 3, 6),
                CrosswordWord(3, "HEART", "A hollow muscular organ that pumps blood.", true, 1, 2, 5),
                CrosswordWord(4, "RADIO", "The transmission and reception of electromagnetic waves.", false, 1, 5, 5),
                CrosswordWord(5, "PATIO", "A paved outdoor area adjoining a house.", true, 5, 1, 5)
            )
        ),
        Level(
            id = 5,
            title = "Master Scholar",
            gridWidth = 6,
            gridHeight = 6,
            words = listOf(
                CrosswordWord(1, "GENIUS", "Exceptional intellectual or creative power.", true, 0, 0, 6),
                CrosswordWord(2, "SYSTEM", "A set of things working together as parts of a mechanism.", false, 0, 5, 6),
                CrosswordWord(3, "FIFTHS", "An interval of five steps in a scale (musical or fraction).", true, 2, 0, 6),
                CrosswordWord(4, "FAVOR", "An act of kindness beyond what is due or usual.", false, 2, 2, 5),
                CrosswordWord(5, "SEVERE", "Very intense; grave or grievous.", true, 4, 0, 6)
            )
        )
    )

    // Pre-defined static meanings of all words in the game for robust offline fallback!
    val offlineMeanings = mapOf(
        "FLASH" to Triple("A sudden brief burst of bright light.", "Noun", "A flash of lightning lit up the night sky."),
        "LIGHT" to Triple("The natural agent that stimulates sight and makes things visible.", "Noun", "The room was filled with warm morning light."),
        "AGENT" to Triple("A person who acts on behalf of another person or group.", "Noun", "The real estate agent helped them find a beautiful house."),
        "SINCE" to Triple("In the intervening period between the time mentioned and the present.", "Adverb/Conjunction", "They have been friends since childhood."),
        "THEM" to Triple("Used as the object of a verb or preposition to refer to two or more people.", "Pronoun", "I saw them walking down the street."),
        
        "BRAIN" to Triple("An organ of soft nervous tissue contained in the skull of vertebrates.", "Noun", "The brain controls all bodily functions and thoughts."),
        "REALM" to Triple("A kingdom, or a primary field of interest, activity, or study.", "Noun", "He was a legend in the realm of classical physics."),
        "SPACE" to Triple("A continuous area or expanse which is free, available, or unoccupied.", "Noun", "The desk takes up too much space in this small office."),
        "CLEAN" to Triple("Free from dirt, marks, or stains; unsoiled.", "Adjective", "Keep your room clean and organized."),
        "SMELL" to Triple("The faculty or power of perceiving odors or scents by means of the nose.", "Noun/Verb", "The pleasant smell of fresh coffee filled the kitchen."),

        "CLIMB" to Triple("Go up or ascend, especially by using the hands and feet.", "Verb", "They plan to climb the mountain next weekend."),
        "LEVEL" to Triple("A position on a scale of amount, quantity, capability, or progress.", "Noun", "She reached the advanced level in her French class."),
        "VOCAL" to Triple("Relating to, or produced by the voice, or expressing opinions freely.", "Adjective", "He has been very vocal about his support for environmental conservation."),
        "MACRO" to Triple("Large-scale, overall, or relating to a whole system rather than details.", "Adjective", "The government needs to focus on macro economic policies."),
        "GLOOM" to Triple("A state of partial or total darkness, or a feeling of deep sadness.", "Noun", "The rain added to the general gloom of the day."),

        "PUZZLE" to Triple("A game, toy, or problem designed to test ingenuity or knowledge.", "Noun", "He spent the afternoon solving a difficult jigsaw puzzle."),
        "ZEALOT" to Triple("A person who is fanatical and uncompromising in pursuit of their religious, political, or other ideals.", "Noun", "The environmental zealot protested outside the factory gates."),
        "HEART" to Triple("A hollow muscular organ that pumps the blood through the circulatory system.", "Noun", "Regular exercise keeps your heart strong and healthy."),
        "RADIO" to Triple("The transmission and reception of electromagnetic waves of radio frequency, especially those carrying sound messages.", "Noun", "They listened to the news on the radio during their road trip."),
        "PATIO" to Triple("A paved outdoor area adjoining a house, typically used for dining or recreation.", "Noun", "We sat on the patio and enjoyed the warm evening breeze."),

        "GENIUS" to Triple("Exceptional intellectual or creative power or other natural ability.", "Noun", "Her genius was apparent from an early age."),
        "SYSTEM" to Triple("A set of things working together as parts of a mechanism or an interconnecting network.", "Noun", "The security system protects the building from intruders."),
        "FIFTHS" to Triple("Each of five equal parts into which something is or may be divided.", "Noun", "Four-fifths of the class attended the field trip."),
        "FAVOR" to Triple("An act of kindness beyond what is due or usual, or approval and support.", "Noun", "Could you do me a favor and carry this bag?"),
        "SEVERE" to Triple("Very intense, strict, or harsh; causing great pain, damage, or distress.", "Adjective", "A severe storm caused power outages across the city.")
    )
}
