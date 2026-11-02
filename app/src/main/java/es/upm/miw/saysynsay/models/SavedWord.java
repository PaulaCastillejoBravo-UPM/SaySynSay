package es.upm.miw.saysynsay.models;

import com.google.firebase.Timestamp;

public class SavedWord {
    private String id;
    private Word palabra;
    private Timestamp fechaGuardada;

    public SavedWord(String id, Word palabra, Timestamp fechaGuardada) {
        this.id = id;
        this.palabra = palabra;
        this.fechaGuardada = fechaGuardada;
    }

    public String getId() {return id;}

    public Word getPalabra() {return palabra;}

    public Timestamp getFechaGuardada() {return fechaGuardada;}
}
