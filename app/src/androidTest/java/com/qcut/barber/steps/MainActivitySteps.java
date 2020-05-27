package com.qcut.barber.steps;

import android.app.Activity;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.FirebaseApp;
import com.qcut.barber.R;
import com.qcut.barber.views.activities.MainActivity;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

/**
 * We use {@link ActivityTestRule} in order to have access to methods like getActivity
 * and getInstrumentation.
 * </p>
 * The CucumberOptions annotation is mandatory for exactly one of the classes in the test project.
 * Only the first annotated class that is found will be used, others are ignored. If no class is
 * annotated, an exception is thrown.
 * <p/>
 * The options need to at least specify features = "features". Features must be placed inside
 * assets/features/ of the test project (or a subdirectory thereof).
 */
public class MainActivitySteps {

    ActivityTestRule rule = new ActivityTestRule<>(MainActivity.class, false, false);

    @Before
    public void launchActivity() throws Exception {
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().getContext());
        rule.launchActivity(null);
    }

    @After
    public void finishActivity() throws Exception {
        getActivity().finish();
    }

    private Activity getActivity() {
        return rule.getActivity();
    }

    @Given("I have a MainActivity")
    public void I_have_a_MainActivity() {
        assertNotNull(getActivity());
    }

    @Then("I should see shop status {string}")
    public void I_should_see_shop_status(final String status) {
        onView(withId(R.id.status_change)).check(matches(withText(status)));
    }

}
