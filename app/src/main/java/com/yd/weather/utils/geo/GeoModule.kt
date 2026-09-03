package com.yd.weather.utils.geo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GeoModule {
    @Binds
    @Singleton
    abstract fun bindGeoResolver(impl: SystemGeocoderResolver): GeoResolver
}
