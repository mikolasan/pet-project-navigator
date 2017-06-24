package io.github.mikolasan.petprojectnavigator;

import android.view.View;
import android.widget.AdapterView;

/**
 * Created by neupo on 6/23/2017.
 */

public interface PetOnItemClickListener extends AdapterView.OnItemClickListener {
    @Override
    void onItemClick(AdapterView<?> parent, View view, int position, long id);
}
