package com.yasinkacmaz.jetflix.ui.main.movies

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.yasinkacmaz.jetflix.data.Genre
import com.yasinkacmaz.jetflix.ui.main.genres.GenreUiModel
import com.yasinkacmaz.jetflix.ui.main.genres.AmbientSelectedGenre
import com.yasinkacmaz.jetflix.ui.theme.JetflixTheme

@Preview(name = "Preview", group = "Size")
@Preview(name = "At Activity", showSystemUi = true, group = "Devices")
@Preview(name = "Pixel 4XL", showSystemUi = true, device = Devices.PIXEL_4_XL, group = "Devices")
@Preview(name = "Wide", widthDp = 666, group = "Size")
@Composable
private fun MovieMultiPreview() {
    MoviePreview {
        MovieContent(fakeMovie)
    }
}

@Composable
fun MoviePreview(content: @Composable () -> Unit) {
    Providers(AmbientSelectedGenre provides mutableStateOf(GenreUiModel(Genre(-1, "Genre")))) {
        JetflixTheme(content = content)
    }
}
