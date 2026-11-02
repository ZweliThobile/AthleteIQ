package com.example.athleteiq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.athleteiq.data.AthleteProfile
import com.example.athleteiq.data.DeviceAccountStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val destinations = listOf("dashboard" to "Training", "profile" to "Profile", "goals" to "Goals", "recommendations" to "Insights")

@Composable
private fun deviceViewModel(): AthleteViewModel {
    val context = LocalContext.current.applicationContext
    val factory = remember(context) { viewModelFactory { initializer { AthleteViewModel(DeviceAccountStore(context)) } } }
    return viewModel(factory = factory)
}

@Composable
fun AthleteApp(model: AthleteViewModel = deviceViewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    LaunchedEffect(state.signedIn) {
        if (!state.signedIn) nav.navigate("login") { popUpTo(nav.graph.id) { inclusive = true } }
    }
    Scaffold(bottomBar = {
        if (state.signedIn && route != "login") NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            destinations.forEach { (destination, title) ->
                NavigationBarItem(selected = route == destination, onClick = {
                    nav.navigate(destination) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }, icon = { SportIcon(destination) }, label = { Text(title) })
            }
        }
    }) { padding ->
        NavHost(navController = nav, startDestination = "login", modifier = Modifier.padding(padding)) {
            composable("login") {
                LoginScreen(model) {
                    nav.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                }
            }
            composable("dashboard") { DashboardScreen(state) }
            composable("profile") { ProfileScreen(state, model::saveProfile, model::logout) }
            composable("goals") { GoalsScreen(state, model) }
            composable("recommendations") { RecommendationsScreen(state) }
        }
    }
}

@Composable
private fun SportIcon(kind: String, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier.size(24.dp).clearAndSetSemantics { }) {
        val scale = size.width / 24f
        val path = Path()
        fun line(x: Float, y: Float) = path.lineTo(x * scale, y * scale)
        fun move(x: Float, y: Float) = path.moveTo(x * scale, y * scale)
        when (kind) {
            "dashboard" -> { move(3f, 19f); line(3f, 13f); move(9f, 19f); line(9f, 9f); move(15f, 19f); line(15f, 12f); move(21f, 19f); line(21f, 4f) }
            "profile" -> {
                drawCircle(color, 4f * scale, Offset(12f * scale, 7f * scale), style = Stroke(1.8f * scale))
                move(4f, 21f); line(4f, 18f); path.cubicTo(4f * scale, 11f * scale, 20f * scale, 11f * scale, 20f * scale, 18f * scale); line(20f, 21f)
            }
            "goals" -> { move(5f, 22f); line(5f, 3f); line(20f, 3f); line(16f, 8f); line(20f, 13f); line(5f, 13f) }
            else -> { move(12f, 2f); line(15f, 9f); line(22f, 12f); line(15f, 15f); line(12f, 22f); line(9f, 15f); line(2f, 12f); line(9f, 9f); path.close() }
        }
        drawPath(path, color, style = Stroke(1.8f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    }
}

@Composable
private fun Brand() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            SportIcon("dashboard", Modifier.padding(10.dp))
        }
        Column {
            Text("athleteiq", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.8).sp)
            Text("BUILT FOR THE LONG RUN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Page(title: String, subtitle: String, hero: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Brand()
        if (hero) WelcomeHero() else {
            Spacer(Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.headlineLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        content()
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun WelcomeHero() {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(Color(0xFF112E28), Color(0xFF245A43))))) {
        Canvas(Modifier.matchParentSize().clearAndSetSemantics { }) {
            repeat(5) { index ->
                drawCircle(Color(0xFFB9E985).copy(alpha = 0.14f), size.width * (0.36f + index * 0.09f), Offset(size.width * 1.10f, size.height * 0.88f), style = Stroke(2.dp.toPx()))
            }
        }
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(color = Color(0xFFB9E985), shape = RoundedCornerShape(50)) {
                Text("YOUR ENDURANCE ADVANTAGE", Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF112E28))
            }
            Text("Big goals.\nStronger every day.", color = Color.White, style = MaterialTheme.typography.headlineLarge)
            Text("Your training, your progress, your next finish line. All in one place.", color = Color(0xFFCFDDD3), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth(0.86f))
            Text("RUN  /  RIDE  /  GO FURTHER", color = Color(0xFFB9E985), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun InfoCard(title: String, detail: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
        Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.width(3.dp).height(38.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primaryContainer))
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit, numeric: Boolean = false) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
}

@Composable
private fun Feedback(message: String?) {
    message?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium) }
}

