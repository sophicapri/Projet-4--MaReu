package com.sophie.mareu.ui.list_meetings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sophie.mareu.DI.DI;
import com.sophie.mareu.R;
import com.sophie.mareu.controller.FilterAndSort;
import com.sophie.mareu.controller.AvailabilityByDate;

import static com.sophie.mareu.Constants.*;

import com.sophie.mareu.model.Meeting;
import com.sophie.mareu.service.RoomsAvailabilityByHourImpl;
import com.sophie.mareu.ui.DetailFragment;
import com.sophie.mareu.ui.meeting_creation.HomeStartMeetingCreationFragment;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sophie.mareu.model.Meeting.iconSelector;

public class ListMeetingsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ListMeetingsFragment listMeetingsFragment = new ListMeetingsFragment();
    private DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
    private Date selectedDate = null;
    private String selectedRoom = null;
    private Menu menu;

    @BindView(R.id.my_toolbar)
    Toolbar mainToolbar;
    @BindView(R.id.choose_date)
    Button chooseDateBtn;
    @BindView(R.id.back_button)
    ImageButton backBtn;
    @BindView(R.id.ok_filter)
    Button okButtonFilter;
    @BindView(R.id.spinner_filter)
    Spinner roomsSpinner;
    @BindView(R.id.filter_activity)
    CardView filterSelectionView;
    @BindView(R.id.filter_selected)
    CardView filterSelectedView;
    @BindView(R.id.deactivate_filter)
    CardView deactivateFilterBtn;
    @BindView(R.id.filter_selected_text)
    TextView filterModeText;
    @BindView(R.id.filter_activated)
    View filterActivatedView;
    @BindView(R.id.deactivate_sorted_list)
    CardView sortedModeBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listmeetings);
        ButterKnife.bind(this);
        setSupportActionBar(mainToolbar);

        DI.setResources(getResources());
        AvailabilityByDate.setService(DI.getService());

        // Add dummyMeeting for presentation :
        Meeting dummyMeeting = DI.getDummyMeetings().get(3);
        AvailabilityByDate.addMeeting(dummyMeeting, dummyMeeting.getDate());
        AvailabilityByDate.updateAvailabilityByDate(dummyMeeting.getDate(), new RoomsAvailabilityByHourImpl());
        //
        configureAndShowListMeetingFragment();
        configureAndShowHomeStartMeetingCreationFragment();

        sortedModeBtn.setOnClickListener(this);
        deactivateFilterBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        okButtonFilter.setOnClickListener(this);
        chooseDateBtn.setOnClickListener(this);
        roomsSpinner.setOnItemSelectedListener(this);
    }

    private void configureAndShowListMeetingFragment() {
        Fragment listMeetingFrame = getSupportFragmentManager().findFragmentById(R.id.frame_listmeetings);
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        if (listMeetingFrame == null) {
            fm.add(R.id.frame_listmeetings, listMeetingsFragment).commit();
        } else // in case we previously were in landscape mode.
            fm.replace(R.id.frame_listmeetings, listMeetingsFragment).commit();
    }

    private void configureAndShowHomeStartMeetingCreationFragment() {
        Fragment meetingCreationFrame = getSupportFragmentManager().findFragmentById(R.id.frame_setmeeting);
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        if (findViewById(R.id.frame_setmeeting) != null) {
            if (meetingCreationFrame == null) {
                fm.add(R.id.frame_setmeeting, new HomeStartMeetingCreationFragment()).commit();
            } else if (!(meetingCreationFrame.getClass().equals(HomeStartMeetingCreationFragment.class))) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fm.replace(R.id.frame_setmeeting, new HomeStartMeetingCreationFragment()).commit();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Because the spinner displayed starts at ""  and our array starts at "Peach"
        if (position == 0) selectedRoom = "";
        else selectedRoom = getResources().getStringArray(R.array.room_names)[position - 1];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        super.onMenuOpened(featureId, menu);
        if (filterSelectionView.isShown()) {
            filterSelectionView.setVisibility(View.GONE);
            this.menu.close();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        filterActivatedView.setVisibility(View.VISIBLE);
        switch (item.getItemId()) {
            case R.id.ascending:
                if (FilterAndSort.getFilteredList().isEmpty() && deactivateFilterBtn.getVisibility() == View.VISIBLE)
                    break;
                else {
                    sortList(ASCENDING);
                    break;
                }
            case R.id.descending:
                if (FilterAndSort.getFilteredList().isEmpty() && deactivateFilterBtn.getVisibility() == View.VISIBLE)
                    break;
                else {
                    sortList(DESCENDING);
                    break;
                }
            case R.id.filter:
                initSpinner();
                filterSelectionView.setVisibility(View.VISIBLE);
                break;
        }
        return true;
    }

    private void sortList(int sortingOrder) {
        FilterAndSort.sortList(sortingOrder);
        listMeetingsFragment.initList(SORTED);
        sortedModeBtn.setVisibility(View.VISIBLE);
    }

    private void initSpinner() {
        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("");
        spinnerArray.addAll(DI.getNewRoomsList());
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, spinnerArray);
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        roomsSpinner.setAdapter(spinnerAdapter);
    }

    private void showDatePickerDialog() {
        Locale.setDefault(Locale.FRANCE);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        selectedDate = new GregorianCalendar(year, month, dayOfMonth).getTime();
        chooseDateBtn.setText(df.format(selectedDate));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose_date:
                showDatePickerDialog();
                break;
            case R.id.ok_filter:
                if (selectedDate != null || !selectedRoom.isEmpty()) filterList();
                else
                    Toast.makeText(this, getString(R.string.choose_date_or_room), Toast.LENGTH_LONG).show();
                break;
            case R.id.back_button:
                filterSelectionView.setVisibility(View.GONE);
                break;
            case R.id.deactivate_filter:
                deactivateFilter();
                resetFilterView();
                break;
            case R.id.deactivate_sorted_list:
                if (FilterAndSort.getFilteredList().isEmpty())
                    listMeetingsFragment.initList(UNCHANGED);
                else {
                    FilterAndSort.filterMeetingsList(selectedDate, selectedRoom);
                    listMeetingsFragment.initList(FILTERED);
                }
                sortedModeBtn.setVisibility(View.GONE);
                break;
        }
    }

    private void filterList() {
        FilterAndSort.filterMeetingsList(selectedDate, selectedRoom);
        filterSelectionView.setVisibility(View.GONE);
        sortedModeBtn.setVisibility(View.GONE);
        listMeetingsFragment.initList(FILTERED);

        String text;
        if (selectedDate != null && selectedRoom.isEmpty())
            text = getString(R.string.filtered_date_only, df.format(selectedDate));
        else if (selectedDate == null && !selectedRoom.isEmpty())
            text = getString(R.string.filtered_room_only, selectedRoom);
        else
            text = getString(R.string.filtered_date_and_room, df.format(Objects.requireNonNull(selectedDate)), selectedRoom);
        filterModeText.setText(text);
        filterSelectedView.setVisibility(View.VISIBLE);
        deactivateFilterBtn.setVisibility(View.VISIBLE);
        resetFilterView();
    }


    private void resetFilterView() {
        chooseDateBtn.setText(getResources().getString(R.string.select_date));
        selectedDate = null;
        selectedRoom = "";
    }

    private void deactivateFilter() {
        listMeetingsFragment.initList(UNCHANGED);
        FilterAndSort.clearLists();
        filterSelectionView.setVisibility(View.GONE);
        filterSelectedView.setVisibility(View.GONE);
        deactivateFilterBtn.setVisibility(View.GONE);
        sortedModeBtn.setVisibility(View.GONE);
        filterSelectionView.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        resetFilterView();
        deactivateFilter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iconSelector = 0;
        AvailabilityByDate.clearAllMeetings();
    }

    @Override
    public void onBackPressed() {
        Fragment detailFragment = getSupportFragmentManager().findFragmentById(R.id.frame_setmeeting);
        if (detailFragment != null && detailFragment.getClass().equals(DetailFragment.class)) {
            if (detailFragment.getFragmentManager() != null)
                detailFragment.getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else
            super.onBackPressed();
    }
}
