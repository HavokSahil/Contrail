package com.havok.contrail.ui.theme

import android.util.Log
import androidx.annotation.DrawableRes
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.havok.contrail.R
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun HomeScreen(
    onButtonClick: (() -> Boolean)? = null,
    onActionButtonClick: ((String) -> Unit)? = null,
    onJoystickMoved: ((Float, Float) -> Unit)? = null,
    onRotStickMoved: ((Float) -> Unit)? = null,
    logs: List<String>,
    onSaveClick: (ip: String, port: String, fl: Float, fr: Float, br: Float, bl: Float) -> Unit,
    ip: String = "10.38.3.118",
    port: String = "4000",
    motorSpeedCoefficient: FloatArray
) {

    var socketIP by remember { mutableStateOf(ip) }
    var socketPort by remember { mutableStateOf(port) }
    var showSettings by remember { mutableStateOf(false) }
    var curMotorSpeedCoefficient by remember {
        mutableStateOf(motorSpeedCoefficient)
    }

    fun onClickSettings() {
        showSettings = !showSettings
    }
    fun onClickSave(ip: String, port: String, fl: Float, fr: Float, br: Float, bl: Float) {
        socketIP = ip
        socketPort = port
        curMotorSpeedCoefficient[2] = fl
        curMotorSpeedCoefficient[3] = fr
        curMotorSpeedCoefficient[0] = br
        curMotorSpeedCoefficient[1] = bl
    }

    PopupBox(popupWidth = 400F, popupHeight = 360F, showPopup = showSettings) {
        SettingsOverlay(
            socketIP = socketIP,
            socketPort = socketPort,
            onDismiss = { showSettings = false },
            onSaveClick = onSaveClick,
            onClickSave = ::onClickSave,
            motorSpeedCoefficient = motorSpeedCoefficient
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Grey80)) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =  Arrangement.SpaceEvenly
        ) {
            Column (
                modifier = Modifier
                    .fillMaxHeight()
                    .width(400.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row (modifier = Modifier
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                    ) {
                    SettingsButton { onClickSettings() }
                    ConnectButton(onButtonClick)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    StatusWidget(logs = logs)
                }

                Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                ) {
                    JoyStick(
                        onMoved = { x, y ->
                            onJoystickMoved?.invoke(x, -y)
                        }
                    )
                    FineDriveContainer(onActionButtonClick)
                }
            }

            Column (modifier = Modifier
                .fillMaxHeight()
                .width(400.dp)
                .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppTitle()
                }
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ActionButtonColumnLeft(onActionButtonClick)
                    Spacer(modifier = Modifier.width(10.dp))
                    ActionButtonContainer(onActionButtonClick)
                    Spacer(modifier = Modifier.width(10.dp))
                    ActionButtonColumnRight(onActionButtonClick)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    DirectionButton({onActionButtonClick?.invoke("<")}, R.drawable.rotate_left_icon, {onActionButtonClick?.invoke("x")})
                    Spacer(modifier = Modifier.width(10.dp))
                    HorizontalJoyStick(
                        onMoved = { x ->
                            onRotStickMoved?.invoke(x)
                        }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    DirectionButton({onActionButtonClick?.invoke(">")}, R.drawable.rotate_right_icon, {onActionButtonClick?.invoke("x")})
                }
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
    onButtonClick: (() -> Unit)? = null,
    secondaryColor: Color
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
            .height(60.dp)
            .width(60.dp),
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = if (isPressed) Color.Black else secondaryColor,
            containerColor = if (isPressed) secondaryColor else Color.Black,
        ),
        contentPadding = PaddingValues(10.dp)
    ) {
        Text(
            text = btnText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
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
        .width(280.dp)
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
    size: Dp = 140.dp,
    dotSize: Dp = 30.dp,
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
fun HorizontalJoyStick(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    dotSize: Dp = 30.dp,
    backgroundImage: Int = R.drawable.rect_background,
    dotImage: Int = R.mipmap.phoenix_logo,
    onMoved: (Float) -> Unit = { _ -> }
) {
    Box(
        modifier = modifier
            .width(size)
            .height(50.dp)

    ) {
        val maxRadius = with(LocalDensity.current) { (size / 2).toPx() }
        val centerX = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }
        val centerY = with(LocalDensity.current) { ((50.dp - dotSize) / 2).toPx() }

        var offsetX by remember { mutableStateOf(centerX) }
        var offsetY by remember { mutableStateOf(centerY) }

        var radius by remember { mutableStateOf(0f) }

        Box(
            modifier = Modifier
                .height(50.dp)
                .width(size)
                .onGloballyPositioned {
                    radius = it.size.width.toFloat() / 2f
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            offsetX = centerX
                            offsetY = centerY
                            onMoved(0f)
                        },
                        onDrag = { change: PointerInputChange, _: Offset ->
                            val x = change.position.x
                            val distance = abs(x - radius)

                            offsetX = if (distance < maxRadius) {
                                x - (dotSize.toPx() / 2)
                            } else {
                                val ratio = maxRadius / distance
                                val constrainedX = (x - radius) * ratio + radius
                                constrainedX - (dotSize.toPx() / 2)
                            }

                            val xPercent = ((offsetX - centerX) / centerX).coerceIn(-1f, 1f)

                            onMoved(xPercent)
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
                            centerY.roundToInt()  // Keep y-coordinate fixed
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
        .clip(
            RoundedCornerShape(
                topStart = 10.dp,
                bottomEnd = 10.dp,
                topEnd = 10.dp,
                bottomStart = 10.dp
            )
        )
        .height(100.dp)
        .width(360.dp)
        .background(Color.Black)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
                ) {
                Text(
                    text = "Terminal",
                    color = Color.Green,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
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
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = log,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
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
    motorSpeedCoefficient: FloatArray,
    onDismiss: () -> Unit,
    onSaveClick: (ip: String, port: String, fl: Float, fr: Float, br: Float, bl: Float) -> Unit,
    onClickSave: (ip: String, port: String, fl: Float, fr: Float, br: Float, bl: Float) ->Unit,
) {
    var currentIp by remember {
        mutableStateOf(socketIP)
    }
    var currentPort by remember {
        mutableStateOf(socketPort)
    }
    var flCoefficient by remember {
        mutableStateOf(motorSpeedCoefficient[2])
    }
    var frCoefficient by remember {
        mutableStateOf(motorSpeedCoefficient[3])
    }
    var brCoefficient by remember {
        mutableStateOf(motorSpeedCoefficient[0])
    }
    var blCoefficient by remember {
        mutableStateOf(motorSpeedCoefficient[1])
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
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "FL", color = Color.White, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = flCoefficient,
                        onValueChange = { flCoefficient = it },
                        valueRange = 0f..4f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Orange80
                        ),
                        modifier = Modifier.width(80.dp)
                    )
                    Text(text = String.format("%.2f", flCoefficient), color = Color.White, fontFamily = FontFamily.Monospace)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "FR", color = Color.White, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = frCoefficient,
                        onValueChange = { frCoefficient = it },
                        valueRange = 0f..4f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Orange80
                        ),
                        modifier = Modifier.width(80.dp)
                    )
                    Text(text = String.format("%.2f", frCoefficient), color = Color.White, fontFamily = FontFamily.Monospace)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "BR", color = Color.White, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = brCoefficient,
                        onValueChange = { brCoefficient = it },
                        valueRange = 0f..4f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Orange80
                        ),
                        modifier = Modifier.width(80.dp)
                    )
                    Text(text = String.format("%.2f", brCoefficient), color = Color.White, fontFamily = FontFamily.Monospace)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "BL", color = Color.White, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = blCoefficient,
                        onValueChange = { blCoefficient = it },
                        valueRange = 0f..4f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Orange80
                        ),
                        modifier = Modifier.width(80.dp)
                    )
                    Text(text = String.format("%.2f", blCoefficient), color = Color.White, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        onSaveClick(currentIp, currentPort, flCoefficient, frCoefficient, brCoefficient, blCoefficient)
                        onDismiss()
                        onClickSave(currentIp, currentPort, flCoefficient, frCoefficient, brCoefficient, blCoefficient)
                    },
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
fun FineDriveContainer(onActionButtonClick: ((String) -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
       Row(
           horizontalArrangement = Arrangement.Center,
           verticalAlignment = Alignment.CenterVertically
       ) {
           DirectionButton({onActionButtonClick?.invoke("w")} ,id = R.drawable.up_arrow, {onActionButtonClick?.invoke("x")})
       }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DirectionButton({onActionButtonClick?.invoke("a")} ,id = R.drawable.left_arrow, {onActionButtonClick?.invoke("x")})
            Spacer(modifier = Modifier.width(5.dp))
            DirectionButton({onActionButtonClick?.invoke("x")} ,id = R.drawable.circle, {onActionButtonClick?.invoke("x")})
            Spacer(modifier = Modifier.width(5.dp))
            DirectionButton({onActionButtonClick?.invoke("z")} ,id = R.drawable.right_arrow, {onActionButtonClick?.invoke("x")})

        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DirectionButton({onActionButtonClick?.invoke("q")} ,id = R.drawable.down_arrow, {onActionButtonClick?.invoke("x")})
        }
    }
}

