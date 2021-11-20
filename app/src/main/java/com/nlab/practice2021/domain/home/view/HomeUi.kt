package com.nlab.practice2021.domain.home.view

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * @author Doohyun
 */
@Composable
fun Home() {
    Text(text = "Hello", color = Color.White)
}

@Preview("HomePreview")
@Composable
fun HomePreview() {
    Home()
}