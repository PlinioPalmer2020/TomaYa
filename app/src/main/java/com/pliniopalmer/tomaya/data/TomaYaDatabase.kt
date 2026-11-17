package com.pliniopalmer.tomaya.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Tratamiento::class, Toma::class],
    version = 1,
    exportSchema = false,
)
abstract class TomaYaDatabase : RoomDatabase() {
    abstract fun dao(): TomaYaDao
}
