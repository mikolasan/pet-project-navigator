package io.github.mikolasan.petprojectnavigator;

import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.net.URISyntaxException;
import java.util.Locale;

public class TaskFragment extends Fragment {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_EDIT = 1;

    PetDatabase petDatabase;
    PetTask petTask;

    EditText eName;
    EditText eDesc;
    EditText eLinks;
    EditText eTime;

    SimpleCursorAdapter techAdapter;
    SimpleCursorAdapter typeAdapter;
    private Spinner sTech;
    private Spinner sType;

    private int currentTechId = PetDatabase.TECH_UNDEFINED_ID;
    private int currentTypeId = PetDatabase.TYPE_UNDEFINED_ID;

    FragmentTransaction fragmentTransaction;

    private void hideTechDialog() {
        hideDialog("TechNameDialogFragment");
    }

    private void hideTypeDialog() {
        hideDialog("TypeNameDialogFragment");
    }

    private void hideDialog(String tag) {
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentTransaction = fragmentManager.beginTransaction();
//        Fragment prev = fragmentManager.findFragmentByTag(tag);
//        if (prev != null) {
//            ((DialogFragment)prev).dismiss();
//            fragmentTransaction.remove(prev);
//        }
//        fragmentTransaction.addToBackStack(null);
    }

    private void openTechDialog() {
        openDialog("TechNameDialogFragment");
    }

    private void openTypeDialog() {
        openDialog("TypeNameDialogFragment");
    }

    private void openDialog(String tag) {
//        if (tag.equals("TechNameDialogFragment")){
//            TechNameDialogFragment dialog = TechNameDialogFragment.newInstance(TaskFragment.this);
//            dialog.show(fragmentTransaction, tag);
//        } else {
//            TypeNameDialogFragment dialog = TypeNameDialogFragment.newInstance(TaskFragment.this);
//            dialog.show(fragmentTransaction, tag);
//        }
    }

    private void updateTechList() {
        if (techAdapter != null) {
            techAdapter.swapCursor(petDatabase.getAllTech());
        }
    }

    private void updateTypeList() {
        if (typeAdapter != null) {
            typeAdapter.swapCursor(petDatabase.getAllTypes());
        }
    }

    private void selectItem(String result, Spinner spinner, SimpleCursorAdapter adapter) {
        Cursor c = adapter.getCursor();
        int id = 0;
        if (c.moveToLast()) {
            id = c.getPosition();
        }
        int name_id = c.getColumnIndex(PetDatabase.COLUMN_NAME);
        String label = c.getString(name_id);
        if(label.equals(result)) {
            spinner.setSelection(id);
        }
    }

    private SimpleCursorAdapter fillList(View v, Spinner spinner, Cursor cursor) {
        String[] from = new String[] { PetDatabase.COLUMN_NAME };
        int[] to = new int[] { android.R.id.text1 };
        int flags = 0;
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(v.getContext(),
                android.R.layout.simple_spinner_item,
                cursor,
                from,
                to,
                flags);
        spinner.setAdapter(adapter);
        return adapter;
    }

    private int getSelectedItemDBId(Spinner spinner, SimpleCursorAdapter adapter) {
        Cursor cursor = adapter.getCursor();
        if (cursor.getCount() > 0 && spinner.getCount() > 0) {
            cursor.moveToPosition(spinner.getSelectedItemPosition());
            int id = cursor.getColumnIndex(PetDatabase.COLUMN_ID);
            return cursor.getInt(id);
        }
        return 0;
    }

    private void fillTask(Intent intent, int status) {
        petTask = new PetTask();
        petTask.setProjectId(intent.getIntExtra("project_id", 0));
        petTask.setTaskId(intent.getIntExtra("task_id", 0));
        if (STATUS_EDIT == status) {
            petTask.setName(intent.getStringExtra("title"));
            petTask.setLinks(intent.getStringExtra("links"));
            petTask.setStatement(intent.getStringExtra("statement"));
            petTask.setTech(intent.getIntExtra("tech_id", 0));
            petTask.setType(intent.getIntExtra("type_id", 0));
            petTask.setTime(intent.getIntExtra("time", 0));
        }
    }

    private void fillLabels(PetTask petTask, int status) {
        if (STATUS_EDIT == status) {
            eName.setText(petTask.getName());
            eLinks.setText(petTask.getLinks());
            eDesc.setText(petTask.getStatement());
            if (sTech.getCount() > petTask.getTech()) {
                sTech.setSelection(petTask.getTech());
            }
            if (sType.getCount() > petTask.getType()) {
                sType.setSelection(petTask.getType());
            }
            eTime.setText(String.format(Locale.US, "%d", petTask.getTime()));
        }
    }

    private void parseLabels() {
        petTask.setName(eName.getText().toString());
        petTask.setLinks(eLinks.getText().toString());
        petTask.setStatement(eDesc.getText().toString());
        petTask.setTech(getSelectedItemDBId(sTech, techAdapter));
        String timeStr = eTime.getText().toString();
        int time = 0;
        if (!timeStr.isEmpty()) time = Integer.parseInt(timeStr);
        petTask.setTime(time);
        petTask.setType(getSelectedItemDBId(sType, typeAdapter));
    }

    private void saveTask() {
        parseLabels();
        petDatabase.dbTask.add(petTask);
    }

