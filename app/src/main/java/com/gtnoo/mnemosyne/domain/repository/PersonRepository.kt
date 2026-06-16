package com.gtnoo.mnemosyne.domain.repository

import com.gtnoo.mnemosyne.domain.model.Person
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun observeAll(): Flow<List<Person>>
    fun observeFavorites(): Flow<List<Person>>
    suspend fun getById(id: String): Person?
    suspend fun getAll(): List<Person>
    suspend fun getFavorites(): List<Person>
    suspend fun save(person: Person): Person
    suspend fun update(person: Person)
    suspend fun delete(id: String)
}
