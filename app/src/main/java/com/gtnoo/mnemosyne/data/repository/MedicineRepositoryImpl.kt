package com.gtnoo.mnemosyne.data.repository

import com.gtnoo.mnemosyne.data.local.dao.MedicineDao
import com.gtnoo.mnemosyne.data.local.entity.MedicineBottleEntity
import com.gtnoo.mnemosyne.data.local.entity.MedicineDoseEntity
import com.gtnoo.mnemosyne.data.local.entity.MedicineEntity
import com.gtnoo.mnemosyne.domain.model.Medicine
import com.gtnoo.mnemosyne.domain.model.MedicineBottle
import com.gtnoo.mnemosyne.domain.model.MedicineDose
import com.gtnoo.mnemosyne.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MedicineRepositoryImpl @Inject constructor(
    private val medicineDao: MedicineDao
) : MedicineRepository {

    override fun observeAll(): Flow<List<Medicine>> =
        medicineDao.observeAll().map { it.map(MedicineEntity::toDomain) }

    override fun observeById(id: String): Flow<Medicine?> =
        medicineDao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): Medicine? =
        medicineDao.getById(id)?.toDomain()

    override suspend fun getAll(): List<Medicine> =
        medicineDao.getAll().map(MedicineEntity::toDomain)

    override suspend fun save(medicine: Medicine) =
        medicineDao.insertMedicine(MedicineEntity.fromDomain(medicine))

    override suspend fun update(medicine: Medicine) =
        medicineDao.updateMedicine(MedicineEntity.fromDomain(medicine))

    override suspend fun delete(id: String) =
        medicineDao.deleteMedicineById(id)

    override fun observeBottlesForMedicine(medicineId: String): Flow<List<MedicineBottle>> =
        medicineDao.observeBottlesForMedicine(medicineId).map { it.map(MedicineBottleEntity::toDomain) }

    override fun observeCurrentBottles(): Flow<List<MedicineBottle>> =
        medicineDao.observeCurrentBottles().map { it.map(MedicineBottleEntity::toDomain) }

    override suspend fun getCurrentBottle(medicineId: String): MedicineBottle? =
        medicineDao.getCurrentBottle(medicineId)?.toDomain()

    override suspend fun saveBottle(bottle: MedicineBottle) =
        medicineDao.insertBottle(MedicineBottleEntity.fromDomain(bottle))

    override suspend fun updateBottle(bottle: MedicineBottle) =
        medicineDao.updateBottle(MedicineBottleEntity.fromDomain(bottle))

    override suspend fun retireCurrentBottle(medicineId: String) =
        medicineDao.retireCurrentBottle(medicineId)

    override fun observeDosesForDate(date: LocalDate): Flow<List<MedicineDose>> =
        medicineDao.observeDosesForDate(date).map { it.map(MedicineDoseEntity::toDomain) }

    override fun observeDosesForMedicineOnDate(medicineId: String, date: LocalDate): Flow<List<MedicineDose>> =
        medicineDao.observeDosesForMedicineOnDate(medicineId, date).map { it.map(MedicineDoseEntity::toDomain) }

    override suspend fun getDosesForDate(date: LocalDate): List<MedicineDose> =
        medicineDao.getDosesForDate(date).map(MedicineDoseEntity::toDomain)

    override suspend fun getDoseForMedicineOnDate(medicineId: String, date: LocalDate): MedicineDose? =
        medicineDao.getDoseForMedicineOnDate(medicineId, date)?.toDomain()

    override suspend fun getAllDosesForMedicine(medicineId: String): List<MedicineDose> =
        medicineDao.getAllDosesForMedicine(medicineId).map(MedicineDoseEntity::toDomain)

    override suspend fun getAllDoses(): List<MedicineDose> =
        medicineDao.getAllDoses().map(MedicineDoseEntity::toDomain)

    override suspend fun getAllBottles(): List<MedicineBottle> =
        medicineDao.getAllBottles().map(MedicineBottleEntity::toDomain)

    override suspend fun saveDose(dose: MedicineDose) =
        medicineDao.insertDose(MedicineDoseEntity.fromDomain(dose))

    override suspend fun deleteDose(id: String) =
        medicineDao.deleteDoseById(id)
}
