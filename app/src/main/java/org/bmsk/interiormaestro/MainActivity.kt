package org.bmsk.interiormaestro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.bmsk.interiormaestro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var emailTextIsNotEmpty = false
    private var passwordTextIsNotEmpty = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpEmailEditText()
        setUpPasswordEditText()
        setUpSignInOutButton()
        setUpSignUpButton()
    }

    override fun onStart() {
        super.onStart()

        if(Firebase.auth.currentUser == null) {
            initViewsToSignOutState()
        } else {
            initViewsToSignInState()
        }
    }

    private fun setUpEmailEditText() {
        binding.emailEditText.addTextChangedListener {
            emailTextIsNotEmpty = it.toString().isNotEmpty()
        }
    }

    private fun setUpPasswordEditText() {
        binding.passwordEditText.addTextChangedListener {
            passwordTextIsNotEmpty = it.toString().isNotEmpty()
        }
    }

    private fun setUpSignUpButton() {
        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (emailTextIsNotEmpty.not() || passwordTextIsNotEmpty.not()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.guid_input_email_or_password),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (password.length < MIN_PASSWORD_LENGTH) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.guid_min_password_length),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.guid_sign_up_success),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        initViewsToSignInState()
                    } else {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.guid_fail_sign_up),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun setUpSignInOutButton() {
        binding.signInOutButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (Firebase.auth.currentUser == null) {
                // 로그인 과정
                if (emailTextIsNotEmpty.not() || passwordTextIsNotEmpty.not()) {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.guid_input_email_or_password),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            initViewsToSignInState()
                        } else {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.guid_fail_sign_in_check_email_or_password),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener {
                        it.printStackTrace()
                    }
            } else {
                // 로그아웃 과정
                Firebase.auth.signOut()
                initViewsToSignOutState()
            }
        }
    }

    private fun initViewsToSignInState() {
        binding.emailEditText.setText(Firebase.auth.currentUser?.email)
        binding.emailEditText.isEnabled = false
        binding.passwordEditText.isVisible = false
        binding.signInOutButton.text = getString(R.string.sign_out)
        binding.signUpButton.isEnabled = false
    }

    private fun initViewsToSignOutState() {
        binding.emailEditText.text.clear()
        binding.passwordEditText.text.clear()
        binding.emailEditText.isEnabled = true
        binding.passwordEditText.isVisible = true
        binding.signInOutButton.text = getString(R.string.sign_in)
        binding.signUpButton.isEnabled = true
    }
}