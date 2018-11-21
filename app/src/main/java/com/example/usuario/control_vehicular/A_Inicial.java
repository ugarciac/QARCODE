package com.example.usuario.control_vehicular;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class A_Inicial extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,View.OnClickListener{

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    Button sign_out;
    SignInButton signin;
    TextView tvname;
    Button siguiente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a__inicial);

        signin = (SignInButton) findViewById(R.id.sign_in_button);
        sign_out= (Button) findViewById(R.id.sign_out);
        sign_out.setVisibility(View.GONE);
        siguiente = (Button)findViewById(R.id.siguiente);
        siguiente.setVisibility(View.GONE);

        tvname = (TextView) findViewById(R.id.name);


        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = view.getId();
                if (i==R.id.sign_in_button){ }
                sign_out();
            }
        });

        findViewById(R.id.sign_in_button).setOnClickListener((View.OnClickListener) A_Inicial.this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(A_Inicial.this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i==R.id.sign_in_button){
            signIn();
        }
    }

    public void signIn(){
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }
    public void sign_out(){
        mAuth.signOut();

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Toast.makeText(A_Inicial.this, "Cerraste sesión",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        sign_out.setVisibility(View.GONE);
        tvname.setText(null);
        signin.setVisibility(View.VISIBLE);
        siguiente.setVisibility(View.GONE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        Log.d(TAG,"firebaseAuthWithGoogle: "+acct.getId());

        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Toast.makeText(A_Inicial.this,""+credential.getProvider(),Toast.LENGTH_LONG).show();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:"+task.isSuccessful());
                        String name = getdata();

                        if (task.isSuccessful()){
                            sign_out.setVisibility(View.VISIBLE);
                            signin.setVisibility(View.GONE);
                            siguiente.setVisibility(View.VISIBLE);
                            //tvname.setText("Bienvenido "+name);

                        }
                        else {
                            Toast.makeText(A_Inicial.this,"Ocurrio un error en algo",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }



    public String getdata(){
        String name = null;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            name = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
            String uid = user.getUid();
        }
        return name;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    public void Siguiente(View view) {
        Intent intent = new Intent(A_Inicial.this,MainActivity.class);
        startActivity(intent);
    }
}
