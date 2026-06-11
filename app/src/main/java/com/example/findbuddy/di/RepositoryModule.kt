package com.example.findbuddy.di

import com.example.findbuddy.data.repository.AuthRepositoryImpl
import com.example.findbuddy.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        accountRepositoryImpl: com.example.findbuddy.data.repository.AccountRepositoryImpl
    ): com.example.findbuddy.domain.repository.AccountRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: com.example.findbuddy.data.repository.TransactionRepositoryImpl
    ): com.example.findbuddy.domain.repository.TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: com.example.findbuddy.data.repository.CategoryRepositoryImpl
    ): com.example.findbuddy.domain.repository.CategoryRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        budgetRepositoryImpl: com.example.findbuddy.data.repository.BudgetRepositoryImpl
    ): com.example.findbuddy.domain.repository.BudgetRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: com.example.findbuddy.data.repository.DashboardRepositoryImpl
    ): com.example.findbuddy.domain.repository.DashboardRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        reportRepositoryImpl: com.example.findbuddy.data.repository.ReportRepositoryImpl
    ): com.example.findbuddy.domain.repository.ReportRepository
}
