package example.compose.pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class PageInfo(val isVisible: Boolean = false)

val LocalPageInfo = compositionLocalOf { PageInfo() }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPager(
    items: List<Page>,
    searchValue: String,
    onSearchQuery: (String) -> Unit,
) {
    val pagerState = rememberPagerState { items.size }

    Column(Modifier.fillMaxSize()) {
        TextField(
            value = searchValue,
            onValueChange = onSearchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondBoundsPageCount = 1,
        ) { index ->
            println("Pager:: lambda called with index: $index -- currentPage: ${pagerState.currentPage}")

            val pageInfo = PageInfo(isVisible = pagerState.currentPage == index)
            CompositionLocalProvider(LocalPageInfo provides pageInfo) {
                items[index].Content()
            }
        }
    }
}
