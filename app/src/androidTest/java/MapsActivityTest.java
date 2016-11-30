/**
 * Created by junweixu on 16/11/29.
 */

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import cse5236.pinpoint.MapsActivity;
import cse5236.pinpoint.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapsActivityTest {

    @Rule
    public ActivityTestRule<MapsActivity> mActivityRule = new ActivityTestRule<>(
            MapsActivity.class);


    @Test
    public void  onCreate() throws InterruptedException {
        String STRING_TO_BE_TYPED = "library";
        wait(11111);
        onView(withId(R.id.fab)).perform(click()); //line 1

    }
}
