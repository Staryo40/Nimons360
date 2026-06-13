package com.labpro.nimons360.ui.features.families

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.WRAP_CONTENT
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.labpro.nimons360.R
import com.labpro.nimons360.ui.theme.Nimons360Theme

/**
 * Join-family popup (Compose-based).
 *
 * Shown from [FamilyDetailFragment] via its [childFragmentManager].
 * On confirmation, sends the entered code back through [setFragmentResult]
 * using [REQUEST_KEY] so [FamilyDetailFragment] can call
 * `viewModel.joinFamily(code)` without tight coupling.
 *
 * Usage from FamilyDetailFragment:
 *   // Show:
 *   JoinFamilyDialog().show(childFragmentManager, JoinFamilyDialog.TAG)
 *
 *   // Listen (in onViewCreated):
 *   childFragmentManager.setFragmentResultListener(JoinFamilyDialog.REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
 *       val code = bundle.getString(JoinFamilyDialog.KEY_CODE) ?: return@setFragmentResultListener
 *       viewModel.joinFamily(code)
 *   }
 */
class JoinFamilyDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_Nimons360)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            Nimons360Theme {
                JoinFamilyDialogContent(
                    onDismiss = { dismiss() },
                    onConfirm = { code ->
                        parentFragmentManager.setFragmentResult(
                            REQUEST_KEY,
                            bundleOf(KEY_CODE to code),
                        )
                        dismiss()
                    },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(WRAP_CONTENT, WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    companion object {
        const val TAG         = "JoinFamilyDialog"
        const val REQUEST_KEY = "join_family_result"
        const val KEY_CODE    = "family_code"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinFamilyDialogContent(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        withFrameNanos {  }
        runCatching { focusRequester.requestFocus() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text  = stringResource(R.string.join_family),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text  = stringResource(R.string.join_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { input ->
                        // Only accept up to 6 alphanumeric characters, forced uppercase
                        if (input.length <= 6) {
                            code = input.uppercase().filter { it.isLetterOrDigit() }
                        }
                        hasError = false
                    },
                    label   = { Text(stringResource(R.string.family_code_hint)) },
                    isError = hasError,
                    supportingText = if (hasError) {
                        { Text(stringResource(R.string.family_code_error)) }
                    } else {
                        { Text(stringResource(R.string.family_code_sub_hint)) }
                    },
                    singleLine = true,
                    textStyle  = LocalTextStyle.current.copy(
                        fontFamily    = FontFamily.Monospace,
                        letterSpacing = 4.sp,
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType   = KeyboardType.Text,
                        imeAction      = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (code.length == 6) onConfirm(code) else hasError = true
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        focusedLabelColor    = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.length == 6) onConfirm(code) else hasError = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(stringResource(R.string.btn_join_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text  = stringResource(R.string.btn_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
    )
}

private val Int.sp get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)
private val Float.sp get() = androidx.compose.ui.unit.TextUnit(this, androidx.compose.ui.unit.TextUnitType.Sp)
