package com.qcut.barber.steps;

import android.app.Activity;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import com.qcut.barber.R;
import com.qcut.barber.views.activities.SignInActivity;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
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
public class SigninActivitySteps {

    /**
     * Since {@link cucumber.runtime.android.CucumberJUnitRunner} and CucumberInstrumentationCore have the control over the
     * test lifecycle, activity test rules must not be launched automatically. Automatic launching of test rules is only
     * feasible for JUnit tests. Fortunately, we are able to launch the activity in Cucumber's {@link Before} method.
     */
    ActivityTestRule rule = new ActivityTestRule<>(SignInActivity.class, false, false);

    /**
     * We launch the activity in Cucumber's {@link Before} hook.
     * See the notes above for the reasons why we are doing this.
     *
     * @throws Exception any possible Exception
     */
    @Before
    public void launchActivity() throws Exception {
        rule.launchActivity(null);
    }

    /**
     * All the clean up of application's data and state after each scenario must happen here
     */
    @After
    public void finishActivity() throws Exception {
        getActivity().finish();
    }

    /**
     * Gets the activity from our test rule.
     *
     * @return the activity
     */
    private Activity getActivity() {
        return rule.getActivity();
    }

    @Given("I have a SigninActivity")
    public void I_have_a_SigninActivity() {
        assertNotNull(getActivity());
    }

    @When("I entered username {string}")
    public void I_entered_email(final String email) {
        onView(ViewMatchers.withId(R.id.sign_in_email)).perform(typeText(email));
    }
    @When("I entered password {string}")
    public void I_entered_password(String password) {
        onView(withId(R.id.sign_in_password)).perform(typeText(password));
    }

    @When("I click Signin button")
    public void I_click_Signin_button() {
        onView(withId(R.id.sign_in_screen)).perform(click());
    }

//    @ParameterType("([0-9]{4})-([0-9]{2})-([0-9]{2})")
//    public String iso8601Date(String year){
//        return LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
//    }
}
