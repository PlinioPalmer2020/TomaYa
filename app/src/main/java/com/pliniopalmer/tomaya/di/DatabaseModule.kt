package com.pliniopalmer.tomaya.di

import android.content.Context
import androidx.room.Room
import com.pliniopalmer.tomaya.data.TomaYaDao
import com.pliniopalmer.tomaya.data.TomaYaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun proveerDatabase(@ApplicationContext context: Context): TomaYaDatabase =
        Room.databaseBuilder(context, TomaYaDatabase::class.java, "tomaya.db").build()

    @Provides
    fun proveerDao(db: TomaYaDatabase): TomaYaDao = db.dao()
}
