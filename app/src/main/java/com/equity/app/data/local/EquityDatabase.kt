package com.equity.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.equity.app.data.local.dao.SavingsGoalDao
import com.equity.app.data.local.dao.TransactionDao
import com.equity.app.data.local.entity.SavingsGoalEntity
import com.equity.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [TransactionEntity::class, SavingsGoalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EquityDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        const val DATABASE_NAME = "equity_database.db"

        fun buildDatabase(context: Context, applicationScope: CoroutineScope): EquityDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                EquityDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(PrepopulateCallback(applicationScope))
                .build()
        }
    }

    private class PrepopulateCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-seed initial sample data so the app looks stunning on first launch
            scope.launch(Dispatchers.IO) {
                // Prepopulate is executed when the database is created
            }
        }
    }
}
