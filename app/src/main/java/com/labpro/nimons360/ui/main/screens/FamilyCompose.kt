package com.labpro.nimons360.ui.main.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.viewmodel.FamilyViewModel
import com.labpro.nimons360.viewmodel.FamilyViewModelFactory

@Composable
fun FamilyCompose(
    user: UserData,
    onFamilyClick: (familyId: Int) -> Unit = {},
    vm: FamilyViewModel = viewModel(
        factory = FamilyViewModelFactory(
            (LocalContext.current.applicationContext as MainApplication).familyRepository,
        ),
    ),
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Hello ${user.fullName}! This is the family screen")
    }
}