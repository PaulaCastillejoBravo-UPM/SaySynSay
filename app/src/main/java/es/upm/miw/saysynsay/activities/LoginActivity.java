package es.upm.miw.saysynsay.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import es.upm.miw.saysynsay.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_ACTUALIZAR_CUENTA_ANONIMA = "actualizarCuentaAnonima";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        estableceInserciones();

        boolean actualizarCuentaAnonima = getIntent().getBooleanExtra(EXTRA_ACTUALIZAR_CUENTA_ANONIMA, false);

        if (actualizarCuentaAnonima) {
            findViewById(R.id.btnAnonymous).setVisibility(View.GONE);
        }

        findViewById(R.id.btnEmail).setOnClickListener(this);
        findViewById(R.id.btnGitHub).setOnClickListener(this);
        findViewById(R.id.btnAnonymous).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int idItem = v.getId();

        if (idItem == R.id.btnEmail) {
            iniciarSesionEmail();
        } else if (idItem == R.id.btnGitHub) {
            iniciarSesionGithub();
        } else if (idItem == R.id.btnAnonymous) {
            iniciarSesionAnonima();
        }
    }

    private void iniciarSesionAnonima() {
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if (user != null) irAlBuscador();
                    } else {
                        Toast.makeText(this, getString(R.string.login_error_generic), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void iniciarSesionEmail() {
        List<AuthUI.IdpConfig> authProviders = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setCredentialManagerEnabled(false)
                .enableAnonymousUsersAutoUpgrade()
                .setAvailableProviders(authProviders)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private void iniciarSesionGithub() {
        List<AuthUI.IdpConfig> authProviders = Arrays.asList(
                new AuthUI.IdpConfig.GitHubBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setCredentialManagerEnabled(false)
                .enableAnonymousUsersAutoUpgrade()
                .setAvailableProviders(authProviders)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                    new FirebaseAuthUIActivityResultContract(),
                    new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                        @Override
                        public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                            onSignInResult(result);
                        }
                    }
            );

    private void irAlBuscador() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();

        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                irAlBuscador();
            }
        } else {
            if (response != null && response.getError() != null) {
                Toast.makeText(this, getString(R.string.login_error_generic), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void estableceInserciones() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
