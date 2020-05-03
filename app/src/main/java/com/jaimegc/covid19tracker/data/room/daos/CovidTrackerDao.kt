package com.jaimegc.covid19tracker.data.room.daos

import androidx.room.*
import com.jaimegc.covid19tracker.data.room.entities.*
import com.jaimegc.covid19tracker.data.room.pojos.CountryAndOneStatsPojo
import com.jaimegc.covid19tracker.data.room.pojos.CountryAndStatsPojo
import com.jaimegc.covid19tracker.data.room.pojos.WorldAndCountriesStatsPojo
import kotlinx.coroutines.flow.Flow


@Dao
abstract class CovidTrackerDao {

    @Transaction
    @Query("SELECT * FROM world_stats WHERE date =:date")
    abstract fun getWorldAndCountriesStatsByDate(date: String): Flow<WorldAndCountriesStatsPojo>

    @Query("SELECT * FROM world_stats")
    abstract suspend fun getWorld(): WorldStatsEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWorldStats(worldStats: WorldStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllWorldsStats(worldsStats: List<WorldStatsEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertAllCountries(countries: List<CountryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllCountriesStats(countriesStats: List<CountryStatsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllRegionsStats(regionsStats: List<RegionStatsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAllSubRegionsStats(subRegionsStats: List<SubRegionStatsEntity>)

    @Transaction
    open suspend fun populateDatabase(
        worldsStats: List<WorldStatsEntity>,
        countries: List<CountryEntity>,
        countriesStats: List<CountryStatsEntity>,
        regionsStats: List<RegionStatsEntity>,
        subRegionsStats: List<SubRegionStatsEntity>
    ) {
        insertAllWorldsStats(worldsStats)
        insertAllCountries(countries)
        insertAllCountriesStats(countriesStats)
        insertAllRegionsStats(regionsStats)
        insertAllSubRegionsStats(subRegionsStats)
    }
}

@Dao
abstract class WorldStatsDao {

    @Query("SELECT * FROM world_stats WHERE date =:date")
    abstract fun getByDate(date: String): Flow<List<WorldStatsEntity>>

    @Query("SELECT * FROM world_stats ORDER BY date ASC")
    abstract fun getAll(): Flow<List<WorldStatsEntity>>
}

@Dao
abstract class CountryStatsDao {

    @Query("SELECT * FROM country WHERE name =:name")
    abstract fun getByName(name: String): Flow<List<CountryEntity>>

    @Query("""
        SELECT * FROM country, country_stats 
        WHERE country.id = country_stats.id_country_fk
        GROUP BY country.name
        ORDER BY country_stats.confirmed DESC""")
    abstract fun getCountriesAndStatsOrderByConfirmed(): Flow<List<CountryAndStatsPojo>>

    @Query("""
        SELECT * FROM country c, country_stats s
        WHERE c.id = s.id_country_fk AND s.confirmed > 2000 AND c.id IN (
            SELECT id FROM country 
                INNER JOIN (
                    SELECT id_country_fk, MAX(confirmed) AS maxConfirmed FROM country_stats 
                    GROUP BY id_country_fk
                ) statsMaxConfirmed
                ON country.id = statsMaxConfirmed.id_country_fk 
                ORDER BY statsMaxConfirmed.maxConfirmed DESC LIMIT 5
            )
        ORDER BY c.id ASC, s.confirmed ASC
        """)
    abstract fun getCountriesAndStatsWithMostConfirmed(): Flow<List<CountryAndOneStatsPojo>>

    @Query("""
        SELECT * FROM country c
        INNER JOIN country_stats s ON c.id = s.id_country_fk AND c.id IN (
            SELECT id FROM country 
                INNER JOIN (
                    SELECT id_country_fk, MAX(deaths) AS maxDeaths FROM country_stats 
                    GROUP BY id_country_fk
                ) statsMaxDeaths
                ON country.id = statsMaxDeaths.id_country_fk 
                ORDER BY statsMaxDeaths.maxDeaths DESC LIMIT 5
            )
        WHERE deaths > 100
        ORDER BY c.id ASC, s.deaths ASC
        """)
    abstract fun getCountriesAndStatsWithMostDeaths(): Flow<List<CountryAndOneStatsPojo>>

    @Query("""
        SELECT * FROM country c, country_stats s
        WHERE c.id = s.id_country_fk AND s.recovered > 2000 AND c.id IN (
            SELECT id FROM country 
                INNER JOIN (
                    SELECT id_country_fk, MAX(recovered) AS maxRecovered FROM country_stats 
                    GROUP BY id_country_fk
                ) statsMaxRecovered
                ON country.id = statsMaxRecovered.id_country_fk 
                ORDER BY statsMaxRecovered.maxRecovered DESC LIMIT 5
            )
        ORDER BY c.id ASC, s.recovered ASC
        """)
    abstract fun getCountriesAndStatsWithMostRecovered(): Flow<List<CountryAndOneStatsPojo>>

    @Query("""
        SELECT * FROM country c
        INNER JOIN country_stats s ON c.id = s.id_country_fk AND c.id IN (
            SELECT id FROM country 
                INNER JOIN (
                    SELECT id_country_fk, MAX(open_cases) AS maxOpenCases FROM country_stats 
                    GROUP BY id_country_fk
                ) statsMaxOpenCases
                ON country.id = statsMaxOpenCases.id_country_fk 
                ORDER BY statsMaxOpenCases.maxOpenCases DESC LIMIT 5
            )
        WHERE open_cases > 2000
        ORDER BY c.id ASC, s.open_cases ASC
        """)
    abstract fun getCountriesAndStatsWithMostOpenCases(): Flow<List<CountryAndOneStatsPojo>>
}