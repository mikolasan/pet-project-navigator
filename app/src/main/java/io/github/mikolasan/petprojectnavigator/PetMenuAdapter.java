package io.github.mikolasan.petprojectnavigator;


import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mikolasan on 12/3/17.
 */

public class PetMenuAdapter implements ListAdapter {
    private Context context;
    private String[] items;
    private ArrayList<Drawable> images;
    private static final int spaceLength = 20;
    private ImageView headerImage;

    public PetMenuAdapter(Context context) {
        this.context = context;
        items = context.getResources().getStringArray(R.array.menu_list);
        String[] drawables = context.getResources().getStringArray(R.array.image_list);
        images = new ArrayList<>(drawables.length);
        for (String uri : drawables) {
            if (uri.length() > 0) {
                int id = context.getResources().getIdentifier("@drawable/" + uri, null, context.getPackageName());
                Drawable d = context.getResources().getDrawable(id);
                images.add(d);
            } else {
                images.add(null);
            }
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return items[i].length() > 0;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int i) {
        return items[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            if (i == 0) {
                headerImage = (ImageView)LayoutInflater.from(this.context).inflate(R.layout.drawer_image, viewGroup, false);
                Bitmap bitmap = BitmapFactory.decodeFile(context.getResources().getString(R.string.header_file_path));
                headerImage.setImageBitmap(bitmap);
                return headerImage;
            } else {
                View v = LayoutInflater.from(this.context).inflate(R.layout.drawer_list_item, viewGroup, false);
                ((TextView) v.findViewById(R.id.drawer_text)).setText(items[i]);
                boolean spacer = (items[i].length() > 0);
                int space = spacer ? 0 : spaceLength;
                v.setPadding(0, space, 0, 0);
                if (spacer) {
                    ((ImageView) v.findViewById(R.id.drawer_image)).setImageDrawable(images.get(i));
                }
                return v;
            }
        } else {
            return view;
        }
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
