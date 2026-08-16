package com.baldbuffalo.behindthecreator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Creator(
    val name: String,
    val handle: String,
    val initials: String,
    val facts: List<String>,
    val story: String
)

private val creators = listOf(
    Creator(
        "MrBeast", "@MrBeast", "MB",
        listOf("Built a huge network of channels beyond the main MrBeast channel.", "Expanded from YouTube into products and food brands.", "His early videos focused heavily on experiments, challenges and counting-style content."),
        "Jimmy Donaldson started making videos as a kid and gradually learned what made people click and keep watching. His large-scale challenges and reinvestment of revenue helped turn the channel into a global media business."
    ),
    Creator(
        "Mark Rober", "@MarkRober", "MR",
        listOf("Worked as an engineer before becoming a full-time creator.", "Uses engineering projects to make science entertaining.", "His videos often turn a simple question into a large experiment."),
        "Mark Rober combined engineering knowledge with YouTube storytelling. His projects are designed around curiosity: show a problem, build something surprising, test it, and explain the science behind it."
    ),
    Creator(
        "Marques Brownlee", "@mkbhd", "MB",
        listOf("Started reviewing technology while still a teenager.", "Built a production-focused tech channel over many years.", "His work expanded into podcasts, interviews and other media."),
        "Marques Brownlee grew from early laptop and technology tutorials into one of the best-known tech creators. Consistent quality, clear explanations and strong production became central to the MKBHD brand."
    ),
    Creator(
        "PewDiePie", "@PewDiePie", "PD",
        listOf("Began with gaming videos and developed a distinctive commentary style.", "Was one of the earliest creators to reach enormous global YouTube audiences.", "His content evolved substantially over the years."),
        "Felix Kjellberg built a massive audience through gaming commentary and personality-driven videos. His career also shows how quickly creator culture and YouTube itself changed during the 2010s."
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BehindTheCreatorApp() }
    }
}

@Composable
private fun BehindTheCreatorApp() {
    var selected by remember { mutableStateOf<Creator?>(null) }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F7FA)) {
            if (selected == null) HomeScreen(onCreatorClick = { selected = it })
            else CreatorScreen(creator = selected!!, onBack = { selected = null })
        }
    }
}

@Composable
private fun HomeScreen(onCreatorClick: (Creator) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("BehindTheCreator", fontWeight = FontWeight.Bold)
                    Text("Discover what you didn't know", fontSize = 12.sp, color = Color.Gray)
                }
            })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            items(creators) { creator -> CreatorCard(creator, onCreatorClick) }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CreatorCard(creator: Creator, onClick: (Creator) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(creator) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(creator.initials)
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(creator.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(creator.handle, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text("Tap to uncover the story", fontSize = 13.sp, color = Color(0xFF6750A4))
            }
        }
    }
}

@Composable
private fun CreatorScreen(creator: Creator, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(creator.name, fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(20.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(creator.initials, 76)
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(creator.name, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text(creator.handle, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("The story", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(creator.story, fontSize = 16.sp, lineHeight = 24.sp)
                Spacer(Modifier.height(24.dp))
                Text("Things you might not know", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
            }
            items(creator.facts) { fact ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(fact, modifier = Modifier.padding(16.dp), fontSize = 15.sp)
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                Text("More creator profiles coming soon.", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun Avatar(initials: String, size: Int = 58) {
    BoxPlaceholder(initials, size)
}

@Composable
private fun BoxPlaceholder(text: String, size: Int) {
    Surface(
        modifier = Modifier.size(size.dp).clip(CircleShape),
        color = Color(0xFFE7E0EC)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text, fontWeight = FontWeight.Bold, color = Color(0xFF4A4458))
        }
    }
}
