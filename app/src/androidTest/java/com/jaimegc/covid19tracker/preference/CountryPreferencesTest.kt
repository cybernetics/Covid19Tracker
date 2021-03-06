package com.jaimegc.covid19tracker.preference

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.jaimegc.covid19tracker.data.preference.CountryPreferences
import com.jaimegc.covid19tracker.util.SharedPreferencesTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class CountryPreferencesTest : SharedPreferencesTest() {

    companion object {
        private const val COUNTRY = "germany"
    }

    private lateinit var countryPreferences: CountryPreferences

    @Before
    fun setUp() {
        countryPreferences = CountryPreferences(context)
    }

    @Test
    fun getId_shouldReturnDefaultId() {
        assertThat(countryPreferences.getId()).isEqualTo(COUNTRY_DEFAULT)
    }

    @Test
    fun saveId_shouldReturnId() {
        assertThat(countryPreferences.getId()).isEqualTo(COUNTRY_DEFAULT)
        countryPreferences.save(COUNTRY)
        assertThat(countryPreferences.getId()).isEqualTo(COUNTRY)
    }
}
