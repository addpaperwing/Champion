package com.zzy.champions.data.repository

import com.zzy.champions.data.model.Item

interface ItemRepository {
    suspend fun getRemoteItems(version: String, language: String): List<Item>
    suspend fun saveLocalItems(items: List<Item>)
    suspend fun getLocalItems(): List<Item>
    suspend fun getItemCount(): Int
    suspend fun getItemById(id: String): Item?
}
