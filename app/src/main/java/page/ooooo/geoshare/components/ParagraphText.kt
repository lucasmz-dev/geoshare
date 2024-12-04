package page.ooooo.geoshare.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.sp

private val listItemIndent = 15.sp

@Composable
fun codeStyle() = SpanStyle(
    color = MaterialTheme.colorScheme.secondary,
    fontFamily = FontFamily.Monospace,
)

@Composable
fun linkStyle() = SpanStyle(
    color = MaterialTheme.colorScheme.tertiary,
    textDecoration = TextDecoration.Underline
)

@Composable
fun paragraphStyle() = MaterialTheme.typography.bodyMedium.copy(
    lineBreak = LineBreak.Paragraph,
)

@Composable
fun listItemStyle() = paragraphStyle().copy(
    textIndent = TextIndent(restLine = listItemIndent),
)

@Composable
fun listItemParagraphStyle() = listItemStyle().copy(
    lineBreak = LineBreak.Paragraph, textIndent = TextIndent(
        firstLine = listItemIndent,
        restLine = listItemIndent,
    )
)

@Composable
fun ParagraphText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    Text(text, modifier, style = paragraphStyle())
}

@Composable
@Suppress("unused")
fun ParagraphText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(text, modifier, style = paragraphStyle())
}

@Composable
fun ListItemText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(text, modifier, style = listItemStyle())
}

@Composable
fun ListItemText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    Text(text, modifier, style = listItemStyle())
}

@Composable
fun ListItemParagraphText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(text, modifier, style = listItemParagraphStyle())
}

@Composable
fun ListItemParagraphText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    Text(text, modifier, style = listItemParagraphStyle())
}
