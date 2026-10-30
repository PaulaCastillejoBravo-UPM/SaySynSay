package es.upm.miw.saysynsay;

import es.upm.miw.saysynsay.models.Word;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FreeDictionaryRESTAPIService {
    @GET("entries/en/{word}")
    Call<Word> getWord(@Path("word") String word);
}
