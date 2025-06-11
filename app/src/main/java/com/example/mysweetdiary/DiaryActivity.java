package com.example.mysweetdiary;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class DiaryActivity extends AppCompatActivity {

    private ChipGroup chipGroup;
    private EditText editNota, editObservacao;
    private Button btnSalvar;
    private FirebaseFirestore db;
    private final String idUsuario = "usuario123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_diary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupBottomNavigation();

        chipGroup = findViewById(R.id.chipGroupEmocoes);
        editNota = findViewById(R.id.editNota);
        editObservacao = findViewById(R.id.editObservacao);
        btnSalvar = findViewById(R.id.btnSalvar);

        db = FirebaseFirestore.getInstance();

        btnSalvar.setOnClickListener(view -> salvarEntrada());
    }
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_diary);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_graph) {
                startActivity(new Intent(this, GraphActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });
    }

    private void salvarEntrada() {
        List<String> emocoesSelecionadas = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                emocoesSelecionadas.add(chip.getText().toString());
            }
        }

        String notaStr = editNota.getText().toString().trim();
        if (notaStr.isEmpty()) {
            Toast.makeText(this, "Insira uma nota válida.", Toast.LENGTH_SHORT).show();
            return;
        }

        int nota = Integer.parseInt(notaStr);
        if (nota < 1 || nota > 100) {
            Toast.makeText(this, "A nota deve estar entre 1 e 100.", Toast.LENGTH_SHORT).show();
            return;
        }

        String observacao = editObservacao.getText().toString().trim();

        String data = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> entrada = new HashMap<>();
        entrada.put("idUsuario", idUsuario);
        entrada.put("data", data);
        entrada.put("emocao", emocoesSelecionadas);
        entrada.put("nota", nota);
        entrada.put("observacao", observacao);

        db.collection("entradas")
                .add(entrada)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Entrada salva com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.d("FIREBASE", "ID: " + documentReference.getId());
                    Intent intent = new Intent(DiaryActivity.this, GraphActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar entrada.", Toast.LENGTH_SHORT).show();
                    Log.e("FIREBASE", "Erro: ", e);
                });
    }

}