package es.upm.miw.saysynsay;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.ui.auth.AuthUI;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;
import java.util.Locale;

import es.upm.miw.saysynsay.models.Entry;
import es.upm.miw.saysynsay.models.Form;
import es.upm.miw.saysynsay.models.Pronunciation;
import es.upm.miw.saysynsay.models.Sense;
import es.upm.miw.saysynsay.models.Word;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String API_BASE_URL = "https://freedictionaryapi.com/api/v1/";
    private FreeDictionaryRESTAPIService apiService;

    private EditText etWord;

    private TextView tvRespuesta;
    private TextView tvPalabra;
    private TextView tvPronunciacion;

    private Button btnTabDefiniciones;
    private Button btnTabSinonimos;
    private Button btnTabFormas;

    private View indicadorDefiniciones;
    private View indicadorSinonimos;
    private View indicadorFormas;

    private View scrollDefiniciones;
    private View scrollSinonimos;
    private View scrollFormas;

    private LinearLayout layoutDefiniciones;
    private LinearLayout layoutFormas;
    private LinearLayout layoutEtiquetasSinonimos;
    private LinearLayout layoutEtiquetasAntonimos;

    private ImageButton btnPronunciar;
    private TextToSpeech sintetizador;
    private boolean sintetizadorPreparado = false;


    private WordViewModel wordViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        estableceInserciones();

        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        etWord = findViewById(R.id.etWord);

        tvRespuesta = findViewById(R.id.tvRespuesta);
        tvPalabra = findViewById(R.id.tvPalabra);
        tvPronunciacion = findViewById(R.id.tvPronunciacion);

        btnTabDefiniciones = findViewById(R.id.btnTabDefiniciones);
        btnTabSinonimos = findViewById(R.id.btnTabSinonimos);
        btnTabFormas = findViewById(R.id.btnTabFormas);

        indicadorDefiniciones = findViewById(R.id.indicadorDefiniciones);
        indicadorSinonimos = findViewById(R.id.indicadorSinonimos);
        indicadorFormas = findViewById(R.id.indicadorFormas);

        scrollDefiniciones = findViewById(R.id.scrollDefiniciones);
        scrollSinonimos = findViewById(R.id.scrollSinonimos);
        scrollFormas = findViewById(R.id.scrollFormas);

        btnTabDefiniciones.setOnClickListener(v -> wordViewModel.setPestanaSeleccionada(0));
        btnTabSinonimos.setOnClickListener(v -> wordViewModel.setPestanaSeleccionada(1));
        btnTabFormas.setOnClickListener(v -> wordViewModel.setPestanaSeleccionada(2));

        layoutDefiniciones = findViewById(R.id.layoutDefiniciones);
        layoutFormas = findViewById(R.id.layoutFormas);
        layoutEtiquetasSinonimos = findViewById(R.id.layoutEtiquetasSinonimos);
        layoutEtiquetasAntonimos = findViewById(R.id.layoutEtiquetasAntonimos);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(FreeDictionaryRESTAPIService.class);


        sintetizador = new TextToSpeech(this, estado -> {
            if (estado == TextToSpeech.SUCCESS) {
                int resultado = sintetizador.setLanguage(Locale.UK);
                sintetizadorPreparado = resultado != TextToSpeech.LANG_MISSING_DATA && resultado != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });

        btnPronunciar = findViewById(R.id.btnPronunciar);

        btnPronunciar.setOnClickListener(v -> {
            String palabra = tvPalabra.getText().toString();

            if (sintetizadorPreparado && !palabra.isEmpty()) {
                sintetizador.speak(palabra, TextToSpeech.QUEUE_FLUSH, null, "pronunciarPalabra");
            }
        });

        wordViewModel.getPalabra().observe(this, palabra -> {
            if (palabra != null) {
                mostrarResultado(palabra);
            }
        });

        wordViewModel.getPestanaSeleccionada().observe(this, pestana -> {
            if (pestana != null) {
                seleccionarPestana(pestana);
            }
        });

        configuraNavegacion();
    }

    @Override
    protected void onDestroy() {
        if (sintetizador != null) {
            sintetizador.stop();
            sintetizador.shutdown();
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opcLogout) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void buscarPalabra(View v) {
        String word = etWord.getText().toString().trim();
        realizarBusqueda(word, false);
    }

    private void buscarPalabraDesdeChip(String termino) {
        etWord.setText(termino);
        realizarBusqueda(termino, true);
    }

    public void realizarBusqueda(String word, boolean desdeChip) {
        if (word.isEmpty()) {
            tvRespuesta.setText(R.string.search_empty);
            return;
        }

        tvRespuesta.setText("");

        Call<Word> callAsync = apiService.getWord(word);

        callAsync.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(Call<Word> call, Response<Word> response) {
                if (!response.isSuccessful()) {
                    if (response.code() == 404) {
                        tvRespuesta.setText(R.string.search_not_found);
                    } else {
                        tvRespuesta.setText(R.string.search_error);
                    }
                    return;
                }

                Word result = response.body();

                if (result == null || result.getEntries() == null || result.getEntries().isEmpty()) {
                    tvRespuesta.setText(R.string.search_not_found);
                    return;
                }

                if (desdeChip) {
                    wordViewModel.guardarPalabraActual();
                } else {
                    wordViewModel.limpiarHistorialNavegacion();
                }

                wordViewModel.setPalabra(result);


            }

            @Override
            public void onFailure(Call<Word> call, Throwable t) {
                tvRespuesta.setText(R.string.search_error);

                Toast.makeText(
                        MainActivity.this,
                        t.getMessage() == null
                                ? getString(R.string.search_error)
                                : t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }


    private void mostrarResultado(Word palabra) {
        findViewById(R.id.layoutCabeceraPalabra).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutPestanas).setVisibility(View.VISIBLE);

        tvPalabra.setText(palabra.getWord());
        mostrarPronunciacion(palabra);
        mostrarDefiniciones(palabra);
        mostrarFormas(palabra);
        mostrarSinonimosYAntonimos(palabra);
    }

    public void limpiarBuscador(View v) {
        etWord.setText("");

        wordViewModel.setPalabra(null);
        wordViewModel.setPestanaSeleccionada(0);

        tvRespuesta.setText("");
        tvPalabra.setText("");
        tvPronunciacion.setText("");

        findViewById(R.id.layoutCabeceraPalabra).setVisibility(View.GONE);
        findViewById(R.id.layoutPestanas).setVisibility(View.GONE);

        scrollDefiniciones.setVisibility(View.GONE);
        scrollSinonimos.setVisibility(View.GONE);
        scrollFormas.setVisibility(View.GONE);
    }


    private void mostrarDefiniciones(Word word) {
        layoutDefiniciones.removeAllViews();

        for (Entry entry : word.getEntries()) {
            if (layoutDefiniciones.getChildCount() > 0) {
                TextView saltoLinea = new TextView(this);
                saltoLinea.setText("\n");
                layoutDefiniciones.addView(saltoLinea);
            }

            String categoria = entry.getPartOfSpeech();
            TextView tvCategoria = crearBadgeCategoria(categoria);
            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            parametros.setMargins(0, 16, 0, 8);
            layoutDefiniciones.addView(tvCategoria, parametros);

            if (entry.getSenses() != null) {
                int numeroDefinicion = 1;

                for (Sense sense : entry.getSenses()) {
                    if (sense.getDefinition() != null && !sense.getDefinition().isEmpty()) {
                        TextView tvDefinicion = new TextView(this);
                        tvDefinicion.setText(numeroDefinicion + ". " + sense.getDefinition());
                        tvDefinicion.setTextSize(16);
                        LinearLayout.LayoutParams parametrosDefinicion = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        parametrosDefinicion.setMargins(0, 0, 0, 8);
                        layoutDefiniciones.addView(tvDefinicion, parametrosDefinicion);

                        numeroDefinicion++;
                    }

                    if (sense.getExamples() != null && !sense.getExamples().isEmpty()) {
                        TextView tvEjemplos = new TextView(this);

                        tvEjemplos.setText(R.string.examples_label);
                        tvEjemplos.setTextSize(14);
                        tvEjemplos.setTypeface(null, android.graphics.Typeface.BOLD);

                        tvEjemplos.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);

                        LinearLayout.LayoutParams parametrosEjemplos = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        parametrosEjemplos.setMargins(0, 4, 0, 20);
                        layoutDefiniciones.addView(tvEjemplos, parametrosEjemplos);

                        LinearLayout layoutEjemplos = new LinearLayout(this);
                        layoutEjemplos.setOrientation(LinearLayout.VERTICAL);
                        layoutEjemplos.setVisibility(View.GONE);

                        for (String example : sense.getExamples()) {
                            TextView tvEjemplo = new TextView(this);

                            tvEjemplo.setText("• " + example);
                            tvEjemplo.setTextSize(14);

                            LinearLayout.LayoutParams parametrosListaEjemplos = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            parametrosListaEjemplos.setMargins(0, 0, 0, 10);
                            layoutEjemplos.addView(tvEjemplo, parametrosListaEjemplos);
                        }

                        LinearLayout.LayoutParams parametrosContenedorEjemplos = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int espacio = (int) (16 * getResources().getDisplayMetrics().density);
                        parametrosContenedorEjemplos.setMargins(0, 0, 0, espacio);
                        layoutDefiniciones.addView(layoutEjemplos, parametrosContenedorEjemplos);

                        tvEjemplos.setOnClickListener(v -> {
                            if (layoutEjemplos.getVisibility() == View.GONE) {
                                layoutEjemplos.setVisibility(View.VISIBLE);
                                tvEjemplos.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_up_float, 0);
                            } else {
                                layoutEjemplos.setVisibility(View.GONE);
                                tvEjemplos.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
                            }
                        });
                    }
                }
            }
        }
    }

    private void mostrarFormas(Word word) {
        layoutFormas.removeAllViews();

        for (Entry entry : word.getEntries()) {
            if (entry.getForms() != null && !entry.getForms().isEmpty()) {
                String categoria = entry.getPartOfSpeech();

                TextView tvCategoria = crearBadgeCategoria(categoria);
                LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                int espacioSuperior = (int) (16 * getResources().getDisplayMetrics().density);
                int espacioInferior = (int) (8 * getResources().getDisplayMetrics().density);
                parametros.setMargins(0, espacioSuperior, 0, espacioInferior);
                layoutFormas.addView(tvCategoria, parametros);

                for (Form form : entry.getForms()) {
                    LinearLayout tarjeta = new LinearLayout(this);
                    tarjeta.setOrientation(LinearLayout.VERTICAL);
                    tarjeta.setPadding(16, 12, 16, 12);
                    tarjeta.setBackgroundResource(R.drawable.tarjeta_forma);

                    TextView tvForma = new TextView(this);
                    tvForma.setText(form.getWord());
                    tvForma.setTextSize(16);
                    tvForma.setTypeface(null, android.graphics.Typeface.BOLD);

                    tarjeta.addView(tvForma);

                    if (form.getTags() != null && !form.getTags().isEmpty()) {
                        TextView tvEtiquetas = new TextView(this);
                        tvEtiquetas.setText(android.text.TextUtils.join(", ", form.getTags()));
                        tvEtiquetas.setTextSize(14);
                        tarjeta.addView(tvEtiquetas);
                    }

                    LinearLayout.LayoutParams parametrosTarjeta = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    int espacio = (int) (8 * getResources().getDisplayMetrics().density);
                    parametrosTarjeta.setMargins(0, 0, 0, espacio);
                    layoutFormas.addView(tarjeta, parametrosTarjeta);
                }
            }
        }
    }

    private void mostrarSinonimosYAntonimos(Word word) {
        layoutEtiquetasSinonimos.removeAllViews();
        layoutEtiquetasAntonimos.removeAllViews();

        for (Entry entry : word.getEntries()) {
            if (entry.getSenses() != null) {
                int numeroDefinicion = 1;
                boolean tieneSinonimos = false;
                boolean tieneAntonimos = false;

                for (Sense sense : entry.getSenses()) {
                    if (sense.getDefinition() != null && !sense.getDefinition().isEmpty()) {
                        if (sense.getSynonyms() != null && !sense.getSynonyms().isEmpty()) {
                            tieneSinonimos = true;
                        }

                        if (sense.getAntonyms() != null && !sense.getAntonyms().isEmpty()) {
                            tieneAntonimos = true;
                        }
                    }
                }

                if (tieneSinonimos) {
                    agregarEncabezadoCategoria(layoutEtiquetasSinonimos, entry.getPartOfSpeech());
                }

                if (tieneAntonimos) {
                    agregarEncabezadoCategoria(layoutEtiquetasAntonimos, entry.getPartOfSpeech());
                }

                for (Sense sense : entry.getSenses()) {
                    if (sense.getDefinition() != null && !sense.getDefinition().isEmpty()) {
                        if (sense.getSynonyms() != null && !sense.getSynonyms().isEmpty()) {
                            agregarTerminosRelacionados(layoutEtiquetasSinonimos, numeroDefinicion, sense.getSynonyms(), true);
                        }

                        if (sense.getAntonyms() != null && !sense.getAntonyms().isEmpty()) {
                            agregarTerminosRelacionados(layoutEtiquetasAntonimos, numeroDefinicion, sense.getAntonyms(), false);
                        }

                        numeroDefinicion++;
                    }
                }
            }
        }
    }

    private void mostrarPronunciacion(Word palabra) {
        String pronunciacionPrincipal = "";

        for (Entry entrada : palabra.getEntries()) {
            if (entrada.getPronunciations() == null) {
                continue;
            }

            for (Pronunciation pronunciacion : entrada.getPronunciations()) {
                if (pronunciacion.getText() != null && !pronunciacion.getText().isEmpty()) {
                    pronunciacionPrincipal = pronunciacion.getText();
                    break;
                }
            }

            if(!pronunciacionPrincipal.isEmpty()) {
                break;
            }
        }

        tvPronunciacion.setText(pronunciacionPrincipal);
    }


    private void seleccionarPestana(int pestana) {
        indicadorDefiniciones.setVisibility(View.INVISIBLE);
        indicadorSinonimos.setVisibility(View.INVISIBLE);
        indicadorFormas.setVisibility(View.INVISIBLE);

        scrollDefiniciones.setVisibility(View.GONE);
        scrollSinonimos.setVisibility(View.GONE);
        scrollFormas.setVisibility(View.GONE);

        int colorInactivo = getColor(android.R.color.darker_gray);
        int colorActivo = getColor(R.color.blue);

        btnTabDefiniciones.setTextColor(colorInactivo);
        btnTabSinonimos.setTextColor(colorInactivo);
        btnTabFormas.setTextColor(colorInactivo);

        switch (pestana) {
            case 0:
                indicadorDefiniciones.setVisibility(View.VISIBLE);
                btnTabDefiniciones.setTextColor(colorActivo);
                scrollDefiniciones.setVisibility(View.VISIBLE);
                break;

            case 1:
                indicadorSinonimos.setVisibility(View.VISIBLE);
                btnTabSinonimos.setTextColor(colorActivo);
                scrollSinonimos.setVisibility(View.VISIBLE);
                break;

            case 2:
                indicadorFormas.setVisibility(View.VISIBLE);
                btnTabFormas.setTextColor(colorActivo);
                scrollFormas.setVisibility(View.VISIBLE);
                break;
        }
    }

    private TextView crearBadgeCategoria(String categoria) {
        TextView tvCategoria = new TextView(this);

        tvCategoria.setText(categoria);
        tvCategoria.setTextSize(12);
        tvCategoria.setTypeface(null, android.graphics.Typeface.BOLD);
        tvCategoria.setBackgroundResource(R.drawable.badge_categoria);

        int colorFondo = R.color.badge_default_bg;
        int colorTexto = R.color.badge_default_text;

        if (categoria != null) {
            switch (categoria.toLowerCase(java.util.Locale.ROOT)) {
                case "adjective":
                    colorFondo = R.color.badge_adjective_bg;
                    colorTexto = R.color.badge_adjective_text;
                    break;

                case "noun":
                    colorFondo = R.color.badge_noun_bg;
                    colorTexto = R.color.badge_noun_text;
                    break;

                case "verb":
                    colorFondo = R.color.badge_verb_bg;
                    colorTexto = R.color.badge_verb_text;
                    break;

                case "adverb":
                    colorFondo = R.color.badge_adverb_bg;
                    colorTexto = R.color.badge_adverb_text;
                    break;

                case "preposition":
                    colorFondo = R.color.badge_preposition_bg;
                    colorTexto = R.color.badge_preposition_text;
                    break;

                case "interjection":
                    colorFondo = R.color.badge_interjection_bg;
                    colorTexto = R.color.badge_interjection_text;
                    break;

                case "conjunction":
                    colorFondo = R.color.badge_conjunction_bg;
                    colorTexto = R.color.badge_conjunction_text;
                    break;

                case "pronoun":
                    colorFondo = R.color.badge_pronoun_bg;
                    colorTexto = R.color.badge_pronoun_text;
                    break;

                case "determiner":
                    colorFondo = R.color.badge_determiner_bg;
                    colorTexto = R.color.badge_determiner_text;
                    break;

                case "particle":
                    colorFondo = R.color.badge_particle_bg;
                    colorTexto = R.color.badge_particle_text;
                    break;
            }
        }

        tvCategoria.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(colorFondo)));

        tvCategoria.setTextColor(getColor(colorTexto));

        return tvCategoria;
    }

    private void agregarTerminosRelacionados(LinearLayout contenedor, int numeroDefinicion, List<String> terminos, boolean sonSinonimos) {
        TextView tvNumero = new TextView(this);
        tvNumero.setText(numeroDefinicion + ". ");
        tvNumero.setTextSize(14);
        tvNumero.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams parametrosNumero = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int espacioInferior = (int) (8 * getResources().getDisplayMetrics().density);
        parametrosNumero.setMargins(0, 0, 0, espacioInferior);
        contenedor.addView(tvNumero, parametrosNumero);

        FlexboxLayout filaChips = new FlexboxLayout(this);
        filaChips.setFlexWrap(FlexWrap.WRAP);
        filaChips.setPadding(0, 0, 0, 0);

        for (String termino : terminos) {
            TextView tvTermino = new TextView(this);
            tvTermino.setText(termino);
            tvTermino.setTextSize(14);

            if (sonSinonimos) {
                tvTermino.setBackgroundResource(R.drawable.chip_sinonimo);
                tvTermino.setTextColor(getColor(R.color.chip_synonym_text));
            } else {
                tvTermino.setBackgroundResource(R.drawable.chip_antonimo);
                tvTermino.setTextColor(getColor(R.color.chip_antonym_text));
            }

            tvTermino.setOnClickListener(v ->  buscarPalabraDesdeChip(termino));

            FlexboxLayout.LayoutParams parametrosChip = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );

            int margen = (int) (4 * getResources().getDisplayMetrics().density);
            parametrosChip.setMargins(0, 0, margen, margen);
            filaChips.addView(tvTermino, parametrosChip);

        }

        LinearLayout.LayoutParams parametrosFila = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int espacioFila = (int) (12 * getResources().getDisplayMetrics().density);
        parametrosFila.setMargins(0, 0, 0, espacioFila);
        contenedor.addView(filaChips, parametrosFila);
    }

    private void agregarEncabezadoCategoria(LinearLayout contenedor, String categoria) {
        TextView tvCategoria = crearBadgeCategoria(categoria);

        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int espacioSuperior = (int) (16 * getResources().getDisplayMetrics().density);
        int espacioInferior = (int) (8 * getResources().getDisplayMetrics().density);
        parametros.setMargins(0, espacioSuperior, 0, espacioInferior);

        contenedor.addView(tvCategoria, parametros);
    }

    private void configuraNavegacion() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (wordViewModel.hayPalabrasAnteriores()) {
                    wordViewModel.recuperarPalabraAnterior();

                    Word palabraAnterior = wordViewModel.getPalabra().getValue();

                    if (palabraAnterior != null) {
                        etWord.setText(palabraAnterior.getWord());
                    }

                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void estableceInserciones() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}