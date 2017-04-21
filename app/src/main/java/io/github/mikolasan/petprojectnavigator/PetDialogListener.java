package io.github.mikolasan.petprojectnavigator;

import android.view.View;

interface PetDialogListener {
    // you can define any parameter as per your requirement
    void techCallback(View view, String result);
    void typeCallback(View view, String result);
}