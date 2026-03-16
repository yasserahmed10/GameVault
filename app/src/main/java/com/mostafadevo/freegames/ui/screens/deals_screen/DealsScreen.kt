package com.mostafadevo.freegames.ui.screens.deals_screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mostafadevo.freegames.ui.components.DealsListItem
import com.mostafadevo.freegames.ui.components.FilterIcon
import com.mostafadevo.freegames.ui.components.GiveawayListItem
import com.mostafadevo.freegames.ui.components.History
import com.mostafadevo.freegames.ui.components.ShimmeringText
import com.mostafadevo.freegames.utils.openUrl
import com.mostafadevo.freegames.R
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun DealsScreen(
    viewModel: DealsAndGiveawayScreenViewModel,
) {
    val state = viewModel.dealsAndGiveawayScreenUiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val options = listOf(
        stringResource(R.string.deals_tab),
        stringResource(R.string.giveaways_tab)
    )
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val sheetStateGiveaways = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.dealsSearchBarText) {
        if (state.dealsSearchBarText.isEmpty() && state.dealsSearchBardata?.isNotEmpty() == true) {
            viewModel.onEvent(DealsAndGiveawayScreenUiEvent.OnClearSearchBarDeals) // clear list of deals inside search bar
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        AnimatedVisibility(
            visible = state.isDealsSearchBarActive.not(),
            enter = scaleIn(transformOrigin = TransformOrigin.Center),
            exit = scaleOut(transformOrigin = TransformOrigin.Center)
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = {
                            viewModel.onEvent(DealsAndGiveawayScreenUiEvent.onTabSelected(index))
                        },
                        selected = index == state.selectedTab
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }, floatingActionButton = {
            if (
                state.selectedTab == 0 && state.isDealsSearchBarActive.not()
            ) {
                FloatingActionButton(
                    onClick = {
                        viewModel.onEvent(DealsAndGiveawayScreenUiEvent.OnToggleBottomSheet(true))
                    }
                ) {
                    Icon(
                        imageVector = FilterIcon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (state.selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        viewModel.onEvent(
                            DealsAndGiveawayScreenUiEvent.OnToggleGiveawaysBottomSheet(
                                true
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = FilterIcon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }) { innerpadding ->
        // deals content
        LaunchedEffect(true) {
            viewModel.uiEffect.collect {
                when (it) {
                    is DealsAndGiveawayScreenUiEffect.ShowSnackBar -> {
                        // show snackbar
                        snackbarHostState.showSnackbar(it.message)
                    }
                }
            }
        }
        // animateddpasstate padding search bar
        val searchbarPaddingAnimation = animateDpAsState(
            targetValue = 8.dp
        )

        // offers screen content
        // deals screen
        AnimatedVisibility(
            modifier = Modifier.padding(innerpadding),
            visible = state.selectedTab == 0,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Column(
                modifier = Modifier.fillMaxSize()

            ) {
                SearchBar(
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_for_deals),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            if (state.isDealsSearchBarActive) 0.dp else searchbarPaddingAnimation.value
                        ),
                    query = state.dealsSearchBarText, onQueryChange = {
                        viewModel.onEvent(DealsAndGiveawayScreenUiEvent.OnSearchBarTextChanged(it))
                    }, onSearch = { searchQuery ->
                        searchQuery.let {
                            viewModel.onEvent(
                                DealsAndGiveawayScreenUiEvent.OnSearchBarTextSubmit(it)
                            )
                            // hide keyboard
                            keyboardController?.hide()
                        }
                    }, active = state.isDealsSearchBarActive, onActiveChange = {
                        viewModel.onEvent(DealsAndGiveawayScreenUiEvent.OnToggleSearchBar(it))
                    }, leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.icon_favorite)
                        )
                    }, trailingIcon = {
                        if (state.isDealsSearchBarActive) {
                            IconButton(onClick = {
                                if (state.dealsSearchBarText.isNotEmpty()) {
                                    viewModel.onEvent(
                                        DealsAndGiveawayScreenUiEvent.OnSearchBarTextChanged(
                                            ""
                                        )
                                    )
                                } else {
                                    viewModel.onEvent(
                                        DealsAndGiveawayScreenUiEvent.OnToggleSearchBar(
                                            false
                                        )
                                    )
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.icon_close)
                                )
                            }
                        }
                    }
                ) {
                    // TODO:change the status bar color to be surface container color
                    // Search bar content
                    if (state.dealsSearchBarText.isEmpty() || state.dealsSearchBarText.isBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                                .imeNestedScroll()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.recent_searches),
                                style = MaterialTheme.typography.bodySmall
                            )
                            state.dealsSearchHistory?.forEach { deal ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onEvent(
                                                DealsAndGiveawayScreenUiEvent.OnSearchBarTextChanged(
                                                    deal
                                                )
                                            )
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = History,
                                        contentDescription = stringResource(R.string.icon_history)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = deal
                                    )
                                }
                            }
                        }
                    }

                    if (state.isDealsSearchBarLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    state.dealsSearchBardata?.let {
                        LazyColumn() {
                            if (state.dealsSearchBardata.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.search_results, state.dealsSearchBardata.size),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(
                                                Alignment.CenterHorizontally
                                            )
                                    )
                                }
                            }
                            items(it, key = { it.dealID }) { deal ->
                                DealsListItem(
                                    modifier = Modifier.animateItem(),
                                    onClickListener = {
                                        openUrl(
                                            context,
                                            "https://www.cheapshark.com/redirect?dealID=${deal.dealID}"
                                        )
                                    },
                                    imageLink = deal.thumb,
                                    string1 = deal.title,
                                    string2 = deal.salePrice + "$",
                                    string3LineThrough = "${deal.normalPrice}$",
                                    string4 = "${String.format(Locale.US, "%.2f", deal.savings.toFloatOrNull() ?: 0f)}% off"
                                )
                            }
                        }
                    }
                }
                if (state.isDealsLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ShimmeringText(
                            text = stringResource(R.string.loading_deals),
                            shimmerColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier.testTag("deals_list")
                ) {
                    items(state.deals!!, key = { it.dealID }) { deal ->
                        DealsListItem(
                            modifier = Modifier.animateItem(),
                            onClickListener = {
                                openUrl(
                                    context,
                                    "https://www.cheapshark.com/redirect?dealID=${deal.dealID}"
                                )
                            },
                            imageLink = deal.thumb,
                            string1 = deal.title,
                            string2 = deal.salePrice + "$",
                            string3LineThrough = "${deal.normalPrice}$",
                            string4 = "${String.format(Locale.US, "%.2f", deal.savings.toFloatOrNull() ?: 0f)}% off"
                        )
                    }
                }
            }
        }

        // giveaways screen
        AnimatedVisibility(
            modifier = Modifier.padding(innerpadding),
            visible = state.selectedTab == 1,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            if (state.isGiveawaysLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ShimmeringText(
                        text = stringResource(R.string.loading_giveaways),
                        shimmerColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.testTag("giveaways_list")
            ) {
                items(state.giveaways!!, key = { it.id }) { giveaway ->
                    GiveawayListItem(
                        modifier = Modifier.animateItem(),
                        onClickListener = {
                            openUrl(context, giveaway.open_giveaway_url)
                        },
                        imageLink = giveaway.thumbnail,
                        title = giveaway.title,
                        shortDescription = giveaway.description,
                        timeRemaining = giveaway.end_date,
                        users = giveaway.users
                    )
                }
            }

            // TODO: add filters by shop , onSale , sortby
        }
        if (state.isBottomSheetVisible) {
            val listOfSortByOptions = listOf(
                stringResource(R.string.sort_deal_rating),
                stringResource(R.string.sort_title),
                stringResource(R.string.sort_savings),
                stringResource(R.string.sort_price),
                stringResource(R.string.sort_metacritic),
                stringResource(R.string.sort_reviews),
                stringResource(R.string.sort_release),
                stringResource(R.string.sort_store),
                stringResource(R.string.sort_recent)
            )
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.onEvent(DealsAndGiveawayScreenUiEvent.OnToggleBottomSheet(false))
                },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(R.string.filtering_options),
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.sort_by),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1f)
                        )

                        Spacer(modifier = Modifier.weight(1f))
                        Text(stringResource(R.string.descending), modifier = Modifier.padding(end = 8.dp))
                        Switch(
                            state.filterDesc ?: false,
                            onCheckedChange = {
                                viewModel.onEvent(
                                    DealsAndGiveawayScreenUiEvent.OnDescFilterChanged(
                                        it
                                    )
                                )
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOfSortByOptions.forEachIndexed { index, it ->
                            FilterChip(
                                onClick = {
                                    if (state.filterSortBy == it) {
                                        viewModel.onEvent(
                                            DealsAndGiveawayScreenUiEvent.OnSortByFilterChanged(
                                                ""
                                            )
                                        )
                                    } else {
                                        viewModel.onEvent(
                                            DealsAndGiveawayScreenUiEvent.OnSortByFilterChanged(
                                                it
                                            )
                                        )
                                    }
                                },
                                label = {
                                    Text(it)
                                },
                                selected = state.filterSortBy == it,
                                leadingIcon = if (state.filterSortBy == it) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = stringResource(R.string.icon_done),
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )

                    val store = listOf(
                        1 to stringResource(R.string.store_steam),
                        2 to stringResource(R.string.store_gamersgate),
                        3 to stringResource(R.string.store_greenmangaming),
                        4 to stringResource(R.string.store_amazon),
                        5 to stringResource(R.string.store_gamestop),
                        6 to stringResource(R.string.store_direct2drive),
                        7 to stringResource(R.string.store_gog),
                        8 to stringResource(R.string.store_origin),
                        9 to stringResource(R.string.store_get_games),
                        10 to stringResource(R.string.store_shiny_loot),
                        11 to stringResource(R.string.store_humble),
                        12 to stringResource(R.string.store_desura),
                        13 to stringResource(R.string.store_uplay),
                        14 to stringResource(R.string.store_indie_gamestand),
                        15 to stringResource(R.string.store_fanatical),
                        16 to stringResource(R.string.store_gamesrocket),
                        17 to stringResource(R.string.store_games_republic),
                        18 to stringResource(R.string.store_sila_games),
                        19 to stringResource(R.string.store_playfield),
                        20 to stringResource(R.string.store_imperial_games),
                        21 to stringResource(R.string.store_wingamestore),
                        22 to stringResource(R.string.store_funstockdigital),
                        23 to stringResource(R.string.store_gamebillet),
                        24 to stringResource(R.string.store_voidu),
                        25 to stringResource(R.string.store_epic_games),
                        26 to stringResource(R.string.store_razer),
                        27 to stringResource(R.string.store_gamesplanet),
                        28 to stringResource(R.string.store_gamesload),
                        29 to stringResource(R.string.store_2game),
                        30 to stringResource(R.string.store_indiegal),
                        31 to stringResource(R.string.store_blizzard),
                        32 to stringResource(R.string.store_allyouplay),
                        33 to stringResource(R.string.store_dlgamer),
                        34 to stringResource(R.string.store_noctre),
                        35 to stringResource(R.string.store_dreamgame)
                    )
                    Text(
                        text = stringResource(R.string.store),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        store.forEach { (id, name) ->
                            FilterChip(
                                onClick = {
                                    if (state.filterStoreId == id.toString()) {
                                        viewModel.onEvent(
                                            DealsAndGiveawayScreenUiEvent.OnStoreFilterChanged(
                                                ""
                                            )
                                        )
                                    } else {
                                        viewModel.onEvent(
                                            DealsAndGiveawayScreenUiEvent.OnStoreFilterChanged(
                                                id.toString()
                                            )
                                        )
                                    }
                                },
                                label = {
                                    Text(name)
                                },
                                selected = state.filterStoreId == id.toString(),
                                leadingIcon = if (state.filterStoreId == id.toString()) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = stringResource(R.string.icon_done),
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )
                    Text(
                        text = stringResource(R.string.price),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    // range slider
                    RangeSlider(
                        modifier = Modifier.padding(8.dp),
                        value = (
                            state.filterLowerPrice
                                ?: 0f
                            )..(state.filterUpperPrice ?: 50f),
                        onValueChange = { range ->
                            viewModel.onEvent(
                                DealsAndGiveawayScreenUiEvent.OnLowerPriceFilterChanged(
                                    range.start.toInt()
                                )
                            )
                            viewModel.onEvent(
                                DealsAndGiveawayScreenUiEvent.OnUpperPriceFilterChanged(
                                    range.endInclusive.toInt()
                                )
                            )
                        },
                        valueRange = 0f..50f,
                        steps = 50
                    )
                    val rangeStart = "%.2f".format(state.filterLowerPrice ?: 0f)
                    val rangeEnd = "%.2f".format(state.filterUpperPrice ?: 50f)
                    Text(
                        text = "$rangeStart .. $rangeEnd",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )
                    // filter games onSale
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.on_sale_games),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(state.filterOnSale ?: false, onCheckedChange = {
                            viewModel.onEvent(
                                DealsAndGiveawayScreenUiEvent.OnOnSaleFilterChanged(it)
                            )
                        })
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )
                    Button(
                        onClick = {
                            viewModel.onEvent(DealsAndGiveawayScreenUiEvent.OnApplyDealsFilters)
                            viewModel.onEvent(
                                DealsAndGiveawayScreenUiEvent.OnToggleBottomSheet(false)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.apply_filters)
                        )
                    }
                }
            }
        }
        if (state.isGiveawaysBottomSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.onEvent(
                        DealsAndGiveawayScreenUiEvent.OnToggleGiveawaysBottomSheet(
                            false
                        )
                    )
                },
                sheetState = sheetStateGiveaways
            ) {
                val giveawaysPlatform = listOf(
                    stringResource(R.string.platform_pc),
                    stringResource(R.string.platform_steam),
                    stringResource(R.string.platform_epic),
                    stringResource(R.string.platform_ubisoft),
                    stringResource(R.string.platform_gog),
                    stringResource(R.string.platform_itchio),
                    stringResource(R.string.platform_ps4),
                    stringResource(R.string.platform_ps5),
                    stringResource(R.string.platform_xbox_one),
                    stringResource(R.string.platform_xbox_series),
                    stringResource(R.string.platform_switch),
                    stringResource(R.string.platform_android),
                    stringResource(R.string.platform_ios),
                    stringResource(R.string.platform_vr),
                    stringResource(R.string.platform_battlenet),
                    stringResource(R.string.platform_origin),
                    stringResource(R.string.platform_drm_free),
                    stringResource(R.string.platform_xbox_360)
                )
                val giveawaysSortOptions = listOf(
                    stringResource(R.string.sort_date),
                    stringResource(R.string.sort_value),
                    stringResource(R.string.sort_popularity)
                )
                val giveawaysType = listOf(
                    stringResource(R.string.type_game),
                    stringResource(R.string.type_loot),
                    stringResource(R.string.type_beta)
                )

                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(R.string.filtering_options),
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )

                    Text(
                        text = stringResource(R.string.sort_by),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        giveawaysSortOptions.forEachIndexed { index, it ->
                            FilterChip(
                                onClick = {
                                    if (state.giveawaysFilterSortBy == it) {
                                        viewModel.onEvent(
                                            DealsAndGiveawayScreenUiEvent.OnGiveawaysSortByFilterChanged(
                                                null
                                            )
                                        )
                                    } else {
                                        viewModel.onEvent(
                                            DealsAndGiveawayScreenUiEvent.OnGiveawaysSortByFilterChanged(
                                                it
                                            )
                                        )
                                    }
                                },
                                label = {
                                    Text(it.lowercase().capitalize().replace("_", " "))
                                },
                                selected = state.giveawaysFilterSortBy == it,
                                leadingIcon = if (state.giveawaysFilterSortBy == it) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = stringResource(R.string.icon_done),
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )

                    Text(
                        text = stringResource(R.string.type),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        giveawaysType.forEach { it ->
                            FilterChip(
                                onClick = {
                                    if (state.giveawaysFilterType == it) {
                                        viewModel.onEvent(
                                            DealsAndGiveawayScreenUiEvent.OnGiveawaysTypeFilterChanged(
                                                null
                                            )
                                        )
                                    } else {
                                        viewModel.onEvent(
                                            DealsAndGiveawayScreenUiEvent.OnGiveawaysTypeFilterChanged(
                                                it
                                            )
                                        )
                                    }
                                },
                                label = {
                                    Text(it.lowercase().capitalize().replace("_", " "))
                                },
                                selected = state.giveawaysFilterType == it,
                                leadingIcon = if (state.giveawaysFilterType == it) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = stringResource(R.string.icon_done),
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )
                    Text(
                        text = stringResource(R.string.platform),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        giveawaysPlatform.forEach { it ->
                            FilterChip(
                                onClick = {
                                    if (state.giveawaysFilterPlatform == it) {
                                        viewModel.onEvent(
                                            DealsAndGiveawayScreenUiEvent.OnGiveawaysPlatformFilterChanged(
                                                null
                                            )
                                        )
                                    } else {
                                        viewModel.onEvent(
                                            DealsAndGiveawayScreenUiEvent.OnGiveawaysPlatformFilterChanged(
                                                it
                                            )
                                        )
                                    }
                                },
                                label = {
                                    Text(it.lowercase().capitalize().replace("_", " "))
                                },
                                selected = state.giveawaysFilterPlatform == it,
                                leadingIcon = if (state.giveawaysFilterPlatform == it) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = stringResource(R.string.icon_done),
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )
                    Button(
                        onClick = {
                            viewModel.onEvent(DealsAndGiveawayScreenUiEvent.OnApplyGiveawaysFilters)
                            viewModel.onEvent(
                                DealsAndGiveawayScreenUiEvent.OnToggleGiveawaysBottomSheet(false)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.apply_filters)
                        )
                    }
                }
            }
        }
    }
}
