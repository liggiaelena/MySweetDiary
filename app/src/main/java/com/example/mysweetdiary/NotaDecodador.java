package com.example.mysweetdiary;

import android.graphics.drawable.GradientDrawable;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.*;

import java.util.Collection;
import java.util.HashSet;

public class NotaDecodador implements DayViewDecorator {

    private final int color;
    private final HashSet<CalendarDay> dates;

    public NotaDecodador(Collection<CalendarDay> dates, int color) {
        this.dates = new HashSet<>(dates);
        this.color = color;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // Cria uma bolinha com cantos arredondados e fundo colorido
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(100); // faz virar um círculo
        drawable.setColor(color);

        view.setBackgroundDrawable(drawable);

        // Garante que o número do dia fique branco e visível
//        view.addSpan(new ForegroundColorSpan(android.graphics.Color.WHITE));
    }
}
