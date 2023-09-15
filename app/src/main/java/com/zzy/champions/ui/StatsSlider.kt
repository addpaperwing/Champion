package com.zzy.champions.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.SliderPositions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun StatsSlider() {

}

//@Composable
//private fun SliderImpl(
//    modifier: Modifier,
//    enabled: Boolean,
//    interactionSource: MutableInteractionSource,
//    onValueChange: (Float) -> Unit,
//    onValueChangeFinished: (() -> Unit)?,
//    steps: Int,
//    value: Float,
//    valueRange: ClosedFloatingPointRange<Float>,
//    thumb: @Composable (SliderPositions) -> Unit,
//    track: @Composable (SliderPositions) -> Unit
//) {
//    val onValueChangeState = rememberUpdatedState<(Float) -> Unit> {
//        if (it != value) {
//            onValueChange(it)
//        }
//    }
//
//    val tickFractions = remember(steps) {
//        stepsToTickFractions(steps)
//    }
//
//    val thumbWidth = remember { mutableStateOf(0) }
//    val totalWidth = remember { mutableStateOf(0) }
//
//    fun scaleToUserValue(minPx: Float, maxPx: Float, offset: Float) =
//        scale(minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)
//
//    fun scaleToOffset(minPx: Float, maxPx: Float, userValue: Float) =
//        scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)
//
//    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
//    val rawOffset = remember { mutableStateOf(scaleToOffset(0f, 0f, value)) }
//    val pressOffset = remember { mutableStateOf(0f) }
//    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
//
//    val positionFraction = calcFraction(valueRange.start, valueRange.endInclusive, coerced)
//    val sliderPositions = remember {
//        SliderPositions(0f..positionFraction, tickFractions)
//    }
//    sliderPositions.activeRange = 0f..positionFraction
//    sliderPositions.tickFractions = tickFractions
//
//    val draggableState = remember(valueRange) {
//        SliderDraggableState {
//            val maxPx = max(totalWidth.value - thumbWidth.value / 2, 0f)
//            val minPx = min(thumbWidth.value / 2, maxPx)
//            rawOffset.value = (rawOffset.value + it + pressOffset.value)
//            pressOffset.value = 0f
//            val offsetInTrack = snapValueToTick(rawOffset.value, tickFractions, minPx, maxPx)
//            onValueChangeState.value.invoke(scaleToUserValue(minPx, maxPx, offsetInTrack))
//        }
//    }
//
//    val gestureEndAction = rememberUpdatedState {
//        if (!draggableState.isDragging) {
//            // check isDragging in case the change is still in progress (touch -> drag case)
//            onValueChangeFinished?.invoke()
//        }
//    }
//
//    val press = Modifier.sliderTapModifier(
//        draggableState,
//        interactionSource,
//        totalWidth.value,
//        isRtl,
//        rawOffset,
//        gestureEndAction,
//        pressOffset,
//        enabled
//    )
//
//    val drag = Modifier.draggable(
//        orientation = Orientation.Horizontal,
//        reverseDirection = isRtl,
//        enabled = enabled,
//        interactionSource = interactionSource,
//        onDragStopped = { _ -> gestureEndAction.value.invoke() },
//        startDragImmediately = draggableState.isDragging,
//        state = draggableState
//    )
//
//    Layout(
//        {
//            Box(modifier = Modifier.layoutId(SliderComponents.THUMB)) { thumb(sliderPositions) }
//            Box(modifier = Modifier.layoutId(SliderComponents.TRACK)) { track(sliderPositions) }
//        },
//        modifier = modifier
//            .minimumInteractiveComponentSize()
//            .requiredSizeIn(
//                minWidth = SliderTokens.HandleWidth,
//                minHeight = SliderTokens.HandleHeight
//            )
//            .sliderSemantics(
//                value,
//                enabled,
//                onValueChange,
//                onValueChangeFinished,
//                valueRange,
//                steps
//            )
//            .focusable(enabled, interactionSource)
//            .then(press)
//            .then(drag)
//    ) { measurables, constraints ->
//
//        val thumbPlaceable = measurables.first {
//            it.layoutId == SliderComponents.THUMB
//        }.measure(constraints)
//
//        val trackPlaceable = measurables.first {
//            it.layoutId == SliderComponents.TRACK
//        }.measure(
//            constraints.offset(
//                horizontal = - thumbPlaceable.width
//            ).copy(minHeight = 0)
//        )
//
//        val sliderWidth = thumbPlaceable.width + trackPlaceable.width
//        val sliderHeight = max(trackPlaceable.height, thumbPlaceable.height)
//
//        thumbWidth.value = thumbPlaceable.width.toFloat()
//        totalWidth.value = sliderWidth
//
//        val trackOffsetX = thumbPlaceable.width / 2
//        val thumbOffsetX = ((trackPlaceable.width) * positionFraction).roundToInt()
//        val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
//        val thumbOffsetY = (sliderHeight - thumbPlaceable.height) / 2
//
//        layout(
//            sliderWidth,
//            sliderHeight
//        ) {
//            trackPlaceable.placeRelative(
//                trackOffsetX,
//                trackOffsetY
//            )
//            thumbPlaceable.placeRelative(
//                thumbOffsetX,
//                thumbOffsetY
//            )
//        }
//    }
//}