package com.example.tunerapp.tuner

enum class ChromaticScale (val noteDisplayed: String,) {
    C("C"),
    CSharp("C#"),
    D("D"),
    DSharp("D#"),
    E("E"),
    F("F"),
    FSharp("F#"),
    G("G"),
    GSharp("G#"),
    A("A"),
    ASharp("A#"),
    B("B");

    companion object{
        fun midiToChromatic(midiNumber: Int): ChromaticScale {
            return entries.first(){it.ordinal == (midiNumber % 12)}
        }
    }
}// todo: crear una funcion para que cambie el note displayed por el valor enharmonico.