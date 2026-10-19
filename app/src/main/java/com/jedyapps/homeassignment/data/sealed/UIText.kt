package com.jedyapps.homeassignment.data.sealed

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

sealed class UIText {
    data class DynamicString(val value: String) : UIText()
    data object Empty : UIText()
    class StringResource(
        @param:StringRes val resId: Int,
        vararg val args: Any
    ) : UIText() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StringResource) return false
            if (resId != other.resId) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + args.contentHashCode()
            return result
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is Empty -> ""
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }

    @Composable
    fun asString(): String {
        return when (this) {
            is Empty -> ""
            is DynamicString -> value
            is StringResource -> LocalContext.current.getString(resId, *args)
        }
    }
}

fun Any?.asUIText(): UIText {
    return when (this) {
        is UIText -> this
        is String -> UIText.DynamicString(this)
        is Throwable -> this.localizedMessage?.let { UIText.DynamicString(it) } ?: UIText.StringResource(com.jedyapps.homeassignment.R.string.error_generic)
        else -> UIText.StringResource(com.jedyapps.homeassignment.R.string.error_generic)
    }
}
