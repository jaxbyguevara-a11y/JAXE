package com.jaxia.app

import android.app.Application
import com.jaxia.app.data.database.AppDatabase
import com.jaxia.app.data.repository.JaxiaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Owns the single database and repository instance for the whole process.
 *
 * Previously both were constructed inside [MainActivity.onCreate], which runs
 * again on every configuration change. Each new repository started its own
 * `CoroutineScope(Dispatchers.IO)` that nothing ever cancelled, so rotating the
 * device leaked one scope per rotation while the retained ViewModel kept using
 * the original instance (AUDITORIA.md A-03).
 */
class JaxiaApplication : Application() {

    /**
     * Application-lifetime scope for work that must outlive any single screen —
     * currently only the one-shot seeding of demo content. [SupervisorJob] keeps
     * a failure in one child from tearing the rest down.
     */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    val repository: JaxiaRepository by lazy {
        JaxiaRepository(
            garmentDao = database.garmentDao(),
            avatarDao = database.avatarDao(),
            outfitDao = database.outfitDao(),
            communityDao = database.communityDao(),
            secondChanceDao = database.secondChanceDao(),
            externalScope = applicationScope,
        )
    }
}
