package es.upm.miw.saysynsay.models;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Sense {

    @SerializedName("definition")
    @Expose
    private String definition;
    @SerializedName("examples")
    @Expose
    private List<String> examples = new ArrayList<String>();
    @SerializedName("synonyms")
    @Expose
    private List<String> synonyms = new ArrayList<String>();
    @SerializedName("antonyms")
    @Expose
    private List<String> antonyms = new ArrayList<String>();
    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
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
        sb.append(Sense.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("definition");
        sb.append('=');
        sb.append(((this.definition == null)?"<null>":this.definition));
        sb.append(',');
        sb.append("examples");
        sb.append('=');
        sb.append(((this.examples == null)?"<null>":this.examples));
        sb.append(',');
        sb.append("synonyms");
        sb.append('=');
        sb.append(((this.synonyms == null)?"<null>":this.synonyms));
        sb.append(',');
        sb.append("antonyms");
        sb.append('=');
        sb.append(((this.antonyms == null)?"<null>":this.antonyms));
        return sb.toString();
    }
}