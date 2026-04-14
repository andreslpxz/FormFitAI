package com.formfit.ai.core.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `body_weights` (
                `id` INTEGER NOT NULL,
                `weightKg` REAL NOT NULL,
                `date` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `routines` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL DEFAULT '',
                `isPreset` INTEGER NOT NULL DEFAULT 0,
                `userId` TEXT NOT NULL DEFAULT '',
                `exercises` TEXT NOT NULL DEFAULT '[]',
                `estimatedMinutes` INTEGER NOT NULL DEFAULT 30,
                `difficulty` TEXT NOT NULL DEFAULT 'BEGINNER',
                `thumbnailUrl` TEXT,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}
