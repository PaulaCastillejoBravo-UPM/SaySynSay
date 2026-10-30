package es.upm.miw.saysynsay.models;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Entry {

    @SerializedName("language")
    @Expose
    private Language language;
    @SerializedName("partOfSpeech")
    @Expose
    private String partOfSpeech;
    @SerializedName("pronunciations")
    @Expose
    private List<Pronunciation> pronunciations = new ArrayList<Pronunciation>();
    @SerializedName("forms")
    @Expose
    private List<Form> forms = new ArrayList<Form>();
    @SerializedName("senses")
    @Expose
    private List<Sense> senses = new ArrayList<Sense>();
    @SerializedName("synonyms")
    @Expose
    private List<String> synonyms = new ArrayList<String>();
    @SerializedName("antonyms")
    @Expose
    private List<String> antonyms = new ArrayList<String>();

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public List<Pronunciation> getPronunciations() {
        return pronunciations;
    }

    public void setPronunciations(List<Pronunciation> pronunciations) {
        this.pronunciations = pronunciations;
    }

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    public List<Sense> getSenses() {
        return senses;
    }

    public void setSenses(List<Sense> senses) {
        this.senses = senses;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public List<String> getAntonyms() {
        return antonyms;
    }

    public void setAntonyms(List<String> antonyms) {
        this.antonyms = antonyms;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Entry.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("language");
        sb.append('=');
        sb.append(((this.language == null)?"<null>":this.language));
        sb.append(',');
        sb.append("partOfSpeech");
        sb.append('=');
        sb.append(((this.partOfSpeech == null)?"<null>":this.partOfSpeech));
        sb.append(',');
        sb.append("pronunciations");
        sb.append('=');
        sb.append(((this.pronunciations == null)?"<null>":this.pronunciations));
        sb.append(',');
        sb.append("forms");
        sb.append('=');
        sb.append(((this.forms == null)?"<null>":this.forms));
        sb.append(',');
        sb.append("senses");
        sb.append('=');
        sb.append(((this.senses == null)?"<null>":this.senses));
        sb.append(',');
        sb.append("synonyms");
        sb.append('=');
        sb.append(((this.synonyms == null)?"<null>":this.synonyms));
        sb.append(',');
        sb.append("antonyms");
        sb.append('=');
        sb.append(((this.antonyms == null)?"<null>":this.antonyms));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }
}