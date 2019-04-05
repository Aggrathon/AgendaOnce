package aggrathon.agendaonce;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class EventData implements View.OnClickListener {

	public static final int NO_EVENT_ID = 0;

	public String title;
	public String time;
	public boolean alarm;
	public boolean location;
	public int color;
	public long id;
	public long millisEnd;

	public EventData(long id, String title, String time, int color, boolean alarm, boolean location, long end) {
		this.title = title;
		this.time = time;
		this.alarm = alarm;
		this.location = location;
		this.color = color;
		this.id = id;
		millisEnd = end;
	}

	@Override
	public void onClick(View view) {
		if (id == NO_EVENT_ID)
			view.getContext().startActivity(AgendaActivity.OpenCalendarIntent());
		else
			view.getContext().startActivity(AgendaActivity.OpenEventIntent(id));
	}

	public static EventDataFactory Factory() { return new EventDataFactory(); }
}

class EventDataFactory {

	private static final int MILLIS_PER_HOUR = 1000 * 60 * 60;

	private static final String[] INSTANCE_PROJECTION = new String[] {
		CalendarContract.Instances.EVENT_ID,      // 0
		CalendarContract.Instances.BEGIN,         // 1
		CalendarContract.Instances.END,           // 2
		CalendarContract.Instances.TITLE,         // 3
		CalendarContract.Instances.ALL_DAY,       // 4
		CalendarContract.Instances.EVENT_COLOR,   // 5
		CalendarContract.Instances.CALENDAR_COLOR,// 6
		CalendarContract.Instances.EVENT_LOCATION,// 7
		CalendarContract.Instances.HAS_ALARM,     // 8
		CalendarContract.Instances.VISIBLE
	};
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_BEGIN_INDEX = 1;
	private static final int PROJECTION_END_INDEX = 2;
	private static final int PROJECTION_TITLE_INDEX = 3;
	private static final int PROJECTION_ALL_DAY_INDEX = 4;
	private static final int PROJECTION_COLOR_INDEX = 5;
	private static final int PROJECTION_COLOR2_INDEX = 6;
	private static final int PROJECTION_LOCATION_INDEX = 7;
	private static final int PROJECTION_ALARM_INDEX = 8;

	DateFormat formatterDateTime = new SimpleDateFormat("EEEE d MMMM H:mm");
	DateFormat formatterTime = new SimpleDateFormat("H:mm");
	DateFormat formatterDate = new SimpleDateFormat("EEEE d MMMM");
	Calendar currentTime = Calendar.getInstance();
	Calendar beginTime = Calendar.getInstance();
	Calendar endTime = Calendar.getInstance();

	public  EventData Build(long id, String title, boolean all_day, long begin, long end, int color1, int color2, boolean alarm, String location) {
		beginTime.setTimeInMillis(begin);
		endTime.setTimeInMillis(end);
		String time = null;
		if (all_day) {
			boolean sameDay = (endTime.getTimeInMillis() - beginTime.getTimeInMillis()) / MILLIS_PER_HOUR < 25;
			if (sameDay) {
				time = formatterDate.format(beginTime.getTime());
			} else if (currentTime.getTimeInMillis() > beginTime.getTimeInMillis()) {
				time = "-> " + formatterDate.format(endTime.getTime());
			} else {
				time = formatterDate.format(beginTime.getTime()) + " - " + formatterDate.format(endTime.getTime());
			}
		} else {
			boolean sameDay = (beginTime.get(Calendar.YEAR) == endTime.get(Calendar.YEAR)) &&
					(beginTime.get(Calendar.DAY_OF_YEAR) == endTime.get(Calendar.DAY_OF_YEAR));
			if (sameDay) {
				time = formatterDateTime.format(beginTime.getTime()) + " - " + formatterTime.format(endTime.getTime());
			} else if (currentTime.getTimeInMillis() > beginTime.getTimeInMillis()) {
				time = "-> " + formatterDateTime.format(endTime.getTime());
			} else {
				time = formatterDateTime.format(beginTime.getTime()) + " - " + formatterDateTime.format(endTime.getTime());
			}
		}
		if (color1 == 0) color1 = color2;
		return new EventData(id, title, time, color1, alarm, location != null && location.length() > 0, endTime.getTimeInMillis());
	}

	public EventData NoEvents(int days) {
		return new EventData(0, "No events during the next "+ days + " days!", "", EventData.NO_EVENT_ID, false, false, 0);
	}

	public EventData Loading() {
		return new EventData(0, "Loading...", "", EventData.NO_EVENT_ID, false, false, 0);
	}

	public EventData Permission() {
		return new EventData(0, "Requires permission to read calendars", "", EventData.NO_EVENT_ID, false, false, 0);
	}

	public ArrayList<EventData> ReadCalendar(Context context) { return ReadCalendar(context,30, 50); }

	public ArrayList<EventData> ReadCalendar(Context context, int days, int maxEvents) {
		ArrayList<EventData> list = new ArrayList<>();
		try {
			ContentResolver cr = context.getContentResolver();
			Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
			ContentUris.appendId(builder, currentTime.getTimeInMillis());
			endTime = Calendar.getInstance();
			endTime.roll(Calendar.DAY_OF_YEAR, days);
			ContentUris.appendId(builder, endTime.getTimeInMillis());
			Cursor cur = cr.query(builder.build(), INSTANCE_PROJECTION, CalendarContract.Instances.VISIBLE + " = 1", null, CalendarContract.Instances.BEGIN);

			while (cur.moveToNext()) {
				list.add(Build(
						cur.getLong(PROJECTION_ID_INDEX),
						cur.getString(PROJECTION_TITLE_INDEX),
						cur.getInt(PROJECTION_ALL_DAY_INDEX) > 0,
						cur.getLong(PROJECTION_BEGIN_INDEX),
						cur.getLong(PROJECTION_END_INDEX),
						cur.getInt(PROJECTION_COLOR_INDEX),
						cur.getInt(PROJECTION_COLOR2_INDEX),
						cur.getInt(PROJECTION_ALARM_INDEX) > 0,
						cur.getString(PROJECTION_LOCATION_INDEX)
				));
				if (list.size() >= maxEvents) break;
			}
			cur.close();
			if (list.size() == 0) {
				list.add(NoEvents(days));
			}
		} catch (SecurityException e) {
			Log.e("EventData", "Permission Denied");
			list.clear();
			list.add(Permission());
		} catch (Exception e) {
			Log.e("EventData", e.getLocalizedMessage(), e);
		}
		return list;
	}
}
