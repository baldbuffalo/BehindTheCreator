package com.baldbuffalo.behindthecreator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

private data class CreatorEntry(val channelUrl:String,val category:String,val story:String,val facts:List<String>,val timeline:List<String>)
private data class Channel(val name:String="Unknown creator",val handle:String="",val image:String="",val subscribers:String="—",val joined:String="—")
private data class Creator(val entry:CreatorEntry,val channel:Channel)

class MainActivity:ComponentActivity(){
    override fun onCreate(state:Bundle?){super.onCreate(state);setContent{App()}}
}

@Composable private fun App(){
    val context=LocalContext.current
    var creators by remember{mutableStateOf<List<Creator>>(emptyList())}
    var selected by remember{mutableStateOf<Creator?>(null)}
    var loading by remember{mutableStateOf(true)}
    LaunchedEffect(Unit){creators=loadEntries(context).map{Creator(it,fetchChannel(it.channelUrl))};loading=false}
    MaterialTheme{Surface(Modifier.fillMaxSize(),color=Color(0xFFF8F7FA)){when{selected!=null->Detail(selected!!){selected=null};loading->Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){CircularProgressIndicator()};else->Home(creators){selected=it}}}}
}

private fun loadEntries(context:android.content.Context):List<CreatorEntry>{
    val root=JSONObject(context.assets.open("creators.json").bufferedReader().use{it.readText()});val a=root.getJSONArray("creators")
    return List(a.length()){i->val x=a.getJSONObject(i);CreatorEntry(x.getString("channelUrl"),x.optString("category","Other"),x.optString("story",""),x.optJSONArray("facts")?.let{List(it.length()){n->it.getString(n)}}?:emptyList(),x.optJSONArray("timeline")?.let{List(it.length()){n->it.getString(n)}}?:emptyList())}
}

private fun fetchChannel(url:String):Channel=runCatching{if(BuildConfig.YOUTUBE_API_KEY.isNotBlank())fetchApi(url)else fetchOEmbed(url)}.getOrElse{fetchOEmbed(url)}

private fun fetchApi(url:String):Channel{
    val uri=Uri.parse(url);val parts=uri.pathSegments;val handle=parts.lastOrNull{it.startsWith("@")};val id=if(parts.size>=2&&parts[parts.size-2]=="channel")parts.last()else null
    val selector=when{handle!=null->"forHandle=${enc(handle)}";id!=null->"id=${enc(id)}";else->error("Unsupported channel URL")}
    val endpoint="https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&$selector&key=${enc(BuildConfig.YOUTUBE_API_KEY)}"
    val item=JSONObject(get(endpoint)).getJSONArray("items").getJSONObject(0);val s=item.getJSONObject("snippet");val stats=item.getJSONObject("statistics")
    return Channel(s.optString("title"),s.optString("customUrl",handle ?: ""),s.getJSONObject("thumbnails").getJSONObject("high").getString("url"),compact(stats.optString("subscriberCount","—")),date(s.optString("publishedAt","")))
}

private fun fetchOEmbed(url:String):Channel{val x=JSONObject(get("https://www.youtube.com/oembed?url=${enc(url)}&format=json"));return Channel(x.optString("author_name",x.optString("title","Unknown creator")),Uri.parse(url).pathSegments.lastOrNull() ?: "",x.optString("thumbnail_url",""))}
private fun enc(s:String)=URLEncoder.encode(s,"UTF-8")
private fun get(url:String):String{val c=URL(url).openConnection() as HttpURLConnection;c.connectTimeout=10000;c.readTimeout=10000;return try{c.inputStream.bufferedReader().use{it.readText()}}finally{c.disconnect()}}
private fun compact(v:String):String{val n=v.toLongOrNull()?:return v;return when{n>=1e9->"%.1fB".format(Locale.US,n/1e9);n>=1e6->"%.1fM".format(Locale.US,n/1e6);n>=1e3->"%.1fK".format(Locale.US,n/1e3);else->"%,d".format(Locale.US,n)}}
private fun date(v:String):String=runCatching{SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).parse(v)?.let{SimpleDateFormat("MMM d, yyyy",Locale.US).format(it)}?:"—"}.getOrDefault("—")

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Home(creators:List<Creator>,open:(Creator)->Unit){
    var q by remember{mutableStateOf("")};var cat by remember{mutableStateOf("All")};val cats=listOf("All")+creators.map{it.entry.category}.distinct().sorted()
    val filtered=creators.filter{(q.isBlank()||it.channel.name.contains(q,true)||it.channel.handle.contains(q,true))&&(cat=="All"||it.entry.category==cat)}
    Scaffold(topBar={TopAppBar(title={Column{Text("BehindTheCreator",fontWeight=FontWeight.Bold);Text("The stories behind the channels",fontSize=12.sp,color=Color.Gray)}})}){p->LazyColumn(Modifier.padding(p).padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{OutlinedTextField(q,{q=it},Modifier.fillMaxWidth(),singleLine=true,label={Text("Search creators")},placeholder={Text("MrBeast, MKBHD...")},shape=RoundedCornerShape(16.dp));Spacer(Modifier.height(10.dp));LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){items(cats){c->AssistChip(onClick={cat=c},label={Text(c)})}}}
        if(filtered.isEmpty())item{Text("No creators found.",Modifier.padding(16.dp),color=Color.Gray)}else items(filtered){CreatorCard(it,open)}
    }}
}

