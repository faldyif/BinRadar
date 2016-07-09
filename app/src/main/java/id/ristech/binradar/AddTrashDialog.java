package id.ristech.binradar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Faldy on 7/1/2016.
 */
public class AddTrashDialog extends DialogFragment {

    static AddTrashDialog newInstance(Double latitude, Double longitude, String userID) {
        AddTrashDialog atd = new AddTrashDialog();

        Bundle args = new Bundle();
        args.putDouble("lat", latitude);
        args.putDouble("lng", longitude);
        args.putString("uid", userID);
        atd.setArguments(args);

        return atd;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Double lt = getArguments().getDouble("lat");
        String latitude = String.valueOf(lt);
        String userID = getArguments().getString("uid");
    }

    private Integer type;
    private String desc;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_add_trash,null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(v)
                .setTitle("Submit new trash can")
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Double lat = getArguments().getDouble("lat");
                        Double lng = getArguments().getDouble("lng");
                        String uid = getArguments().getString("uid");

                        EditText editText = (EditText) v.findViewById(R.id.description);
                        desc = editText.getText().toString();
                        if(desc == null || desc == "") {
                            desc = "No description";
                        }

                        RadioGroup radio = (RadioGroup) v.findViewById(R.id.type);
                        type = radio.getCheckedRadioButtonId();

                        Trash trash = new Trash(uid, lat, lng, type, desc);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("trashes");
                        ref.push().setValue(trash);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AddTrashDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
