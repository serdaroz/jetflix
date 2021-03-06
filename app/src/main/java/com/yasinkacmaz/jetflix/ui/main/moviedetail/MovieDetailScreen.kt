package com.yasinkacmaz.jetflix.ui.main.moviedetail

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.rounded.Public
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Providers
import androidx.compose.runtime.ambientOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onActive
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.viewModel
import androidx.compose.ui.zIndex
import com.yasinkacmaz.jetflix.R
import com.yasinkacmaz.jetflix.data.Genre
import com.yasinkacmaz.jetflix.ui.common.error.ErrorColumn
import com.yasinkacmaz.jetflix.ui.common.loading.LoadingColumn
import com.yasinkacmaz.jetflix.ui.main.genres.AmbientSelectedGenre
import com.yasinkacmaz.jetflix.ui.main.moviedetail.credits.Credits
import com.yasinkacmaz.jetflix.ui.main.moviedetail.image.Image
import com.yasinkacmaz.jetflix.ui.main.moviedetail.person.Person
import com.yasinkacmaz.jetflix.ui.navigation.AmbientNavigator
import com.yasinkacmaz.jetflix.ui.navigation.Screen
import com.yasinkacmaz.jetflix.ui.widget.BottomArcShape
import com.yasinkacmaz.jetflix.ui.widget.SpacedRow
import com.yasinkacmaz.jetflix.util.animation.springAnimation
import com.yasinkacmaz.jetflix.util.FetchDominantColorFromPoster
import com.yasinkacmaz.jetflix.util.navigationBarsHeightPlus
import com.yasinkacmaz.jetflix.util.statusBarsPadding
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.ExperimentalCoroutinesApi

val AmbientDominantColor = ambientOf<MutableState<Color>> { error("No dominant color") }

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun MovieDetailScreen(movieId: Int) {
    val movieDetailViewModel: MovieDetailViewModel = viewModel(key = movieId.toString())
    val movieDetailUiState = movieDetailViewModel.uiState.collectAsState().value
    onActive {
        if (movieDetailUiState.movieDetail == null) {
            movieDetailViewModel.fetchMovieDetail(movieId)
        }
    }
    when {
        movieDetailUiState.loading -> {
            val title = stringResource(id = R.string.fetching_movie_detail)
            LoadingColumn(title)
        }
        movieDetailUiState.error != null -> {
            ErrorColumn(movieDetailUiState.error.message.orEmpty())
        }
        movieDetailUiState.movieDetail != null -> {
            val primaryColor = AmbientSelectedGenre.current.value.primaryColor
            val dominantColor = remember(movieDetailUiState.movieDetail.id) { mutableStateOf(primaryColor) }
            Providers(AmbientDominantColor provides dominantColor) {
                AppBar(movieDetailUiState.movieDetail.homepage)
                MovieDetail(movieDetailUiState.movieDetail, movieDetailUiState.credits, movieDetailUiState.images)
            }
        }
    }
}

@Composable
private fun AppBar(homepage: String?) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 2.dp).zIndex(8f)
    ) {
        val navigator = AmbientNavigator.current
        val tint = Color.White
        IconButton(onClick = { navigator.goBack() }) {
            Icon(Icons.Filled.ArrowBack, tint = tint)
        }
        if (homepage != null) {
            val dominantColor = AmbientDominantColor.current.value
            val context = AmbientContext.current
            IconButton(onClick = { openHomepage(context, homepage, dominantColor) }) {
                Icon(Icons.Rounded.Public, tint = tint)
            }
        }
    }
}

private fun openHomepage(context: Context, homepage: String, dominantColor: Color) {
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.setToolbarColor(dominantColor.toArgb()).build()
    customTabsIntent.launchUrl(context, Uri.parse(homepage))
}

