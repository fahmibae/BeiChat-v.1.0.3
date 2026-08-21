package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekBluePrimary

data class EmojiCategory(
    val title: String,
    val icon: String,
    val emojis: List<String>
)

val EMOJI_CATEGORIES = listOf(
    EmojiCategory(
        title = "Ekspresi",
        icon = "😊",
        emojis = listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
            "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
            "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🥸",
            "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️",
            "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡",
            "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓",
            "🤗", "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄",
            "😯", "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵",
            "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🤑", "🤠",
            "😈", "👿", "👻", "💀", "☠️", "👽", "🤖", "🎃", "💩"
        )
    ),
    EmojiCategory(
        title = "Gestur",
        icon = "👍",
        emojis = listOf(
            "👍", "👎", "👊", "✊", "🤛", "🤜", "🤞", "✌️", "🤟", "🤘",
            "👌", "🤏", "👈", "👉", "👆", "👇", "☝️", "✋", "🤚", "🖐️",
            "🖖", "👋", "🤙", "💪", "🦾", "🖕", "✍️", "🙏", "🤝", "👏",
            "🙌", "👐", "🤲", "🦶", "🦵", "👂", "👃", "🧠", "🫀", "🫁",
            "👀", "👁️", "👅", "👄"
        )
    ),
    EmojiCategory(
        title = "Hati",
        icon = "❤️",
        emojis = listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
            "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "💌",
            "💋", "💯", "🔥", "✨", "🌟", "⭐", "💥", "💫", "💬", "💭"
        )
    ),
    EmojiCategory(
        title = "Makanan",
        icon = "🍕",
        emojis = listOf(
            "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
            "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑",
            "🥦", "🥬", "🥒", "🌶️", "🧄", "🧅", "🥔", "🍠", "🥐", "🍞",
            "🥖", "🧀", "🥚", "🍳", "🥞", "🥓", "🥩", "🍗", "🍖", "🌭",
            "🍔", "🍟", "🍕", "🥪", "🥙", "🌮", "🌯", "🥗", "🍲", "🍜",
            "🍝", "🍣", "🍱", "🍦", "🍧", "🍨", "🍩", "🍪", "🎂", "🍰",
            "🧁", "🍫", "🍬", "🍭", "☕", "🍵", "🧃", "🥤", "🧋", "🍺",
            "🍻", "🥂", "🍷", "🥃", "🍸", "🍹"
        )
    ),
    EmojiCategory(
        title = "Hewan",
        icon = "🐱",
        emojis = listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
            "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆",
            "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋",
            "🐌", "🐞", "🐜", "🦟", "🦗", "🕷️", "🦂", "🐢", "🐍", "🦎",
            "🐙", "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬", "🐳",
            "🦈", "🐊", "🐅", "🐆", "🦓", "🦍", "🦧", "🐘", "🦛", "🦏",
            "🐪", "🐫", "🦒", "🦘", "🐃", "🐂", "🐄", "🐎", "🐖", "🐏",
            "🐑", "🦙", "🐐", "🦌", "🐕", "🐩", "🐈", "🐓", "🦃", "🦚",
            "🦜", "🦢", "🦩", "🕊️", "🌲", "🌳", "🌴", "🌵", "🌾", "🌿",
            "🍀", "🍁", "🍂", "🍃", "🍄", "🌸", "🌺", "🌻", "🌼", "🌷"
        )
    ),
    EmojiCategory(
        title = "Aktivitas",
        icon = "🎉",
        emojis = listOf(
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
            "🏓", "🏸", "🥊", "🥋", "🛹", "⛸️", "🎯", "🏆", "🥇", "🥈",
            "🥉", "🏅", "🎖️", "🎮", "🕹️", "🎲", "🧩", "♟️", "🎭", "🎨",
            "🎬", "🎤", "🎧", "🎼", "🎹", "🥁", "🎷", "🎺", "🎸", "🎻",
            "🚗", "🚕", "🚙", "🚌", "🏎️", "🚓", "🚑", "🚒", "🛵", "🏍️",
            "🚲", "🛴", "🚨", "🚀", "🛸", "🚁", "✈️", "⛵", "💡", "📱",
            "💻", "🖥️", "📷", "🔍", "🔒", "🔑", "🎁", "🎈", "🎉", "🎊"
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerSheet(
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val currentEmojis = remember(selectedCategoryIndex, searchQuery) {
        if (searchQuery.isBlank()) {
            EMOJI_CATEGORIES[selectedCategoryIndex].emojis
        } else {
            EMOJI_CATEGORIES.flatMap { it.emojis }.distinct()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("emoji_picker_sheet")
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pilih Emoticon & Stiker",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF1F5F9) else MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Backspace Button
                    IconButton(
                        onClick = onBackspace,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Hapus Karakter Terakhir",
                            tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Category Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EMOJI_CATEGORIES.forEachIndexed { index, category ->
                    val isSelected = selectedCategoryIndex == index
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) SleekBluePrimary else if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                selectedCategoryIndex = index
                                searchQuery = ""
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = category.icon, fontSize = 16.sp)
                            Text(
                                text = category.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Emoji Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 44.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(currentEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 22.dp)
                            ) {
                                onEmojiSelected(emoji)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
