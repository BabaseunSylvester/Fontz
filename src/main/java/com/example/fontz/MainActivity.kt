package com.example.fontz

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fontz.data.styles
import com.example.fontz.data.symbolCategories
import com.example.fontz.data.transformText
import com.example.fontz.ui.theme.FontzTheme
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : ComponentActivity() {

    // Variable to hold the Interstitial Ad
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}

        // 2. Load the initial Interstitial Ad
        loadInterstitial()

        enableEdgeToEdge()
        setContent {
            FontzTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FontStylerApp(
                        showInterstitial = { onAdDismissed ->
                            showInterstitial(onAdDismissed)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // Load Interstitial Ad (Test ID)
    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        // Test Ad Unit ID for Interstitial: ca-app-pub-3940256099942544/1033173712
        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            }
        )
    }

    // Show Interstitial Ad logic
    private fun showInterstitial(onAdDismissed: () -> Unit) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    loadInterstitial() // Preload the next ad
                    onAdDismissed() // Execute the action (copy text)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    mInterstitialAd = null
                    onAdDismissed() // Execute action anyway if ad fails
                }
            }
            mInterstitialAd?.show(this)
        } else {
            loadInterstitial() // Try loading again for next time
            onAdDismissed() // Ad wasn't ready, execute action immediately
        }
    }


}



// --- Composable: Banner Ad View ---
@Composable
fun BannerAdView() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                // Test Ad Unit ID for Banner: ca-app-pub-3940256099942544/6300978111
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}




// --- UI Composable ---

enum class AppScreen { Styler, Symbols }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontStylerApp(
    viewModel: FontStylerViewModel = viewModel(),
    showInterstitial: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    // We observe the state from the ViewModel
    // Note: 'currentScreen', 'inputText', etc. are exposed as MutableState in the VM

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("𝔽𝕠𝕟𝕥𝕫 - 𝕊𝕥𝕪𝕝𝕖𝕣 ℙ𝕣𝕖𝕞𝕚𝕦𝕞", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Styler") },
                    label = { Text("Styler") },
                    selected = viewModel.currentScreen == AppScreen.Styler,
                    onClick = { viewModel.navigateTo(AppScreen.Styler) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Face, contentDescription = "Symbols") },
                    label = { Text("Symbols") },
                    selected = viewModel.currentScreen == AppScreen.Symbols,
                    onClick = { viewModel.navigateTo(AppScreen.Symbols) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Pass state and events down. The screen Composables are now stateless.
            when (viewModel.currentScreen) {
                AppScreen.Styler -> StylerScreen(
                    inputText = viewModel.inputText,
                    onTextChange = viewModel::updateInputText,
                    showInterstitial = showInterstitial
                )
                AppScreen.Symbols -> SymbolsScreen(
                    selectedSymbols = viewModel.selectedSymbols,
                    onSymbolsChange = viewModel::updateSelectedSymbols,
                    onAppendSymbol = viewModel::appendSymbol,
                    onClearSymbols = viewModel::clearSymbols,
                    showInterstitial = showInterstitial
                )
            }
        }
    }
}

@Composable
fun StylerScreen(
    inputText: String,
    onTextChange: (String) -> Unit,
    showInterstitial: (() -> Unit) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Input Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onTextChange,
                    label = { Text("Type here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = { onTextChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
                Text(
                    text = "${inputText.length} chars",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                )
            }
        }

        // Banner Ad
        BannerAdView()

        // Styles List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (inputText.isBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Start typing to see magic!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(styles) { style ->
                    val transformedText = transformText(inputText, style.mappedChars)

                    StyleCard(
                        styleName = style.name,
                        styledText = transformedText,
                        onCopy = {
                            // Show Ad first, then execute copy
                            showInterstitial {
                                clipboardManager.setText(AnnotatedString(transformedText))
                                Toast.makeText(context, "Copied ${style.name}!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SymbolsScreen(
    selectedSymbols: String,
    onSymbolsChange: (String) -> Unit,
    onAppendSymbol: (String) -> Unit,
    onClearSymbols: () -> Unit,
    showInterstitial: (() -> Unit) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Result Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = selectedSymbols,
                        onValueChange = onSymbolsChange,
                        label = { Text("Tap symbols below") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (selectedSymbols.isNotEmpty()) {
                            showInterstitial {
                                clipboardManager.setText(AnnotatedString(selectedSymbols))
                                Toast.makeText(context, "Copied symbols!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(painter = painterResource(R.drawable.outline_content_copy_24), contentDescription = "Copy")
                    }
                }

                if (selectedSymbols.isNotEmpty()) {
                    TextButton(
                        onClick = onClearSymbols,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear")
                    }
                }
            }
        }

        // Banner Ad
        BannerAdView()

        // Symbol Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 60.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            symbolCategories.forEach { (categoryName, symbols) ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                items(symbols) { symbol ->
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onAppendSymbol(symbol) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = symbol,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun StyleCard(styleName: String, styledText: String, onCopy: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopy() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = styleName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = styledText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_content_copy_24),
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}





