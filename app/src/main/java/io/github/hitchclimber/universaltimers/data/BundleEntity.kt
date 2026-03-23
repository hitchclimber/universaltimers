package io.github.hitchclimber.universaltimers.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity. Blocks are stored as JSON to avoid a complex relational schema.
 */
@Entity(tableName = "bundles")
data class BundleEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** JSON-serialized List<TimerBlock> */
    val blocksJson: String,
)
