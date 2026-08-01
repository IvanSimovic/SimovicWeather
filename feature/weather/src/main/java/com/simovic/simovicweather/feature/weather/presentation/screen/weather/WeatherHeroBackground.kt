package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

@Composable
internal fun WeatherHeroBackground(
    artwork: WeatherHeroArtwork,
    modifier: Modifier = Modifier,
) {
    val isInspectionMode = LocalInspectionMode.current
    var isVisible by remember { mutableStateOf(true) }

    Box(
        modifier =
            modifier
                .background(
                    Brush.verticalGradient(
                        listOf(AppTheme.colors.backgroundStart, AppTheme.colors.backgroundEnd),
                    ),
                ).onVisibilityChanged(minFractionVisible = MIN_VISIBLE_FRACTION) { isVisible = it },
    ) {
        if (isInspectionMode) {
            Image(
                painter = painterResource(artwork.scene.frameResources.first()),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            val readyFrames = artwork.framesState as? WeatherFramesState.Ready
            if (readyFrames != null) {
                AnimatedWeatherFrame(
                    frames = readyFrames.frames,
                    isVisible = isVisible,
                    animationsEnabled = artwork.animationsEnabled,
                )
            }
        }
    }
}

@Composable
internal fun rememberWeatherHeroArtwork(scene: WeatherScene): WeatherHeroArtwork {
    val context = LocalContext.current
    val isInspectionMode = LocalInspectionMode.current
    var animationsEnabled by remember { mutableStateOf(context.areSystemAnimationsEnabled()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        animationsEnabled = context.areSystemAnimationsEnabled()
    }
    val framesState =
        if (isInspectionMode) {
            WeatherFramesState.Loading
        } else {
            rememberWeatherFrames(scene, animationsEnabled)
        }
    return WeatherHeroArtwork(
        scene = scene,
        animationsEnabled = animationsEnabled,
        framesState = framesState,
    )
}

@Composable
private fun AnimatedWeatherFrame(
    frames: List<ImageBitmap>,
    isVisible: Boolean,
    animationsEnabled: Boolean,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var frameIndex by remember(frames) { mutableIntStateOf(0) }
    LaunchedEffect(frames, isVisible, animationsEnabled, lifecycleOwner) {
        frameIndex = 0
        if (animationsEnabled && isVisible && frames.size > 1) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    delay(WEATHER_FRAME_DURATION_MILLIS)
                    frameIndex = (frameIndex + 1) % frames.size
                }
            }
        }
    }
    Image(
        bitmap = frames[frameIndex],
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun rememberWeatherFrames(
    scene: WeatherScene,
    animationsEnabled: Boolean,
): WeatherFramesState {
    val resources = LocalContext.current.resources
    val resourceIds = if (animationsEnabled) scene.frameResources else scene.frameResources.take(1)
    var framesState by remember { mutableStateOf<WeatherFramesState>(WeatherFramesState.Loading) }
    LaunchedEffect(resources, resourceIds) {
        val result =
            withContext(Dispatchers.Default) {
                try {
                    WeatherFramesState.Ready(resources.decodeWeatherFrames(resourceIds))
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    Log.e(LOG_TAG, "Unable to decode weather artwork for $scene", exception)
                    WeatherFramesState.Failed
                }
            }
        if (result is WeatherFramesState.Ready || framesState !is WeatherFramesState.Ready) {
            framesState = result
        }
    }
    return framesState
}

private suspend fun Resources.decodeWeatherFrames(resourceIds: List<Int>): List<ImageBitmap> {
    val options = BitmapFactory.Options().apply { inScaled = false }
    return resourceIds.map { resourceId ->
        coroutineContext.ensureActive()
        checkNotNull(BitmapFactory.decodeResource(this, resourceId, options)) {
            "Unable to decode weather artwork resource $resourceId"
        }.asImageBitmap().also { frame -> frame.prepareToDraw() }
    }
}

private fun android.content.Context.areSystemAnimationsEnabled(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ValueAnimator.areAnimatorsEnabled()
    } else {
        Settings.Global.getFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            DEFAULT_ANIMATOR_DURATION_SCALE,
        ) != 0f
    }

internal data class WeatherHeroArtwork(
    val scene: WeatherScene,
    val animationsEnabled: Boolean,
    val framesState: WeatherFramesState,
)

internal sealed interface WeatherFramesState {
    data object Loading : WeatherFramesState

    data class Ready(
        val frames: List<ImageBitmap>,
    ) : WeatherFramesState

    data object Failed : WeatherFramesState
}

private const val LOG_TAG = "WeatherHeroBackground"
private const val MIN_VISIBLE_FRACTION = 0.01f
private const val DEFAULT_ANIMATOR_DURATION_SCALE = 1f
