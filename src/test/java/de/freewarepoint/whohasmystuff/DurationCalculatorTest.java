package de.freewarepoint.whohasmystuff;

import android.content.res.Resources;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DurationCalculatorTest {

    @Mock
    private Resources resources;

    private DurationCalculator durationCalculator;

    @Before
    public void setup() {
        durationCalculator = new DurationCalculator(resources);
    }

    @Test
    public void noTimeDifference() {
        final Calendar now = Calendar.getInstance();

        when(resources.getString(R.string.today)).thenReturn("Today");

        final String duration =  durationCalculator.getTimeDifference(now, now);
        assertEquals("Today", duration);
    }

    @Test
    public void yesterdayButNot24Hours() {
        final Calendar yesterday = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        yesterday.set(2017, 2, 11, 18, 0);
        now.set(2017, 2, 12, 15, 0);

        when(resources.getString(R.string.yesterday)).thenReturn("Yesterday");

        final String duration =  durationCalculator.getTimeDifference(yesterday, now);
        assertEquals("Yesterday", duration);
    }

    @Test
    public void yesterdayMoreThan24Hours() {
        final Calendar yesterday = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        yesterday.set(2017, 2, 11, 12, 0);
        now.set(2017, 2, 12, 15, 0);

        when(resources.getString(R.string.yesterday)).thenReturn("Yesterday");

        final String duration =  durationCalculator.getTimeDifference(yesterday, now);
        assertEquals("Yesterday", duration);
    }

    @Test
    public void twoDaysAgoButNot48Hours() {
        final Calendar before = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        before.set(2017, 2, 10, 18, 0);
        now.set(2017, 2, 12, 15, 0);

        when(resources.getString(R.string.days)).thenReturn("days");

        final String duration =  durationCalculator.getTimeDifference(before, now);
        assertEquals("2 days", duration);
    }

    @Test
    public void twoDaysAgoMoreThan48Hours() {
        final Calendar before = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        before.set(2017, 2, 10, 12, 0);
        now.set(2017, 2, 12, 15, 0);

        when(resources.getString(R.string.days)).thenReturn("days");

        final String duration =  durationCalculator.getTimeDifference(before, now);
        assertEquals("2 days", duration);
    }

    @Test
    public void singleWeek() {
        final Calendar before = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        before.set(2017, 3, 1, 12, 0);
        now.set(2017, 3, 10, 15, 0);

        when(resources.getString(R.string.week)).thenReturn("week");

        final String duration =  durationCalculator.getTimeDifference(before, now);
        assertEquals("1 week", duration);
    }

    @Test
    public void multipleWeeks() {
        final Calendar before = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        before.set(2017, 2, 12, 12, 0);
        now.set(2017, 3, 10, 15, 0);

        when(resources.getString(R.string.weeks)).thenReturn("weeks");

        final String duration =  durationCalculator.getTimeDifference(before, now);
        assertEquals("4 weeks", duration);
    }

    @Test
    public void singleMonth() {
        final Calendar before = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        before.set(2017, 1, 10, 12, 0);
        now.set(2017, 2, 12, 15, 0);

        when(resources.getString(R.string.month)).thenReturn("month");

        final String duration =  durationCalculator.getTimeDifference(before, now);
        assertEquals("1 month", duration);
    }

    @Test
    public void nearlyTwoMonths() {
        final Calendar before = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        before.set(2017, 1, 15, 12, 0);
        now.set(2017, 3, 12, 15, 0);

        when(resources.getString(R.string.month)).thenReturn("month");

        final String duration =  durationCalculator.getTimeDifference(before, now);
        assertEquals("1 month", duration);
    }

    @Test
    public void multipleMonths() {
        final Calendar before = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        before.set(2016, 8, 10, 12, 0);
        now.set(2017, 1, 12, 15, 0);

        when(resources.getString(R.string.months)).thenReturn("months");

        final String duration =  durationCalculator.getTimeDifference(before, now);
        assertEquals("5 months", duration);
    }

    @Test
    public void singleYear() {
        final Calendar before = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        before.set(2016, 1, 10, 12, 0);
        now.set(2017, 2, 12, 15, 0);

        when(resources.getString(R.string.year)).thenReturn("year");

        final String duration =  durationCalculator.getTimeDifference(before, now);
        assertEquals("1 year", duration);
    }

    @Test
    public void multipleYears() {
        final Calendar before = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        before.set(2014, 8, 10, 12, 0);
        now.set(2017, 1, 12, 15, 0);

        when(resources.getString(R.string.years)).thenReturn("years");

        final String duration =  durationCalculator.getTimeDifference(before, now);
        assertEquals("2 years", duration);
    }

    @Test
    public void inTheFuture() {
        final Calendar notBefore = Calendar.getInstance();
        final Calendar now = Calendar.getInstance();
        notBefore.set(2017, 1, 12, 16, 0);
        now.set(2017, 1, 12, 15, 0);

        final String duration =  durationCalculator.getTimeDifference(notBefore, now);
        assertEquals("0 days", duration);
    }

}