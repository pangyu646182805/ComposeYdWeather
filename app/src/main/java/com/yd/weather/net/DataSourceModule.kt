package com.yd.weather.net

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideWeatherNetworkDataSource(weatherService: WeatherService): WeatherNetworkDataSource {
        return WeatherNetworkDataSourceImpl(weatherService)
    }
}