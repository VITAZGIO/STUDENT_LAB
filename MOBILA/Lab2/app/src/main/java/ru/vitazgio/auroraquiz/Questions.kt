package ru.vitazgio.auroraquiz

/** Один вопрос квиза: текст, варианты ответа и индекс правильного варианта. */
data class Question(
    val text: String,
    val answers: List<String>,
    val correct: Int,
)

val questions = listOf(
    Question(
        "На основе какой ОС создана ОС Аврора?",
        listOf("Android", "Sailfish OS", "iOS", "Windows Phone"), 1
    ),
    Question(
        "Какой язык используется для описания интерфейса приложений ОС Аврора?",
        listOf("Java", "Swift", "QML", "Kotlin"), 2
    ),
    Question(
        "Какой позиционер располагает элементы вертикально, один под другим?",
        listOf("Row", "Column", "Grid", "Flow"), 1
    ),
    Question(
        "Какой компонент Silica используется для ввода текста?",
        listOf("Label", "Button", "TextField", "Slider"), 2
    ),
    Question(
        "Какой тип обеспечивает стековую модель навигации между страницами?",
        listOf("PageStack", "Cover", "ListModel", "Timer"), 0
    ),
    Question(
        "В каком формате распространяются приложения для ОС Аврора?",
        listOf("APK", "IPA", "RPM", "EXE"), 2
    ),
    Question(
        "Какой элемент Silica отображает заголовок страницы?",
        listOf("PageHeader", "SectionHeader", "Separator", "ValueButton"), 0
    ),
    Question(
        "Как называется представление приложения на домашнем экране, когда оно работает в фоне?",
        listOf("Иконка", "Виджет", "Обложка (Cover)", "Заставка"), 2
    ),
    Question(
        "Какой компонент Silica реализует горизонтальный ползунок?",
        listOf("ProgressBar", "Switch", "Slider", "Separator"), 2
    ),
    Question(
        "Какой объект Silica хранит стандартные цвета, шрифты и отступы текущей темы?",
        listOf("Theme", "Style", "Palette", "Screen"), 0
    ),
)
