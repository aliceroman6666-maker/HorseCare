package com.horsecare.app.data.entity

enum class HorseSex(val displayName: String) {
    MARE("Кобила"),
    GELDING("Мерин"),
    STALLION("Жеребець")
}

enum class ContactRole(val displayName: String) {
    VET("Ветеринар"),
    FARRIER("Коваль")
}

enum class HealthRecordType(val displayName: String) {
    VACCINATION("Вакцинація"),
    DEWORMING("Дегельмінтизація"),
    VET_PROCEDURE("Ветпроцедура"),
    HOOF_CARE("Розчистка/Кування")
}

enum class HoofCareType(val displayName: String) {
    TRIMMING("Розчистка"),
    SHOEING("Кування")
}

enum class ReminderType {
    THREE_DAYS_BEFORE,
    ON_DUE_DATE
}

enum class HorseConditionAfterTraining(val emoji: String, val displayName: String) {
    GOOD("🙂", "Добре"),
    TIRED("😐", "Втома"),
    LAMENESS("😟", "Кульгавість")
}