@Composable
private fun LoginScreen(model: AthleteViewModel, onAuthenticated: () -> Unit) {
    var signingUp by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    // Passwords are deliberately kept out of saved instance state.
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var hours by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var sport by rememberSaveable { mutableStateOf("Running") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Page("", "", hero = true) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf("Sign In", "Sign Up").forEachIndexed { index, label ->
                SegmentedButton(selected = signingUp == (index == 1), enabled = !busy,
                    onClick = { signingUp = index == 1; error = null; password = ""; confirm = "" },
                    shape = SegmentedButtonDefaults.itemShape(index, 2)) { Text(label) }
            }
        }
        Text(if (signingUp) "Create your athlete profile" else "Welcome back", style = MaterialTheme.typography.titleLarge)
        Text(if (signingUp) "A little about you. A plan for what comes next." else "Your next chapter starts with today’s training.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (signingUp) {
            Field("Full name", name, { name = it; error = null })
            Field("Age", age, { age = it; error = null }, true)
            Text("Body measurements · Optional", style = MaterialTheme.typography.titleMedium)
            Field("Weight (kg)", weight, { weight = it; error = null }, true)
            Field("Height (cm)", height, { height = it; error = null }, true)
            Text("Primary sport")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Running", "Cycling", "Triathlon").forEach { option ->
                    FilterChip(selected = sport == option, onClick = { sport = option }, label = { Text(option) })
                }
            }
            Field("Training hours per week", hours, { hours = it; error = null }, true)
        }
        OutlinedTextField(value = email, onValueChange = { email = it; error = null }, label = { Text("Email address") }, singleLine = true, enabled = !busy, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it; error = null }, label = { Text("Password") }, singleLine = true, enabled = !busy, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        if (signingUp) {
            Text("Use at least 8 characters.", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value = confirm, onValueChange = { confirm = it; error = null }, label = { Text("Confirm password") }, singleLine = true, enabled = !busy, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(enabled = !busy, onClick = {
            busy = true
            // Capture the form before launching background credential derivation and disk access.
            val submittedEmail = email
            val submittedPassword = password
            val submittedConfirm = confirm
            val submittedProfile = AthleteProfile(name, age, sport, hours, weight.trim(), height.trim())
            val createAccount = signingUp
            scope.launch {
                error = withContext(Dispatchers.IO) {
                    if (createAccount) model.signUp(submittedEmail, submittedPassword, submittedConfirm, submittedProfile)
                    else model.login(submittedEmail, submittedPassword)
                }
                busy = false
                if (error == null) { password = ""; confirm = ""; onAuthenticated() }
            }
        }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
            if (busy) CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text(if (signingUp) "Create my account  →" else "Sign In  →")
        }
        Text("Saved on this device · Sample training data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        }
    }
}

@Composable
private fun DashboardScreen(state: AthleteUiState) {
    val distance = state.sessions.sumOf { it.distanceKm }
    Page("Hello, ${state.profile.name.substringBefore(' ')}", "Your training dashboard · Illustrative sample week") {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(26.dp)) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("THIS WEEK • SAMPLE PLAN", style = MaterialTheme.typography.labelSmall)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("%.1f".format(distance), fontSize = 52.sp, fontWeight = FontWeight.Bold, letterSpacing = (-2).sp)
                    Text("km planned", Modifier.padding(bottom = 10.dp), style = MaterialTheme.typography.bodyLarge)
                }
                LinearProgressIndicator(progress = { (distance / 45).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))
                Text("${"%.1f".format(distance)} of 45 km weekly target", style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text("4 RUNS", style = MaterialTheme.typography.labelSmall); Text("3h 55m", style = MaterialTheme.typography.titleLarge) }
                    Column { Text("AVAILABILITY", style = MaterialTheme.typography.labelSmall); Text("${state.profile.weeklyHours}h / week", style = MaterialTheme.typography.titleLarge) }
                }
            }
        }
        state.goals.firstOrNull()?.let { InfoCard("Next goal: ${it.name}", "${it.distanceKm} km · Target ${it.targetMinutes} minutes") }
        Text("This week's sessions", style = MaterialTheme.typography.titleLarge)
        state.sessions.forEach { session ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(14.dp)) {
                        Text(session.day, Modifier.padding(horizontal = 12.dp, vertical = 18.dp), style = MaterialTheme.typography.labelSmall)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(session.title, style = MaterialTheme.typography.titleMedium)
                        Text(session.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(state: AthleteUiState, save: (AthleteProfile) -> String?, logout: () -> Unit) {
    var name by rememberSaveable(state.profile.name) { mutableStateOf(state.profile.name) }
    var age by rememberSaveable(state.profile.age) { mutableStateOf(state.profile.age) }
    var hours by rememberSaveable(state.profile.weeklyHours) { mutableStateOf(state.profile.weeklyHours) }
    var weight by rememberSaveable(state.profile.weightKg) { mutableStateOf(state.profile.weightKg) }
    var height by rememberSaveable(state.profile.heightCm) { mutableStateOf(state.profile.heightCm) }
    var sport by rememberSaveable(state.profile.sport) { mutableStateOf(state.profile.sport) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    Page("Athlete profile", "Set your training context. Your profile is saved on this device.") {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(24.dp)) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
                    Text(state.profile.name.split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }, Modifier.padding(18.dp), style = MaterialTheme.typography.titleLarge)
                }
                Column { Text(state.profile.name, style = MaterialTheme.typography.titleMedium); Text("${state.profile.sport} athlete", style = MaterialTheme.typography.bodyMedium) }
            }
        }
        Field("Full name", name, { name = it; message = null })
        Field("Age", age, { age = it; message = null }, true)
        Text("Body measurements · Optional", style = MaterialTheme.typography.titleMedium)
        Field("Weight (kg)", weight, { weight = it; message = null }, true)
        Field("Height (cm)", height, { height = it; message = null }, true)
        Text("Primary sport")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Running", "Cycling", "Triathlon").forEach { option -> FilterChip(selected = sport == option, onClick = { sport = option; message = null }, label = { Text(option) }) }
        }
        Field("Training hours per week", hours, { hours = it; message = null }, true)
        Feedback(message)
        Button(enabled = !saving, onClick = {
            val submittedProfile = AthleteProfile(name, age, sport, hours, weight.trim(), height.trim())
            saving = true
            scope.launch {
                message = withContext(Dispatchers.IO) { save(submittedProfile) } ?: "Profile saved."
                saving = false
            }
        }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) { Text(if (saving) "Saving…" else "Save profile") }
        OutlinedButton(enabled = !saving, onClick = logout, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) { Text("Sign out") }
    }
}

