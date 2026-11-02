package es.upm.miw.saysynsay.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.upm.miw.saysynsay.R;
import es.upm.miw.saysynsay.models.SearchHistory;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<SearchHistory> historial;
    private OnPalabraClickListener listener;

    public HistoryAdapter(List<SearchHistory> historial, OnPalabraClickListener listener){
        this.historial = historial;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);

        return new HistoryViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        SearchHistory busqueda = historial.get(position);

        holder.tvHistoryWord.setText(busqueda.getPalabra());
        holder.tvHistoryDate.setText(formatearFecha(busqueda.getFecha()));

        holder.itemView.setOnClickListener(v -> {
            listener.onPalabraClick(busqueda.getPalabra());
        });
    }

    @Override
    public int getItemCount(){
        return historial.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHistoryWord;
        private TextView tvHistoryDate;

        public HistoryViewHolder(@NonNull View itemView){
            super(itemView);

            tvHistoryWord = itemView.findViewById(R.id.tvHistoryWord);
            tvHistoryDate = itemView.findViewById(R.id.tvHistoryDate);
        }
    }


    public interface OnPalabraClickListener {
        void onPalabraClick(String palabra);
    }
    private String formatearFecha(Long fecha) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        return formato.format(new Date(fecha));
    }
}
