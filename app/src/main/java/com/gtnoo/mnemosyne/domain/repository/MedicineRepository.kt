package com.gtnoo.mnemosyne.domain.repository

import com.gtnoo.mnemosyne.domain.model.Medicine
import com.gtnoo.mnemosyne.domain.model.MedicineBottle
import com.gtnoo.mnemosyne.domain.model.MedicineDose
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MedicineRepository {
    fun observeAll(): Flow<List<Medicine>>
    fun observeById(id: String): Flow<Medicine?>
    suspend fun getById(id: String): Medicine?
    suspend fun getAll(): List<Medicine>
    suspend fun save(medicine: Medicine)
    suspend fun update(medicine: Medicine)
    suspend fun delete(id: String)

    fun observeBottlesForMedicine(medicineId: String): Flow<List<MedicineBottle>>
    fun observeCurrentBottles(): Flow<List<MedicineBottle>>
    suspend fun getCurrentBottle(medicineId: String): MedicineBottle?
    suspend fun saveBottle(bottle: MedicineBottle)
    suspend fun updateBottle(bottle: MedicineBottle)
    suspend fun retireCurrentBottle(medicineId: String)

    fun observeDosesForDate(date: LocalDate): Flow<List<MedicineDose>>
    fun observeDosesForMedicineOnDate(medicineId: String, date: LocalDate): Flow<List<MedicineDose>>
    suspend fun getDosesForDate(date: LocalDate): List<MedicineDose>
    suspend fun getDoseForMedicineOnDate(medicineId: String, date: LocalDate): MedicineDose?
    suspend fun getAllDosesForMedicine(medicineId: String): List<MedicineDose>
    suspend fun getAllDoses(): List<MedicineDose>
    suspend fun getAllBottles(): List<MedicineBottle>
    suspend fun saveDose(dose: MedicineDose)
    suspend fun deleteDose(id: String)
}
