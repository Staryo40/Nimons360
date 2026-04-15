package com.labpro.nimons360.data.model.family

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_families")
data class PinnedFamilyEntity(
    @PrimaryKey val familyId: Int
)