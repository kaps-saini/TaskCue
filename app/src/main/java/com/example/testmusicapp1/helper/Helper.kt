package com.example.testmusicapp1.helper

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.testmusicapp1.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

object Helper {

    fun makeSnackBar(view:View,msg:String){
        Snackbar.make(view,msg,Snackbar.LENGTH_SHORT).show()
    }

    fun btnVisibility(
        context: Context,
        vararg inputs: TextInputEditText?,
        button: Button
    ) {
        val anyInputEmpty = inputs.any { input ->
            input?.text?.toString()?.trim().isNullOrEmpty()
        }

        if (anyInputEmpty) {
            button.alpha = 0.4f
            button.isEnabled = false
            button.setTextColor(ContextCompat.getColor(context, R.color.black))
        } else {
            button.alpha = 1f
            button.isEnabled = true
        }
    }

    fun shareText(firstString:String,secondString:String?):Intent{
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT ,"Share via my app")
            putExtra(Intent.EXTRA_TEXT,"$firstString\n$secondString")
        }
        return shareIntent
    }

    fun showDeleteAccountDialog(context: Context, callback:()->Unit){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Account Deletion")
        builder.setMessage("Are you sure you want to delete your account? This can't be undone")

        builder.setPositiveButton("Delete"){ dialog,_->
            callback()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel"){ dialog,_->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun showLogOutDialog(context: Context, callback:()->Unit){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Signout")
        builder.setMessage("Are you sure you want to sign out your account?")

        builder.setPositiveButton("Yes"){ dialog,_->
            callback()
            dialog.dismiss()
        }
        builder.setNegativeButton("No"){ dialog,_->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}