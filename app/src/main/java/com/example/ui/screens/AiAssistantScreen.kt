package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AiStudioAssistant
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AiAssistantScreen(
    aiStudioAssistant: AiStudioAssistant
) {
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "ai",
                text = "أهلاً بك في المساعد الذكي لاستوديو فخري للقرآن ألترا 🎙️✨\nأنا هنا لمساعدتك في اقتراح أفضل العناوين والوسوم وتناغم الخطوط والألوان الإسلامية لفيديوهاتك."
            )
        )
    }

    val quickPrompts = listOf(
        "اقترح عناوين جذابة لسورة الفاتحة على يوتيوب",
        "أفضل وسوم (Hashtags) للتلاوات الخاشعة",
        "تناغم الخطوط المثالي للآيات والترجمة",
        "لوحة ألوان إسلامية فخمة وهادئة"
    )

    fun sendPrompt(prompt: String) {
        if (prompt.isBlank() || isLoading) return
        messages.add(ChatMessage(sender = "user", text = prompt))
        inputText = ""
        isLoading = true

        scope.launch {
            val reply = aiStudioAssistant.askAssistant(prompt)
            messages.add(ChatMessage(sender = "ai", text = reply))
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(IslamicGoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = IslamicEmeraldDark,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "المساعد الذكي للاستوديو",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicCream,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اقتراح الثيمات البصرية • تناغم الخطوط • أفكار العناوين والوسوم",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight
                )
            }
        }

        // Quick suggestions row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { p ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainerHighDark)
                        .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { sendPrompt(p) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = p, color = IslamicGoldLight, fontSize = 11.5.sp)
                }
            }
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                val isAi = msg.sender == "ai"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAi) SurfaceContainerDark else IslamicGoldPrimary
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isAi) 4.dp else 16.dp,
                            bottomEnd = if (isAi) 16.dp else 4.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .border(
                                1.dp,
                                if (isAi) IslamicEmeraldLight.copy(alpha = 0.4f) else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = msg.text,
                                color = if (isAi) IslamicCream else IslamicEmeraldDark,
                                fontSize = 13.5.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(color = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "جاري صياغة التوصيات الذكية...", color = IslamicGoldLight, fontSize = 12.sp)
                    }
                }
            }
        }

        // Input Box
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("اكتب سؤالك أو اطلب اقتراح عنوان/ثيم...", color = IslamicCream.copy(alpha = 0.5f), fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { sendPrompt(inputText) },
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) IslamicGoldPrimary else SurfaceContainerHighDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "إرسال",
                        tint = if (inputText.isNotBlank()) IslamicEmeraldDark else IslamicCream.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
