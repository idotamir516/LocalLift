package com.workout.app.data.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.workout.app.data.AppDatabase
import com.workout.app.data.entities.WorkoutFolder
import com.workout.app.data.entities.WorkoutTemplate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TemplateDaoFolderPreservationTest {

    private lateinit var database: AppDatabase
    private lateinit var templateDao: TemplateDao
    private lateinit var folderDao: FolderDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        templateDao = database.templateDao()
        folderDao = database.folderDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun updatingTemplateNameDoesNotRemoveFolderAssignment() = runBlocking {
        val folderId = folderDao.insertFolder(WorkoutFolder(name = "Strength"))
        val createdAt = System.currentTimeMillis() - 10_000

        val templateId = templateDao.insertTemplate(
            WorkoutTemplate(
                name = "Upper A",
                folderId = folderId,
                createdAt = createdAt,
                updatedAt = createdAt
            )
        )

        templateDao.updateTemplateName(templateId = templateId, name = "Upper A - Updated")

        val updatedTemplate = templateDao.getTemplateById(templateId)
        assertNotNull(updatedTemplate)
        assertEquals("Upper A - Updated", updatedTemplate?.name)
        assertEquals(folderId, updatedTemplate?.folderId)
        assertEquals(createdAt, updatedTemplate?.createdAt)
    }

    @Test
    fun movingTemplateExplicitlyCanStillChangeFolder() = runBlocking {
        val originalFolderId = folderDao.insertFolder(WorkoutFolder(name = "Hypertrophy"))
        val templateId = templateDao.insertTemplate(
            WorkoutTemplate(name = "Leg Day", folderId = originalFolderId)
        )

        templateDao.moveTemplateToFolder(templateId = templateId, folderId = null)

        val movedTemplate = templateDao.getTemplateById(templateId)
        assertNotNull(movedTemplate)
        assertNull(movedTemplate?.folderId)
    }
}
