package io.github.mikolasan.petprojectnavigator;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;

/**
 * Created by neupo on 6/22/2017.
 */

class PetMainPager {

    private PetPagerAdapter pagerAdapter;
    private ViewPager pager;
    private ListView navigationView;
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
            boolean result;
            switch (item.getItemId()) {
                case R.id.action_search:
                    searchView.setQuery("", true);
                    result = true;
                    break;
                default:
                    result = false;
            }
            return result && pagerAdapter.onMenuItemActionCollapse(item);
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
        navigationView = (ListView) activity.findViewById(R.id.left_drawer);
        // Set the adapter for the list view
        navigationView.setAdapter(new PetMenuAdapter(activity));
        // Set the list's click listener
        navigationView.setOnItemClickListener((parent, view, position, id) -> {
            clickListener.onItemClick(parent, view, position, id);
            drawerLayout.closeDrawer(navigationView);
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
        navigationView.setItemChecked(listItemId, true);
        if (prevMenuItem != null) {
            prevMenuItem.setChecked(false);
        } else {
            bottomNavigationView.getMenu().getItem( 0).setChecked(false);
        }
        bottomNavigationView.getMenu().getItem(pageId).setChecked(true);
        prevMenuItem = bottomNavigationView.getMenu().getItem(pageId);

        if (searchView != null) {
            searchView.setQuery(pagerAdapter.getSearchQuery(pageId), false);
        }
    }

    public int getCurrentPage() {
        return pager.getCurrentItem();
    }
}
