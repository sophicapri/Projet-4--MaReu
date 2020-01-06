package com.sophie.mareu.ui.list_meetings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.sophie.mareu.R;
import com.sophie.mareu.controller.SortList;
import com.sophie.mareu.service.AvailabilityByDate;
import com.sophie.mareu.service.MeetingsService;
import com.sophie.mareu.service.RoomsAvailabilityService;
import com.sophie.mareu.ui.meeting_creation.HomeStartMeetingCreationFragment;


import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListMeetingsActivity extends AppCompatActivity {
    private ListMeetingFragment mListMeetingFragment;
    private Date mCurrentDate = Calendar.getInstance().getTime();
    private Date mSelectedDate = Calendar.getInstance().getTime();
    private String mSelectedName = "";
    private boolean ascendingDate = true;
    private boolean ascendingName = true;

    @BindView(R.id.my_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listmeetings);

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mListMeetingFragment = (ListMeetingFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frame_listmeetings);
        configureAndShowListMeetingFragment();
        configureAndShowHomeStartMeetingCreationFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }

    private static final String TAG = "LOGGListMeeActivity";

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_date:
                if (ascendingDate)
                    item.setTitle(getString(R.string.sort_by_date, getString(R.string.arrow_down)));
                else
                    item.setTitle(getString(R.string.sort_by_date, getString(R.string.arrow_up)));
                SortList.sortByHour(ascendingDate);
                ascendingDate = !ascendingDate;
                break;
            case R.id.sort_by_roomname_:
                if (ascendingName)
                    item.setTitle(getString(R.string.sort_by_roomname, getString(R.string.arrow_down)));
                else
                    item.setTitle(getString(R.string.sort_by_roomname, getString(R.string.arrow_up)));
                SortList.sortByRoomName(ascendingName);
                ascendingName = !ascendingName;
                break;
        }
        refreshView();
        return true;
    }

    private void sortByDate(){
        mSelectedDate = date selected;
        if mSelectedDate = null

        AvailabilityByDate.filterMeetingsList(mSelectedDate, mSelectedName);

    }

    public void refreshView() {
        mListMeetingFragment.onStop();
        mListMeetingFragment.onStart();
        mListMeetingFragment.onResume();
    }

    private void configureAndShowListMeetingFragment() {
        if (mListMeetingFragment == null) {
            mListMeetingFragment = new ListMeetingFragment();
            FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
            fm.add(R.id.frame_listmeetings, mListMeetingFragment).commit();
        }
    }

    private void configureAndShowHomeStartMeetingCreationFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_setmeeting);
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();

        if (findViewById(R.id.frame_setmeeting) != null) {
            if (fragment == null)
                fm.add(R.id.frame_setmeeting, new HomeStartMeetingCreationFragment()).commit();
            else if (!(fragment.getClass().equals(HomeStartMeetingCreationFragment.class))) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fm.replace(R.id.frame_setmeeting, new HomeStartMeetingCreationFragment()).commit();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MeetingsService.clearMeetingList();
    }
}