@Composable
fun ActionButtonContainer(onActionButtonClick: ((String) -> Unit)?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .height(220.dp)
            .width(220.dp)
            .padding(15.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ActionButton(btnText = "U", onButtonClick = { onActionButtonClick?.invoke("u") }, secondaryColor = Color.Yellow)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ActionButton(btnText = "P", onButtonClick = { onActionButtonClick?.invoke("g") }, secondaryColor = Color.Blue)
            ActionButton(btnText = "S", onButtonClick = { onActionButtonClick?.invoke("r") }, secondaryColor = Color.White)
            ActionButton(btnText = "X", onButtonClick = { onActionButtonClick?.invoke("l") }, secondaryColor = Color.Red)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ActionButton(btnText = "D", onButtonClick = { onActionButtonClick?.invoke("d") }, secondaryColor = Color.Green)
        }
    }
}

@Composable
fun ActionButtonColumnRight(onActionButtonClick: ((String) -> Unit)?) {
    Column (
        modifier = Modifier
            .height(220.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ActionButton(btnText = "G", onButtonClick = { onActionButtonClick?.invoke("p") }, secondaryColor = ShinyPink)
        Spacer(modifier = Modifier.height(10.dp))
        ActionButton(btnText = "L", onButtonClick = { onActionButtonClick?.invoke("o") }, secondaryColor = Cyan)
    }
}

@Composable
fun ActionButtonColumnLeft(onActionButtonClick: ((String) -> Unit)?) {
    Column (
        modifier = Modifier
            .height(220.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ActionButton(btnText = "M", onButtonClick = { onActionButtonClick?.invoke("h") }, secondaryColor = Purple40)
        Spacer(modifier = Modifier.height(10.dp))
        ActionButton(btnText = "N", onButtonClick = { onActionButtonClick?.invoke("k") }, secondaryColor = Pink40)
    }
}

@Composable
fun DirectionButton(
    onButtonClick: (() -> Unit)? = null,
    @DrawableRes id: Int,
    onStop: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        while (isPressed) {
            onButtonClick?.invoke()
            delay(100) // Adjust delay as needed for desired repetition speed
        }
        onStop?.invoke()
    }

    Button(
        onClick = { /* Do nothing here */ },
        interactionSource = interactionSource,
        enabled = true,
        modifier = Modifier
            .height(50.dp)
            .width(50.dp),
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = if (isPressed) Color.Black else Orange80,
            containerColor = if (isPressed) Orange80 else Color.Black,
        ),
        contentPadding = PaddingValues(15.dp)
    ) {
        Image(
            painter = painterResource(id),
            contentDescription = "Direction Button",
            contentScale = ContentScale.Inside,
            modifier = Modifier.fillMaxSize()
        )
    }
}