@Composable
private fun GoalsScreen(state: AthleteUiState, model: AthleteViewModel) {
    var name by rememberSaveable { mutableStateOf("") }
    var distance by rememberSaveable { mutableStateOf("") }
    var minutes by rememberSaveable { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    Page("Race goals", "Give every training week a purpose.") {
        if (state.goals.isEmpty()) Text("No goals yet. Add your next race below.")
        state.goals.forEach { goal ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text(goal.name, style = MaterialTheme.typography.titleMedium)
                Text("${goal.distanceKm} km · Target ${goal.targetMinutes} minutes")
                TextButton(onClick = { model.removeGoal(goal) }) { Text("Remove goal") }
            } }
        }
        Text("Add a race", style = MaterialTheme.typography.titleLarge)
        Field("Race name", name, { name = it; message = null })
        Field("Distance (km)", distance, { distance = it; message = null }, true)
        Field("Target time (minutes)", minutes, { minutes = it; message = null }, true)
        Feedback(message)
        Button(onClick = {
            val error = model.addGoal(name, distance, minutes)
            message = error ?: "Race goal added."
            if (error == null) { name = ""; distance = ""; minutes = "" }
        }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) { Text("Add goal") }
    }
}

@Composable
private fun RecommendationsScreen(state: AthleteUiState) {
    Page("Train with purpose", "Sample guidance based on your profile and goals. No live sensor data is connected.") {
        InfoCard("Make room for recovery", "Keep at least one full rest day between demanding sessions. Adjust your plan when fatigue persists.")
        InfoCard("Build your ${state.profile.sport.lowercase()} base", "You have ${state.profile.weeklyHours} hours available each week. Prioritise comfortable aerobic sessions and consistency before adding intensity.")
        state.goals.firstOrNull()?.let {
            val pace = it.targetMinutes / it.distanceKm
            val seconds = (pace * 60).toInt()
            InfoCard("Know your target pace", "${it.name}: ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')} min/km average to finish ${it.distanceKm} km in ${it.targetMinutes} minutes. This is a goal calculation, not a fitness assessment.")
        } ?: InfoCard("Choose your next finish line", "Add a race goal to see your target average pace here.")
        InfoCard("Fuel the long session", "Practise your race-day food and hydration strategy during longer sessions, adjusting to conditions and your own tolerance.")
    }
}
