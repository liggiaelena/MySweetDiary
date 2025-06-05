package com.example.mysweetdiary;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.*;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class CalendarActivity extends AppCompatActivity {
    private MaterialCalendarView calendarView;
    private FirebaseFirestore db;
    private final String idUsuario = "usuario123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupBottomNavigation();
        calendarView = findViewById(R.id.calendarView);
        db = FirebaseFirestore.getInstance();

        carregarNotasNoCalendario();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_calendar);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_diary) {
                startActivity(new Intent(this, DiaryActivity.class));
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

    private void carregarNotasNoCalendario() {
        Map<String, List<CalendarDay>> notasPorCor = new HashMap<>();
        notasPorCor.put("vermelho", new ArrayList<>());
        notasPorCor.put("laranja", new ArrayList<>());
        notasPorCor.put("amarelo", new ArrayList<>());
        notasPorCor.put("azul", new ArrayList<>());
        notasPorCor.put("verde", new ArrayList<>());

        db.collection("entradas")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .addOnSuccessListener(docs -> {
                    for (QueryDocumentSnapshot doc : docs) {
                        String data = doc.getString("data");
                        int nota = doc.getLong("nota").intValue();
                        CalendarDay dia = new CalendarDay();

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date date = sdf.parse(data);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            dia = CalendarDay.from(cal);
                        }catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (nota <= 20) notasPorCor.get("vermelho").add(dia);
                        else if (nota <= 40) notasPorCor.get("laranja").add(dia);
                        else if (nota <= 60) notasPorCor.get("amarelo").add(dia);
                        else if (nota <= 80) notasPorCor.get("azul").add(dia);
                        else notasPorCor.get("verde").add(dia);
                    }

                    calendarView.addDecorator(new NotaDecodador(notasPorCor.get("vermelho"), Color.parseColor("#EF9A9A")));
                    calendarView.addDecorator(new NotaDecodador(notasPorCor.get("laranja"), Color.parseColor("#FFCC80")));
                    calendarView.addDecorator(new NotaDecodador(notasPorCor.get("amarelo"), Color.parseColor("#FFF59D")));
                    calendarView.addDecorator(new NotaDecodador(notasPorCor.get("azul"), Color.parseColor("#90CAF9")));
                    calendarView.addDecorator(new NotaDecodador(notasPorCor.get("verde"), Color.parseColor("#A5D6A7")));
                });
    }
}