package com.example.findbuddy.ui.categories

import com.example.findbuddy.domain.model.Category

sealed class CategoryIntent {
    object LoadCategories : CategoryIntent()
    object OpenCreateDialog : CategoryIntent()
    object DismissCreateDialog : CategoryIntent()
    data class ChangeNewName(val name: String) : CategoryIntent()
    data class ChangeNewType(val type: String) : CategoryIntent()
    object CreateCategory : CategoryIntent()
    object ClearError : CategoryIntent()

    // Module 5 Limit Setup Intents
    data class OpenLimitDialog(val category: Category) : CategoryIntent()
    object DismissLimitDialog : CategoryIntent()
    data class ChangeLimitAmount(val amount: String) : CategoryIntent()
    object SaveLimit : CategoryIntent()
}
