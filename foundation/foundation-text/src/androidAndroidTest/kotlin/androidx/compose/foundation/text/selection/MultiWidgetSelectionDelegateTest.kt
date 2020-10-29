/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.foundation.text.selection

import android.content.Context
import android.graphics.Typeface
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.selection.Selectable
import androidx.compose.ui.selection.Selection
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.InternalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextDelegate
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.ResourceFont
import androidx.compose.ui.text.font.asFontFamily
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.test.R
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

val BASIC_MEASURE_FONT = font(
    resId = R.font.sample_font,
    weight = FontWeight.Normal,
    style = FontStyle.Normal
)

@OptIn(InternalTextApi::class)
@RunWith(AndroidJUnit4::class)
@MediumTest
class MultiWidgetSelectionDelegateTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fontFamily = BASIC_MEASURE_FONT.asFontFamily()
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val defaultDensity = Density(density = 1f)
    private val resourceLoader = TestFontResourceLoader(context)

    @Test
    fun getHandlePosition_StartHandle_invalid() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "hello world\n"
                val fontSize = 20.sp

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val selectableInvalid = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { null },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('h')
                val endOffset = text.indexOf('o')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectableInvalid
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectableInvalid
                    ),
                    handlesCrossed = false
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = true
                )

                // Assert.
                assertThat(coordinates).isEqualTo(Offset.Zero)
            }
        }
    }

    @Test
    fun getHandlePosition_EndHandle_invalid() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "hello world\n"
                val fontSize = 20.sp

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val selectableInvalid = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { null },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('h')
                val endOffset = text.indexOf('o')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectableInvalid
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectableInvalid
                    ),
                    handlesCrossed = false
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = false
                )

                // Assert.
                assertThat(coordinates).isEqualTo(Offset.Zero)
            }
        }
    }

    @Test
    fun getHandlePosition_StartHandle_not_cross_ltr() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "hello world\n"
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('h')
                val endOffset = text.indexOf('o')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = false
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = true
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * startOffset), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getHandlePosition_StartHandle_cross_ltr() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "hello world\n"
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('o')
                val endOffset = text.indexOf('h')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = true
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = true
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * startOffset), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getHandlePosition_StartHandle_not_cross_rtl() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "\u05D0\u05D1\u05D2 \u05D3\u05D4\u05D5\n"
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('\u05D1')
                val endOffset = text.indexOf('\u05D5')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = false
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = true
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * (text.length - 1 - startOffset)), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getHandlePosition_StartHandle_cross_rtl() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "\u05D0\u05D1\u05D2 \u05D3\u05D4\u05D5\n"
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('\u05D5')
                val endOffset = text.indexOf('\u05D1')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = true
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = true
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * (text.length - 1 - startOffset)), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getHandlePosition_StartHandle_not_cross_bidi() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val textLtr = "Hello"
                val textRtl = "\u05D0\u05D1\u05D2"
                val text = textLtr + textRtl
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('\u05D0')
                val endOffset = text.indexOf('\u05D2')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = false
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = true
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * (text.length)), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getHandlePosition_StartHandle_cross_bidi() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val textLtr = "Hello"
                val textRtl = "\u05D0\u05D1\u05D2"
                val text = textLtr + textRtl
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('\u05D0')
                val endOffset = text.indexOf('H')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = true
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = true
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * (textLtr.length)), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getHandlePosition_EndHandle_not_cross_ltr() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "hello world\n"
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('h')
                val endOffset = text.indexOf('o')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = false
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = false
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * endOffset), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getHandlePosition_EndHandle_cross_ltr() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "hello world\n"
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('o')
                val endOffset = text.indexOf('h')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = true
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = false
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * endOffset), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getHandlePosition_EndHandle_not_cross_rtl() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "\u05D0\u05D1\u05D2 \u05D3\u05D4\u05D5\n"
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('\u05D1')
                val endOffset = text.indexOf('\u05D5')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = false
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = false
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * (text.length - 1 - endOffset)), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getHandlePosition_EndHandle_cross_rtl() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "\u05D0\u05D1\u05D2 \u05D3\u05D4\u05D5\n"
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('\u05D5')
                val endOffset = text.indexOf('\u05D1')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = true
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = false
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * (text.length - 1 - endOffset)), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getHandlePosition_EndHandle_not_cross_bidi() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val textLtr = "Hello"
                val textRtl = "\u05D0\u05D1\u05D2"
                val text = textLtr + textRtl
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('e')
                val endOffset = text.indexOf('\u05D0')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = false
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = false
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * (textLtr.length)), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getHandlePosition_EndHandle_cross_bidi() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val textLtr = "Hello"
                val textRtl = "\u05D0\u05D1\u05D2"
                val text = textLtr + textRtl
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val startOffset = text.indexOf('\u05D2')
                val endOffset = text.indexOf('\u05D0')

                val selection = Selection(
                    start = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = startOffset,
                        selectable = selectable
                    ),
                    end = Selection.AnchorInfo(
                        direction = ResolvedTextDirection.Ltr,
                        offset = endOffset,
                        selectable = selectable
                    ),
                    handlesCrossed = true
                )

                // Act.
                val coordinates = selectable.getHandlePosition(
                    selection = selection,
                    isStartHandle = false
                )

                // Assert.
                assertThat(coordinates).isEqualTo(
                    Offset((fontSizeInPx * (text.length)), fontSizeInPx)
                )
            }
        }
    }

    @Test
    fun getText_textLayoutResult_Null_Return_Empty_AnnotatedString() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val layoutResult = null

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                assertThat(selectable.getText()).isEqualTo(AnnotatedString(""))
            }
        }
    }

    @Test
    fun getText_textLayoutResult_NotNull_Return_AnnotatedString() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val textLtr = "Hello"
                val textRtl = "\u05D0\u05D1\u05D2"
                val text = textLtr + textRtl
                val fontSize = 20.sp
                val spanStyle = SpanStyle(fontSize = fontSize, fontFamily = fontFamily)

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = {},
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                assertThat(selectable.getText()).isEqualTo(AnnotatedString(text, spanStyle))
            }
        }
    }

    @Test
    fun getBoundingBox_valid() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "hello\nworld\n"
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = mock(),
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                val textOffset = text.indexOf('w')

                // Act.
                val box = selectable.getBoundingBox(textOffset)

                // Assert.
                assertThat(box.left).isZero()
                assertThat(box.right).isEqualTo(fontSizeInPx)
                assertThat(box.top).isEqualTo(fontSizeInPx)
                assertThat(box.bottom).isEqualTo((2f + 1 / 5f) * fontSizeInPx)
            }
        }
    }

    @Test
    fun getBoundingBox_negative_offset_should_return_zero_rect() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "hello\nworld\n"
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = mock(),
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                // Act.
                val box = selectable.getBoundingBox(-2)

                // Assert.
                assertThat(box.left).isZero()
                assertThat(box.right).isEqualTo(fontSizeInPx)
                assertThat(box.top).isZero()
                assertThat(box.bottom).isEqualTo(fontSizeInPx)
            }
        }
    }

    @Test
    fun getBoundingBox_offset_larger_than_range_should_return_largest() {
        with(defaultDensity) {
            composeTestRule.setContent {
                val text = "hello\nworld"
                val fontSize = 20.sp
                val fontSizeInPx = fontSize.toPx()

                val layoutResult = simpleTextLayout(
                    text = text,
                    fontSize = fontSize,
                    density = defaultDensity
                )

                val layoutCoordinates = mock<LayoutCoordinates>()
                whenever(layoutCoordinates.isAttached).thenReturn(true)

                val selectable = MultiWidgetSelectionDelegate(
                    selectionRangeUpdate = mock(),
                    coordinatesCallback = { layoutCoordinates },
                    layoutResultCallback = { layoutResult }
                )

                // Act.
                val box = selectable.getBoundingBox(text.indexOf('d') + 5)

                // Assert.
                assertThat(box.left).isEqualTo(4 * fontSizeInPx)
                assertThat(box.right).isEqualTo(5 * fontSizeInPx)
                assertThat(box.top).isEqualTo(fontSizeInPx)
                assertThat(box.bottom).isEqualTo((2f + 1 / 5f) * fontSizeInPx)
            }
        }
    }

    @Test
    fun getTextSelectionInfo_long_press_select_word_ltr() {
        val text = "hello world\n"
        val fontSize = 20.sp
        val fontSizeInPx = with(defaultDensity) { fontSize.toPx() }

        val textLayoutResult = simpleTextLayout(
            text = text,
            fontSize = fontSize,
            density = defaultDensity
        )

        val start = Offset((fontSizeInPx * 2), (fontSizeInPx / 2))
        val end = start

        // Act.
        val textSelectionInfo = getTextSelectionInfo(
            textLayoutResult = textLayoutResult,
            selectionCoordinates = Pair(start, end),
            selectable = mock(),
            wordBasedSelection = true
        )

        // Assert.
        assertThat(textSelectionInfo).isNotNull()

        assertThat(textSelectionInfo?.start).isNotNull()
        textSelectionInfo?.start?.let {
            assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
            assertThat(it.offset).isEqualTo(0)
        }

        assertThat(textSelectionInfo?.end).isNotNull()
        textSelectionInfo?.end?.let {
            assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
            assertThat(it.offset).isEqualTo("hello".length)
        }
    }

    @Test
    fun getTextSelectionInfo_long_press_select_word_rtl() {
        val text = "\u05D0\u05D1\u05D2 \u05D3\u05D4\u05D5\n"
        val fontSize = 20.sp
        val fontSizeInPx = with(defaultDensity) { fontSize.toPx() }

        val textLayoutResult = simpleTextLayout(
            text = text,
            fontSize = fontSize,
            density = defaultDensity
        )

        val start = Offset((fontSizeInPx * 2), (fontSizeInPx / 2))
        val end = start

        // Act.
        val textSelectionInfo = getTextSelectionInfo(
            textLayoutResult = textLayoutResult,
            selectionCoordinates = Pair(start, end),
            selectable = mock(),
            wordBasedSelection = true
        )

        // Assert.
        assertThat(textSelectionInfo).isNotNull()

        assertThat(textSelectionInfo?.start).isNotNull()
        textSelectionInfo?.start?.let {
            assertThat(it.direction).isEqualTo(ResolvedTextDirection.Rtl)
            assertThat(it.offset).isEqualTo(text.indexOf("\u05D3"))
        }

        assertThat(textSelectionInfo?.end).isNotNull()
        textSelectionInfo?.end?.let {
            assertThat(it.direction).isEqualTo(ResolvedTextDirection.Rtl)
            assertThat(it.offset).isEqualTo(text.indexOf("\u05D5") + 1)
        }
    }

    @Test
    fun getTextSelectionInfo_long_press_drag_handle_not_cross_select_word() {
        val text = "hello world"
        val fontSize = 20.sp
        val fontSizeInPx = with(defaultDensity) { fontSize.toPx() }

        val textLayoutResult = simpleTextLayout(
            text = text,
            fontSize = fontSize,
            density = defaultDensity
        )

        val rawStartOffset = text.indexOf('e')
        val rawEndOffset = text.indexOf('r')
        val start = Offset((fontSizeInPx * rawStartOffset), (fontSizeInPx / 2))
        val end = Offset((fontSizeInPx * rawEndOffset), (fontSizeInPx / 2))

        // Act.
        val textSelectionInfo = getTextSelectionInfo(
            textLayoutResult = textLayoutResult,
            selectionCoordinates = Pair(start, end),
            selectable = mock(),
            wordBasedSelection = true
        )

        // Assert.
        assertThat(textSelectionInfo).isNotNull()

        assertThat(textSelectionInfo?.start).isNotNull()
        textSelectionInfo?.start?.let {
            assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
            assertThat(it.offset).isEqualTo(0)
        }

        assertThat(textSelectionInfo?.end).isNotNull()
        textSelectionInfo?.end?.let {
            assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
            assertThat(it.offset).isEqualTo(text.length)
        }
        assertThat(textSelectionInfo?.handlesCrossed).isFalse()
    }

    @Test
    fun getTextSelectionInfo_long_press_drag_handle_cross_select_word() {
        with(defaultDensity) {
            val text = "hello world"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()

            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )

            val rawStartOffset = text.indexOf('r')
            val rawEndOffset = text.indexOf('e')
            val start = Offset((fontSizeInPx * rawStartOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * rawEndOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                textLayoutResult = textLayoutResult,
                selectionCoordinates = Pair(start, end),
                selectable = mock(),
                wordBasedSelection = true
            )

            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(text.length)
            }

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(0)
            }
            assertThat(textSelectionInfo?.handlesCrossed).isTrue()
        }
    }

    @Test
    fun getTextSelectionInfo_drag_select_range_ltr() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()

            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )

            // "llo wor" is selected.
            val startOffset = text.indexOf("l")
            val endOffset = text.indexOf("r") + 1
            val start = Offset((fontSizeInPx * startOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * endOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                textLayoutResult = textLayoutResult,
                selectionCoordinates = Pair(start, end),
                selectable = mock(),
                wordBasedSelection = false
            )

            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(startOffset)
            }

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(endOffset)
            }
        }
    }

    @Test
    fun getTextSelectionInfo_drag_select_range_rtl() {
        with(defaultDensity) {
            val text = "\u05D0\u05D1\u05D2 \u05D3\u05D4\u05D5\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()

            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )

            // "\u05D1\u05D2 \u05D3" is selected.
            val startOffset = text.indexOf("\u05D1")
            val endOffset = text.indexOf("\u05D3") + 1
            val start = Offset(
                (fontSizeInPx * (text.length - 1 - startOffset)),
                (fontSizeInPx / 2)
            )
            val end = Offset(
                (fontSizeInPx * (text.length - 1 - endOffset)),
                (fontSizeInPx / 2)
            )

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                textLayoutResult = textLayoutResult,
                selectionCoordinates = Pair(start, end),
                selectable = mock(),
                wordBasedSelection = false
            )

            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Rtl)
                assertThat(it.offset).isEqualTo(startOffset)
            }

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Rtl)
                assertThat(it.offset).isEqualTo(endOffset)
            }
        }
    }

    @Test
    fun getTextSelectionInfo_drag_select_range_bidi() {
        with(defaultDensity) {
            val textLtr = "Hello"
            val textRtl = "\u05D0\u05D1\u05D2\u05D3\u05D4"
            val text = textLtr + textRtl
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()

            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )

            // "llo"+"\u05D0\u05D1\u05D2" is selected
            val startOffset = text.indexOf("l")
            val endOffset = text.indexOf("\u05D2") + 1
            val start = Offset(
                (fontSizeInPx * startOffset),
                (fontSizeInPx / 2)
            )
            val end = Offset(
                (fontSizeInPx * (textLtr.length + text.length - endOffset)),
                (fontSizeInPx / 2)
            )

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                textLayoutResult = textLayoutResult,
                selectionCoordinates = Pair(start, end),
                selectable = mock(),
                wordBasedSelection = false
            )

            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(startOffset)
            }

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Rtl)
                assertThat(it.offset).isEqualTo(endOffset)
            }
        }
    }

    @Test
    fun getTextSelectionInfo_single_widget_handles_crossed_ltr() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "llo wor" is selected.
            val startOffset = text.indexOf("r") + 1
            val endOffset = text.indexOf("l")
            val start = Offset((fontSizeInPx * startOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * endOffset), (fontSizeInPx / 2))
            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = mock(),
                wordBasedSelection = false
            )
            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(startOffset)
            }

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(endOffset)
            }
            assertThat(textSelectionInfo?.handlesCrossed).isTrue()
        }
    }

    @Test
    fun getTextSelectionInfo_single_widget_handles_crossed_rtl() {
        with(defaultDensity) {
            val text = "\u05D0\u05D1\u05D2 \u05D3\u05D4\u05D5\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "\u05D1\u05D2 \u05D3" is selected.
            val startOffset = text.indexOf("\u05D3") + 1
            val endOffset = text.indexOf("\u05D1")
            val start = Offset(
                (fontSizeInPx * (text.length - 1 - startOffset)),
                (fontSizeInPx / 2)
            )
            val end = Offset(
                (fontSizeInPx * (text.length - 1 - endOffset)),
                (fontSizeInPx / 2)
            )

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = mock(),
                wordBasedSelection = false
            )
            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Rtl)
                assertThat(it.offset).isEqualTo(startOffset)
            }

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Rtl)
                assertThat(it.offset).isEqualTo(endOffset)
            }
            assertThat(textSelectionInfo?.handlesCrossed).isTrue()
        }
    }

    @Test
    fun getTextSelectionInfo_single_widget_handles_crossed_bidi() {
        with(defaultDensity) {
            val textLtr = "Hello"
            val textRtl = "\u05D0\u05D1\u05D2\u05D3\u05D4"
            val text = textLtr + textRtl
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "llo"+"\u05D0\u05D1\u05D2" is selected
            val startOffset = text.indexOf("\u05D2") + 1
            val endOffset = text.indexOf("l")
            val start = Offset(
                (fontSizeInPx * (textLtr.length + text.length - startOffset)),
                (fontSizeInPx / 2)
            )
            val end = Offset(
                (fontSizeInPx * endOffset),
                (fontSizeInPx / 2)
            )

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = mock(),
                wordBasedSelection = false
            )
            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Rtl)
                assertThat(it.offset).isEqualTo(startOffset)
            }

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(endOffset)
            }
            assertThat(textSelectionInfo?.handlesCrossed).isTrue()
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_ltr_drag_endHandle() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "llo" is selected.
            val oldStartOffset = text.indexOf("l")
            val oldEndOffset = text.indexOf("o") + 1
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = false
            )
            // "l" is selected.
            val start = Offset((fontSizeInPx * oldStartOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * oldStartOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = false
            )
            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isEqualTo(previousSelection.start)

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(oldStartOffset - 1)
            }

            assertThat(textSelectionInfo?.handlesCrossed).isTrue()
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_rtl_drag_endHandle() {
        with(defaultDensity) {
            val text = "\u05D0\u05D1\u05D2 \u05D3\u05D4\u05D5\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "\u05D0\u05D1" is selected.
            val oldStartOffset = text.indexOf("\u05D1")
            val oldEndOffset = text.length
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Rtl,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Rtl,
                    selectable = selectable
                ),
                handlesCrossed = false
            )
            // "\u05D1" is selected.
            val start = Offset(
                (fontSizeInPx * (text.length - 1 - oldStartOffset)),
                (fontSizeInPx / 2)
            )
            val end = Offset(
                (fontSizeInPx * (text.length - 1 - oldStartOffset)),
                (fontSizeInPx / 2)
            )

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = false
            )
            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isEqualTo(previousSelection.start)

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Rtl)
                assertThat(it.offset).isEqualTo(oldStartOffset - 1)
            }

            assertThat(textSelectionInfo?.handlesCrossed).isTrue()
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_drag_startHandle_not_crossed() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "llo" is selected.
            val oldStartOffset = text.indexOf("l")
            val oldEndOffset = text.indexOf("o") + 1
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = false
            )
            // The Space after "o" is selected.
            val start = Offset((fontSizeInPx * oldEndOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * oldEndOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = true
            )

            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo((oldEndOffset + 1))
            }

            assertThat(textSelectionInfo?.end).isEqualTo(previousSelection.end)

            assertThat(textSelectionInfo?.handlesCrossed).isTrue()
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_drag_startHandle_crossed() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "llo" is selected.
            val oldStartOffset = text.indexOf("o") + 1
            val oldEndOffset = text.indexOf("l")
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = true
            )
            // "l" is selected.
            val start = Offset((fontSizeInPx * oldEndOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * oldEndOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = true
            )

            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo((oldEndOffset - 1))
            }

            assertThat(textSelectionInfo?.end).isEqualTo(previousSelection.end)

            assertThat(textSelectionInfo?.handlesCrossed).isFalse()
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_drag_startHandle_not_crossed_bounded() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "e" is selected.
            val oldStartOffset = text.indexOf("e")
            val oldEndOffset = text.indexOf("l")
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = false
            )
            // "e" should be selected.
            val start = Offset((fontSizeInPx * oldEndOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * oldEndOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = true
            )

            // Assert.
            assertThat(textSelectionInfo).isEqualTo(previousSelection)
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_drag_startHandle_crossed_bounded() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "e" is selected.
            val oldStartOffset = text.indexOf("l")
            val oldEndOffset = text.indexOf("e")
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = true
            )
            // "e" should be selected.
            val start = Offset((fontSizeInPx * oldEndOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * oldEndOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = true
            )

            // Assert.
            assertThat(textSelectionInfo).isEqualTo(previousSelection)
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_drag_startHandle_not_crossed_boundary() {
        with(defaultDensity) {
            val text = "hello world"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "d" is selected.
            val oldStartOffset = text.length - 1
            val oldEndOffset = text.length
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = false
            )
            // "d" should be selected.
            val start = Offset(
                (fontSizeInPx * oldEndOffset) - (fontSizeInPx / 2),
                (fontSizeInPx / 2)
            )
            val end = Offset(
                (fontSizeInPx * oldEndOffset) - 1,
                (fontSizeInPx / 2)
            )

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = true
            )

            // Assert.
            assertThat(textSelectionInfo).isEqualTo(previousSelection)
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_drag_startHandle_crossed_boundary() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "h" is selected.
            val oldStartOffset = text.indexOf("e")
            val oldEndOffset = 0
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = true
            )
            // "e" should be selected.
            val start = Offset((fontSizeInPx * oldEndOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * oldEndOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = true
            )

            // Assert.
            assertThat(textSelectionInfo).isEqualTo(previousSelection)
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_drag_endHandle_crossed() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "llo" is selected.
            val oldStartOffset = text.indexOf("o") + 1
            val oldEndOffset = text.indexOf("l")
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = true
            )
            // The space after "o" is selected.
            val start = Offset((fontSizeInPx * oldStartOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * oldStartOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = false
            )

            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isEqualTo(previousSelection.start)

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo((oldStartOffset + 1))
            }

            assertThat(textSelectionInfo?.handlesCrossed).isFalse()
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_drag_endHandle_not_crossed_bounded() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "e" is selected.
            val oldStartOffset = text.indexOf("e")
            val oldEndOffset = text.indexOf("l")
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = false
            )
            // "e" should be selected.
            val start = Offset((fontSizeInPx * oldStartOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * oldStartOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = false
            )

            // Assert.
            assertThat(textSelectionInfo).isEqualTo(previousSelection)
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_drag_endHandle_crossed_bounded() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "e" is selected.
            val oldStartOffset = text.indexOf("l")
            val oldEndOffset = text.indexOf("e")
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = true
            )
            // "e" should be selected.
            val start = Offset((fontSizeInPx * oldStartOffset), (fontSizeInPx / 2))
            val end = Offset((fontSizeInPx * oldStartOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = false
            )

            // Assert.
            assertThat(textSelectionInfo).isEqualTo(previousSelection)
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_drag_endHandle_not_crossed_boundary() {
        with(defaultDensity) {
            val text = "hello world"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "h" is selected.
            val oldStartOffset = 0
            val oldEndOffset = text.indexOf('e')
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = false
            )
            // "h" should be selected.
            val start = Offset(
                (fontSizeInPx * oldStartOffset),
                (fontSizeInPx / 2)
            )
            val end = Offset(
                (fontSizeInPx * oldStartOffset),
                (fontSizeInPx / 2)
            )

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = false
            )

            // Assert.
            assertThat(textSelectionInfo).isEqualTo(previousSelection)
        }
    }

    @Test
    fun getTextSelectionInfo_bound_to_one_character_drag_endHandle_crossed_boundary() {
        with(defaultDensity) {
            val text = "hello world"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "d" is selected.
            val oldStartOffset = text.length
            val oldEndOffset = text.length - 1
            val selectable: Selectable = mock()
            val previousSelection = Selection(
                start = Selection.AnchorInfo(
                    offset = oldStartOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                end = Selection.AnchorInfo(
                    offset = oldEndOffset,
                    direction = ResolvedTextDirection.Ltr,
                    selectable = selectable
                ),
                handlesCrossed = true
            )
            // "d" should be selected.
            val start = Offset(
                (fontSizeInPx * oldStartOffset) - 1,
                (fontSizeInPx / 2)
            )
            val end = Offset(
                (fontSizeInPx * oldStartOffset) - 1,
                (fontSizeInPx / 2)
            )

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = selectable,
                wordBasedSelection = false,
                previousSelection = previousSelection,
                isStartHandle = false
            )

            // Assert.
            assertThat(textSelectionInfo).isEqualTo(previousSelection)
        }
    }

    @Test
    fun getTextSelectionInfo_cross_widget_not_contain_start() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "hello w" is selected.
            val endOffset = text.indexOf("w") + 1
            val start = Offset(-50f, -50f)
            val end = Offset((fontSizeInPx * endOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = mock(),
                wordBasedSelection = false
            )
            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(0)
            }

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(endOffset)
            }
        }
    }

    @Test
    fun getTextSelectionInfo_cross_widget_not_contain_end() {
        with(defaultDensity) {
            val text = "hello world"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "o world" is selected.
            val startOffset = text.indexOf("o")
            val start = Offset((fontSizeInPx * startOffset), (fontSizeInPx / 2))
            val end = Offset(
                (fontSizeInPx * text.length * 2), (fontSizeInPx * 2)
            )

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = mock(),
                wordBasedSelection = false
            )
            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(startOffset)
            }

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(text.length)
            }
        }
    }

    @Test
    fun getTextSelectionInfo_cross_widget_not_contain_start_handles_crossed() {
        with(defaultDensity) {
            val text = "hello world"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "world" is selected.
            val endOffset = text.indexOf("w")
            val start =
                Offset((fontSizeInPx * text.length * 2), (fontSizeInPx * 2))
            val end = Offset((fontSizeInPx * endOffset), (fontSizeInPx / 2))

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = mock(),
                wordBasedSelection = false
            )
            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(text.length)
            }

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(endOffset)
            }
            assertThat(textSelectionInfo?.handlesCrossed).isTrue()
        }
    }

    @Test
    fun getTextSelectionInfo_cross_widget_not_contain_end_handles_crossed() {
        with(defaultDensity) {
            val text = "hello world"
            val fontSize = 20.sp
            val fontSizeInPx = fontSize.toPx()
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            // "hell" is selected.
            val startOffset = text.indexOf("o")
            val start =
                Offset((fontSizeInPx * startOffset), (fontSizeInPx / 2))
            val end = Offset(-50f, -50f)

            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = mock(),
                wordBasedSelection = false
            )
            // Assert.
            assertThat(textSelectionInfo).isNotNull()

            assertThat(textSelectionInfo?.start).isNotNull()
            textSelectionInfo?.start?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(startOffset)
            }

            assertThat(textSelectionInfo?.end).isNotNull()
            textSelectionInfo?.end?.let {
                assertThat(it.direction).isEqualTo(ResolvedTextDirection.Ltr)
                assertThat(it.offset).isEqualTo(0)
            }
            assertThat(textSelectionInfo?.handlesCrossed).isTrue()
        }
    }

    @Test
    fun getTextSelectionInfo_not_selected() {
        with(defaultDensity) {
            val text = "hello world\n"
            val fontSize = 20.sp
            val textLayoutResult = simpleTextLayout(
                text = text,
                fontSize = fontSize,
                density = defaultDensity
            )
            val start = Offset(-50f, -50f)
            val end = Offset(-20f, -20f)
            // Act.
            val textSelectionInfo = getTextSelectionInfo(
                selectionCoordinates = Pair(start, end),
                textLayoutResult = textLayoutResult,
                selectable = mock(),
                wordBasedSelection = true
            )
            assertThat(textSelectionInfo).isNull()
        }
    }

    private fun simpleTextLayout(
        text: String = "",
        fontSize: TextUnit = TextUnit.Inherit,
        density: Density
    ): TextLayoutResult {
        val spanStyle = SpanStyle(fontSize = fontSize, fontFamily = fontFamily)
        val annotatedString = AnnotatedString(text, spanStyle)
        return TextDelegate(
            text = annotatedString,
            style = TextStyle(),
            density = density,
            resourceLoader = resourceLoader
        ).layout(Constraints(), LayoutDirection.Ltr)
    }
}

class TestFontResourceLoader(val context: Context) : Font.ResourceLoader {
    override fun load(font: Font): Typeface {
        return when (font) {
            is ResourceFont -> ResourcesCompat.getFont(context, font.resId)!!
            else -> throw IllegalArgumentException("Unknown font type: $font")
        }
    }
}
