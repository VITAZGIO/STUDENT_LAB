package ru.vitazgio.auroraquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { QuizApp() }
    }
}

/** Состояние квиза — аналог свойств current / selected / results в MainPage.qml. */
class QuizState {
    var current by mutableIntStateOf(0)
    var selected by mutableIntStateOf(-1)
    val results = mutableStateListOf<Boolean>()

    val answered get() = selected != -1
    val score get() = results.count { it }
    val question get() = questions[current]
    val isLast get() = current == questions.size - 1

    fun answer(index: Int) {
        if (answered) return
        selected = index
        results.add(index == question.correct)
    }

    fun next() {
        if (!isLast) {
            current++
            selected = -1
        }
    }

    fun restart() {
        current = 0
        selected = -1
        results.clear()
    }
}

enum class Screen { Quiz, Result, About }

@Composable
fun QuizApp() {
    val quiz = remember { QuizState() }
    var screen by remember { mutableStateOf(Screen.Quiz) }

    BackHandler(enabled = screen != Screen.Quiz) { screen = Screen.Quiz }

    Box(
        Modifier
            .fillMaxSize()
            .background(AuroraTheme.background)
    ) {
        // Переходы между страницами — сдвиг вбок, как в PageStack
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                if (targetState != Screen.Quiz) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 3 } + fadeOut()
                } else {
                    slideInHorizontally { -it / 3 } + fadeIn() togetherWith slideOutHorizontally { it }
                }
            },
            label = "pages"
        ) { s ->
            Box(Modifier.fillMaxSize().systemBarsPadding()) {
                when (s) {
                    Screen.Quiz -> QuizPage(
                        quiz = quiz,
                        onResult = { screen = Screen.Result },
                        onAbout = { screen = Screen.About }
                    )
                    Screen.Result -> ResultPage(
                        quiz = quiz,
                        onBack = { screen = Screen.Quiz },
                        onRestart = { quiz.restart(); screen = Screen.Quiz }
                    )
                    Screen.About -> AboutPage(onBack = { screen = Screen.Quiz })
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Главная страница

@Composable
fun QuizPage(quiz: QuizState, onResult: () -> Unit, onAbout: () -> Unit) {
    val density = LocalDensity.current
    val threshold = with(density) { 80.dp.toPx() }
    var pull by remember { mutableFloatStateOf(0f) }
    var menuOpen by remember { mutableStateOf(false) }

    // Аналог PullDownMenu: тянем страницу вниз — появляется меню
    val pullConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0 && pull > 0) {
                    val used = maxOf(available.y, -pull)
                    pull += used
                    return Offset(0f, used)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0 && source == NestedScrollSource.Drag) {
                    pull += available.y * 0.5f
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pull > threshold) menuOpen = true
                pull = 0f
                return Velocity.Zero
            }
        }
    }
    val pullAnimated by animateFloatAsState(pull, tween(if (pull == 0f) 200 else 0), label = "pull")

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .nestedScroll(pullConnection)
                .verticalScroll(rememberScrollState())
                .graphicsLayer { translationY = pullAnimated }
                .alpha(if (menuOpen) 0.45f else 1f),
            verticalArrangement = Arrangement.spacedBy(AuroraTheme.paddingMedium)
        ) {
            PageHeader(
                title = "Квиз по ОС Аврора",
                description = "Вопрос ${quiz.current + 1} из ${questions.size}"
            )

            // Индикатор прогресса: рамка и 10 сегментов
            Row(
                Modifier
                    .padding(horizontal = AuroraTheme.horizontalPageMargin)
                    .fillMaxWidth()
                    .progressFrame(),
                horizontalArrangement = Arrangement.spacedBy(AuroraTheme.paddingSmall)
            ) {
                questions.indices.forEach { i ->
                    val target = when {
                        i < quiz.results.size ->
                            if (quiz.results[i]) AuroraTheme.right else AuroraTheme.wrong
                        i == quiz.current -> AuroraTheme.highlight.copy(alpha = 0.28f)
                        else -> Color.Transparent
                    }
                    val color by animateColorAsState(target, tween(200), label = "seg")
                    Box(
                        Modifier
                            .weight(1f)
                            .height(26.dp)
                            .background(color, RoundedCornerShape(3.dp))
                            .then(
                                if (i < quiz.results.size) Modifier
                                else Modifier.border(
                                    1.dp,
                                    AuroraTheme.secondary.copy(alpha = 0.5f),
                                    RoundedCornerShape(3.dp)
                                )
                            )
                    )
                }
            }

            SectionHeader("Вопрос ${quiz.current + 1}")

            Text(
                quiz.question.text,
                modifier = Modifier.padding(horizontal = AuroraTheme.horizontalPageMargin),
                color = AuroraTheme.primary,
                fontSize = 21.sp,
                lineHeight = 27.sp,
                minLines = 3
            )

            // Варианты ответа
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AuroraTheme.paddingMedium)
            ) {
                quiz.question.answers.forEachIndexed { index, answer ->
                    val isCorrect = index == quiz.question.correct
                    val label = when {
                        quiz.answered && isCorrect -> "✓  $answer"
                        quiz.answered && index == quiz.selected -> "✗  $answer"
                        else -> answer
                    }
                    val color = when {
                        quiz.answered && isCorrect -> AuroraTheme.right
                        quiz.answered && index == quiz.selected -> AuroraTheme.wrong
                        else -> AuroraTheme.primary
                    }
                    AuroraButton(
                        text = label,
                        textColor = color,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        onClick = { quiz.answer(index) }
                    )
                }
            }

            // Сообщение о результате — место зарезервировано, надпись только проявляется
            val feedbackAlpha by animateFloatAsState(
                if (quiz.answered) 1f else 0f, tween(250), label = "fb"
            )
            val right = quiz.selected == quiz.question.correct
            Text(
                text = if (right) "Верно!"
                else "Неверно. Правильный ответ: ${quiz.question.answers[quiz.question.correct]}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AuroraTheme.horizontalPageMargin)
                    .alpha(feedbackAlpha),
                color = if (right) AuroraTheme.right else AuroraTheme.wrong,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                minLines = 2
            )

            Separator()

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AuroraTheme.horizontalPageMargin),
                horizontalArrangement = Arrangement.spacedBy(AuroraTheme.paddingLarge)
            ) {
                AuroraButton("Заново", Modifier.weight(1f)) { quiz.restart() }
                AuroraButton(
                    text = if (quiz.isLast) "Результат" else "Далее",
                    modifier = Modifier.weight(1f),
                    enabled = quiz.answered
                ) {
                    if (quiz.isLast) onResult() else quiz.next()
                }
            }
            Spacer(Modifier.height(AuroraTheme.paddingLarge))
        }

        // Закрытие меню тапом по странице
        if (menuOpen) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { menuOpen = false }
            )
        }

        // Само вытягиваемое меню
        AnimatedVisibility(
            visible = menuOpen,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .background(Color(0xE68FA2B0), RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .padding(top = 36.dp, bottom = 22.dp)
            ) {
                MenuItem("О приложении") { menuOpen = false; onAbout() }
                MenuItem("Начать заново") { menuOpen = false; quiz.restart() }
            }
        }
    }
}

