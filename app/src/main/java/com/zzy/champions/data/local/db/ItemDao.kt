package com.zzy.champions.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zzy.champions.data.model.Item

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>)

    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<Item>

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getItemCount(): Int

    @Query("DELETE FROM items")
    suspend fun clearItems()

    @Transaction
    suspend fun clearAndInsertItems(items: List<Item>) {
        clearItems()
        insertItems(items)
    }

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): Item?
}
