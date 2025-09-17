package com.example.smartsplit.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.ListItem
import com.example.smartsplit.Viewmodel.ListViewModel
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

// 🎨 Colors
val lightPrimaryColor = Color(0xFF2196F3)
val lightGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFE6F2FF),
        Color(0xFFCCE5FF),
        Color(0xFFB3DAFF)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListsScreen(navController: NavController, viewModel: ListViewModel = viewModel()) {
    val lists by viewModel.lists.collectAsState()


    LaunchedEffect(lists) {
        android.util.Log.d("MyListsScreen", "Lists updated: size=${lists.size}, data=$lists")
    }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My lists") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("Group") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = lightPrimaryColor,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showBottomSheet = true
                    scope.launch { sheetState.show() }
                },
                containerColor = lightPrimaryColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(lightGradientBrush)
        ) {
            if (lists.isEmpty()) {
                Text(
                    "No lists yet. Tap + to create one",
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
                    color = Color.Gray
                )
            } else {
                LazyColumn {
                    items(lists) { list ->
                        android.util.Log.d("MyListsScreen", "Rendering list: ${list.id}, ${list.title}")
                        ListCard(
                            id = list.id,
                            title = list.title,
                            date = DateFormat.getDateTimeInstance().format(Date(list.createdAt)),
                            navController = navController,
                            items = list.items // ✅ Pass items so we can show qty & total
                        )
                    }
                }

            }
        }
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            CreateNewListBottomSheet(
                onCancel = { showBottomSheet = false },
                onCreate = { newList ->
                    android.util.Log.d("MyListsScreen", "Creating new list: $newList")
                    viewModel.createNewList(newList)
                    showBottomSheet = false
                }
            )
        }
    }
}

@Composable
fun ListCard(title: String, date: String, navController: NavController, id: String, items: List<ListItem> = emptyList()) {
    // ✅ Calculate totals
    val totalItems = items.sumOf { it.quantity }
    val totalAmount = items.sumOf { (it.price ?: 0.0) * it.quantity }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("listdetail/$id") }
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, color = Color.Black, fontWeight = FontWeight.Bold)
                    Text(date, color = Color.Gray, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Items: $totalItems", color = Color.Black)
                    if (totalAmount > 0) {
                        Text("₹$totalAmount", color = lightPrimaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun CreateNewListBottomSheet(
    onCancel: () -> Unit,
    onCreate: (String) -> Unit
) {
    var text by remember { mutableStateOf("New List Name") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create new list", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Title") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester) // 👈 attach focus requester
        )

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            Button(
                onClick = { onCreate(text) },
                colors = ButtonDefaults.buttonColors(containerColor = lightPrimaryColor)
            ) {
                Text("Create", color = Color.White)
            }
        }
    }
}
