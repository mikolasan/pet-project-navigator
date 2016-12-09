package io.github.mikolasan.petprojectnavigator;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

interface MyListener {
    // you can define any parameter as per your requirement
    public void callback(View view, String result);
}

public class TaskActivity extends FragmentActivity implements MyListener {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_EDIT = 1;

    private int project_id;
    DB db;
    Button btn_save_task;
    EditText e_name;
    EditText e_desc;
    EditText e_links;

    SimpleCursorAdapter spinnerAdapter;
    private Spinner s_tech;

    FragmentTransaction ft;

    private void removeTechDialog() {
        ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("TechNameDialogFragment");
        if (prev != null) {
            ((DialogFragment)prev).dismiss();
            ft.remove(prev);
        }
        ft.addToBackStack(null);
    }
    private void openTechDialog() {
        TechNameDialogFragment dialog = TechNameDialogFragment.newInstance(TaskActivity.this);
        dialog.show(ft,"TechNameDialogFragment");
    }

    private void updateSpinner() {
        if (spinnerAdapter != null) {
            spinnerAdapter.swapCursor(db.getAllTech());
        }
    }

    private void selectItem(String result) {
        Cursor c = spinnerAdapter.getCursor();
        int id = 0;
        if (c.moveToLast()) {
            id = c.getPosition();
        }
        int name_id = c.getColumnIndex(DB.COLUMN_NAME);
        String label = c.getString(name_id);
        if(label.equals(result)) {
            s_tech.setSelection(id);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        db = DB.getOpenedInstance();

        String[] from = new String[] { DB.COLUMN_NAME };
        int[] to = new int[] { android.R.id.text1 };
        int flags = 0;
        Cursor techCursor = db.getAllTech();
        spinnerAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                techCursor,
                from,
                to,
                flags);
        s_tech = (Spinner) findViewById(R.id.s_tech);
        s_tech.setAdapter(spinnerAdapter);

        btn_save_task = (Button) findViewById(R.id.btn_save_task);
        e_name = (EditText) findViewById(R.id.e_name);
        e_desc = (EditText) findViewById(R.id.e_desc);
        e_links = (EditText) findViewById(R.id.e_links);



        btn_save_task.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.addTask(project_id,
                        e_name.getText().toString(),
                        e_links.getText().toString(),
                        e_desc.getText().toString(),
                        0,
                        0,
                        0);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        s_tech.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                Cursor cursor = (Cursor) parentView.getItemAtPosition(position);
                int index = cursor.getColumnIndex(DB.COLUMN_ID);
                int tech_id = cursor.getInt(index);
                switch (tech_id) {
                    case DB.TECH_UNDEFINED_ID: {
                        break;
                    }
                    case DB.TECH_NEW_ID: {
                        removeTechDialog();
                        openTechDialog();
                        break;
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        Intent intent = getIntent();
        int status = intent.getIntExtra("status", STATUS_NEW);
        switch  (status) {
            case STATUS_NEW: {
                project_id = intent.getIntExtra("project_id", 0);
                break;
            }
            case STATUS_EDIT: {


                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void callback(View view, String result) {
        db.addTech(result);
        removeTechDialog();
        updateSpinner();
        selectItem(result);
    }


    public static class TechNameDialogFragment extends DialogFragment {
        MyListener ml;

        static TechNameDialogFragment newInstance(MyListener ml) {
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
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ml.callback(e, e.getText().toString());

                }
            });
            return v;
        }
    }

}
