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
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class CreatorEntry(
    val channelUrl: String,
    val category: String,
    val story: String,
    val facts: List<String>,
    val timeline: List<String>
)

private data class YouTubeChannel(
    val name: String = "Unknown creator",
    val handle: String = "",
    val imageUrl: String = "",
    val subscribers: String = "—",
    val joined: String = "—"
)

private data class Creator(
    val entry: CreatorEntry,
    val channel: YouTubeChannel
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BehindTheCreatorApp() }
    }
}

@Composable
private fun BehindTheCreatorApp() {
    val context = LocalContext.current
    var entries by remember { mutableStateOf<List<CreatorEntry>>(emptyList()) }
    var creators by remember { mutableStateOf<List<Creator>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf<Creator?>(null) }

    LaunchedEffect(Unit) {
        entries = loadEntries(context)
        creators = entries.map { entry -> Creator(entry, fetchChannelInfo(entry.channelUrl)) }
        loading = false
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F7FA)) {
            when {
                selected != null -> CreatorScreen(selected!!) { selected = null }
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                else -> HomeScreen(creators) { selected = it }
            }
        }
    }
}

private fun loadEntries(context: android.content.Context): List<CreatorEntry> {
    val json = context.assets.open("creators.json").bufferedReader().use { it.readText() }
    val array = JSONObject(json).getJSONArray("creators")
    return buildList {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            add(
                CreatorEntry(
                    channelUrl = item.getString("channelUrl"),
                    category = item.optString("category", "Other"),
                    story = item.optString("story", ""),
                    facts = item.optJSONArray("facts")?.let { a -> List(a.length()) { n -> a.getString(n) } } ?: emptyList(),
                    timeline = item.optJSONArray("timeline")?.let { a -> List(a.length()) { n -> a.getString(n) } } ?: emptyList()
                )
            )
        }
    }
}

private fun fetchChannelInfo(channelUrl: String): YouTubeChannel {
    return runCatching {
        if (BuildConfig.YOUTUBE_API_KEY.isNotBlank()) fetchWithYouTubeApi(channelUrl)
        else fetchWithOEmbed(channelUrl)
    }.getOrElse { fetchWithOEmbed(channelUrl) }
}

private fun fetchWithYouTubeApi(channelUrl: String): YouTubeChannel {
    val uri = Uri.parse(channelUrl)
    val segments = uri.pathSegments
    val handle = segments.lastOrNull { it.startsWith("@") }
    val channelId = if (segments.size >= 2 && segments[segments.size - 2] == "channel") segments.last() else null
    val selector = when {
        handle != null -> "forHandle=${URLEncoder.encode(handle, "UTF-8")}"
        channelId != null -> "id=${URLEncoder.encode(channelId, "UTF-8")}"
        else -> error("Unsupported YouTube channel URL")
    }

    val endpoint = "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&$selector&key=${URLEncoder.encode(BuildConfig.YOUTUBE_API_KEY, "UTF-8")}"
    val obj = JSONObject(httpGet(endpoint))
    val item = obj.getJSONArray("items").getJSONObject(0)
    val snippet = item.getJSONObject("snippet")
    val statistics = item.getJSONObject("statistics")
    val customUrl = snippet.optString("customUrl", handle ?: "")
    val publishedAt = snippet.optString("publishedAt", "")

    return YouTubeChannel(
        name = snippet.optString("title", "Unknown creator"),
        handle = customUrl,
        imageUrl = snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url"),
        subscribers = statistics.optString("subscriberCount", "—").let(::formatNumber),
        joined = formatDate(publishedAt)
    )
}

private fun fetchWithOEmbed(channelUrl: String): YouTubeChannel {
    val endpoint = "https://www.youtube.com/oembed?url=${URLEncoder.encode(channelUrl, "UTF-8")}&format=json"
    val obj = JSONObject(httpGet(endpoint))
    return YouTubeChannel(
        name = obj.optString("author_name", obj.optString("title", "Unknown creator")),
        handle = Uri.parse(channelUrl).pathSegments.lastOrNull() ?: "",
        imageUrl = obj.optString("thumbnail_url", "")
    )
}

private fun httpGet(url: String): String {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = 10_000
    connection.readTimeout = 10_000
    return connection.inputStream.bufferedReader().use { it.readText() }.also { connection.disconnect() }
}

private fun formatNumber(value: String): String {
    val number = value.toLongOrNull() ?: return value
    return when {
        number >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", number / 1_000_000_000.0)
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%.1fK", number / 1_000.0)
        else -> String.format(Locale.US, "%,d", number)
    }
}

private fun formatDate(value: String): String = runCatching {
    val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    val output = SimpleDateFormat("MMM d, yyyy", Locale.US)
    output.format(input.parse(value) ?: Date())
}.getOrDefault("—")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(creators: List<Creator>, onCreatorClick: (Creator) -> Unit) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    val categories = listOf("All") + creators.map { it.entry.category }.distinct().sorted()
    val filtered = creators.filter {
        (query.isBlank() || it.channel.name.contains(query, true) || it.channel.handle.contains(query, true)) &&
            (category == "All" || it.entry.category == category)
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Column { Text("BehindTheCreator", fontWeight = FontWeight.Bold); Text("The stories behind the channels", fontSize = 12.sp, color = Color.Gray) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F7FA))
        )
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Search creators") }, placeholder = { Text("MrBeast, MKBHD...") }, shape = RoundedCornerShape(16.dp))
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
            CreatorImage(creator.channel.imageUrl, creator.channel.name, 64)
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(creator.channel.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(creator.channel.handle, color = Color.Gray)
                Text(creator.entry.category, fontSize = 12.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.SemiBold)
                Text("${creator.channel.subscribers} subscribers", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatorScreen(creator: Creator, onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(title = { Text(creator.channel.name, fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F7FA)))
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CreatorImage(creator.channel.imageUrl, creator.channel.name, 88)
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(creator.channel.name, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text(creator.channel.handle, color = Color.Gray)
                        Text(creator.entry.category, color = Color(0xFF6750A4), fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Subscribers", creator.channel.subscribers)
                    StatCard("Joined", creator.channel.joined)
                }
                Spacer(Modifier.height(24.dp))
                SectionTitle("The story")
                Text(creator.entry.story, fontSize = 16.sp, lineHeight = 25.sp)
                Spacer(Modifier.height(24.dp))
                SectionTitle("Things you might not know")
                Spacer(Modifier.height(10.dp))
            }
            items(creator.entry.facts) { fact ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Text(fact, modifier = Modifier.padding(16.dp), fontSize = 15.sp, lineHeight = 22.sp)
                }
            }
            item { Spacer(Modifier.height(14.dp)); SectionTitle("Creator timeline"); Spacer(Modifier.height(8.dp)) }
            items(creator.entry.timeline) { event ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF6750A4)))
                    Text(event, modifier = Modifier.padding(start = 12.dp), fontSize = 15.sp, lineHeight = 21.sp)
                }
            }
            item {
                Spacer(Modifier.height(18.dp)); SectionTitle("YouTube")
                Text("Open the original channel.", fontSize = 13.sp, color = Color.Gray)
                Text("YouTube channel", modifier = Modifier.fillMaxWidth().clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(creator.entry.channelUrl))) }.padding(vertical = 12.dp), color = Color(0xFF6750A4), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
    AsyncImage(model = url, contentDescription = "$name profile picture", modifier = Modifier.size(size.dp).clip(CircleShape), contentScale = ContentScale.Crop)
}
