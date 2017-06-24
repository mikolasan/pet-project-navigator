package io.github.mikolasan.petprojectnavigator;

import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

/**
 * Created by neupo on 6/22/2017.
 */

class PetMainPager {

    private PetPagerAdapter pagerAdapter;
    private ViewPager pager;
    private ListView listView;
    private BottomNavigationView bottomNavigationView;
    private SearchView searchView;
    private MenuItem prevMenuItem;
    private PetOnItemClickListener clickListener;
    private final int drawerOpenProjectsPos = 0;
    private final int drawerOpenTasksPos = 1;
    private final int drawerOpenBufferPos = 2;

    private class PetPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case PetPagerAdapter.PROJECTS_PAGE_ID:
                    selectPage(position, drawerOpenProjectsPos);
                    break;
                case PetPagerAdapter.TASKS_PAGE_ID:
                    selectPage(position, drawerOpenTasksPos);
                    break;
                case PetPagerAdapter.BUFFER_PAGE_ID:
                    selectPage(position, drawerOpenBufferPos);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private class PetQueryTextListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            int page = getCurrentPage();
            return pagerAdapter.onQueryTextChange(newText, page);
        }
    }

    private class PetActionExpandListener implements MenuItemCompat.OnActionExpandListener {
        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            return false;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            return pagerAdapter.onMenuItemActionExpand(item);
        }
    }

    public PetMainPager(AppCompatActivity activity) {
        pagerAdapter = new PetPagerAdapter(activity.getSupportFragmentManager());
        pager = (ViewPager)activity.findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new PetPageChangeListener());

        DrawerLayout drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        listView = (ListView) activity.findViewById(R.id.left_drawer);
        // Set the adapter for the list view
        listView.setAdapter(new ArrayAdapter<>(activity,
                R.layout.drawer_list_item, activity.getResources().getStringArray(R.array.menu_list)));
        // Set the list's click listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
            clickListener.onItemClick(parent, view, position, id);
            drawerLayout.closeDrawer(listView);
        });
    }

    public void setOnItemClickListener(PetOnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setupSearchItem(MenuItem searchItem) {
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // Configure the search info and add any event listeners...
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new PetQueryTextListener());
        // Assign the listener to that action item
        MenuItemCompat.setOnActionExpandListener(searchItem, new PetActionExpandListener());
    }

    public void setSearchCriterion(int criterion) {
        pagerAdapter.setCriterion(criterion);
    }

    public void defineAction(int drawerPosition) {
        switch (drawerPosition) {
            case drawerOpenProjectsPos:
                selectPage(PetPagerAdapter.PROJECTS_PAGE_ID, drawerPosition);
                break;
            case drawerOpenTasksPos:
                selectPage(PetPagerAdapter.TASKS_PAGE_ID, drawerPosition);
                break;
            case drawerOpenBufferPos:
                selectPage(PetPagerAdapter.BUFFER_PAGE_ID, drawerPosition);
                break;
        }
    }

    public void setButtonListeners(AppCompatActivity activity) {
        bottomNavigationView = (BottomNavigationView)
                activity.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.action_projects:
                            selectPage(PetPagerAdapter.PROJECTS_PAGE_ID, drawerOpenProjectsPos);
                            break;
                        case R.id.action_tasks:
                            selectPage(PetPagerAdapter.TASKS_PAGE_ID, drawerOpenTasksPos);
                            break;
                        case R.id.action_buffer:
                            selectPage(PetPagerAdapter.BUFFER_PAGE_ID, drawerOpenBufferPos);
                            break;
                        default:
                            break;
                    }
                    return true;
                });
    }

    public void selectPage(int pageId, int listItemId) {
        pager.setCurrentItem(pageId);
        listView.setItemChecked(listItemId, true);
        if (prevMenuItem != null) {
            prevMenuItem.setChecked(false);
        } else {
            bottomNavigationView.getMenu().getItem( 0).setChecked(false);
        }
        bottomNavigationView.getMenu().getItem(pageId).setChecked(true);
        prevMenuItem = bottomNavigationView.getMenu().getItem(pageId);

        pagerAdapter.selectPage(pageId);
        if (searchView != null && pagerAdapter.getCount() > pageId) {
            searchView.setQuery(pagerAdapter.getSearchQuery(pageId), false);
        }
    }

    public int getCurrentPage() {
        return pager.getCurrentItem();
    }
}