@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        color = AuroraTheme.primary,
        fontSize = 18.sp,
        textAlign = TextAlign.Center
    )
}

// ---------------------------------------------------------------- Результат (аналог Dialog)

@Composable
fun ResultPage(quiz: QuizState, onBack: () -> Unit, onRestart: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        // DialogHeader: «Назад» слева, «Заново» справа
        Row(
            Modifier
                .fillMaxWidth()
                .height(58.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Назад",
                Modifier
                    .clickable(onClick = onBack)
                    .padding(horizontal = AuroraTheme.horizontalPageMargin, vertical = 12.dp),
                color = AuroraTheme.primary,
                fontSize = 19.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                "Заново",
                Modifier
                    .clickable(onClick = onRestart)
                    .padding(horizontal = AuroraTheme.horizontalPageMargin, vertical = 12.dp),
                color = AuroraTheme.primary,
                fontSize = 19.sp
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AuroraTheme.primary.copy(alpha = 0.5f))
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AuroraTheme.paddingLarge)
        ) {
            Text(
                "Результат",
                Modifier.padding(start = AuroraTheme.horizontalPageMargin, top = 18.dp),
                color = AuroraTheme.highlight,
                fontSize = 24.sp
            )
            Text(
                "${quiz.score} из ${questions.size}",
                Modifier.fillMaxWidth(),
                color = AuroraTheme.highlight,
                fontSize = 30.sp,
                textAlign = TextAlign.Center
            )
            val p = quiz.score.toFloat() / questions.size
            Text(
                when {
                    p == 1f -> "Отлично! Все ответы верные."
                    p >= 0.7f -> "Хороший результат!"
                    p >= 0.4f -> "Неплохо, но есть над чем поработать."
                    else -> "Стоит повторить материал лекций."
                },
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AuroraTheme.horizontalPageMargin),
                color = AuroraTheme.primary,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            SectionHeader("Разбор ответов")
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                questions.forEachIndexed { i, q ->
                    val ok = quiz.results.getOrNull(i) == true
                    Text(
                        (if (ok) "✓ " else "✗ ") + "${i + 1}. ${q.answers[q.correct]}",
                        Modifier.padding(horizontal = AuroraTheme.horizontalPageMargin),
                        color = if (ok) AuroraTheme.right else AuroraTheme.wrong,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(Modifier.height(AuroraTheme.paddingLarge))
        }
    }
}

// ---------------------------------------------------------------- О приложении

@Composable
fun AboutPage(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AuroraTheme.paddingLarge)
    ) {
        PageHeader("О приложении")
        Text(
            "«Квиз по ОС Аврора» — викторина из 10 вопросов по разработке приложений для ОС Аврора.",
            Modifier.padding(horizontal = AuroraTheme.horizontalPageMargin),
            color = AuroraTheme.highlight,
            fontSize = 18.sp,
            lineHeight = 25.sp
        )
        SectionHeader("Как пользоваться")
        Text(
            "• Выберите один из четырёх вариантов ответа.\n" +
                "• Верный ответ отмечается зелёным, неверный — красным.\n" +
                "• Полоса вверху показывает прогресс по всем вопросам.\n" +
                "• После последнего вопроса откроется результат с разбором ответов.\n" +
                "• Начать заново можно кнопкой «Заново» или из верхнего меню (потяните страницу вниз).",
            Modifier.padding(horizontal = AuroraTheme.horizontalPageMargin),
            color = AuroraTheme.primary,
            fontSize = 16.sp,
            lineHeight = 23.sp
        )
        SectionHeader("Сведения")
        Text(
            "Android-версия лабораторной работы № 2 (вариант 17), " +
                "повторяющая дизайн и функции приложения для ОС Аврора.",
            Modifier.padding(horizontal = AuroraTheme.horizontalPageMargin),
            color = AuroraTheme.secondary,
            fontSize = 16.sp,
            lineHeight = 23.sp
        )
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AuroraButton("Назад", Modifier.fillMaxWidth(0.42f), onClick = onBack)
        }
        Spacer(Modifier.height(AuroraTheme.paddingLarge))
    }
}
