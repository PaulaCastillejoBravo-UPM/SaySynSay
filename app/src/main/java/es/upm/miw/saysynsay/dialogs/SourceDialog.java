package es.upm.miw.saysynsay.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import es.upm.miw.saysynsay.R;

public class SourceDialog extends DialogFragment {
    private static final String ARG_SOURCE_URL = "sourceUrl";
    private static final String ARG_LICENSE_NAME = "licenseName";
    private static final String ARG_LICENSE_URL = "licenseUrl";

    public static SourceDialog newInstance(String sourceUrl,String licenseName, String licenseUrl) {
        SourceDialog dialog = new SourceDialog();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_SOURCE_URL, sourceUrl);
        arguments.putString(ARG_LICENSE_NAME, licenseName);
        arguments.putString(ARG_LICENSE_URL, licenseUrl);

        dialog.setArguments(arguments);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View vista = requireActivity().getLayoutInflater().inflate(R.layout.dialog_source, null);

        TextView tvSourceUrl = vista.findViewById(R.id.tvSourceUrl);
        TextView tvLicenseName = vista.findViewById(R.id.tvLicenseName);
        TextView tvLicenseUrl = vista.findViewById(R.id.tvLicenseUrl);

        tvSourceUrl.setPaintFlags(tvSourceUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvLicenseUrl.setPaintFlags(tvLicenseUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        Bundle arguments = getArguments();

        String sourceUrl = arguments != null ? arguments.getString(ARG_SOURCE_URL) : null;
        String licenseName = arguments != null ? arguments.getString(ARG_LICENSE_NAME) : null;
        String licenseUrl = arguments != null ? arguments.getString(ARG_LICENSE_URL) : null;

        if (licenseName != null && !licenseName.isEmpty()) {
            tvLicenseName.setText(licenseName);
        } else {
            tvLicenseName.setText(R.string.source_license_placeholder);
        }

        configurarEnlace(tvSourceUrl, sourceUrl, R.string.source_original_link);
        configurarEnlace(tvLicenseUrl, licenseUrl, R.string.source_license_link);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(vista).setPositiveButton(R.string.close, (dialog, which) -> dismiss());

        return builder.create();
    }

    private void configurarEnlace(TextView texto, String url, int textoEnlace) {
        if(url == null || url.isEmpty()) {
            texto.setEnabled(false);
            return;
        }

        texto.setText(textoEnlace);

        texto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }
}
