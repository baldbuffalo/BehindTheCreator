package com.baldbuffalo.behindthecreator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

private data class Creator(
    val name: String,
    val handle: String,
    val category: String,
    val imageUrl: String,
    val summary: String,
    val story: String,
    val facts: List<String>,
    val timeline: List<String>,
    val sources: List<Pair<String, String>>
)

private val creators = listOf(
    Creator("MrBeast", "@MrBeast", "Entertainment", "https://unavatar.io/youtube/MrBeast",
        "Challenge videos, philanthropy and a creator-led business empire.",
        "Jimmy Donaldson began uploading as a young creator and spent years studying what made videos compelling. His challenge videos grew dramatically as he reinvested revenue into larger productions, eventually turning the MrBeast name into a broad media and consumer brand.",
        listOf(
            "MrBeast has operated multiple major YouTube channels beyond his main channel.",
            "The brand expanded beyond videos into products, food and other businesses.",
            "His early uploads included gaming, commentary and experimental challenge-style videos.",
            "A major part of his strategy has been putting substantial resources back into content."
        ),
        listOf(
            "Early years — experimented with different types of YouTube videos.",
            "2017 — gained major attention from viral counting and challenge content.",
            "Late 2010s — scaled production and philanthropy-focused videos.",
            "2020s — expanded the MrBeast brand into multiple channels and businesses."
        ),
        listOf("YouTube channel" to "https://www.youtube.com/@MrBeast", "MrBeast profile" to "https://en.wikipedia.org/wiki/MrBeast")
    ),
    Creator("Mark Rober", "@MarkRober", "Science", "https://unavatar.io/youtube/MarkRober",
        "Engineer and creator known for giant experiments and science storytelling.",
        "Mark Rober brought an engineering background to YouTube and built a format around making technical ideas entertaining. His videos often begin with a curious problem, turn it into an ambitious build, and then explain the science through the experiment.",
        listOf(
            "Before YouTube became his full-time focus, Rober worked as an engineer at NASA.",
            "His videos frequently combine engineering, science and practical demonstrations.",
            "He has created large public-facing projects designed to teach through entertainment.",
            "His storytelling usually makes the experiment understandable even without an engineering background."
        ),
        listOf(
            "Engineering career — worked on projects including NASA's Curiosity rover.",
            "2011 — started publishing science and engineering videos.",
            "2010s — developed increasingly large experiments and educational projects.",
            "2020s — expanded science education initiatives and large-scale collaborations."
        ),
        listOf("YouTube channel" to "https://www.youtube.com/@MarkRober", "Mark Rober profile" to "https://en.wikipedia.org/wiki/Mark_Rober")
    ),
    Creator("Marques Brownlee", "@MKBHD", "Technology", "https://unavatar.io/youtube/MKBHD",
        "Technology reviewer known for polished videos, interviews and podcasts.",
        "Marques Brownlee started making technology videos as a teenager. Over time, his channel evolved from simple tutorials and reviews into a highly produced technology media operation built around clear explanations, product testing and conversations with notable people in technology and beyond.",
        listOf(
            "He began making technology videos while still in school.",
            "The MKBHD name comes from his initials and the letters HD.",
            "His work expanded beyond reviews into podcasts and long-form interviews.",
            "He became known for making production quality a major part of technology content."
        ),
        listOf(
            "2009 — began publishing technology videos.",
            "Early 2010s — channel grew through reviews and technology tutorials.",
            "Late 2010s — expanded production and interview-focused content.",
            "2020s — continued growing MKBHD into a broader technology media brand."
        ),
        listOf("YouTube channel" to "https://www.youtube.com/@mkbhd", "MKBHD profile" to "https://en.wikipedia.org/wiki/Marques_Brownlee")
    ),
    Creator("PewDiePie", "@PewDiePie", "Gaming", "https://unavatar.io/youtube/PewDiePie",
        "Gaming creator whose personality-driven videos helped shape modern YouTube culture.",
        "Felix Kjellberg built a huge audience through gaming commentary and a distinctive personality-driven style. His career tracks the transformation of YouTube from a video site into a global creator industry, with his content changing considerably as the platform and audience changed.",
        listOf(
            "His channel originally focused heavily on gaming commentary.",
            "His audience growth made him one of the most recognizable creators of the 2010s.",
            "His content changed genres several times rather than staying only gaming-focused.",
            "The channel became an important example of personality-driven creator media."
        ),
        listOf(
            "2010 — created the PewDiePie channel.",
            "Early 2010s — rapidly grew through gaming commentary.",
            "2013 — became the most-subscribed YouTube channel for a period.",
            "Later years — diversified content and shifted away from a purely gaming format."
        ),
        listOf("YouTube channel" to "https://www.youtube.com/@PewDiePie", "PewDiePie profile" to "https://en.wikipedia.org/wiki/PewDiePie")
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
            if (selected == null) HomeScreen { selected = it }
            else CreatorScreen(selected!!) { selected = null }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(onCreatorClick: (Creator) -> Unit) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    val categories = listOf("All", "Entertainment", "Science", "Technology", "Gaming")
    val filtered = creators.filter {
        (query.isBlank() || it.name.contains(query, true) || it.handle.contains(query, true)) &&
            (category == "All" || it.category == category)
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Column { Text("BehindTheCreator", fontWeight = FontWeight.Bold); Text("The stories behind the channels", fontSize = 12.sp, color = Color.Gray) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F7FA))
        )
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                OutlinedTextField(
                    value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, label = { Text("Search creators") }, placeholder = { Text("MrBeast, MKBHD...") },
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { item -> AssistChip(onClick = { category = item }, label = { Text(item) }, leadingIcon = if (category == item) ({ Text("✓") }) else null) }
                }
                Spacer(Modifier.height(10.dp))
            }
            if (filtered.isEmpty()) item { Text("No creators found.", modifier = Modifier.padding(16.dp), color = Color.Gray) }
            else items(filtered) { creator -> CreatorCard(creator, onCreatorClick) }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CreatorCard(creator: Creator, onClick: (Creator) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick(creator) }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CreatorImage(creator.imageUrl, creator.name, 64)
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(creator.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(creator.handle, color = Color.Gray)
                Text(creator.category, fontSize = 12.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(creator.summary, fontSize = 13.sp, color = Color.DarkGray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatorScreen(creator: Creator, onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(creator.name, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F7FA))
        )
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CreatorImage(creator.imageUrl, creator.name, 88)
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(creator.name, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text(creator.handle, color = Color.Gray)
                        Text(creator.category, color = Color(0xFF6750A4), fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(24.dp))
                SectionTitle("The story")
                Text(creator.story, fontSize = 16.sp, lineHeight = 25.sp)
                Spacer(Modifier.height(24.dp))
                SectionTitle("Things you might not know")
                Spacer(Modifier.height(10.dp))
            }
            items(creator.facts) { fact ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Text(fact, modifier = Modifier.padding(16.dp), fontSize = 15.sp, lineHeight = 22.sp)
                }
            }
            item { Spacer(Modifier.height(14.dp)); SectionTitle("Creator timeline"); Spacer(Modifier.height(8.dp)) }
            items(creator.timeline) { event ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF6750A4)))
                    Text(event, modifier = Modifier.padding(start = 12.dp), fontSize = 15.sp, lineHeight = 21.sp)
                }
            }
            item {
                Spacer(Modifier.height(18.dp)); SectionTitle("Sources")
                Text("Tap a source to open it in your browser.", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
            }
            items(creator.sources) { source ->
                Text(
                    source.first,
                    modifier = Modifier.fillMaxWidth().clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source.second)))
                    }.padding(vertical = 9.dp),
                    color = Color(0xFF6750A4), fontWeight = FontWeight.SemiBold
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun CreatorImage(url: String, name: String, size: Int) {
    AsyncImage(
        model = url,
        contentDescription = "$name profile picture",
        modifier = Modifier.size(size.dp).clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}
