package com.example.findbuddy.di

import android.content.Context
import androidx.room.Room
import com.example.findbuddy.data.local.FinBuddyDatabase
import com.example.findbuddy.data.local.dao.AccountDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FinBuddyDatabase {
        return Room.databaseBuilder(
            context,
            FinBuddyDatabase::class.java,
            "finbuddy_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAccountDao(database: FinBuddyDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: FinBuddyDatabase): com.example.findbuddy.data.local.dao.TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: FinBuddyDatabase): com.example.findbuddy.data.local.dao.CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(database: FinBuddyDatabase): com.example.findbuddy.data.local.dao.BudgetDao {
        return database.budgetDao()
    }
}

