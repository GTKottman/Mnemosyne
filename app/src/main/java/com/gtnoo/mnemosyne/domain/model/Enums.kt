package com.gtnoo.mnemosyne.domain.model

enum class RelationshipType(val label: String) {
    FRIEND("Friend"),
    CLOSE_FRIEND("Close Friend"),
    ROMANTIC_INTEREST("Romantic Interest"),
    PARTNER("Partner"),
    FAMILY("Family"),
    CLASSMATE("Classmate"),
    COWORKER("Coworker"),
    ACQUAINTANCE("Acquaintance"),
    OTHER("Other")
}

enum class PlaceType(val label: String) {
    HOME("Home"),
    SCHOOL("School"),
    WORK("Work"),
    SOCIAL_VENUE("Social Venue"),
    OUTDOORS("Outdoors"),
    TRANSIT("Transit"),
    OTHER("Other")
}

enum class InteractionMode(val label: String) {
    DIGITAL_ONLY("Digital Only"),
    IN_PERSON_ONLY("In Person Only"),
    BOTH("Both"),
    NO_CONTACT("No Contact")
}

enum class EntryType(val label: String) {
    DAILY_SUMMARY("Daily Summary")
}

enum class WeatherCondition(val label: String) {
    SUNNY("Sunny"),
    CLOUDY("Cloudy"),
    RAINY("Rainy"),
    STORMY("Stormy"),
    FOGGY("Foggy"),
    WINDY("Windy"),
    SNOWY("Snowy"),
    UNKNOWN("Unknown")
}

enum class Season(val label: String) {
    WINTER("Winter"),
    SPRING("Spring"),
    SUMMER("Summer"),
    FALL("Fall")
}

enum class SimilarityType(val label: String) {
    FULL_VECTOR("Full"),
    EMOTIONAL("Emotional"),
    COMMUNICATION("Communication"),
    IN_PERSON("In-Person"),
    WEATHER("Weather"),
    HEALTH_CONTEXT("Health"),
    OUTCOME("Outcome"),
    PER_PERSON("Per Person")
}
