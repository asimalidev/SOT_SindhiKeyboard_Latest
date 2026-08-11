package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sindhi.urdu.english.keybad.R

class PermissionDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Dialog(onDismissRequest = { finish() }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Light blue circular background with microphone icon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFD3E3FD), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mike_new),
                                contentDescription = "Microphone",
                                tint = Color(0xFFD20024),
                                modifier = Modifier.size(35.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Microphone Permission",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "We need access to your microphone.\nThis lets you speak text.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { finish() }) {
                                Text(
                                    text = "Deny",
                                    color = Color.Black,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            TextButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", packageName, null)
                                    }
                                    startActivity(intent)
                                    finish()
                                }
                            ) {
                                Text(
                                    text = "Open Settings",
                                    color = Color(0xFFD20024),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}