package com.example.findbuddy.ui.transactions

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.findbuddy.ui.theme.*
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    state: TransactionState,
    onIntent: (TransactionIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    // Date picker dialog setup
    val calendar = Calendar.getInstance()
    if (state.date.isNotEmpty()) {
        try {
            val parts = state.date.split("-")
            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        } catch (e: Exception) {
            // fallback
        }
    }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onIntent(TransactionIntent.ChangeDate(formattedDate))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    // Prevent selecting future dates
    datePickerDialog.datePicker.maxDate = Date().time

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditMode) "Edit Transaction" else "New Transaction",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PrimaryTeal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PrimaryTeal
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OutlineVariant)
                            .border(1.dp, OutlineVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = PrimaryTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message Banner
            if (state.errorMsg != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.errorMsg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onIntent(TransactionIntent.ClearError) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Transaction Type Segmented Tab Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECEEF0), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val types = listOf("EXPENSE", "INCOME", "TRANSFER")
                types.forEach { tabType ->
                    val isSelected = state.type == tabType
                    val label = tabType.lowercase(Locale.US).replaceFirstChar { it.uppercase() }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryTeal else Color.Transparent)
                            .clickable {
                                onIntent(TransactionIntent.ChangeType(tabType))
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else OnSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Amount Entry (Hero Focus)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BoxBorder(1.dp, OutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AMOUNT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        BasicTextField(
                            value = state.amount,
                            onValueChange = { onIntent(TransactionIntent.ChangeAmount(it)) },
                            textStyle = TextStyle(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal,
                                textAlign = TextAlign.Start
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.width(IntrinsicSize.Min)
                        )
                    }
                }
            }

            // Date and Description fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date Selector
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Date", fontSize = 12.sp, color = OnSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                            .clickable { datePickerDialog.show() }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select Date",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = state.date.ifEmpty { "YYYY-MM-DD" },
                                fontSize = 14.sp,
                                color = if (state.date.isEmpty()) OutlineVariant else OnSurfaceDark
                            )
                        }
                    }
                }

                // Description Input
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Description", fontSize = 12.sp, color = OnSurfaceVariant)
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { onIntent(TransactionIntent.ChangeDescription(it)) },
                        placeholder = { Text("What was it for?", color = OutlineVariant) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = OnSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = OutlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Account and Category Selectors
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.type == "TRANSFER") {
                    // Source Account Dropdown
                    DropdownField(
                        label = "From Account",
                        selectedId = state.selectedAccountId,
                        options = state.accountsList.map { it.id to it.accountName },
                        onSelect = { onIntent(TransactionIntent.SelectAccount(it)) },
                        hasAccentBar = true
                    )

                    // Destination Account Dropdown
                    DropdownField(
                        label = "To Account",
                        selectedId = state.selectedDestinationAccountId,
                        options = state.accountsList.map { it.id to it.accountName },
                        onSelect = { onIntent(TransactionIntent.SelectDestinationAccount(it)) },
                        hasAccentBar = true
                    )
                } else {
                    // Standard Transaction Source Account
                    DropdownField(
                        label = "From Account",
                        selectedId = state.selectedAccountId,
                        options = state.accountsList.map { it.id to it.accountName },
                        onSelect = { onIntent(TransactionIntent.SelectAccount(it)) },
                        hasAccentBar = true
                    )

                    // Category Dropdown
                    DropdownField(
                        label = "Category",
                        selectedId = state.selectedCategoryId,
                        options = state.categoriesList.map { it.id to it.categoryName },
                        onSelect = { onIntent(TransactionIntent.SelectCategory(it)) },
                        hasAccentBar = false
                    )
                }
            }

            // Receipt Photo Placeholder Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .dashedBorder(2.dp, OutlineVariant, 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { /* No action since it's out of scope */ }
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Attach receipt or photo note",
                        color = OnSurfaceMuted,
                        fontSize = 14.sp
                    )
                }
            }

            // Budget Insight Banner (Only for EXPENSE)
            if (state.type == "EXPENSE") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GrowthGreen, RoundedCornerShape(12.dp))
                        .border(1.dp, OnSecondaryGreenFixed.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Insight",
                            tint = OnSecondaryGreenFixed
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "BUDGET INSIGHT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSecondaryGreenFixed,
                                letterSpacing = 0.05.sp
                            )
                            val displayAmt = if (state.amount.isNotEmpty()) "$${state.amount}" else "$0.00"
                            Text(
                                text = "Adding this $displayAmt expense will bring your budget to its monthly limit. Make sure to audit categories regularly.",
                                fontSize = 14.sp,
                                color = OnSecondaryGreenFixed.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onIntent(TransactionIntent.Save) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = if (state.isEditMode) "Save Changes" else "Save Transaction",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                if (state.isEditMode) {
                    OutlinedButton(
                        onClick = { onIntent(TransactionIntent.Delete) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TertiaryCoral),
                        border = BoxBorder(1.dp, TertiaryCoral),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = TertiaryCoral
                            )
                            Text(
                                text = "Delete Transaction",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Text(
                    text = "This will be saved to your local secure vault.",
                    color = OnSurfaceMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DropdownField(
    label: String,
    selectedId: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    hasAccentBar: Boolean
) {
    var expanded by remember { mutableStateFlowOf(false) }
    val selectedName = options.find { it.first == selectedId }?.second ?: "Select Option"

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 12.sp, color = OnSurfaceVariant)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasAccentBar) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .background(PrimaryTeal, CircleShape)
                        )
                    }
                    Text(
                        text = selectedName,
                        fontSize = 14.sp,
                        color = if (selectedId.isEmpty()) OutlineVariant else OnSurfaceDark
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand Options",
                    tint = OnSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                options.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onSelect(id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// Helpers for borders and custom layouts
fun BoxBorder(width: Dp, color: Color) = BorderStroke(width, color)

fun Modifier.dashedBorder(width: Dp, color: Color, cornerRadius: Dp) = this.drawBehind {
    val stroke = Stroke(
        width = width.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}

// Inline mutableStateOf wrapper for cleaner syntax
fun <T> mutableStateFlowOf(value: T) = mutableStateOf(value)
