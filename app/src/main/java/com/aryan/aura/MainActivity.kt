package com.aryan.aura

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

// --- 1. The ViewModel ---
class MainViewModel : ViewModel() {
    companion object {
        private val _auraInsight = MutableStateFlow(Insight("Aura is resting...", "Calm", "Relax", "Now", 1))
        val auraInsight = _auraInsight.asStateFlow()

        private val _insightHistory = MutableStateFlow<List<Insight>>(emptyList())
        val insightHistory = _insightHistory.asStateFlow()

        private val _debugLogs = MutableStateFlow<List<String>>(emptyList())
        val debugLogs = _debugLogs.asStateFlow()

        var isWhatsAppEnabled by mutableStateOf(true)
        var isInstagramEnabled by mutableStateOf(true)

        private var lastRemoteInput: RemoteInput? = null
        private var lastPendingIntent: PendingIntent? = null
        var canReply by mutableStateOf(false)

        fun setReplyData(remoteInput: RemoteInput?, pendingIntent: PendingIntent?) {
            lastRemoteInput = remoteInput
            lastPendingIntent = pendingIntent
            canReply = remoteInput != null && pendingIntent != null
        }

        fun sendReply(context: Context, replyText: String) {
            val remoteInput = lastRemoteInput
            val pendingIntent = lastPendingIntent
            if (remoteInput != null && pendingIntent != null) {
                val intent = Intent()
                val bundle = Bundle()
                bundle.putCharSequence(remoteInput.resultKey, replyText)
                RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, bundle)
                try {
                    pendingIntent.send(context, 0, intent)
                    addDebugLog("Reply sent: $replyText")
                } catch (e: Exception) {
                    addDebugLog("Failed to send reply: ${e.message}")
                }
            } else {
                addDebugLog("No reply action available")
            }
        }

        fun addDebugLog(log: String) {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            _debugLogs.value = (listOf("[$timestamp] $log") + _debugLogs.value).take(5)
        }

        fun updateInsight(jsonStringOrText: String) {
            try {
                if (jsonStringOrText.trim().startsWith("{")) {
                    val json = JSONObject(jsonStringOrText)
                    val newInsight = Insight(
                        text = json.optString("summary", "Sensing vibes..."),
                        vibe = json.optString("vibe", "Unknown"),
                        reply = json.optString("reply", "..."),
                        time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                        stressScore = json.optInt("stress_score", 1)
                    )
                    _auraInsight.value = newInsight
                    _insightHistory.value = (listOf(newInsight) + _insightHistory.value).take(10)
                } else {
                    val fallback = Insight(jsonStringOrText, "Vibing", "Keep going", "Now", 2)
                    _auraInsight.value = fallback
                }
            } catch (e: Exception) {
                val fallback = Insight("Aura is processing...", "Thinking", "...", "Now", 1)
                _auraInsight.value = fallback
            }
        }
    }

    data class Insight(val text: String, val vibe: String, val reply: String, val time: String, val stressScore: Int)

    val auraInsight = MainViewModel.auraInsight
    val insightHistory = MainViewModel.insightHistory
    val debugLogs = MainViewModel.debugLogs

    fun toggleWhatsApp(enabled: Boolean) { MainViewModel.isWhatsAppEnabled = enabled }
    fun toggleInstagram(enabled: Boolean) { MainViewModel.isInstagramEnabled = enabled }
}

// --- 2. The Activity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        
        // Start the background service for persistence and vibe monitoring
        val serviceIntent = Intent(this, com.aryan.aura.service.AuraEmpathyService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        enableEdgeToEdge()
        setContent {
            AuraTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AuraHomeScreen()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Aura Protection"
            val descriptionText = "Ensures Aura stays active in the background"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("AURA_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun AuraTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = Color(0xFFBB86FC),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}

// --- 3. UI Components ---
@Composable
fun AuraHomeScreen(vm: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val insight by vm.auraInsight.collectAsState()
    val history by vm.insightHistory.collectAsState()
    val logs by vm.debugLogs.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 64.dp, bottom = 32.dp)
    ) {
        item {
            HeaderSection()
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            MainAuraCard(insight)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text(
                "Protection Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppToggleRow("WhatsApp", MainViewModel.isWhatsAppEnabled) { vm.toggleWhatsApp(it) }
            AppToggleRow("Instagram", MainViewModel.isInstagramEnabled) { vm.toggleInstagram(it) }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            val isEnabled = isNotificationServiceEnabled(context)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEnabled) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isEnabled) "System Access: GRANTED" else "System Access: REQUIRED",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (isEnabled) "Manage Access" else "Enable System Access", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = { 
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    context.startActivity(intent)
                    MainViewModel.addDebugLog("Resetting Service Link...")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Refresh Connection (Fix Intercept)")
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { MainViewModel.updateInsight("{\"summary\":\"High stress alert! Take a breath.\",\"vibe\":\"Stress\",\"reply\":\"I'll reply later.\",\"stress_score\":9}") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Test Stress Pulse")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (history.isNotEmpty()) {
            item {
                Text(
                    "Recent Vibes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(history) { item ->
                HistoryItem(item)
                Spacer(modifier = Modifier.height(8.dp))
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        item {
            Text(
                "System Debug Console",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.3f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (logs.isEmpty()) {
                        Text("Waiting for notifications...", color = Color.Gray, fontSize = 12.sp)
                    }
                    logs.forEach { log ->
                        Text(log, color = Color(0xFF00FF00), fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Column {
        Text(
            text = "Aura Shield",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "Your digital peace of mind.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun MainAuraCard(insight: MainViewModel.Insight) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val isHighStress = insight.stressScore > 7

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (isHighStress) {
                        listOf(Color(0xFFB71C1C), Color(0xFF7F0000))
                    } else {
                        listOf(Color(0xFF6200EE), Color(0xFF3700B3))
                    }
                )
            )
            .then(
                if (isHighStress) {
                    Modifier.padding(4.dp).background(Color.Red.copy(alpha = pulseAlpha), RoundedCornerShape(32.dp))
                } else Modifier
            )
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "LIVE INSIGHT",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if(insight.stressScore > 7) Color.Red else Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = insight.vibe.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedContent(targetState = insight.text, label = "") { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 28.sp,
                    maxLines = 3
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stress Meter
            Text("Stress Level: ${insight.stressScore}/10", color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
            LinearProgressIndicator(
                progress = { insight.stressScore / 10f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = if (insight.stressScore > 7) Color.Red else Color(0xFF03DAC6),
                trackColor = Color.White.copy(0.2f)
            )

            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Aura Suggests: \"${insight.reply}\"",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                if (MainViewModel.canReply) {
                    val context = LocalContext.current
                    IconButton(
                        onClick = { MainViewModel.sendReply(context, insight.reply) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send Reply",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppToggleRow(appName: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(appName, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Switch(checked = isEnabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
fun HistoryItem(insight: MainViewModel.Insight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = insight.time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = insight.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                color = Color.LightGray
            )
        }
    }
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = android.provider.Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(pkgName) == true
}