@Composable private fun CreatorCard(c:Creator,open:(Creator)->Unit){Card(Modifier.fillMaxWidth().clickable{open(c)},shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(Color.White)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Image(c.channel.image,c.channel.name,64);Column(Modifier.padding(start=14.dp)){Text(c.channel.name,fontSize=18.sp,fontWeight=FontWeight.Bold);Text(c.channel.handle,color=Color.Gray);Text(c.entry.category,fontSize=12.sp,color=Color(0xFF6750A4));Text("${c.channel.subscribers} subscribers",fontSize=12.sp,color=Color.Gray)}}}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Detail(c:Creator,back:()->Unit){
    val context=LocalContext.current
    Scaffold(topBar={TopAppBar(title={Text(c.channel.name,fontWeight=FontWeight.Bold)},navigationIcon={IconButton(back){Icon(Icons.Default.ArrowBack,"Back")}})}){p->LazyColumn(Modifier.padding(p).padding(horizontal=20.dp)){
        item{Spacer(Modifier.height(8.dp));Row(verticalAlignment=Alignment.CenterVertically){Image(c.channel.image,c.channel.name,88);Column(Modifier.padding(start=16.dp)){Text(c.channel.name,fontSize=26.sp,fontWeight=FontWeight.Bold);Text(c.channel.handle,color=Color.Gray);Text(c.entry.category,color=Color(0xFF6750A4))}}
            Spacer(Modifier.height(16.dp));Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Stat("Subscribers",c.channel.subscribers);Stat("Joined",c.channel.joined)}
            Spacer(Modifier.height(24.dp));Title("The story");Text(c.entry.story,fontSize=16.sp,lineHeight=25.sp);Spacer(Modifier.height(24.dp));Title("Things you might not know");Spacer(Modifier.height(8.dp))}
        items(c.entry.facts){fact->Card(Modifier.fillMaxWidth().padding(bottom=10.dp),shape=RoundedCornerShape(14.dp),colors=CardDefaults.cardColors(Color.White)){Text(fact,Modifier.padding(16.dp),fontSize=15.sp,lineHeight=22.sp)}}
        item{Spacer(Modifier.height(14.dp));Title("Creator timeline")}
        items(c.entry.timeline){event->Row(Modifier.fillMaxWidth().padding(vertical=7.dp)){Box(Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF6750A4)));Text(event,Modifier.padding(start=12.dp),fontSize=15.sp)}}
        item{Spacer(Modifier.height(18.dp));Title("YouTube");Text("Open the original channel.",fontSize=13.sp,color=Color.Gray);Text("YouTube channel",Modifier.fillMaxWidth().clickable{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(c.entry.channelUrl)))}.padding(vertical=12.dp),color=Color(0xFF6750A4),fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(24.dp))}
    }}
}

@Composable private fun RowScope.Stat(title:String,value:String){Card(Modifier.weight(1f),shape=RoundedCornerShape(14.dp),colors=CardDefaults.cardColors(Color.White)){Column(Modifier.padding(14.dp)){Text(title,fontSize=12.sp,color=Color.Gray);Spacer(Modifier.height(4.dp));Text(value,fontWeight=FontWeight.Bold)}}}
@Composable private fun Title(t:String){Text(t,fontSize=20.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp))}
@Composable private fun Image(url:String,name:String,size:Int){AsyncImage(model=url,contentDescription="$name profile picture",modifier=Modifier.size(size.dp).clip(CircleShape),contentScale=ContentScale.Crop)}
