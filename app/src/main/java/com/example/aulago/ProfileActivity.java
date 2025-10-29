package com.example.aulagoagoravai;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Receber dados passados da MainActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String studentName = extras.getString("STUDENT_NAME");
            String studentAge = extras.getString("STUDENT_AGE");
            int studentImage = extras.getInt("STUDENT_IMAGE");

            // Atualizar os componentes da tela com os dados
            TextView tvName = findViewById(R.id.tvName);
            ImageView avatar = findViewById(R.id.avatar);

            if (tvName != null) {
                tvName.setText(studentName);
            }

            if (avatar != null) {
                avatar.setImageResource(studentImage);
            }
        }
    }
}
