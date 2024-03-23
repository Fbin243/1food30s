package com.zebrand.app1food30s.ultis

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.zebrand.app1food30s.R

object ValidateInput{
    fun firstNameFocusListener(context: Context, editText: EditText, inputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                inputLayout.error = validFirstName(context, editText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun validFirstName(context: Context, editText: EditText): String? {
        val firstNameText = editText.text.toString()
        if (firstNameText.trim() == "") {
            return context.resources.getString(R.string.txt_required)
        }
        return null
    }

    fun lastNameFocusListener(context: Context, editText: EditText, inputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                inputLayout.error = validLastName(context, editText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun validLastName(context: Context, editText: EditText): String? {
        val lastNameText = editText.text.toString()
        if (lastNameText.trim() == "") {
            return context.resources.getString(R.string.txt_required)
        }
        return null
    }

    fun emailFocusListener(context: Context, editText: EditText, inputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                inputLayout.error = validEmail(context, editText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun validEmail(context: Context, editText: EditText): String? {
        val emailText = editText.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            return context.resources.getString(R.string.txt_invalid_email)
        }
        return null
    }

    fun passwordFocusListener(context: Context, editText: EditText, inputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                inputLayout.error = validPassword(context, editText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun validPassword(context: Context, editText: EditText): String? {
        val passwordText = editText.text.toString()
        if (passwordText.length < 6) {
//            Please fix 3 -> 6 when finish
            return context.resources.getString(R.string.txt_minimum_password)
        }
//        if(!passwordText.matches(".*[A-Z].*".toRegex()))
//        {
//            return "Must Contain 1 Upper-case Character"
//        }
//        if(!passwordText.matches(".*[a-z].*".toRegex()))
//        {
//            return "Must Contain 1 Lower-case Character"
//        }
//        if(!passwordText.matches(".*[@#\$%^&+=].*".toRegex()))
//        {
//            return "Must Contain 1 Special Character (@#\$%^&+=)"
//        }

        return null
    }

    fun rePasswordFocusListener(context: Context, passEditText: EditText, rePassEditText: EditText, inputLayout: TextInputLayout) {
        rePassEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                inputLayout.error = validRePassword(context, passEditText, rePassEditText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun validRePassword(context: Context, passEditText: EditText, rePassEditText: EditText): String? {
        val passwordText = passEditText.text.toString()
        val rePasswordText = rePassEditText.text.toString()
        if (rePasswordText != passwordText) {
            return context.resources.getString(R.string.txt_confirm_password_not_match)
        }

        return null
    }
}