    private void saveTaskAndClose() {
        saveTask();
//        TaskFragment.this.finish();
    }

    private void completeTask() {

    }

    private void deleteTask() {
        petDatabase.dbTask.delete(petTask.getTaskId());
//        TaskFragment.this.finish();
    }

    private void moveOutTask() {
        petDatabase.dbTask.moveToBuffer(petTask.getTaskId());
//        TaskFragment.this.finish();
    }

    private void createTechControls(View v) {
        sTech = (Spinner) v.findViewById(R.id.s_tech);
        techAdapter = fillList(v, sTech, petDatabase.getAllTech());
        final Button btn_add_tech = (Button) v.findViewById(R.id.btn_add_tech);
        btn_add_tech.setOnClickListener(x -> {
            hideTechDialog();
            openTechDialog();
        });

        final Button btn_delete_tech = (Button) v.findViewById(R.id.btn_delete_tech);
        btn_delete_tech.setOnClickListener(x -> {
            if (currentTechId != PetDatabase.TECH_UNDEFINED_ID) {
                petDatabase.deleteTech(Integer.toString(currentTechId));
                updateTechList();
                sTech.setSelection(0);
            }
        });
    }

    private void createTypeControls(View v) {
        sType = (Spinner) v.findViewById(R.id.s_type);
        typeAdapter = fillList(v, sType, petDatabase.getAllTypes());

        final Button btn_add_type = (Button) v.findViewById(R.id.btn_add_type);
        btn_add_type.setOnClickListener(x -> {
            hideTypeDialog();
            openTypeDialog();
        });

        final Button btn_delete_type = (Button) v.findViewById(R.id.btn_delete_type);
        btn_delete_type.setOnClickListener(x -> {
            // !TODO
            //petDatabase.deleteType(Integer.toString(currentTypeId));
        });
    }

    private void createLabels(View v) {
        eName = (EditText) v.findViewById(R.id.e_name);
        eDesc = (EditText) v.findViewById(R.id.e_desc);
        eLinks = (EditText) v.findViewById(R.id.e_links);
        eTime = (EditText) v.findViewById(R.id.e_time);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.task_fragment, container, false);
        petDatabase = PetDatabase.getOpenedInstance();
        createTechControls(v);
        createTypeControls(v);
        createLabels(v);

        v.findViewById(R.id.btn_complete_task).setOnClickListener(view -> completeTask());
        v.findViewById(R.id.btn_delete_task).setOnClickListener(view -> deleteTask());
        v.findViewById(R.id.btn_out_task).setOnClickListener(view -> moveOutTask());
        v.findViewById(R.id.btn_save_task).setOnClickListener(view -> saveTaskAndClose());

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        sTech.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                Cursor cursor = (Cursor) parentView.getItemAtPosition(position);
                int index = cursor.getColumnIndex(PetDatabase.COLUMN_ID);
                int tech_id = cursor.getInt(index);
                currentTechId = tech_id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        sType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                Cursor cursor = (Cursor) parentView.getItemAtPosition(position);
                int index = cursor.getColumnIndex(PetDatabase.COLUMN_ID);
                int type_id = cursor.getInt(index);
                currentTypeId = type_id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        // TODO !!!
        Intent intent = null;
        try {
            intent = Intent.getIntent("");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        int status = intent.getIntExtra("status", STATUS_NEW);
        fillTask(intent, status);
        fillLabels(petTask, status);
    }

//
//    // Menu icons are inflated just as they were with actionbar
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.task_toolbar, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int itemId = item.getItemId();
//        if (itemId == R.id.action_settings) {// User chose the "Settings" item, show the app settings UI...
//            return true;
//        } else if (itemId == R.id.action_save_task) {
//            saveTask();
//            return true;
//        }// If we got here, the user's action was not recognized.
//        // Invoke the superclass to handle it.
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void techCallback(View view, String result) {
//        petDatabase.addTech(result);
//        hideTechDialog();
//        updateTechList();
//        selectItem(result, sTech, techAdapter);
//    }
//
//    public void typeCallback(View view, String result) {
//        petDatabase.addType(result);
//        hideTypeDialog();
//        updateTypeList();
//        selectItem(result, sType, typeAdapter);
//    }


    public static class TechNameDialogFragment extends DialogFragment {
        PetDialogListener ml;

        static TechNameDialogFragment newInstance(PetDialogListener ml) {
            TechNameDialogFragment f = new TechNameDialogFragment();
            f.ml = ml;
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.dialog_tech, container, false);
            final EditText e = (EditText)v.findViewById(R.id.e_tech_name);
            // Watch for button clicks.
            Button button = (Button)v.findViewById(R.id.btn_add_tech);
            button.setOnClickListener(v1 -> ml.techCallback(e, e.getText().toString()));
            return v;
        }
    }

    public static class TypeNameDialogFragment extends DialogFragment {
        PetDialogListener ml;

        static TypeNameDialogFragment newInstance(PetDialogListener ml) {
            TypeNameDialogFragment f = new TypeNameDialogFragment();
            f.ml = ml;
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.dialog_type, container, false);
            final EditText e = (EditText)v.findViewById(R.id.e_type_name);
            // Watch for button clicks.
            Button button = (Button)v.findViewById(R.id.btn_add_type);
            button.setOnClickListener(v1 -> ml.typeCallback(e, e.getText().toString()));
            return v;
        }
    }
}
