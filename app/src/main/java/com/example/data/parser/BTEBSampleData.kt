package com.example.data.parser

import com.example.data.model.DataQualityStatus
import com.example.data.model.Routine
import com.example.data.model.RoutineEntry
import java.util.UUID

object BTEBSampleData {

    fun createSampleRoutine(): Pair<Routine, List<RoutineEntry>> {
        val routineId = "bteb-diploma-aug-2026"

        val routine = Routine(
            id = routineId,
            title = "BTEB Diploma in Engineering Exam Routine August 2026",
            organization = "Bangladesh Technical Education Board, Dhaka",
            noticeNumber = "BTEB/EXAM/DIPLOMA/2026/1084",
            publicationDate = "01-08-2026",
            examSession = "Diploma in Engineering 1st, 3rd, 5th, 7th & 8th Semester Exam 2026",
            regulation = "2022 & 2016 Regulation",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val entries = listOf(
            RoutineEntry(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                date = "2026-08-08",
                day = "Saturday",
                time = "10:00 AM",
                session = "Morning",
                semester = listOf("4th", "6th"),
                subjectCode = "26811",
                subjectName = "Electrical Engineering Fundamentals",
                technology = listOf("Electrical", "Electronics", "Power"),
                regulation = "2022 Regulation",
                examSession = "2026",
                rawText = "08-08-2026 | Saturday | 10:00 AM | 26811 - Electrical Engineering Fundamentals | Electrical, Electronics | 4th, 6th",
                status = DataQualityStatus.VERIFIED,
                prepProgress = 40
            ),
            RoutineEntry(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                date = "2026-08-08",
                day = "Saturday",
                time = "02:00 PM",
                session = "Afternoon",
                semester = listOf("4th"),
                subjectCode = "28511",
                subjectName = "Programming Essentials with Python",
                technology = listOf("Computer", "Data Telecommunication"),
                regulation = "2022 Regulation",
                examSession = "2026",
                rawText = "08-08-2026 | Saturday | 02:00 PM | 28511 - Programming Essentials with Python | Computer | 4th",
                status = DataQualityStatus.VERIFIED,
                prepProgress = 75
            ),
            RoutineEntry(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                date = "2026-08-10",
                day = "Monday",
                time = "10:00 AM",
                session = "Morning",
                semester = listOf("5th"),
                subjectCode = "26831",
                subjectName = "Digital Electronics & Logic Design",
                technology = listOf("Computer", "Electronics", "Electrical"),
                regulation = "2022 Regulation",
                examSession = "2026",
                rawText = "10-08-2026 | Monday | 10:00 AM | 26831 - Digital Electronics & Logic Design | Computer, Electronics | 5th",
                status = DataQualityStatus.VERIFIED,
                prepProgress = 20
            ),
            RoutineEntry(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                date = "2026-08-10",
                day = "Monday",
                time = "02:00 PM",
                session = "Afternoon",
                semester = listOf("6th"),
                subjectCode = "26431",
                subjectName = "Civil Engineering Drawing & Surveying",
                technology = listOf("Civil", "Architecture"),
                regulation = "2022 Regulation",
                examSession = "2026",
                rawText = "10-08-2026 | Monday | 02:00 PM | 26431 - Civil Engineering Drawing | Civil | 6th",
                status = DataQualityStatus.VERIFIED,
                prepProgress = 0
            ),
            RoutineEntry(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                date = "2026-08-12",
                day = "Wednesday",
                time = "10:00 AM",
                session = "Morning",
                semester = listOf("6th"),
                subjectCode = "28561",
                subjectName = "Database Management Systems",
                technology = listOf("Computer"),
                regulation = "2022 Regulation",
                examSession = "2026",
                rawText = "12-08-2026 | Wednesday | 10:00 AM | 28561 - Database Management Systems | Computer | 6th",
                status = DataQualityStatus.VERIFIED,
                prepProgress = 60
            ),
            RoutineEntry(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                date = "2026-08-12",
                day = "Wednesday",
                time = "02:00 PM",
                session = "Afternoon",
                semester = listOf("7th"),
                subjectCode = "26871",
                subjectName = "Power System Protection & Switchgear",
                technology = listOf("Electrical"),
                regulation = "2016 Regulation",
                examSession = "2026",
                rawText = "12-08-2026 | Wednesday | 02:00 PM | 26871 - Power System Protection | Electrical | 7th",
                status = DataQualityStatus.VERIFIED,
                prepProgress = 10
            ),
            RoutineEntry(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                date = "2026-08-15",
                day = "Saturday",
                time = "10:00 AM",
                session = "Morning",
                semester = listOf("7th"),
                subjectCode = "28571",
                subjectName = "Web Development & Mobile App Frameworks",
                technology = listOf("Computer"),
                regulation = "2022 Regulation",
                examSession = "2026",
                rawText = "15-08-2026 | Saturday | 10:00 AM | 28571 - Web Development | Computer | 7th",
                status = DataQualityStatus.VERIFIED,
                prepProgress = 85
            ),
            RoutineEntry(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                date = "2026-08-18",
                day = "Tuesday",
                time = "10:00 AM",
                session = "Morning",
                semester = listOf("8th"),
                subjectCode = "25881",
                subjectName = "Industrial Management & Entrepreneurship",
                technology = listOf("Computer", "Electrical", "Civil", "Mechanical", "Electronics"),
                regulation = "2022 Regulation",
                examSession = "2026",
                rawText = "18-08-2026 | Tuesday | 10:00 AM | 25881 - Industrial Management | All Technologies | 8th",
                status = DataQualityStatus.VERIFIED,
                prepProgress = 50
            )
        )

        return Pair(routine, entries)
    }
}
