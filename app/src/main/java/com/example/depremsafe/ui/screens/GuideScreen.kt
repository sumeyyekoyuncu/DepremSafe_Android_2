package com.example.depremsafe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deprem Hazırlık Rehberi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GuideSection(
                    title = "Deprem Öncesi",
                    items = listOf(
                        "Evdeki ağır eşyaları sabitleyin",
                        "Acil durum çantası hazırlayın",
                        "Ailenizle toplanma noktası belirleyin",
                        "Gaz, elektrik ve su vanalarının yerlerini öğrenin"
                    )
                )
            }

            item {
                GuideSection(
                    title = "Deprem Sırasında",
                    items = listOf(
                        "Masa altına girin, başınızı koruyun",
                        "Camlardan uzak durun",
                        "Asansör kullanmayın",
                        "Dışarıdaysanız açık alana çıkın"
                    )
                )
            }

            item {
                GuideSection(
                    title = "Deprem Sonrası",
                    items = listOf(
                        "Artçı sarsıntılara karşı dikkatli olun",
                        "Yaralıları kontrol edin",
                        "Gaz kaçağı kontrolü yapın",
                        "Hasarlı binaların içine girmeyin"
                    )
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Acil Durum Telefonları",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Polis: 155\nİtfaiye: 110\nAmbulans: 112\nAFAD: 122")
                    }
                }
            }
        }
    }
}

@Composable
fun GuideSection(title: String, items: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("• ")
                    Text(item, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
