package com.proper.bin2bin;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.proper.bin2bin.ActChooserTest \
 * com.proper.bin2bin.tests/android.test.InstrumentationTestRunner
 */
public class ActChooserTest extends ActivityInstrumentationTestCase2<ActChooser> {
    private ActChooser mActivity = null;

    public ActChooserTest() {
        super("com.proper.bin2bin", ActChooser.class);
        // This constructor was deprecated - but we want to support lower API levels.
        //super("com.google.android.apps.common.testing.ui.testapp", ActChooser.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mActivity = this.getActivity();
    }

//        public void testShouldDisableButtonBeforeNavigation() {
//        assertEquals(View.VISIBLE, goButton.getVisibility());
//        goButton.performClick();
//        assertEquals(View.GONE, goButton.getVisibility());
//    }

    @SmallTest
    public void testLayout() {
        int btnId = com.proper.bin2bin.R.id.bnSingleMove;
        assertNotNull(mActivity.findViewById(btnId));
        Button thisButton = (Button) mActivity.findViewById(btnId);
        assertEquals("Incorrect label on the button", mActivity.getString(com.proper.bin2bin.R.string.action_single, thisButton.getText()));
    }

    @SmallTest
    public void testLogOn() {
        String user = "Current User [Unknown]";
        //input text in view
        onView(withId(com.proper.bin2bin.R.id.etxtInitials)).perform(typeText(user));
        //assert equality of the editText with our defined variable
        onView(withText(mActivity.getTitle().toString())).check(matches(withText(user)));
    }

//    public void testIntentTriggerdByOnClick() {
//        Button thisButton = (Button) mActivity.findViewById(com.proper.bin2bin.R.id.bnSingleMove);
//    }

}
