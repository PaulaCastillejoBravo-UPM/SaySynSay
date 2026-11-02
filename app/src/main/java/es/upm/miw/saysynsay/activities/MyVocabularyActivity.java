package es.upm.miw.saysynsay.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.upm.miw.saysynsay.R;
import es.upm.miw.saysynsay.adapters.VocabularyAdapter;
import es.upm.miw.saysynsay.models.Entry;
import es.upm.miw.saysynsay.models.SavedWord;
import es.upm.miw.saysynsay.models.Word;

public class MyVocabularyActivity extends AppCompatActivity {
    public static final String EXTRA_PALABRA_SELECCIONADA = "palabraSeleccionada";
    private RecyclerView rvMyVocabulary;
    private FirebaseFirestore firestore;
    private VocabularyAdapter adaptador;
    private List<SavedWord> vocabulario;

    private TextView tvTotalWords;
    private TextView tvTotalCategories;
    private LinearLayout llCategoryStatistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_vocabulary);
        establecerInserciones();

        inicializarComponentes();
        cargarVocabulario();
    }

    private void cargarVocabulario() {
        FirebaseAuth usuario = FirebaseAuth.getInstance();

        if (usuario.getCurrentUser() == null) {
            return;
        }

        String uidUser = usuario.getCurrentUser().getUid();

        firestore.collection("users")
                .document(uidUser)
                .collection("savedWords")
                .orderBy("saveDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(documentos -> {
                    vocabulario.clear();

                    for (QueryDocumentSnapshot documento : documentos) {
                        String id = documento.getId();
                        Word palabra = documento.toObject(Word.class);
                        Timestamp fechaGuardada = documento.getTimestamp("saveDate");

                        if (palabra != null) {
                            SavedWord palabraGuardada = new SavedWord(id, palabra, fechaGuardada);
                            vocabulario.add(palabraGuardada);
                        }
                    }

                    adaptador.notifyDataSetChanged();
                    actualizarEstadisticas();
                })
                .addOnFailureListener(error -> {
                    String mensajeError = error.getMessage() != null ? error.getMessage() : "";

                    Snackbar.make(
                            findViewById(R.id.myVocabulary),
                            getString(R.string.vocabulary_check_error, mensajeError),
                            Snackbar.LENGTH_LONG
                    ).show();
                });
    }

    private void eliminarPalabra(SavedWord palabraGuardada) {
        FirebaseAuth usuario = FirebaseAuth.getInstance();

        if (usuario.getCurrentUser() == null) {
            return;
        }

        String uidUser = usuario.getCurrentUser().getUid();

        firestore.collection("users")
                .document(uidUser)
                .collection("savedWords")
                .document(palabraGuardada.getId())
                .delete()
                .addOnSuccessListener(resultado -> {
                    int posicion = vocabulario.indexOf(palabraGuardada);

                    if (posicion != -1) {
                        vocabulario.remove(posicion);
                        adaptador.notifyItemRemoved(posicion);
                    }

                    actualizarEstadisticas();

                    Snackbar.make(
                            findViewById(R.id.myVocabulary),
                            getString(R.string.vocabulary_remove_success, palabraGuardada.getPalabra().getWord()),
                            Snackbar.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(error -> {
                    String mensajeError = error.getMessage() != null ? error.getMessage() : "";

                    Snackbar.make(
                            findViewById(R.id.myVocabulary),
                            getString(R.string.vocabulary_remove_error, mensajeError),
                            Snackbar.LENGTH_LONG
                    ).show();
                });
    }

    private void actualizarEstadisticas() {
        Map<String, Integer> categorias = new LinkedHashMap<>();

        for (SavedWord palabraGuardada : vocabulario) {
            Word palabra = palabraGuardada.getPalabra();

            Set<String> categoriasPalabra = new LinkedHashSet<>();

            if (palabra.getEntries() != null) {
                for (Entry entrada : palabra.getEntries()) {
                    if (entrada != null && entrada.getPartOfSpeech() != null && !entrada.getPartOfSpeech().trim().isEmpty()) {
                        categoriasPalabra.add(entrada.getPartOfSpeech().trim());
                    }
                }
            }

            for (String categoria : categoriasPalabra) {
                int cantidad = categorias.containsKey(categoria) ? categorias.get(categoria) : 0;

                categorias.put(categoria, cantidad + 1);
            }
        }

        tvTotalWords.setText(String.valueOf(vocabulario.size()));
        tvTotalCategories.setText(String.valueOf(categorias.size()));

        mostrarEstadisticasCategorias(categorias);
    }

    private void mostrarEstadisticasCategorias(Map<String, Integer> categorias) {
        llCategoryStatistics.removeAllViews();

        int totalPalabras = vocabulario.size();

        for (Map.Entry<String, Integer> categoria : categorias.entrySet()) {
            String nombreCategoria = categoria.getKey();
            int cantidad = categoria.getValue();

            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams parametrosFila = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            parametrosFila.setMargins(0, 0, 0, 12);
            fila.setLayoutParams(parametrosFila);

            TextView tvCategoria = new TextView(this);

            tvCategoria.setText(getString(R.string.vocabulary_category_count, nombreCategoria, cantidad));

            tvCategoria.setTextSize(14);
            tvCategoria.setTypeface(null, Typeface.BOLD);
            fila.addView(tvCategoria);

            ProgressBar barra = new ProgressBar(
                    this,
                    null,
                    android.R.attr.progressBarStyleHorizontal
            );

            barra.setMax(totalPalabras);
            barra.setProgress(cantidad);

            LinearLayout.LayoutParams parametrosBarra = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            fila.addView(barra, parametrosBarra);

            llCategoryStatistics.addView(fila);
        }
    }


    private void establecerInserciones() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.myVocabulary), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void inicializarComponentes() {
        rvMyVocabulary = findViewById(R.id.rvMyVocabulary);

        firestore = FirebaseFirestore.getInstance();

        vocabulario = new ArrayList<>();

        rvMyVocabulary.setLayoutManager(new LinearLayoutManager(this));
        rvMyVocabulary.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adaptador = new VocabularyAdapter(vocabulario, palabra -> {
            Intent resultado = new Intent();
            resultado.putExtra(EXTRA_PALABRA_SELECCIONADA, palabra);

            setResult(RESULT_OK, resultado);
            finish();
        }, palabraGuardada -> eliminarPalabra(palabraGuardada));

        rvMyVocabulary.setAdapter(adaptador);

        tvTotalWords = findViewById(R.id.tvTotalWords);
        tvTotalCategories = findViewById(R.id.tvTotalCategories);

        llCategoryStatistics = findViewById(R.id.llCategoryStatistics);
    }
}
