package es.upm.miw.saysynsay.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Stack;

import es.upm.miw.saysynsay.models.Word;

public class WordViewModel extends ViewModel {
    private final MutableLiveData<Word> palabra = new MutableLiveData<>();
    private final MutableLiveData<Integer> pestanaSeleccionada = new MutableLiveData<>(0);

    private Stack<Word> historialNavegacion = new Stack<>();

    public MutableLiveData<Word> getPalabra() {
        return palabra;
    }

    public void setPalabra(Word nuevaPalabra) {
        palabra.setValue(nuevaPalabra);
    }

    public MutableLiveData<Integer> getPestanaSeleccionada() {
        return pestanaSeleccionada;
    }

    public void setPestanaSeleccionada(int pestana) {
        pestanaSeleccionada.setValue(pestana);
    }

    public void guardarPalabraActual() {
        if (palabra.getValue() != null) {
            historialNavegacion.push(palabra.getValue());
        }
    }

    public boolean hayPalabrasAnteriores() {
        return !historialNavegacion.empty();
    }

    public void recuperarPalabraAnterior() {
        if (hayPalabrasAnteriores()) {
            palabra.setValue(historialNavegacion.pop());
        }
    }

    public void limpiarHistorialNavegacion() {
        historialNavegacion.clear();
    }
}
