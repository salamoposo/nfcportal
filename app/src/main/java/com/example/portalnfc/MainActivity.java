package com.example.portalnfc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ImageDecoderKt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
    private ImageView about_btn;
    private ProgressBar progressBar;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userdb, idcarddb, idcardpush;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser fbuser;

    String IdKartu;

    Integer matchid = 0;
    int a = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fbuser = auth.getCurrentUser();
        about_btn = findViewById(R.id.tentang);
        progressBar = findViewById(R.id.ppbarr);
        edtEmail = findViewById(R.id.edt_email);
        edtNama = findViewById(R.id.edt_nama);
        edtNim = findViewById(R.id.edt_nim);
        edtkartu = findViewById(R.id.idkartu);
        edtkartu.setEnabled(false);
        btRegist = findViewById(R.id.register_btn);

        about_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, aboutDeveloper.class));
                finish();
            }
        });

        idcarddb = database.getReference("uId");
        idcarddb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    IdKartu = snapshot.getValue(String.class);
                    edtkartu.setText(IdKartu);
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

    @Override
    public void onBackPressed() {
        a++;
        if (a == 1) {
            Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();
        } else if (a == 2) {
            finish();
        }
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
        matchid = 0;
        auth.fetchSignInMethodsForEmail(edtEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                Boolean check = !Objects.requireNonNull(task.getResult().getSignInMethods()).isEmpty();

                if (check) {
                    Toast.makeText(MainActivity.this, "Email Sudah Terdaftar", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Query queryId = database.getReference("User")
                            .orderByChild("cardId")
                            .equalTo(IdCard);
                    queryId.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                User user = dataSnapshot.getValue(User.class);
                                assert user != null;
                                String useridcard = user.cardId;
                                if (Objects.equals(IdCard, useridcard)) {
                                    matchid = 1;
                                    Toast.makeText(MainActivity.this, "Id sudah terdaftar!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                            if (matchid != 1) {
                                auth.createUserWithEmailAndPassword(Email, Nim)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    User newUser = new User(Nama, Email, Nim, IdKartu);
                                                    String userid = auth.getUid();
                                                    userdb = database.getReference("User");
                                                    userdb.child(userid).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Toast.makeText(MainActivity.this, "Berhasil Registrasi ", Toast.LENGTH_SHORT).show();
                                                            idcardpush = database.getReference("userId").push();
                                                            idcardpush.setValue(IdKartu);
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

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, "Database error!@", Toast.LENGTH_SHORT).show();
                        }
                    });


                }

            }

        });


    }

}