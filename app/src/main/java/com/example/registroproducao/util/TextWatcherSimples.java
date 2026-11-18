package com.example.registroproducao.util;

import android.text.Editable;
import android.text.TextWatcher;

/** TextWatcher com implementações vazias, para sobrescrever apenas o método necessário. */
public abstract class TextWatcherSimples implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public abstract void afterTextChanged(Editable s);
}
