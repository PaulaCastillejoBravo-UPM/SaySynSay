package es.upm.miw.saysynsay.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import es.upm.miw.saysynsay.R;
import es.upm.miw.saysynsay.models.Entry;
import es.upm.miw.saysynsay.models.SavedWord;
import es.upm.miw.saysynsay.models.Word;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {
    private List<SavedWord> vocabulario;
    private VocabularyAdapter.OnPalabraClickListener listener;
    private VocabularyAdapter.OnEliminarPalabraListener eliminarListener;

    public VocabularyAdapter(List<SavedWord> vocabulario, VocabularyAdapter.OnPalabraClickListener listener, VocabularyAdapter.OnEliminarPalabraListener eliminarListener) {
        this.vocabulario = vocabulario;
        this.listener = listener;
        this.eliminarListener = eliminarListener;
    }

    @NonNull
    @Override
    public VocabularyAdapter.VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_vocabulary, parent, false);

        return new VocabularyAdapter.VocabularyViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyAdapter.VocabularyViewHolder holder, int position) {
        SavedWord palabraGuardada = vocabulario.get(position);

        Word palabra = palabraGuardada.getPalabra();

        holder.tvVocabularyWord.setText(palabra.getWord());
        holder.tvVocabularyDate.setText(formatearFecha(palabraGuardada.getFechaGuardada()));

        Set<String> categorias = new LinkedHashSet<>();

        if(palabra.getEntries() != null){
            for(Entry entrada : palabra.getEntries()){
                if(entrada != null && entrada.getPartOfSpeech() != null && !entrada.getPartOfSpeech().trim().isEmpty()){
                    categorias.add(entrada.getPartOfSpeech());
                }
            }
        }

        holder.tvVocabularyPartsOfSpeech.setText(TextUtils.join(" · ", categorias));

        holder.itemView.setOnClickListener(v -> {
            listener.onPalabraClick(palabraGuardada.getPalabra().getWord());
        });

        holder.btnRemoveVocabulary.setOnClickListener(v -> {
            eliminarListener.onEliminarPalabra(palabraGuardada);
        });
    }

    @Override
    public int getItemCount() {return vocabulario.size();}

    public static class VocabularyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvVocabularyWord;
        private TextView tvVocabularyPartsOfSpeech;
        private TextView tvVocabularyDate;
        private ImageButton btnRemoveVocabulary;

        public VocabularyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvVocabularyWord = itemView.findViewById(R.id.tvVocabularyWord);
            tvVocabularyPartsOfSpeech = itemView.findViewById(R.id.tvVocabularyPartsOfSpeech);
            tvVocabularyDate = itemView.findViewById(R.id.tvVocabularyDate);

            btnRemoveVocabulary = itemView.findViewById(R.id.btnRemoveVocabulary);
        }
    }

    public interface OnPalabraClickListener {
        void onPalabraClick(String palabra);
    }

    public interface OnEliminarPalabraListener {
        void onEliminarPalabra(SavedWord palabraGuardada);
    }

    private String formatearFecha(Timestamp fecha) {
        if (fecha == null) {
            return "";
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        return formato.format(fecha.toDate());
    }
}