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
    void techCallback(View view, String result);
    void typeCallback(View view, String result);
}

public class TaskActivity extends FragmentActivity implements MyListener {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_EDIT = 1;

    private int project_id;
    private int task_id;

    DB db;
    Button btn_save_task;
    EditText e_name;
    EditText e_desc;
    EditText e_links;
    EditText e_time;

    SimpleCursorAdapter spinnerAdapter;
    SimpleCursorAdapter typeAdapter;
    private Spinner s_tech;
    private Spinner s_type;

    FragmentTransaction ft;

    private void hideTechDialog() {
        hideDialog("TechNameDialogFragment");
    }

    private void hideTypeDialog() {
        hideDialog("TypeNameDialogFragment");
    }

    private void hideDialog(String tag) {
        ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ((DialogFragment)prev).dismiss();
            ft.remove(prev);
        }
        ft.addToBackStack(null);
    }

    private void openTechDialog() {
        openDialog("TechNameDialogFragment");
    }

    private void openTypeDialog() {
        openDialog("TypeNameDialogFragment");
    }

    private void openDialog(String tag) {
        if (tag == "TechNameDialogFragment"){
            TechNameDialogFragment dialog = TechNameDialogFragment.newInstance(TaskActivity.this);
            dialog.show(ft, tag);
        } else {
            TypeNameDialogFragment dialog = TypeNameDialogFragment.newInstance(TaskActivity.this);
            dialog.show(ft, tag);
        }
    }

    private void updateTechList() {
        if (spinnerAdapter != null) {
            spinnerAdapter.swapCursor(db.getAllTech());
        }
    }

    private void updateTypeList() {
        if (typeAdapter != null) {
            typeAdapter.swapCursor(db.getAllTypes());
        }
    }

    private void selectItem(String result, Spinner spinner, SimpleCursorAdapter adapter) {
        Cursor c = adapter.getCursor();
        int id = 0;
        if (c.moveToLast()) {
            id = c.getPosition();
        }
        int name_id = c.getColumnIndex(DB.COLUMN_NAME);
        String label = c.getString(name_id);
        if(label.equals(result)) {
            spinner.setSelection(id);
        }
    }

    private SimpleCursorAdapter fillList(Spinner spinner, Cursor cursor) {
        String[] from = new String[] { DB.COLUMN_NAME };
        int[] to = new int[] { android.R.id.text1 };
        int flags = 0;
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
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
            int id = cursor.getColumnIndex(DB.COLUMN_ID);
            return cursor.getInt(id);
        }
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        db = DB.getOpenedInstance();

        s_tech = (Spinner) findViewById(R.id.s_tech);
        s_type = (Spinner) findViewById(R.id.s_type);
        spinnerAdapter = fillList(s_tech, db.getAllTech());
        typeAdapter = fillList(s_type, db.getAllTypes());

        final Button btn_add_tech = (Button) findViewById(R.id.btn_add_tech);
        btn_add_tech.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideTechDialog();
                openTechDialog();
            }
        });

        final Button btn_delete_tech = (Button) findViewById(R.id.btn_delete_tech);
        btn_delete_tech.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO
            }
        });

        final Button btn_add_type = (Button) findViewById(R.id.btn_add_type);
        btn_add_type.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideTypeDialog();
                openTypeDialog();
            }
        });

        final Button btn_delete_type = (Button) findViewById(R.id.btn_delete_type);
        btn_delete_type.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO
            }
        });

        btn_save_task = (Button) findViewById(R.id.btn_save_task);
        e_name = (EditText) findViewById(R.id.e_name);
        e_desc = (EditText) findViewById(R.id.e_desc);
        e_links = (EditText) findViewById(R.id.e_links);
        e_time = (EditText) findViewById(R.id.e_time);

        btn_save_task.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.addTask(task_id,
                        project_id,
                        e_name.getText().toString(),
                        e_links.getText().toString(),
                        e_desc.getText().toString(),
                        getSelectedItemDBId(s_tech, spinnerAdapter),
                        Integer.parseInt(e_time.getText().toString()),
                        getSelectedItemDBId(s_type, typeAdapter));
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
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        s_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                Cursor cursor = (Cursor) parentView.getItemAtPosition(position);
                int index = cursor.getColumnIndex(DB.COLUMN_ID);
                int type_id = cursor.getInt(index);
                switch (type_id) {
                    case DB.TYPE_UNDEFINED_ID: {
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
        project_id = intent.getIntExtra("project_id", 0);
        task_id = intent.getIntExtra("task_id", 0);
        switch  (status) {
            case STATUS_NEW: {
                break;
            }
            case STATUS_EDIT: {
                e_name.setText(intent.getStringExtra("title"));
                e_links.setText(intent.getStringExtra("links"));
                e_desc.setText(intent.getStringExtra("statement"));

                int tech_id = intent.getIntExtra("tech_id", 0);
                s_tech.setSelection(tech_id);
                int type_id = intent.getIntExtra("type_id", 0);
                s_type.setSelection(type_id);
                e_time.setText(Integer.toString(intent.getIntExtra("time", 0)));
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void techCallback(View view, String result) {
        db.addTech(result);
        hideTechDialog();
        updateTechList();
        selectItem(result, s_tech, spinnerAdapter);
    }

    public void typeCallback(View view, String result) {
        db.addType(result);
        hideTypeDialog();
        updateTypeList();
        selectItem(result, s_type, typeAdapter);
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
                    ml.techCallback(e, e.getText().toString());
                }
            });
            return v;
        }
    }

    public static class TypeNameDialogFragment extends DialogFragment {
        MyListener ml;

        static TypeNameDialogFragment newInstance(MyListener ml) {
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
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ml.typeCallback(e, e.getText().toString());
                }
            });
            return v;
        }
    }
}
