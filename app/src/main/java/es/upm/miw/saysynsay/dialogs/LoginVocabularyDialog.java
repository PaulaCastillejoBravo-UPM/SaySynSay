package es.upm.miw.saysynsay.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import es.upm.miw.saysynsay.R;
import es.upm.miw.saysynsay.activities.LoginActivity;

public class LoginVocabularyDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.save_vocabulary_dialog_title)
                .setMessage(R.string.save_vocabulary_dialog_message)
                .setPositiveButton(R.string.login, (dialog, which) -> {
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.putExtra(LoginActivity.EXTRA_ACTUALIZAR_CUENTA_ANONIMA, true);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.not_now, null)
                .create();
    }
}
