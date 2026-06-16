package com.gtnoo.mnemosyne.data.repository

import com.gtnoo.mnemosyne.data.local.dao.PersonDao
import com.gtnoo.mnemosyne.data.local.entity.PersonEntity
import com.gtnoo.mnemosyne.domain.model.Person
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(
    private val dao: PersonDao
) : PersonRepository {

    override fun observeAll(): Flow<List<Person>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeFavorites(): Flow<List<Person>> =
        dao.observeFavorites().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Person? = dao.getById(id)?.toDomain()

    override suspend fun getAll(): List<Person> = dao.getAll().map { it.toDomain() }

    override suspend fun getFavorites(): List<Person> = dao.getFavorites().map { it.toDomain() }

    override suspend fun save(person: Person): Person {
        dao.insert(PersonEntity.fromDomain(person))
        return person
    }

    override suspend fun update(person: Person) {
        dao.update(PersonEntity.fromDomain(person))
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }
}
