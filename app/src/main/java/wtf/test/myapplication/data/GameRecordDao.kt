package wtf.test.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import wtf.test.myapplication.data.models.GameRecord

/**
 * The Data Access Object for the Game class.
 */
@Dao
interface GameRecordDao {
    @Query("SELECT * FROM game_records WHERE gameUuid = :gameUuid ORDER BY score/maxScore DESC LIMIT 1")
    suspend fun getGameHighScore(gameUuid: String): GameRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: GameRecord)
}