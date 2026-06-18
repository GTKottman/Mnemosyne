package com.gtnoo.mnemosyne.data.local.dao

import androidx.room.*
import com.gtnoo.mnemosyne.data.local.entity.MedicineBottleEntity
import com.gtnoo.mnemosyne.data.local.entity.MedicineDoseEntity
import com.gtnoo.mnemosyne.data.local.entity.MedicineEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MedicineDao {

    // --- Medicines ---

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun observeAll(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    suspend fun getAll(): List<MedicineEntity>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: String): MedicineEntity?

    @Query("SELECT * FROM medicines WHERE id = :id")
    fun observeById(id: String): Flow<MedicineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(entity: MedicineEntity)

    @Update
    suspend fun updateMedicine(entity: MedicineEntity)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicineById(id: String)

    // --- Bottles ---

    @Query("SELECT * FROM medicine_bottles WHERE medicineId = :medicineId ORDER BY openedDate DESC")
    fun observeBottlesForMedicine(medicineId: String): Flow<List<MedicineBottleEntity>>

    @Query("SELECT * FROM medicine_bottles WHERE medicineId = :medicineId AND isCurrentBottle = 1 LIMIT 1")
    suspend fun getCurrentBottle(medicineId: String): MedicineBottleEntity?

    @Query("SELECT * FROM medicine_bottles WHERE isCurrentBottle = 1")
    fun observeCurrentBottles(): Flow<List<MedicineBottleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBottle(entity: MedicineBottleEntity)

    @Update
    suspend fun updateBottle(entity: MedicineBottleEntity)

    @Query("UPDATE medicine_bottles SET isCurrentBottle = 0 WHERE medicineId = :medicineId AND isCurrentBottle = 1")
    suspend fun retireCurrentBottle(medicineId: String)

    // --- Doses ---

    @Query("SELECT * FROM medicine_doses WHERE date = :date ORDER BY medicineId ASC")
    fun observeDosesForDate(date: LocalDate): Flow<List<MedicineDoseEntity>>

    @Query("SELECT * FROM medicine_doses WHERE date = :date ORDER BY medicineId ASC")
    suspend fun getDosesForDate(date: LocalDate): List<MedicineDoseEntity>

    @Query("SELECT * FROM medicine_doses WHERE medicineId = :medicineId AND date = :date LIMIT 1")
    suspend fun getDoseForMedicineOnDate(medicineId: String, date: LocalDate): MedicineDoseEntity?

    @Query("SELECT * FROM medicine_doses WHERE medicineId = :medicineId AND date = :date ORDER BY taken_at ASC")
    fun observeDosesForMedicineOnDate(medicineId: String, date: LocalDate): Flow<List<MedicineDoseEntity>>

    @Query("SELECT * FROM medicine_doses WHERE medicineId = :medicineId ORDER BY date DESC, taken_at DESC")
    suspend fun getAllDosesForMedicine(medicineId: String): List<MedicineDoseEntity>

    @Query("SELECT * FROM medicine_doses ORDER BY date DESC")
    suspend fun getAllDoses(): List<MedicineDoseEntity>

    @Query("SELECT * FROM medicine_bottles ORDER BY openedDate DESC")
    suspend fun getAllBottles(): List<MedicineBottleEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDose(entity: MedicineDoseEntity)

    @Query("DELETE FROM medicine_doses WHERE id = :id")
    suspend fun deleteDoseById(id: String)
}
