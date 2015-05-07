package ru.jumatiy.tracker.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ru.jumatiy.tracker.R;
import ru.jumatiy.tracker.activity.MainActivity;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 01.05.2015 9:43.
 */
public class ChangeUrlDialog extends DialogFragment {

    @InjectView(R.id.urlInput)
    EditText urlInput;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Адрес сервера");
        View content = View.inflate(getActivity(), R.layout.change_url_dialog, null);
        ButterKnife.inject(this, content);

        builder.setView(content);

        Bundle args = getArguments();

        final String address = args.getString("API_URL");

        builder.setNeutralButton("Отмена", null);
        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newAddress = urlInput.getText().toString();

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity)getActivity()).saveNewUrl(newAddress);
                }
            }
        });

        Dialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                urlInput.setText(address);
                urlInput.setSelection(urlInput.getText().length());
            }
        });

        return dialog;
    }

    public void show(FragmentManager fm, String currentUrl) {
        String tag = "CHANGE_URL_DIALOG";
        Fragment f = fm.findFragmentByTag(tag);
        if (f == null || !f.isAdded()) {
            Bundle bundle = new Bundle();
            bundle.putString("API_URL", currentUrl);
            setArguments(bundle);
            super.show(fm, tag);
        }
    }
}
