package com.example.portalnfc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ImageDecoderKt;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private EditText edtkartu, edtNama, edtNim, edtEmail;
    private MaterialButton btRegist;
    private ProgressBar progressBar;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userdb, idcarddb;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser fbuser;

    final String[] IdKartu = {""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fbuser = auth.getCurrentUser();
        progressBar = findViewById(R.id.ppbarr);
        edtEmail = findViewById(R.id.edt_email);
        edtNama = findViewById(R.id.edt_nama);
        edtNim = findViewById(R.id.edt_nim);
        edtkartu = findViewById(R.id.idkartu);
        edtkartu.setEnabled(false);
        btRegist = findViewById(R.id.register_btn);

        idcarddb = database.getReference("uId");
        idcarddb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    IdKartu[0] = snapshot.getValue(String.class);
                    edtkartu.setText(IdKartu[0]);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

    }

    private void registerUser() {
        String Nama = edtNama.getText().toString().trim();
        String Email = edtEmail.getText().toString().trim();
        String Nim = edtNim.getText().toString().trim();
        String IdCard = edtkartu.getText().toString();


        if (Nama.isEmpty()) {
            edtNama.setError("Kosong!");
            edtNama.requestFocus();
            return;
        }
        if (Email.isEmpty()) {
            edtEmail.setError("Kosong!");
            edtEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            edtEmail.setError("Email tidak valid");
            edtEmail.requestFocus();
            return;
        }
        if (Nim.isEmpty()) {
            edtNim.setError("Kosong1");
            edtNim.requestFocus();
            return;
        }
        if (IdCard.isEmpty()) {
            edtkartu.setError("Kosong1");
            edtkartu.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.fetchSignInMethodsForEmail(edtEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                Boolean check = !Objects.requireNonNull(task.getResult().getSignInMethods()).isEmpty();

                if (check) {
                    Toast.makeText(MainActivity.this, "Email Sudah Terdaftar", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    auth.createUserWithEmailAndPassword(Email, Nim)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        User newUser = new User(Nama, Email, Nim, IdKartu[0]);
                                        String userid = auth.getUid();
                                        userdb = database.getReference("User");
                                        userdb.child(userid).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(MainActivity.this, "Berhasil Registrasi ", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                                idcarddb = database.getReference("uId");
                                                idcarddb.setValue("");
                                                edtNama.setText("");
                                                edtkartu.setText("");
                                                edtEmail.setText("");
                                                edtNim.setText("");
                                            }
                                        });

                                    } else {
                                        Toast.makeText(MainActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                        idcarddb = database.getReference("uId");
                                        idcarddb.setValue("");
                                    }

                                }
                            });
                }

            }
        });


    }

}