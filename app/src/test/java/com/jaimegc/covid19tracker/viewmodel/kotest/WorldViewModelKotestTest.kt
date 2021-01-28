package com.jaimegc.covid19tracker.viewmodel.kotest

import androidx.lifecycle.Observer
import arrow.core.Either
import com.jaimegc.covid19tracker.domain.usecase.GetCountryStats
import com.jaimegc.covid19tracker.domain.usecase.GetWorldAndCountries
import com.jaimegc.covid19tracker.domain.usecase.GetWorldStats
import com.jaimegc.covid19tracker.util.getOrAwaitValue
import com.jaimegc.covid19tracker.util.observeForTesting
import com.jaimegc.covid19tracker.ui.base.states.MenuItemViewType
import com.jaimegc.covid19tracker.ui.base.states.ScreenState
import com.jaimegc.covid19tracker.ui.base.states.WorldStateScreen
import com.jaimegc.covid19tracker.ui.world.WorldViewModel
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateCovidTrackerEmptyData
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateCovidTrackerSuccess
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateErrorUnknownDatabase
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateCovidTrackerLoading
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateListCountryAndStatsEmptyData
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateListCountryAndStatsLoading
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateListCountryAndStatsMostConfirmedSuccess
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateListCountryAndStatsMostDeathsSuccess
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateListCountryAndStatsMostOpenCasesSuccess
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateListCountryAndStatsMostRecoveredSuccess
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateListCountryAndStatsSuccess
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateListWorldStatsEmptyData
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateListWorldStatsLoading
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateListWorldStatsSuccess
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.worldStateScreenErrorUnknownDatatabase
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateScreenSuccessCountriesStatsPieChartData
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateScreenSuccessCovidTrackerData
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateScreenSuccessListCountryAndStatsBarChartData
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateScreenSuccessListCountryAndStatsLineChartMostConfirmedData
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateScreenSuccessListCountryAndStatsLineChartMostDeathsData
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateScreenSuccessListCountryAndStatsLineChartMostOpenCasesData
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateScreenSuccessListCountryAndStatsLineChartMostRecoveredData
import com.jaimegc.covid19tracker.ScreenStateFactoryTest.stateScreenSuccessListWorldStatsPieChartData
import com.jaimegc.covid19tracker.util.kotest.ProjectConfig
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class WorldViewModelKotestTest : FunSpec({

    val captor = argumentCaptor<ScreenState<WorldStateScreen>>()

    lateinit var worldViewModel: WorldViewModel
    val getWorldAndCountries: GetWorldAndCountries = mock()
    val getWorldStats: GetWorldStats = mock()
    val getCountryStats: GetCountryStats = mock()

    val stateObserver: Observer<ScreenState<WorldStateScreen>> = mock()

    beforeTest {
        worldViewModel = WorldViewModel(getWorldAndCountries, getWorldStats, getCountryStats)
        worldViewModel.screenState.observeForever(stateObserver)
    }

    test("get list stats should return loading and success if date exists") {
        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            emit(Either.right(stateCovidTrackerSuccess))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getListStats()

        verify(stateObserver, Mockito.times(2)).onChanged(captor.capture())

        val loading = captor.firstValue
        val success = captor.secondValue
        
        ScreenState.Loading shouldBe loading
        (success is ScreenState.Render) shouldBe true
        (success is ScreenState.Render) shouldBe true
        ((success as ScreenState.Render).renderState is WorldStateScreen.SuccessCovidTracker) shouldBe true
        stateScreenSuccessCovidTrackerData shouldBe
            (success.renderState as WorldStateScreen.SuccessCovidTracker).data
    }

    /*****************************************************************
     *  getOrAwaitValue and observeForTesting from Google repository *
     *****************************************************************/

    test("get list stats should return loading and success if date exists using getOrAwaitValue & observeForTesting") {
        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            delay(10)
            emit(Either.right(stateCovidTrackerSuccess))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getListStats()

        worldViewModel.screenState.getOrAwaitValue {
            val loading = worldViewModel.screenState.value
            ProjectConfig.testDispatcher.advanceUntilIdle()
            val success = worldViewModel.screenState.value

            ScreenState.Loading shouldBe loading
            success.shouldNotBeNull()
            (success is ScreenState.Render) shouldBe true
            (success is ScreenState.Render) shouldBe true
            ((success as ScreenState.Render)
                .renderState is WorldStateScreen.SuccessCovidTracker) shouldBe true
            stateScreenSuccessCovidTrackerData shouldBe
                (success.renderState as WorldStateScreen.SuccessCovidTracker).data
        }

        worldViewModel.getListStats()

        worldViewModel.screenState.observeForTesting {
            val loading = worldViewModel.screenState.value
            ProjectConfig.testDispatcher.advanceUntilIdle()
            val success = worldViewModel.screenState.value

            ScreenState.Loading shouldBe loading
            success.shouldNotBeNull()
            (success is ScreenState.Render) shouldBe true
            ((success as ScreenState.Render)
                .renderState is WorldStateScreen.SuccessCovidTracker) shouldBe true
            stateScreenSuccessCovidTrackerData shouldBe
                (success.renderState as WorldStateScreen.SuccessCovidTracker).data
        }
    }

    /***********************************************************************************************/

    test("get list stats with empty data should return loading and empty success") {
        worldViewModel = WorldViewModel(getWorldAndCountries, getWorldStats, getCountryStats)
        worldViewModel.screenState.observeForever(stateObserver)

        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            emit(Either.right(stateCovidTrackerEmptyData))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getListStats()

        verify(stateObserver, Mockito.times(2)).onChanged(captor.capture())

        val loading = captor.firstValue
        val empty = captor.secondValue

        ScreenState.Loading shouldBe loading
        ScreenState.EmptyData shouldBe empty
    }

    test("get list stats with database problem should return loading and unknown database error") {
        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            emit(Either.left(stateErrorUnknownDatabase))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getListStats()

        verify(stateObserver, Mockito.times(2)).onChanged(captor.capture())

        val loading = captor.firstValue
        val error = captor.secondValue

        ScreenState.Loading shouldBe loading
        (error is ScreenState.Error) shouldBe true
        ((error as ScreenState.Error).errorState is WorldStateScreen.SomeError) shouldBe true
        worldStateScreenErrorUnknownDatatabase shouldBe
            (error.errorState as WorldStateScreen.SomeError).data
    }

    test("get pie chart stats should return loading and success if date exists") {
        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            emit(Either.right(stateCovidTrackerSuccess))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getPieChartStats()

        verify(stateObserver, Mockito.times(2)).onChanged(captor.capture())

        val loading = captor.firstValue
        val success = captor.secondValue

        ScreenState.Loading shouldBe loading
        (success is ScreenState.Render) shouldBe true
        ((success as ScreenState.Render)
            .renderState is WorldStateScreen.SuccessCountriesStatsPieCharts) shouldBe true
        stateScreenSuccessCountriesStatsPieChartData shouldBe
            (success.renderState as WorldStateScreen.SuccessCountriesStatsPieCharts).data
    }

    test("get pie chart stats with empty data should return loading and empty success") {
        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            emit(Either.right(stateCovidTrackerEmptyData))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getPieChartStats()

        verify(stateObserver, Mockito.times(2)).onChanged(captor.capture())

        val loading = captor.firstValue
        val empty = captor.secondValue

        ScreenState.Loading shouldBe loading
        ScreenState.EmptyData shouldBe empty
    }

    test("get pie chart stats with database problem should return loading and unknown database error") {
        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            emit(Either.left(stateErrorUnknownDatabase))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getPieChartStats()

        verify(stateObserver, Mockito.times(2)).onChanged(captor.capture())

        val loading = captor.firstValue
        val error = captor.secondValue

        ScreenState.Loading shouldBe loading
        (error is ScreenState.Error) shouldBe true
        ((error as ScreenState.Error).errorState is WorldStateScreen.SomeError) shouldBe true
        worldStateScreenErrorUnknownDatatabase shouldBe
            (error.errorState as WorldStateScreen.SomeError).data
    }

    test("get bar chart stats should return loading and success if date exists") {
        val worldFlow = flow {
            emit(Either.right(stateListWorldStatsLoading))
            emit(Either.right(stateListWorldStatsSuccess))
        }

        val countriesFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsSuccess))
        }

        whenever(getWorldStats.getWorldAllStats()).thenReturn(worldFlow)
        whenever(getCountryStats.getCountriesStatsOrderByConfirmed()).thenReturn(countriesFlow)

        worldViewModel.getBarChartStats()

        verify(stateObserver, Mockito.times(4)).onChanged(captor.capture())

        val worldLoading = captor.firstValue
        val worldSuccess = captor.secondValue
        val countriesLoading = captor.thirdValue
        val countriesSuccess = captor.lastValue

        ScreenState.Loading shouldBe worldLoading
        ScreenState.Loading shouldBe countriesLoading
        (worldSuccess is ScreenState.Render) shouldBe true
        ((worldSuccess as ScreenState.Render)
            .renderState is WorldStateScreen.SuccessWorldStatsBarCharts) shouldBe true
        stateScreenSuccessListWorldStatsPieChartData shouldBe
            (worldSuccess.renderState as WorldStateScreen.SuccessWorldStatsBarCharts).data
        (countriesSuccess is ScreenState.Render) shouldBe true
        ((countriesSuccess as ScreenState.Render)
            .renderState is WorldStateScreen.SuccessCountriesStatsBarCharts) shouldBe true
        stateScreenSuccessListCountryAndStatsBarChartData shouldBe
            (countriesSuccess.renderState as WorldStateScreen.SuccessCountriesStatsBarCharts).data
    }

    test("get bar chart stats with empty data should return loading and empty success") {
        val worldFlow = flow {
            emit(Either.right(stateListWorldStatsLoading))
            emit(Either.right(stateListWorldStatsEmptyData))
        }

        val countriesFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsEmptyData))
        }

        whenever(getWorldStats.getWorldAllStats()).thenReturn(worldFlow)
        whenever(getCountryStats.getCountriesStatsOrderByConfirmed()).thenReturn(countriesFlow)

        worldViewModel.getBarChartStats()

        verify(stateObserver, Mockito.times(4)).onChanged(captor.capture())

        val worldLoading = captor.firstValue
        val worldEmpty = captor.secondValue
        val countriesLoading = captor.thirdValue
        val countriesEmpty = captor.lastValue

        ScreenState.Loading shouldBe worldLoading
        ScreenState.Loading shouldBe countriesLoading
        ScreenState.EmptyData shouldBe worldEmpty
        ScreenState.EmptyData shouldBe countriesEmpty
    }

    test("get bar chart stats with database problem should return loading and unknown database error") {
        val worldFlow = flow {
            emit(Either.right(stateListWorldStatsLoading))
            emit(Either.left(stateErrorUnknownDatabase))
        }

        val countriesFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.left(stateErrorUnknownDatabase))
        }

        whenever(getWorldStats.getWorldAllStats()).thenReturn(worldFlow)
        whenever(getCountryStats.getCountriesStatsOrderByConfirmed()).thenReturn(countriesFlow)

        worldViewModel.getBarChartStats()

        verify(stateObserver, Mockito.times(4)).onChanged(captor.capture())

        val worldLoading = captor.firstValue
        val worldError = captor.secondValue
        val countriesLoading = captor.thirdValue
        val countriesError = captor.lastValue

        ScreenState.Loading shouldBe worldLoading
        ScreenState.Loading shouldBe countriesLoading
        (worldError is ScreenState.Error) shouldBe true
        ((worldError as ScreenState.Error).errorState is WorldStateScreen.SomeError) shouldBe true
        worldStateScreenErrorUnknownDatatabase shouldBe
            (worldError.errorState as WorldStateScreen.SomeError).data
        (countriesError is ScreenState.Error) shouldBe true
        ((countriesError as ScreenState.Error).errorState is WorldStateScreen.SomeError) shouldBe true
        worldStateScreenErrorUnknownDatatabase shouldBe
            (countriesError.errorState as WorldStateScreen.SomeError).data
    }

    test("get line charts stats should return loading and success if date exists") {
        val mostConfirmedFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsMostConfirmedSuccess))
        }

        val mostDeathsFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsMostDeathsSuccess))
        }

        val mostOpenCasesFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsMostOpenCasesSuccess))
        }

        val mostRecoveredFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsMostRecoveredSuccess))
        }

        whenever(getCountryStats.getCountriesAndStatsWithMostConfirmed()).thenReturn(mostConfirmedFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostDeaths()).thenReturn(mostDeathsFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostOpenCases()).thenReturn(mostOpenCasesFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostRecovered()).thenReturn(mostRecoveredFlow)

        worldViewModel.getLineChartsStats()

        verify(stateObserver, Mockito.times(8)).onChanged(captor.capture())

        val mostConfirmedLoading = captor.allValues[0]
        val mostConfirmedSuccess = captor.allValues[1]
        val mostDeathsLoading = captor.allValues[2]
        val mostDeathsSuccess = captor.allValues[3]
        val mostOpenCasesLoading = captor.allValues[4]
        val mostOpenCasesSuccess = captor.allValues[5]
        val mostRecoveredLoading = captor.allValues[6]
        val mostRecoveredSuccess = captor.allValues[7]

        ScreenState.Loading shouldBe mostConfirmedLoading
        ScreenState.Loading shouldBe mostDeathsLoading
        ScreenState.Loading shouldBe mostOpenCasesLoading
        ScreenState.Loading shouldBe mostRecoveredLoading

        (mostConfirmedSuccess is ScreenState.Render) shouldBe true
        ((mostConfirmedSuccess as ScreenState.Render)
            .renderState is WorldStateScreen.SuccessCountriesStatsLineCharts) shouldBe true
        stateScreenSuccessListCountryAndStatsLineChartMostConfirmedData[MenuItemViewType.LineChartMostConfirmed] shouldBe
            (mostConfirmedSuccess.renderState as WorldStateScreen.SuccessCountriesStatsLineCharts)
                .data[MenuItemViewType.LineChartMostConfirmed]
        (mostDeathsSuccess is ScreenState.Render) shouldBe true
        ((mostDeathsSuccess as ScreenState.Render)
            .renderState is WorldStateScreen.SuccessCountriesStatsLineCharts) shouldBe true
        stateScreenSuccessListCountryAndStatsLineChartMostDeathsData[MenuItemViewType.LineChartMostDeaths] shouldBe
            (mostDeathsSuccess.renderState as WorldStateScreen.SuccessCountriesStatsLineCharts)
                .data[MenuItemViewType.LineChartMostDeaths]
        (mostOpenCasesSuccess is ScreenState.Render) shouldBe true
        ((mostOpenCasesSuccess as ScreenState.Render)
            .renderState is WorldStateScreen.SuccessCountriesStatsLineCharts) shouldBe true
        stateScreenSuccessListCountryAndStatsLineChartMostOpenCasesData[MenuItemViewType.LineChartMostOpenCases] shouldBe
            (mostOpenCasesSuccess.renderState as WorldStateScreen.SuccessCountriesStatsLineCharts)
                .data[MenuItemViewType.LineChartMostOpenCases]
        (mostRecoveredSuccess is ScreenState.Render) shouldBe true
        ((mostRecoveredSuccess as ScreenState.Render)
            .renderState is WorldStateScreen.SuccessCountriesStatsLineCharts) shouldBe true
        stateScreenSuccessListCountryAndStatsLineChartMostRecoveredData[MenuItemViewType.LineChartMostRecovered] shouldBe
            (mostRecoveredSuccess.renderState as WorldStateScreen.SuccessCountriesStatsLineCharts)
                .data[MenuItemViewType.LineChartMostRecovered]
    }

    test("get line charts stats with empty data should return loading and empty success") {
        val mostConfirmedFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsEmptyData))
        }

        val mostDeathsFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsEmptyData))
        }

        val mostOpenCasesFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsEmptyData))
        }

        val mostRecoveredFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsEmptyData))
        }

        whenever(getCountryStats.getCountriesAndStatsWithMostConfirmed()).thenReturn(mostConfirmedFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostDeaths()).thenReturn(mostDeathsFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostOpenCases()).thenReturn(mostOpenCasesFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostRecovered()).thenReturn(mostRecoveredFlow)

        worldViewModel.getLineChartsStats()

        verify(stateObserver, Mockito.times(8)).onChanged(captor.capture())

        val mostConfirmedLoading = captor.allValues[0]
        val mostConfirmedEmpty = captor.allValues[1]
        val mostDeathsLoading = captor.allValues[2]
        val mostDeathsEmpty = captor.allValues[3]
        val mostOpenCasesLoading = captor.allValues[4]
        val mostOpenCasesEmpty = captor.allValues[5]
        val mostRecoveredLoading = captor.allValues[6]
        val mostRecoveredEmpty = captor.allValues[7]

        ScreenState.Loading shouldBe mostConfirmedLoading
        ScreenState.Loading shouldBe mostDeathsLoading
        ScreenState.Loading shouldBe mostOpenCasesLoading
        ScreenState.Loading shouldBe mostRecoveredLoading
        ScreenState.EmptyData shouldBe mostConfirmedEmpty
        ScreenState.EmptyData shouldBe mostDeathsEmpty
        ScreenState.EmptyData shouldBe mostOpenCasesEmpty
        ScreenState.EmptyData shouldBe mostRecoveredEmpty
    }

    test("get line charts stats with database problem should return loading and unknown database error") {
        val mostConfirmedFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.left(stateErrorUnknownDatabase))
        }

        val mostDeathsFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.left(stateErrorUnknownDatabase))
        }

        val mostOpenCasesFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.left(stateErrorUnknownDatabase))
        }

        val mostRecoveredFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.left(stateErrorUnknownDatabase))
        }

        whenever(getCountryStats.getCountriesAndStatsWithMostConfirmed()).thenReturn(mostConfirmedFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostDeaths()).thenReturn(mostDeathsFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostOpenCases()).thenReturn(mostOpenCasesFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostRecovered()).thenReturn(mostRecoveredFlow)

        worldViewModel.getLineChartsStats()

        verify(stateObserver, Mockito.times(8)).onChanged(captor.capture())

        val mostConfirmedLoading = captor.allValues[0]
        val mostConfirmedError = captor.allValues[1]
        val mostDeathsLoading = captor.allValues[2]
        val mostDeathsError = captor.allValues[3]
        val mostOpenCasesLoading = captor.allValues[4]
        val mostOpenCasesError = captor.allValues[5]
        val mostRecoveredLoading = captor.allValues[6]
        val mostRecoveredError = captor.allValues[7]

        ScreenState.Loading shouldBe mostConfirmedLoading
        ScreenState.Loading shouldBe mostDeathsLoading
        ScreenState.Loading shouldBe mostOpenCasesLoading
        ScreenState.Loading shouldBe mostRecoveredLoading
        (mostConfirmedError is ScreenState.Error) shouldBe true
        ((mostConfirmedError as ScreenState.Error).errorState is WorldStateScreen.SomeError) shouldBe true
        worldStateScreenErrorUnknownDatatabase shouldBe
            (mostConfirmedError.errorState as WorldStateScreen.SomeError).data
        (mostDeathsError is ScreenState.Error) shouldBe true
        ((mostDeathsError as ScreenState.Error).errorState is WorldStateScreen.SomeError) shouldBe true
        worldStateScreenErrorUnknownDatatabase shouldBe
            (mostDeathsError.errorState as WorldStateScreen.SomeError).data
        (mostOpenCasesError is ScreenState.Error) shouldBe true
        ((mostOpenCasesError as ScreenState.Error).errorState is WorldStateScreen.SomeError) shouldBe true
        worldStateScreenErrorUnknownDatatabase shouldBe
            (mostOpenCasesError.errorState as WorldStateScreen.SomeError).data
        (mostRecoveredError is ScreenState.Error) shouldBe true
        ((mostRecoveredError as ScreenState.Error).errorState is WorldStateScreen.SomeError) shouldBe true
        worldStateScreenErrorUnknownDatatabase shouldBe
            (mostRecoveredError.errorState as WorldStateScreen.SomeError).data
    }
})