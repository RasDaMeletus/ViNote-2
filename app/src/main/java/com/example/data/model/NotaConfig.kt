package com.example.data.model

enum class NotaBaseColor {
    SOFT_PINK,
    SOFT_BLUE,
    WARM_YELLOW,
    MINT_GREEN
}

enum class NotaAccessory {
    NONE,
    GLASSES,
    BOWTIE,
    HEADPHONES
}

enum class NotaPresenceMode {
    ALWAYS_VISIBLE,
    MINIMAL
}

enum class NotaEyeState {
    NEUTRAL,      // ● ●
    CURIOUS,      // ⊙ ⊙ or ◉ ◉
    EXCITED,      // ★ ★
    HAPPY,        // ◡ ◡
    THINKING,     // ● ◌
    PROUD         // ✦ ✦
}

data class NotaConfig(
    val baseColor: NotaBaseColor = NotaBaseColor.SOFT_PINK,
    val accessory: NotaAccessory = NotaAccessory.NONE,
    val personalitySlider: Float = 75f, // 0 (Calm & Analytical) to 100 (Playful & Chatty)
    val presenceMode: NotaPresenceMode = NotaPresenceMode.ALWAYS_VISIBLE,
    val offlineAiEngineDownloaded: Boolean = true,
    val eyeState: NotaEyeState = NotaEyeState.HAPPY
)
