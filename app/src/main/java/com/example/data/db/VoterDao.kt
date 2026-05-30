package com.example.data.db

import androidx.room.*
import com.example.data.model.Voter
import kotlinx.coroutines.flow.Flow

@Dao
interface VoterDao {

    @Query("""
        SELECT * FROM voters 
        WHERE (:query IS NULL OR :query = '' OR name LIKE :queryLike OR serialNumber LIKE :queryLike OR nid LIKE :queryLike)
        AND (:division IS NULL OR :division = '' OR division = :division)
        AND (:district IS NULL OR :district = '' OR district = :district)
        AND (:upazila IS NULL OR :upazila = '' OR upazila = :upazila)
        AND (:union IS NULL OR :union = '' OR `union` = :union)
        AND (:village IS NULL OR :village = '' OR village = :village)
        AND (:constituency IS NULL OR :constituency = '' OR constituency = :constituency)
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchVoters(
        query: String?,
        queryLike: String,
        division: String?,
        district: String?,
        upazila: String?,
        union: String?,
        village: String?,
        constituency: String?,
        limit: Int,
        offset: Int
    ): List<Voter>

    @Query("""
        SELECT COUNT(*) FROM voters 
        WHERE (:query IS NULL OR :query = '' OR name LIKE :queryLike OR serialNumber LIKE :queryLike OR nid LIKE :queryLike)
        AND (:division IS NULL OR :division = '' OR division = :division)
        AND (:district IS NULL OR :district = '' OR district = :district)
        AND (:upazila IS NULL OR :upazila = '' OR upazila = :upazila)
        AND (:union IS NULL OR :union = '' OR `union` = :union)
        AND (:village IS NULL OR :village = '' OR village = :village)
        AND (:constituency IS NULL OR :constituency = '' OR constituency = :constituency)
    """)
    suspend fun getVotersCount(
        query: String?,
        queryLike: String,
        division: String?,
        district: String?,
        upazila: String?,
        union: String?,
        village: String?,
        constituency: String?
    ): Int

    @Query("SELECT DISTINCT division FROM voters WHERE division IS NOT NULL AND division != '' ORDER BY division ASC")
    fun getUniqueDivisions(): Flow<List<String>>

    @Query("SELECT DISTINCT district FROM voters WHERE district IS NOT NULL AND district != '' ORDER BY district ASC")
    fun getUniqueDistricts(): Flow<List<String>>

    @Query("SELECT DISTINCT upazila FROM voters WHERE upazila IS NOT NULL AND upazila != '' ORDER BY upazila ASC")
    fun getUniqueUpazilas(): Flow<List<String>>

    @Query("SELECT DISTINCT `union` FROM voters WHERE `union` IS NOT NULL AND `union` != '' ORDER BY `union` ASC")
    fun getUniqueUnions(): Flow<List<String>>

    @Query("SELECT DISTINCT village FROM voters WHERE village IS NOT NULL AND village != '' ORDER BY village ASC")
    fun getUniqueVillages(): Flow<List<String>>

    @Query("SELECT DISTINCT constituency FROM voters WHERE constituency IS NOT NULL AND constituency != '' ORDER BY constituency ASC")
    fun getUniqueConstituencies(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM voters")
    fun getTotalVotersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM voters WHERE gender = 'Male'")
    fun getMaleVotersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM voters WHERE gender = 'Female'")
    fun getFemaleVotersCount(): Flow<Int>

    @Query("SELECT * FROM voters WHERE isSynced = 0")
    suspend fun getUnsyncedVoters(): List<Voter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoters(voters: List<Voter>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoter(voter: Voter)

    @Update
    suspend fun updateVoter(voter: Voter)

    @Query("DELETE FROM voters")
    suspend fun clearAllVoters()
}
