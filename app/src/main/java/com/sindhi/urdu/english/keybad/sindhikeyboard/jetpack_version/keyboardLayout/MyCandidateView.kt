package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardLayout

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.SuggestionItems
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.LayoutState
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.MyPreferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.service.CustomImeService
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.getSelectedLanguage
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.openSettings
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.openThemes
import java.util.Locale
//
@Composable
fun MyCandidateView(
    context: Context? = null,
    imeService: CustomImeService? = null,
    onEditTextSwitchClick: () -> Unit,
    onHusnEguftarSwitchClick: () -> Unit,
    onEmojiSwitchClick: () -> Unit,
    onStickerButtonClick: () -> Unit,
    onEmojiButtonClick: () -> Unit,
    onBackClick: () -> Unit,
    onDragGestureToSelect: (Int) -> Unit,
    deleteTextAfterDrag: () -> Unit,
    onLongKeyPressed: (isEmoji: Boolean) -> Unit,
    onLongKeyPressedEnd: () -> Unit,
    onSuggestedWordSelected: () -> Unit,
    onDelete: () -> Unit,
    currentLayout: LayoutState,
    myPreferences: MyPreferences?,
    layoutLabel: String,
    onChangeLanguageBtnClick: (languageChangedFromTop: String) -> Unit,
    onVoiceInputClicked: (isClicked: Boolean) -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    isShowingSuggestions: Boolean = false,
    isVoiceInputClicked: Boolean = false,
    currentLanguage:String
) {
    val interactionSource = remember { MutableInteractionSource() }


    if (currentLayout == LayoutState.TextEdit || currentLayout == LayoutState.Emojis || currentLayout == LayoutState.HusnEguftar) {
        Row(
            modifier = Modifier
                .padding(start = 4.dp)
                .fillMaxWidth()
                .height(42.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = { onBackClick() }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = if (currentLayout == LayoutState.Emojis) R.drawable.ic_keyboard else R.drawable.back),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (currentLayout == LayoutState.Emojis) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                Log.i("CheckWarranty", "CustomCandidateView: Emoji")
                                onEmojiButtonClick.invoke()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_keypad_emoji),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                Log.i("CheckWarranty", "CustomCandidateView: Stickers")
                                onStickerButtonClick.invoke()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_keypad_sticker),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Text(
                        text = layoutLabel,
                        color = textColor,
                        fontSize = 21.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (currentLayout == LayoutState.Emojis) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onDelete() },
                                onLongPress = { onLongKeyPressed(true) },
                                onPress = { offset ->
                                    val press = PressInteraction.Press(offset)
                                    interactionSource.emit(press)
                                    tryAwaitRelease()
                                    onLongKeyPressedEnd()
                                    interactionSource.emit(PressInteraction.Release(press))
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = { deleteTextAfterDrag() }
                            ) { change, _ ->
                                onDragGestureToSelect(change.position.x.toInt())
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_backspace),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
    else if (isShowingSuggestions) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
            /*.padding(top = 4.dp)*/,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround) {
            IconButton(
                onClick = { onBackClick() },
                modifier = Modifier) {
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            SuggestionRow(
                items = imeService?.filterList?.value ?: emptyList(),
                context = context,
                onItemClick = { clickedItem ->
                    var selectedSuggestion = ""
                    selectedSuggestion = if (getSelectedLanguage(context!!).equals("English")) {
                        clickedItem.engRomanWordsSuggestion + " "
                    } else {
                        clickedItem.urduWordsSuggestion + " "
                    }

                    val wordToBeReplaced = imeService?.currentWordMain
                    val lengthForReplacement = wordToBeReplaced?.length
                    if (lengthForReplacement != null) {
                        if (lengthForReplacement > 0) {
                            imeService.currentInputConnection?.deleteSurroundingText(lengthForReplacement, 0)
                        }
                    }
                    val isCapitalized = wordToBeReplaced?.takeIf { it.isNotEmpty() }?.get(0)?.isUpperCase() ?: false
                    val finalSuggestion = if (isCapitalized) {
                        selectedSuggestion.capitalize(Locale.ROOT)
                    } else {
                        selectedSuggestion
                    }
                    imeService?.currentInputConnection?.commitText(finalSuggestion, 1)
                    imeService?.currentWordMain = ""
                }
            )
        }
    }
    else if (isVoiceInputClicked) {
        Log.e("SpeechToText", "isVoiceInputClicked: $isVoiceInputClicked")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround) {
            IconButton(
                onClick = {
                    /*imeService?.stopVoiceInput()*/
                    onVoiceInputClicked.invoke(false)
                },
                modifier = Modifier
            ) {
                Box(modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(100))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }
            var myText by remember { mutableStateOf("Ready") }
            Column(
                modifier = Modifier
                    .weight(4f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(modifier = Modifier
                    .padding(end = 10.dp),
                    text = myText,
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily(Font(R.font.lexend_medium))
                    ))

                imeService?.speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}

                    override fun onBeginningOfSpeech() {
                        myText = "Listening..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        myText = "Ended..."
                    }

                    override fun onError(error: Int) {
                        onVoiceInputClicked.invoke(false)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.let {
                            Log.e("SpeechToText", "onResults")
                            val currentInputConnection = imeService.currentInputConnection
                            currentInputConnection?.commitText(it[0]+" ", 1)
                            /*imeService.stopVoiceInput()*/
                            onVoiceInputClicked.invoke(false)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    } else {
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround) {
            IconButton(
                onClick = { onEmojiSwitchClick() },
                modifier = Modifier
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_emoji_new),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = {
                    openSettings(
                        context = context,
                    )
                },
                modifier = Modifier
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings_key),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }



            // In MyCandidateView composable, use the currentLanguage parameter instead of getSelectedLanguage(context)
            if (currentLanguage == "English" || currentLanguage == "Sindhi") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .width(60.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .clickable(true, onClick = {
                            onSuggestedWordSelected()
                            onChangeLanguageBtnClick("Urdu")
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "اردو",
                        color = textColor,
                        fontSize = 20.sp,
                        maxLines = 1
                    )
                }
            }

            if (currentLanguage != "English" && currentLanguage != "Sindhi") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .clickable(true, onClick = {
                            onHusnEguftarSwitchClick()
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "  حُسن گفتار  ",
                        color = textColor,
                        fontSize = 14.sp
                    )
                }
            }

            if ((myPreferences != null) && myPreferences.getVoiceTyping()) {
                IconButton(
                    onClick = {
                        imeService!!.startVoiceInput()
                        onVoiceInputClicked.invoke(true)
                    },
                    modifier = Modifier
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mike_key),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp))
                }
            }

            IconButton(
                onClick = {
                    openThemes(
                        context = context,
                    )
                },
                modifier = Modifier
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_themes_key),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SuggestionRow(
    items: List<SuggestionItems>,
    context: Context?,
    onItemClick: (SuggestionItems) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        itemsIndexed(items) { index, item ->
            Row(modifier = Modifier
                .padding(horizontal = 2.dp, vertical = 6.dp)
                .widthIn(min = 100.dp)
                .fillMaxWidth()) {

                var wordSuggestion = ""
                if (getSelectedLanguage(context!!) == "English") {
                    wordSuggestion = item.engRomanWordsSuggestion ?: ""
                } else {
                    wordSuggestion = item.urduWordsSuggestion ?: ""
                }

                Text(
                    text = wordSuggestion,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable { onItemClick(item) }
                        .fillMaxWidth())

                if (index < items.size - 1) {
                    Divider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .padding(start = 2.dp, end = 2.dp),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

