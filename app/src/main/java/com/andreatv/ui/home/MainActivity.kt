package com.andreatv.ui.home

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andreatv.R
import com.andreatv.ui.home.adapter.PosterAdapter
import com.andreatv.ui.home.model.Poster

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        setupRow(R.id.popular_movies, popularMovies())
        setupRow(R.id.series, series())
        setupFocusAnimation(findViewById(R.id.search_action))
        setupFocusAnimation(findViewById(R.id.settings_action))
        setupFocusAnimation(findViewById(R.id.play_action))

        findViewById<View>(R.id.play_action).requestFocus()
    }

    private fun setupRow(viewId: Int, items: List<Poster>) {
        findViewById<RecyclerView>(viewId).apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = PosterAdapter(items)
            setHasFixedSize(true)
            itemAnimator = null
        }
    }

    private fun setupFocusAnimation(view: View) {
        view.setOnFocusChangeListener { focusedView, hasFocus ->
            val scale = if (hasFocus) FOCUSED_SCALE else 1f
            focusedView.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(FOCUS_ANIMATION_MS)
                .start()
        }
    }

    private fun popularMovies() = listOf(
        Poster("Horizonte", "Ciencia ficción", R.drawable.poster_sunset),
        Poster("Código Norte", "Suspenso", R.drawable.poster_blue),
        Poster("La Última Ruta", "Aventura", R.drawable.poster_red),
        Poster("Medianoche", "Drama", R.drawable.poster_purple),
        Poster("Órbita 7", "Ciencia ficción", R.drawable.poster_teal),
        Poster("Fuego Cruzado", "Acción", R.drawable.poster_orange),
        Poster("El Umbral", "Misterio", R.drawable.poster_indigo)
    )

    private fun series() = listOf(
        Poster("Distrito 9", "Serie · T2", R.drawable.poster_indigo),
        Poster("Bajo Cero", "Serie · T1", R.drawable.poster_blue),
        Poster("Punto de Quiebre", "Serie · T3", R.drawable.poster_red),
        Poster("Ecos", "Serie · T1", R.drawable.poster_teal),
        Poster("Archivo 24", "Serie · T4", R.drawable.poster_orange),
        Poster("Ciudad Oculta", "Serie · T2", R.drawable.poster_purple),
        Poster("Los Viajeros", "Serie · T1", R.drawable.poster_sunset)
    )

    companion object {
        private const val FOCUSED_SCALE = 1.05f
        private const val FOCUS_ANIMATION_MS = 120L
    }
}
