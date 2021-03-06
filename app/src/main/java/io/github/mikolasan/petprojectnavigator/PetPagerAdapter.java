package io.github.mikolasan.petprojectnavigator;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.MenuItem;

import java.util.ArrayList;

import static io.github.mikolasan.petprojectnavigator.Tools.applyQuery;

/**
 * Created by neupo on 6/22/2017.
 */

public class PetPagerAdapter extends FragmentPagerAdapter {

    private ProjectFragment projectFragment;
    private TaskListActivity taskFragment;
    private BufferFragment bufferFragment;

    public static final int PROJECTS_PAGE_ID = 0;
    public static final int TASKS_PAGE_ID = 1;
    public static final int BUFFER_PAGE_ID = 2;
    private static final int N_PAGES = 3;
    private ArrayList<String> searchPerPage;
    private int criterion = 0;

    public PetPagerAdapter(FragmentManager manager) {
        super(manager);
        projectFragment = new ProjectFragment();
        taskFragment = new TaskListActivity();
        bufferFragment = new BufferFragment();
        searchPerPage = new ArrayList<>(N_PAGES);
        clearSearch();
    }

    @Override
    public Fragment getItem(int position) {
        // Do NOT try to save references to the Fragments in getItem(),
        // because getItem() is not always called. If the Fragment
        // was already created then it will be retrieved from the FragmentManger
        // and not here (i.e. getItem() won't be called again).
        switch (position) {
            case PROJECTS_PAGE_ID:
                return projectFragment;
            case TASKS_PAGE_ID:
                return taskFragment;
            case BUFFER_PAGE_ID:
                return bufferFragment;
            default:
                // This should never happen. Always account for each position above
                return null;
        }
    }

    @Override
    public int getCount() {
        return N_PAGES;
    }

    public boolean onQueryTextChange(String newText, int page) {
        searchPerPage.set(page, newText);
        switch (page){
            case PROJECTS_PAGE_ID:
                applyQuery(projectFragment, projectFragment.activityDataLoader, criterion, newText);
                break;
            case TASKS_PAGE_ID:
                applyQuery(taskFragment, taskFragment.activityDataLoader, criterion, newText);
                break;
            case BUFFER_PAGE_ID:
                //applyQuery(bufferFragment, bufferFragment.activityDataLoader, criterion, newText);
                break;
        }
        return true;
    }

    public boolean onMenuItemActionCollapse(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                clearSearch();
                applyQuery(projectFragment, projectFragment.activityDataLoader, 0, "");
                applyQuery(taskFragment, taskFragment.activityDataLoader, 0, "");
                //applyQuery(bufferFragment, bufferFragment.activityDataLoader, 0, "");
                return true;
            default:
                return false;
        }
    }

    private void clearSearch() {
        searchPerPage.clear();
        searchPerPage.add("");
        searchPerPage.add("");
        searchPerPage.add("");
    }

    public boolean onMenuItemActionExpand(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                clearSearch();
                return true;
            default:
                return false;
        }
    }

    public String getSearchQuery(int page) {
        if (searchPerPage.size() > page) {
            return searchPerPage.get(page);
        }
        return "";
    }

    public void setCriterion(int criterion) {
        this.criterion = criterion;
    }
}
