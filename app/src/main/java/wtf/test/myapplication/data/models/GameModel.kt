package wtf.test.myapplication.data.models

data class GameModel(
    val uuid: String,
    val audience: String,
    val compatibilityLevel: Int,
    val cover: String,
    val coverMetadata: CoverMetadata,
    val created: Long,
    val creator: String,
    val creator_primary_usage: String,
    val creator_username: String,
    val description: String,
    val folderId: String,
    val language: String,
    val metadata: Metadata,
    val modified: Long,
    val questions: List<Question>,
    val quizType: String,
    val resources: String,
    val slug: String,
    val themeId: String,
    val title: String,
    val type: String,
    val visibility: Int
)

data class Question(
    val choices: List<Choice>,
    val image: String,
    val imageMetadata: ImageMetadata,
    val points: Boolean,
    val pointsMultiplier: Int,
    val question: String,
    val questionFormat: Int,
    val resources: String,
    val time: Int?,
    val type: String,
    val video: Video
)

data class CoverMetadata(
    val id: String,
    val resources: String
)

data class Metadata(
    val access: Access,
    val duplicationProtection: Boolean,
    val lastEdit: LastEdit,
    val moderation: Moderation,
    val resolution: String
)

data class Access(
    val features: List<String>
)

data class LastEdit(
    val editTimestamp: Long,
    val editorUserId: String,
    val editorUsername: String
)

data class Moderation(
    val flaggedTimestamp: Int,
    val resolution: String,
    val timestampResolution: Long
)

data class Choice(
    val answer: String,
    val correct: Boolean
)

data class ImageMetadata(
    val effects: List<Any>,
    val id: String,
    val resources: String
)

data class Video(
    val endTime: Double,
    val fullUrl: String,
    val id: String,
    val service: String,
    val startTime: Double
)