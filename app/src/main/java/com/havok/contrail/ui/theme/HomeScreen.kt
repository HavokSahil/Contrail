package com.havok.contrail.ui.theme

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.havok.contrail.R
import kotlin.math.*

@Composable
fun HomeScreen(
    onButtonClick: (() -> Boolean)? = null,
    onActionButtonClick: ((String) -> Unit)? = null,
    onJoystickMoved: ((Float, Float) -> Unit)? = null,
    logs: List<String>,
    onSaveClick: (ip: String, port: String) -> Unit
) {

    var socketIP by remember { mutableStateOf("10.38.3.118") }
    var socketPort by remember { mutableStateOf("4000") }
    var showSettings by remember { mutableStateOf(false) }

    fun onClickSettings() {
        showSettings = !showSettings
    }
    PopupBox(popupWidth = 400F, popupHeight = 300F, showPopup = showSettings) {
        SettingsOverlay(
            socketIP = socketIP,
            socketPort = socketPort,
            onDismiss = { showSettings = false },
            onSaveClick = onSaveClick
        )
    }

    Box(modifier = Modifier
        .background(Grey80)
        .fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                SettingsButton { onClickSettings() }
                AppTitle()
                ConnectButton(onButtonClick)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                JoyStick(
                    onMoved = { x, y ->
                        onJoystickMoved?.invoke(x, y)
                    }
                )
                StatusWidget(logs)
                ActionButtonContainer(onActionButtonClick)
            }
        }
    }
}



@Composable
fun ConnectButton(
    onButtonClick: (() -> Boolean)? = null,
) {
    var connected by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center
    ) {
        Button(
            enabled = true,
            onClick = {
                onButtonClick?.let {
                    connected = !it()
                }
            },
            modifier = Modifier
                .height(50.dp)
                .width(120.dp),
            shape = RoundedCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp
            ),
            colors = ButtonDefaults.buttonColors(
                contentColor = if (connected) Color.Red else Color.Green,
                containerColor = Color.Black
            )
        ) {
            Text(text = if (connected) "Disconnect" else "Connect")
        }
    }
}

