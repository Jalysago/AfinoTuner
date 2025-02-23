package com.example.tunerapp.tuner

enum class ChromaticScale (val noteDisplayed: String, val midiIndex: Int) {
    C("C", 0),
    CSharp("C#", 1),
    D("D", 2),
    DSharp("D#", 3),
    E("E", 4),
    F("F", 5),
    FSharp("F#", 6),
    G("G", 7),
    GSharp("G#", 8),
    A("A", 9),
    ASharp("A#", 10),
    B("B", 11);

    companion object{
        fun midiToChromatic(midiNumber: Int): ChromaticScale {
            return entries.first(){it.midiIndex == (midiNumber % 12)}
        }
    }
}