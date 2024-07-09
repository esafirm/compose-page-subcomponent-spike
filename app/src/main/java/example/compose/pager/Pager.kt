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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Pager(
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
        ) { index ->
            items[index].Content()
        }
    }
}
