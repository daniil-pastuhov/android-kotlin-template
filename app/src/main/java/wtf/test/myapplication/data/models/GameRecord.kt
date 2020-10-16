package wtf.test.myapplication.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val gameUuid: String,
    val score: Int,
    val maxScore: Int,
    val percent: Int = score * 100 / maxScore
)