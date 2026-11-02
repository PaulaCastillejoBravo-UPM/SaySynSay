package es.upm.miw.saysynsay.models;

public class SearchHistory {
    private String id;
    private String palabra;
    private Long fecha;

    public SearchHistory() {}

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public String getPalabra() {return palabra;}
    public void setPalabra(String palabra) {this.palabra = palabra;}

    public Long getFecha() {return fecha;}
    public void setFecha(Long fecha) {this.fecha = fecha;}
}
