package es.upm.miw.saysynsay.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.miw.saysynsay.R;
import es.upm.miw.saysynsay.adapters.HistoryAdapter;
import es.upm.miw.saysynsay.models.SearchHistory;

public class HistoryActivity extends AppCompatActivity {
    public static final String EXTRA_PALABRA_SELECCIONADA = "palabraSeleccionada";
    private static final String DATABASE_URL = "https://saysynsay-default-rtdb.europe-west1.firebasedatabase.app/";

    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private List<SearchHistory> historial;

    private FirebaseDatabase database;
    private DatabaseReference referenciaHistorial;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        establecerInserciones();

        inicializarRecyclerView();
        inicializarFirebase();
        cargarHistorial();
    }


    private void devolverPalabraSeleccionada(String palabra) {
        Intent respuesta = new Intent();
        respuesta.putExtra(EXTRA_PALABRA_SELECCIONADA, palabra);

        setResult(RESULT_OK, respuesta);
        finish();
    }

    private void cargarHistorial() {
        referenciaHistorial.orderByChild("fecha").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historial.clear();

                for (DataSnapshot registro : snapshot.getChildren()) {
                    SearchHistory busqueda = registro.getValue(SearchHistory.class);

                    if (busqueda != null) {
                        busqueda.setId(registro.getKey());
                        historial.add(busqueda);
                    }
                }

                Collections.reverse(historial);

                historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HistoryActivity", "Error al recuperar el historial", error.toException());
            }
        });
    }

    private ItemTouchHelper configurarDeslizamiento() {
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();

                if(position == RecyclerView.NO_POSITION){
                    return;
                }

                SearchHistory busqueda = historial.get(position);
                String id = busqueda.getId();

                referenciaHistorial.child(id).removeValue()
                        .addOnSuccessListener(unused -> {
                            int posicionActual = historial.indexOf(busqueda);

                            if(posicionActual != -1){
                                historial.remove(posicionActual);
                                historyAdapter.notifyItemRemoved(posicionActual);

                                Snackbar.make(
                                        rvHistory,
                                        R.string.history_delete_success,
                                        Snackbar.LENGTH_SHORT
                                ).show();
                            }
                        })
                        .addOnFailureListener(error -> {
                            int posicionActual = historial.indexOf(busqueda);

                            if (posicionActual != -1) {
                                historyAdapter.notifyItemChanged(posicionActual);


                            }

                            Snackbar.make(
                                    rvHistory,
                                    R.string.history_delete_error,
                                    Snackbar.LENGTH_LONG
                            ).show();
                        });
            }
        });

        return helper;
    }

    private void establecerInserciones() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void inicializarRecyclerView() {
        rvHistory = findViewById(R.id.rvHistory);

        historial = new ArrayList<>();

        historyAdapter = new HistoryAdapter(historial, this::devolverPalabraSeleccionada);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration separator = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);

        rvHistory.addItemDecoration(separator);
        rvHistory.setAdapter(historyAdapter);

        ItemTouchHelper helper = configurarDeslizamiento();
        helper.attachToRecyclerView(rvHistory);
    }

    private void inicializarFirebase() {
        database = FirebaseDatabase.getInstance(DATABASE_URL);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        referenciaHistorial = database.getReference().child(MainActivity.HISTORY_NODE).child(uid);
    }
}