@Composable
fun ActionButton(
    btnText: String,
    onButtonClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Button(
        onClick = {
            onButtonClick?.invoke()
            Log.d("Button", isPressed.toString())
        },
        interactionSource = interactionSource,
        enabled = true,
        modifier = Modifier
            .height(50.dp)
            .width(140.dp),
        shape = RoundedCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = if (isPressed) Color.Black else Color.hsl(36f, 0.98f, 0.48f),
            containerColor = if (isPressed) Color.hsl(36f, 0.98f, 0.48f) else Color.Black,
        ),
    ) {
        Text(
            text = btnText,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun SettingsButton(onSettingsClick: () -> Unit) {
    Button(
        onClick = { onSettingsClick() },
        shape = CircleShape,
        modifier = Modifier.padding(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        )
    ) {
        Image(
            painter = painterResource(id = R.drawable.settings_icon),
            contentDescription = "Settings Button",
            modifier = Modifier.size(30.dp)
        )
    }
}


@Composable
fun AppTitle() {
    Box(modifier = Modifier
        .height(50.dp)
        .width(300.dp)
        .clip(
            CircleShape
        )) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Contrail", color= Orange80, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(text = "by Team Phoenix", color = Color.White, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun JoyStick(
    modifier: Modifier = Modifier,
    size: Dp = 170.dp,
    dotSize: Dp = 40.dp,
    backgroundImage: Int = R.drawable.joy_stick_background,
    dotImage: Int = R.mipmap.phoenix_logo,
    onMoved: (Float, Float) -> Unit = { _, _ -> }
) {
    Box(
        modifier = modifier.size(size)
    ) {
        val maxRadius = with(LocalDensity.current) { (size / 2).toPx() }
        val centerX = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }
        val centerY = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }

        var offsetX by remember { mutableStateOf(centerX) }
        var offsetY by remember { mutableStateOf(centerY) }

        var radius by remember { mutableStateOf(0f) }

        Box(
            modifier = Modifier
                .size(size)
                .onGloballyPositioned {
                    radius = it.size.width.toFloat() / 2f
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            offsetX = centerX
                            offsetY = centerY
                            onMoved(0f, 0f)
                        },
                        onDrag = { change: PointerInputChange, _: Offset ->
                            val (x, y) = change.position
                            val distance = sqrt((x - radius).pow(2) + (y - radius).pow(2))

                            if (distance < maxRadius) {
                                offsetX = x - (dotSize.toPx() / 2)
                                offsetY = y - (dotSize.toPx() / 2)
                            } else {
                                val ratio = maxRadius / distance
                                val constrainedX = (x - radius) * ratio + radius
                                val constrainedY = (y - radius) * ratio + radius
                                offsetX = constrainedX - (dotSize.toPx() / 2)
                                offsetY = constrainedY - (dotSize.toPx() / 2)
                            }

                            val xPercent = ((offsetX - centerX) / centerX).coerceIn(-1f, 1f)
                            val yPercent = ((offsetY - centerY) / centerY).coerceIn(-1f, 1f)

                            onMoved(xPercent, yPercent)
                        }
                    )
                }
        ) {
            Image(
                painter = painterResource(backgroundImage),
                contentDescription = "Joystick Background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
            Image(
                painter = painterResource(dotImage),
                contentDescription = "Joystick Dot",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(dotSize)
                    .offset {
                        IntOffset(
                            offsetX.roundToInt(),
                            offsetY.roundToInt()
                        )
                    }
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun StatusWidget(
    logs: List<String>
) {
    Box(modifier = Modifier
        .clip(RoundedCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
        .height(200.dp)
        .width(280.dp)
        .background(Color.Black)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(15.dp)
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Commands",
                    color = Color.Red,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                logs.forEach { log ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = log,
                            color = Color.Green,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PopupBox(
    popupWidth: Float,
    popupHeight: Float,
    showPopup: Boolean,
    content: @Composable () -> Unit
) {

    if (showPopup) {
        // full screen background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10F),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .width(popupWidth.dp)
                    .height(popupHeight.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsOverlay(
    socketIP: String,
    socketPort: String,
    onDismiss: () -> Unit,
    onSaveClick: (ip: String, port: String) -> Unit
) {

    var currentIp by remember {
        mutableStateOf(socketIP)
    }
    var currentPort by remember {
        mutableStateOf(socketPort)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Grey80)
            .padding(10.dp),
        contentAlignment = Alignment.Center,

    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.Black),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Orange80,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = currentIp,
                onValueChange = { currentIp = it },
                label = { Text("Socket IP", color = Color.White, fontFamily = FontFamily.Monospace) },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Black,
                    textColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = currentPort,
                onValueChange = { currentPort = it },
                label = { Text("Socket Port", color = Color.White, fontFamily = FontFamily.Monospace) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Black,
                    textColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onSaveClick(currentIp, currentPort); onDismiss() },
                    modifier = Modifier.padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Orange80
                    )
                ) {
                    Text("Save", color = Orange80)
                }
            }
        }
    }
}



@Composable
fun ActionButtonContainer(onActionButtonClick: ((String) -> Unit)?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .height(200.dp)
            .width(320.dp)
            .padding(15.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ActionButton(btnText = "PICK", onButtonClick = { onActionButtonClick?.invoke("PICK") })
            ActionButton(btnText = "DROP", onButtonClick = { onActionButtonClick?.invoke("DROP") })
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ActionButton(btnText = "LOWER", onButtonClick = { onActionButtonClick?.invoke("LOWER") })
            ActionButton(btnText = "RAISE", onButtonClick = { onActionButtonClick?.invoke("RAISE") })
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ActionButton(btnText = "STOP", onButtonClick = { onActionButtonClick?.invoke("STOP") })
            ActionButton(btnText = "SHOOT", onButtonClick = { onActionButtonClick?.invoke("SHOOT") })
        }
    }
}
