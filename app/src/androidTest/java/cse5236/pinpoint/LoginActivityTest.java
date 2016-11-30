package cse5236.pinpoint;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by junweixu on 16/11/29.
 */

public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @Test
    public void  onCreate(){
        onView(withId(R.id.map_button)).perform(click());
        onView(withId(R.id.fab)).perform(click());//line 1
    }

}

