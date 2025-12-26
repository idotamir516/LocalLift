package com.workout.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.data.dao.TemplateWithExercises
import com.workout.app.data.entities.WorkoutFolder
import com.workout.app.ui.components.DeleteFolderDialog
import com.workout.app.ui.components.FolderDialog
import com.workout.app.ui.components.MoveToFolderDialog
import com.workout.app.ui.components.parseColorHex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Templates screen - displays all workout templates organized by folders.
 * 
 * @param templates Flow of templates with their exercises
 * @param folders Flow of folders
 * @param onCreateTemplate Callback to create a new template
 * @param onEditTemplate Callback to edit an existing template
 * @param onDeleteTemplate Callback to delete a template
 * @param onCreateFolder Callback to create a new folder
 * @param onEditFolder Callback to edit a folder
 * @param onDeleteFolder Callback to delete a folder
 * @param onMoveTemplateToFolder Callback to move a template to a folder
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TemplatesScreen(
    templates: Flow<List<TemplateWithExercises>> = flowOf(emptyList()),
    folders: Flow<List<WorkoutFolder>> = flowOf(emptyList()),
    onCreateTemplate: () -> Unit = {},
    onEditTemplate: (templateId: Long) -> Unit = {},
    onDeleteTemplate: (templateId: Long) -> Unit = {},
    onCreateFolder: (name: String, colorHex: String?) -> Unit = { _, _ -> },
    onEditFolder: (folder: WorkoutFolder, newName: String, newColorHex: String?) -> Unit = { _, _, _ -> },
    onDeleteFolder: (folderId: Long) -> Unit = {},
    onMoveTemplateToFolder: (templateId: Long, folderId: Long?) -> Unit = { _, _ -> },
    onOpenSettings: () -> Unit = {},
    onOpenAnalyzer: () -> Unit = {}
) {
    val templateList by templates.collectAsState(initial = emptyList())
    val folderList by folders.collectAsState(initial = emptyList())
    var templateToDelete by remember { mutableStateOf<TemplateWithExercises?>(null) }
    var folderToDelete by remember { mutableStateOf<WorkoutFolder?>(null) }
    var folderToEdit by remember { mutableStateOf<WorkoutFolder?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var templateToMove by remember { mutableStateOf<TemplateWithExercises?>(null) }
    val expandedFolders = remember { mutableStateMapOf<Long, Boolean>() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    // Group templates by folder
    val templatesByFolder = templateList.groupBy { it.template.folderId }
    val uncategorizedTemplates = templatesByFolder[null] ?: emptyList()
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Templates",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                actions = {
                    IconButton(onClick = { showCreateFolderDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "Create Folder"
                        )
                    }
                    IconButton(onClick = onOpenAnalyzer) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analyze Program"
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            // Only show FAB when there are templates or folders
            if (templateList.isNotEmpty() || folderList.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onCreateTemplate,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Template"
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (templateList.isEmpty() && folderList.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "No Templates Yet",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Create a workout template to\nsave your favorite exercises",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        onClick = onCreateTemplate,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create Template",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Folders
                items(
                    items = folderList,
                    key = { "folder_${it.id}" }
                ) { folder ->
                    val isExpanded = expandedFolders[folder.id] ?: false
                    val templatesInFolder = templatesByFolder[folder.id] ?: emptyList()
                    
                    Column {
                        FolderListItem(
                            folder = folder,
                            templateCount = templatesInFolder.size,
                            isExpanded = isExpanded,
                            onClick = { expandedFolders[folder.id] = !isExpanded },
                            onEdit = { folderToEdit = folder },
                            onDelete = { folderToDelete = folder }
                        )
                        
                        // Expanded folder contents
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 24.dp, top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                templatesInFolder.forEach { templateWithExercises ->
                                    TemplateListItem(
                                        templateWithExercises = templateWithExercises,
                                        onEdit = { onEditTemplate(templateWithExercises.template.id) },
                                        onDelete = { templateToDelete = templateWithExercises },
                                        onMove = { templateToMove = templateWithExercises }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Uncategorized templates
                if (uncategorizedTemplates.isNotEmpty()) {
                    if (folderList.isNotEmpty()) {
                        item {
                            Text(
                                text = "Uncategorized",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                    }
                    
                    items(
                        items = uncategorizedTemplates,
                        key = { "template_${it.template.id}" }
                    ) { templateWithExercises ->
                        TemplateListItem(
                            templateWithExercises = templateWithExercises,
                            onEdit = { onEditTemplate(templateWithExercises.template.id) },
                            onDelete = { templateToDelete = templateWithExercises },
                            onMove = { templateToMove = templateWithExercises }
                        )
                    }
                }
            }
        }
    }
    
    // Create folder dialog
    if (showCreateFolderDialog) {
        FolderDialog(
            folder = null,
            onDismiss = { showCreateFolderDialog = false },
            onSave = { name, colorHex ->
                onCreateFolder(name, colorHex)
                showCreateFolderDialog = false
            }
        )
    }
    
    // Edit folder dialog
    folderToEdit?.let { folder ->
        FolderDialog(
            folder = folder,
            onDismiss = { folderToEdit = null },
            onSave = { name, colorHex ->
                onEditFolder(folder, name, colorHex)
                folderToEdit = null
            }
        )
    }
    
    // Delete folder confirmation dialog
    folderToDelete?.let { folder ->
        val templateCount = templatesByFolder[folder.id]?.size ?: 0
        DeleteFolderDialog(
            folder = folder,
            templateCount = templateCount,
            onDismiss = { folderToDelete = null },
            onConfirm = {
                onDeleteFolder(folder.id)
                folderToDelete = null
            }
        )
    }
    
    // Move template to folder dialog
    templateToMove?.let { template ->
        MoveToFolderDialog(
            templateName = template.template.name,
            folders = folderList,
            currentFolderId = template.template.folderId,
            onDismiss = { templateToMove = null },
            onMove = { folderId ->
                onMoveTemplateToFolder(template.template.id, folderId)
                templateToMove = null
            }
        )
    }
    
    // Delete template confirmation dialog
    templateToDelete?.let { template ->
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            title = { Text("Delete Template?") },
            text = { 
                Text("Are you sure you want to delete \"${template.template.name}\"? This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTemplate(template.template.id)
                        templateToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderListItem(
    folder: WorkoutFolder,
    templateCount: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val folderColor = folder.colorHex?.let { parseColorHex(it) } ?: MaterialTheme.colorScheme.primary
    var showMenu by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(folderColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = null,
                    tint = folderColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "$templateCount template${if (templateCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TemplateListItem(
    templateWithExercises: TemplateWithExercises,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false // Don't actually dismiss, let the dialog handle it
            } else {
                false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onEdit,
                    onLongClick = { showMenu = true }
                ),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = templateWithExercises.template.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    val exerciseCount = templateWithExercises.exercises.size
                    val exerciseNames = templateWithExercises.exercises
                        .take(3)
                        .joinToString(", ") { it.exerciseName }
                    val suffix = if (exerciseCount > 3) " +${exerciseCount - 3} more" else ""
                    
                    Text(
                        text = if (exerciseCount == 0) "No exercises" else "$exerciseNames$suffix",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to Folder") },
                            onClick = {
                                showMenu = false
                                onMove()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Folder, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