@Composable
fun MovieDetail(movieDetail: MovieDetail, credits: Credits, images: List<Image>) {
    ConstraintLayout(Modifier.background(MaterialTheme.colors.surface).verticalScroll(rememberScrollState())) {
        val (backdrop, poster, title, originalTitle, genres, specs, rateStars, tagline, overview) = createRefs()
        val (cast, crew, imagesSection, productionCompanies, space) = createRefs()
        val startGuideline = createGuidelineFromStart(16.dp)
        val endGuideline = createGuidelineFromEnd(16.dp)

        FetchDominantColorFromPoster(movieDetail.posterUrl, AmbientDominantColor.current)
        Backdrop(backdropUrl = movieDetail.backdropUrl, Modifier.constrainAs(backdrop) {})
        Poster(
            movieDetail.posterUrl,
            Modifier.zIndex(17f).width(160.dp).height(240.dp).constrainAs(poster) {
                centerAround(backdrop.bottom)
                linkTo(startGuideline, endGuideline)
            }
        )

        Text(
            text = movieDetail.title,
            style = MaterialTheme.typography.h1.copy(
                fontSize = 26.sp,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp).constrainAs(title) {
                top.linkTo(poster.bottom, 8.dp)
                linkTo(startGuideline, endGuideline)
            }
        )

        if (movieDetail.title != movieDetail.originalTitle) {
            Text(
                text = "(${movieDetail.originalTitle})",
                style = MaterialTheme.typography.subtitle2.copy(
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp).constrainAs(originalTitle) {
                    top.linkTo(title.bottom)
                    linkTo(startGuideline, endGuideline)
                }
            )
        } else {
            Spacer(
                modifier = Modifier.constrainAs(originalTitle) {
                    top.linkTo(title.bottom)
                    linkTo(startGuideline, endGuideline)
                }
            )
        }

        GenreChips(
            movieDetail.genres.take(4),
            modifier = Modifier.Companion.constrainAs(genres) {
                top.linkTo(originalTitle.bottom, 16.dp)
                linkTo(startGuideline, endGuideline)
            }
        )

        MovieFields(
            movieDetail,
            modifier = Modifier.constrainAs(specs) {
                top.linkTo(genres.bottom, 12.dp)
                linkTo(startGuideline, endGuideline)
            }
        )

        RateStars(
            movieDetail.voteAverage,
            modifier = Modifier.constrainAs(rateStars) {
                top.linkTo(specs.bottom, 12.dp)
                linkTo(startGuideline, endGuideline)
            }
        )

        Text(
            text = movieDetail.tagline,
            color = AmbientDominantColor.current.value,
            style = MaterialTheme.typography.body1.copy(
                letterSpacing = 2.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Justify
            ),
            modifier = Modifier.padding(horizontal = 16.dp).constrainAs(tagline) {
                top.linkTo(rateStars.bottom, 32.dp)
            }
        )

        Text(
            text = movieDetail.overview,
            style = MaterialTheme.typography.body2.copy(
                letterSpacing = 2.sp,
                lineHeight = 30.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Justify
            ),
            modifier = Modifier.padding(horizontal = 16.dp).constrainAs(overview) {
                top.linkTo(tagline.bottom, 8.dp)
                linkTo(startGuideline, endGuideline)
            }
        )

        val navigator = AmbientNavigator.current
        MovieSection(
            credits.cast,
            { SectionHeaderWithDetail(R.string.cast) { navigator.navigateTo(Screen.PeopleGrid(credits.cast)) } },
            { Person(it.profilePhotoUrl, it.name, it.character, it.gender, Modifier.width(140.dp)) },
            Modifier.constrainAs(cast) {
                top.linkTo(overview.bottom, 16.dp)
                linkTo(startGuideline, endGuideline)
            },
            tag = "cast"
        )

        MovieSection(
            credits.crew,
            { SectionHeaderWithDetail(R.string.crew) { navigator.navigateTo(Screen.PeopleGrid(credits.crew)) } },
            { Person(it.profilePhotoUrl, it.name, it.character, it.gender, Modifier.width(140.dp)) },
            Modifier.constrainAs(crew) {
                top.linkTo(cast.bottom, 16.dp)
                linkTo(startGuideline, endGuideline)
            },
            tag = "crew"
        )

        MovieSection(
            images,
            { SectionHeaderWithDetail(R.string.images) { navigator.navigateTo(Screen.Images(images = images)) } },
            { Image(it) },
            Modifier.constrainAs(imagesSection) {
                top.linkTo(crew.bottom, 16.dp)
                linkTo(startGuideline, endGuideline)
            }
        )

        MovieSection(
            movieDetail.productionCompanies,
            { MovieSectionHeader(titleResId = R.string.production_companies) },
            { ProductionCompany(it) },
            Modifier.constrainAs(productionCompanies) {
                top.linkTo(imagesSection.bottom, 16.dp)
                linkTo(startGuideline, endGuideline)
            }
        )

        Spacer(
            modifier = Modifier.navigationBarsHeightPlus(16.dp)
                .constrainAs(space) { top.linkTo(productionCompanies.bottom) }
        )
    }
}

