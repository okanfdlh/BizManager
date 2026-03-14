package com.bizmanager.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PaginationControl(
    currentPage: Int,
    totalPages: Int,
    pageSize: Int,
    totalElements: Int,
    onPageChanged: (Int) -> Unit,
    onPageSizeChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    pageSizeOptions: List<Int> = listOf(10, 20, 50, 100)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Page Size Dropdown
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Baris ter-display:", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(alpha=0.7f))
            Spacer(modifier = Modifier.width(8.dp))
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { expanded = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
                    Text("$pageSize")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    pageSizeOptions.forEach { size ->
                        DropdownMenuItem(onClick = {
                            onPageSizeChanged(size)
                            expanded = false
                        }) {
                            Text("$size")
                        }
                    }
                }
            }
        }

        // Pagination Info & Controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Halaman $currentPage dari $totalPages  (Total: $totalElements)",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha=0.7f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = { if (currentPage > 1) onPageChanged(currentPage - 1) },
                enabled = currentPage > 1
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Page")
            }
            IconButton(
                onClick = { if (currentPage < totalPages) onPageChanged(currentPage + 1) },
                enabled = currentPage < totalPages
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
            }
        }
    }
}

/**
 * Helper to paginate any list in memory
 */
fun <T> paginateList(list: List<T>, page: Int, pageSize: Int): List<T> {
    val fromIndex = ((page - 1) * pageSize).coerceAtLeast(0)
    val toIndex = (fromIndex + pageSize).coerceAtMost(list.size)
    if (fromIndex >= list.size) return emptyList()
    return list.subList(fromIndex, toIndex)
}
