package com.rideos.android.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rideos.android.domain.telemetry.TelemetrySnapshot

@Composable
fun DashboardScreen(telemetry: TelemetrySnapshot, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("RideOS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(if (telemetry.connected) "CONNECTED" else "OFFLINE")
        }

        Spacer(Modifier.height(24.dp))
        Text("${telemetry.speedKmh.toInt()}", fontSize = 72.sp, fontWeight = FontWeight.Bold)
        Text("km/h", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))
        Text("${telemetry.gear} / ${telemetry.gearCount}  •  ${telemetry.erpm} ERPM")
        Spacer(Modifier.height(20.dp))

        val metrics = listOf(
            "Battery" to "${telemetry.batteryVoltage} V • ${telemetry.batteryPercent.toInt()}%",
            "Input power" to "${telemetry.inputPowerWatts.toInt()} W",
            "Battery current" to "${telemetry.batteryCurrent} A",
            "Motor current" to "${telemetry.motorCurrent} A",
            "Duty" to "${(telemetry.dutyCycle * 100).toInt()}%",
            "Motor temp" to "${telemetry.motorTemperatureC} °C"
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(metrics) { (label, value) ->
                MetricCard(label, value)
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String) {
    Card {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}
