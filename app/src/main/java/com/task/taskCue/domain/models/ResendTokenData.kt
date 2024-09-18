package com.task.taskCue.domain.models

import android.os.Parcelable
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResendTokenData(
    val token: PhoneAuthProvider.ForceResendingToken
):Parcelable
