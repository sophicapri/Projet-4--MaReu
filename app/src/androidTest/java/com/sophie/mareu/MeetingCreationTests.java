package com.sophie.mareu;

import android.content.pm.ActivityInfo;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.sophie.mareu.DI.DI;
import com.sophie.mareu.controller.AvailabilityByDate;
import com.sophie.mareu.service.RoomsAvailabilityByHourImpl;
import com.sophie.mareu.controller.RoomsPerHour;
import com.sophie.mareu.service.RoomsAvailabilityService;
import com.sophie.mareu.ui.list_meetings.ListMeetingsActivity;
import com.sophie.mareu.utils.DeleteViewAction;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.sophie.mareu.utils.RecyclerViewItemCountAssertion.withItemCount;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by SOPHIE on 02/01/2020.
 */
@RunWith(AndroidJUnit4.class)
public class MeetingCreationTests {
    private ListMeetingsActivity mActivity;
    private ArrayList<RoomsPerHour> mRoomsAndHours = new ArrayList<>();
    private ArrayList<String> mRooms = DI.getNewRoomsList();
    private RoomsAvailabilityService mService = new RoomsAvailabilityByHourImpl();
    private Date mDate = getDateWithoutTime();
    private char A;

    // To be able to test correctly no matter the day or the time of running the test.
    public static Date getDateWithoutTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @Rule
    public ActivityTestRule<ListMeetingsActivity> mActivityRule =
            new ActivityTestRule<>(ListMeetingsActivity.class);

    @Before
    public void setup() {
        A = 'A';
        mActivity = mActivityRule.getActivity();
        assertThat(mActivity, notNullValue());
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Test
    public void addNewMeetingWithSuccess() {
        addMeeting(0);
        onView(ViewMatchers.withId(R.id.meetings_list)).check(withItemCount(1));
    }

    @Test
    public void addNewMeetingInLandscapeModeWithSuccess() {
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addMeeting(0);
        onView(ViewMatchers.withId(R.id.meetings_list)).check(withItemCount(1));
    }

    @Test
    public void deleteMeetingWithSuccess() {
        for (int position = 3; position != 0; position--)
            addMeeting(position);

        onView(ViewMatchers.withId(R.id.meetings_list)).check(withItemCount(3));
        onView(ViewMatchers.withId(R.id.meetings_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, new DeleteViewAction()));
        onView(ViewMatchers.withId(R.id.meetings_list)).check(withItemCount(2));
    }

    @Test
    public void openDetailsWithSuccess_OnClickOnMeeting() {
        addMeeting(0);
        onView(ViewMatchers.withId(R.id.meetings_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    @Test
    public void deleteAllMeetingsOnPhoneRotationWithSuccess() throws InterruptedException {
        for (int position = 2; position != 0; position--)
            addMeeting(position);
        onView(ViewMatchers.withId(R.id.meetings_list)).check(withItemCount(2));
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.meetings_list)).check(withItemCount(0));
    }

    @Test
    public void updateHoursAvailabilityDummyWithSuccess() {
        int initialHoursAvailable = 2;

        mRooms = new ArrayList<>(Arrays.asList("Peach", "Mario"));
        mRoomsAndHours.add(new RoomsPerHour("8h00", new ArrayList<>(mRooms)));
        mRoomsAndHours.add(new RoomsPerHour("9h00", new ArrayList<>(mRooms)));
        mService.updateAvailableHours(mRoomsAndHours);
        AvailabilityByDate.updateAvailabilityByDate(mDate, mService);
        AvailabilityByDate.setCurrentService(mDate);

        for (int i = 0; i < initialHoursAvailable; i++) {
            int initialRoomsCount = mRooms.size() - 1;
            while (initialRoomsCount >= 0) {
                addMeeting(initialRoomsCount);
                initialRoomsCount--;
            }
        }
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.select_date)).perform(click());
        onView(withId(R.id.ok_button)).perform(click());
        onView(withId(R.id.all_meetings_full)).check(matches(isDisplayed()));
    }

    public void addMeeting(int roomPosition) {
        if (mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            onView(withId(R.id.fab)).perform(click());
        else
            onView(withId(R.id.fab_home)).perform(click());
        onView(withId(R.id.select_date)).perform(click());
        onView(withId(R.id.ok_button)).perform(click());
        onView(withText(mRooms.get(roomPosition))).perform(click());
        onView(withId(R.id.next_page)).perform(click());
        onView(withId(R.id.meeting_title_input)).perform(typeText("Reunion " + A++));
        onView(withId(R.id.email_one)).perform(scrollTo()).perform(typeText("email@address.com"));
        onView(withId(R.id.save_meeting_btn)).perform(click());
    }
}