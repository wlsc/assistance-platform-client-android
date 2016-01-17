package de.tudarmstadt.informatik.tk.assistance.activity;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.tudarmstadt.informatik.tk.assistance.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 22.11.2015
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> activityRule =
            new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void buttonPressedWithoutLoginAndEmail() {

        onView(withId(R.id.sign_in_button))
                .perform(click());
    }
}
