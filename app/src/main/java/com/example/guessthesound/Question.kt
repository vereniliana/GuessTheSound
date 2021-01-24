package com.example.guessthesound

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Question (
    val url: String,
    val answer: String
): Parcelable
