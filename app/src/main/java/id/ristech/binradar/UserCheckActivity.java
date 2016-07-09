package id.ristech.binradar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class UserCheckActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.blank_layout);

/*        ProgressDialog.show(this, "Loading", "Wait while loading...");*/

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // user signed in
            startActivity(new Intent(this, DrawerActivity.class));
            finish();
        } else {
            // not signed in
            //Toast.makeText(getApplicationContext(), "Belom login", Toast.LENGTH_SHORT).show();
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(
                                    AuthUI.EMAIL_PROVIDER)
                            .build(),
                    RC_SIGN_IN);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user is signed in!
                //Toast.makeText(getApplicationContext(), "BRHASIL!!!", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(UserCheckActivity.this, DrawerActivity.class));
                finish();
            } else {
                // user is not signed in. Maybe just wait for the user to press
                // "sign in" again, or show a mess//age
                //Toast.makeText(getApplicationContext(), "Belom login", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
