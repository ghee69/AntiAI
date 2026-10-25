package com.mrantiai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mrantiai.data.StatsManager
import com.mrantiai.service.AntiAIAccessibilityService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MrAntiAIApp()
        }
    }
}

val DarkBackground = Color(0xFF0F0F14)
val CardBackground = Color(0xFF1D1D26)
val PrimaryBlue = Color(0xFF6E6EFA)
val AccentPink = Color(0xFFFF003F)
val TextLight = Color(0xFFE0E0FF)

@Composable
fun MrAntiAIApp() {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var stats by remember { mutableStateOf(StatsManager.getCounts(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isEnabled = isAccessibilityServiceEnabled(context)
                stats = StatsManager.getCounts(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(32.dp))
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Mr.AntiAI",
                    color = TextLight,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zero AI in your feed.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            // Status Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Protection Status",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEnabled) "Shield Active" else "Shield Off",
                            color = if (isEnabled) Color.Green else AccentPink,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isEnabled) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = Color.Green,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Stats Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Filtered Content Tally",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("YouTube Shorts", color = TextLight, fontSize = 14.sp)
                            Text("${stats.first}", color = PrimaryBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Instagram Reels", color = TextLight, fontSize = 14.sp)
                            Text("${stats.second}", color = AccentPink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Toggle / Settings Button
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (isEnabled) "Open Accessibility Settings" else "Enable Accessibility Shield",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityManager.FEEDBACK_GENERIC)
    for (service in enabledServices) {
        if (service.resolveInfo.serviceInfo.packageName == context.packageName &&
            service.resolveInfo.serviceInfo.name == AntiAIAccessibilityService::class.java.name
        ) {
            return true
        }
    }
    return false
}