@Composable
private fun Backdrop(backdropUrl: String, modifier: Modifier) {
    val arcHeight = 240.dp.value * AmbientDensity.current.density
    Card(
        elevation = 16.dp,
        shape = BottomArcShape(arcHeight = arcHeight),
        modifier = modifier.fillMaxWidth().height(360.dp)
    ) {
        CoilImage(
            data = backdropUrl,
            contentScale = ContentScale.FillHeight,
            colorFilter = ColorFilter(Color(0x23000000), BlendMode.SrcOver),
            modifier = modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Poster(posterUrl: String, modifier: Modifier) {
    val isScaled = remember { mutableStateOf(false) }
    val scale = animate(target = if (isScaled.value) 2.2f else 1f, animSpec = springAnimation)

    Card(
        elevation = 24.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .scale(scale)
            .clickable(onClick = { isScaled.value = !isScaled.value })
    ) {
        CoilImage(data = posterUrl, contentScale = ContentScale.FillHeight)
    }
}

@Composable
private fun GenreChips(genres: List<Genre>, modifier: Modifier) {
    SpacedRow(spaceBetween = 8.dp, modifier = modifier) {
        genres.map(Genre::name).forEach {
            Text(
                text = it.orEmpty(),
                style = MaterialTheme.typography.subtitle1.copy(letterSpacing = 2.sp),
                modifier = Modifier.border(
                    1.25.dp,
                    AmbientDominantColor.current.value,
                    RoundedCornerShape(50)
                ).padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun RateStars(voteAverage: Double, modifier: Modifier) {
    val dominantColor = AmbientDominantColor.current.value
    Row(modifier.padding(start = 4.dp)) {
        val maxVote = 10
        val starCount = 5
        repeat(starCount) { starIndex ->
            val voteStarCount = voteAverage / (maxVote / starCount)
            val (tint, asset) = when {
                voteStarCount >= starIndex + 1 -> {
                    dominantColor to Icons.Filled.Star
                }
                voteStarCount in starIndex.toDouble()..(starIndex + 1).toDouble() -> {
                    dominantColor to Icons.Filled.StarHalf
                }
                else -> {
                    dominantColor to Icons.Filled.StarOutline
                }
            }
            Icon(imageVector = asset, tint = tint)
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
private fun MovieFields(movieDetail: MovieDetail, modifier: Modifier) {
    SpacedRow(spaceBetween = 20.dp, modifier = modifier) {
        val context = AmbientContext.current
        MovieField(context.getString(R.string.release_date), movieDetail.releaseDate)
        MovieField(
            context.getString(R.string.duration),
            context.getString(R.string.duration_minutes, movieDetail.duration.toString())
        )
        MovieField(context.getString(R.string.vote_average), movieDetail.voteAverage.toString())
        MovieField(context.getString(R.string.votes), movieDetail.voteCount.toString())
    }
}

@Composable
private fun MovieField(name: String, value: String) {
    Column {
        Text(
            text = name,
            style = MaterialTheme.typography.subtitle2.copy(fontSize = 13.sp, letterSpacing = 1.sp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
        )
    }
}

@Composable
private fun <T : Any> MovieSection(
    items: List<T>,
    header: @Composable () -> Unit,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier,
    tag: String = ""
) {
    Column(modifier = modifier.fillMaxWidth()) {
        header()
        LazyRow(
            modifier = Modifier.semantics { testTag = tag },
            contentPadding = PaddingValues(16.dp)
        ) {
            items(
                items = items,
                itemContent = { item ->
                    itemContent(item)
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        }
    }
}

@Composable
private fun MovieSectionHeader(@StringRes titleResId: Int) = Text(
    text = stringResource(titleResId),
    color = AmbientDominantColor.current.value,
    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
    modifier = Modifier.padding(start = 16.dp)
)

@Composable
private fun SectionHeaderWithDetail(@StringRes textRes: Int, onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(textRes),
            color = AmbientDominantColor.current.value,
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onClick).padding(4.dp)
        ) {
            Text(
                text = stringResource(R.string.see_all),
                color = AmbientDominantColor.current.value,
                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(end = 4.dp)
            )
            Icon(Icons.Filled.ArrowForward, tint = AmbientDominantColor.current.value)
        }
    }
}

@Composable
private fun Image(image: Image) {
    Card(
        Modifier.width(240.dp).height(160.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 8.dp
    ) {
        CoilImage(
            data = image.url,
            contentScale = ContentScale.Crop,
            error = { Icon(imageVector = Icons.Default.Movie, tint = Color.DarkGray) }
        )
    }
}

@Composable
private fun ProductionCompany(company: ProductionCompany) {
    Card(
        Modifier.width(160.dp).height(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 8.dp
    ) {
        Column(Modifier.background(AmbientDominantColor.current.value.copy(alpha = 0.7f)).padding(4.dp)) {
            CoilImage(
                data = company.logoUrl,
                contentScale = ContentScale.Inside,
                modifier = Modifier.size(150.dp, 85.dp).align(Alignment.CenterHorizontally),
                error = { Icon(imageVector = Icons.Default.Movie, tint = Color.DarkGray) }
            )
            Text(
                text = company.name,
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
            )
        }
    }
}
