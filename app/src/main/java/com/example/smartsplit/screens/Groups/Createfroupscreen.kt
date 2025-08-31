import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.example.smartsplit.R
import com.example.smartsplit.Viewmodel.LoginScreenViewModel

val primaryColor = Color(0xFF2196F3)
val accentColor = Color(0xFF2196F3)
val gradientBrush = Brush.verticalGradient(
    colors = listOf(
        primaryColor.copy(alpha = 0.15f),
        Color.White
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavHostController,
    groupViewModel: GroupViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    var groupName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val createdGroupId by groupViewModel.createdGroupId.observeAsState()
    val message by groupViewModel.message.observeAsState("")

    val groupTypes = listOf(
        "Travel" to Icons.Default.Flight,
        "Family" to Icons.Default.Home,
        "Friends" to Icons.Default.Group,
        "Work" to Icons.Default.Work,
        "Grocery" to Icons.Default.ShoppingCart,
        "Other" to Icons.Default.NoteAdd
    )
    val isDark = true

    val primaryColor = Color(0xFF2196F3)
    val accentColor = primaryColor
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.15f),
            Color.White
        )
    )

    val darkBackground = Color.Black
    val darkText = Color.White
    val darkFieldBorder = Color.White
    val darkButtonBg = Color.White
    val darkButtonText = Color.Black

    val backgroundModifier = if (isDark) {
        Modifier.background(darkBackground)
    } else {
        Modifier.background(gradientBrush)
    }
    val currentTextColor = if (isDark) darkText else accentColor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(backgroundModifier)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        IconButton(onClick = { onBackClick(); navController.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = currentTextColor
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Create Group",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = currentTextColor,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .clickable { Log.d("CreateGroup", "Camera clicked") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PeopleOutline,
                    contentDescription = "Group Photo",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group name", color = if (isDark) darkText else Color.Gray) },
                textStyle = LocalTextStyle.current.copy(color = if (isDark) darkText else Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = if (isDark) darkFieldBorder else accentColor,
                    unfocusedBorderColor = if (isDark) darkFieldBorder else Color.Gray,
                    cursorColor = if (isDark) darkText else Color.Black
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Group type
        Text(
            text = "Choose Type",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = currentTextColor
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column {
            for (i in groupTypes.indices step 2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    groupTypes.subList(i, minOf(i + 2, groupTypes.size)).forEach { (label, icon) ->
                        FilterChip(
                            selected = selectedType == label,
                            onClick = { selectedType = label },
                            label = { Text(label, color = currentTextColor) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = currentTextColor
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Travel-specific fields
        if (selectedType == "Travel") {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Start Date (Optional)", color = if (isDark) darkText else Color.Gray) },
                textStyle = LocalTextStyle.current.copy(color = if (isDark) darkText else Color.Black),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = if (isDark) darkFieldBorder else accentColor,
                    unfocusedBorderColor = if (isDark) darkFieldBorder else Color.Gray,
                    cursorColor = if (isDark) darkText else Color.Black
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("End Date (Optional)", color = if (isDark) darkText else Color.Gray) },
                textStyle = LocalTextStyle.current.copy(color = if (isDark) darkText else Color.Black),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = if (isDark) darkFieldBorder else accentColor,
                    unfocusedBorderColor = if (isDark) darkFieldBorder else Color.Gray,
                    cursorColor = if (isDark) darkText else Color.Black
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Done button
        Button(
            onClick = {
                if (groupName.isNotBlank() && selectedType != null) {
                    groupViewModel.createGroup(groupName, selectedType!!)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) darkButtonBg else accentColor,
                contentColor = if (isDark) darkButtonText else Color.White
            )
        ) {
            Text("Done")
        }

        // Show status message
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                message,
                color = if (isDark) darkText else Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    // Navigate only with groupId when created
    LaunchedEffect(createdGroupId, message) {
        if (message.contains("successfully", ignoreCase = true) && createdGroupId != null) {
            Log.d("CreateGroup", "Navigating with ID: $createdGroupId")
            navController.navigate("GroupOverview/$createdGroupId") {
                popUpTo("CreateGroup") { inclusive = true }
            }
        }
    }
}
