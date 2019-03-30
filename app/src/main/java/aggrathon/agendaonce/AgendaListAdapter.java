package aggrathon.agendaonce;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AgendaListAdapter extends BaseAdapter {

	public static final String[] INSTANCE_PROJECTION = new String[] {
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

	private static final int MILLIS_PER_HOUR = 1000 * 60 * 60;

	private Activity activity;
	private ArrayList<Long> ids;
	private ArrayList<String> titles;
	private ArrayList<String> times;
	private ArrayList<Integer> colors;
	private ArrayList<Boolean> locations;
	private ArrayList<Boolean> alarms;
	private int max;

	public AgendaListAdapter(Activity act, int maxEvents) {
		activity = act;
		ids = new ArrayList<>();
		titles = new ArrayList<>();
		times = new ArrayList<>();
		colors = new ArrayList<>();
		locations = new ArrayList<>();
		alarms = new ArrayList<>();
		max = maxEvents;
	}

	public void RefreshEvents() {
		ids.clear();
		titles.clear();
		times.clear();
		colors.clear();
		locations.clear();
		alarms.clear();

		Calendar currentTime = Calendar.getInstance();
		Calendar beginTime = Calendar.getInstance();
		long startMillis = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.roll(Calendar.MONTH, 1);
		long endMillis = endTime.getTimeInMillis();

		ContentResolver cr = activity.getContentResolver();
		Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, startMillis);
		ContentUris.appendId(builder, endMillis);
		Cursor cur =  cr.query(builder.build(), INSTANCE_PROJECTION, CalendarContract.Instances.VISIBLE + " = 1", null, CalendarContract.Instances.BEGIN);

		DateFormat formatterDateTime = new SimpleDateFormat("EEEE d MMMM H:mm");
		DateFormat formatterTime = new SimpleDateFormat("H:mm");
		DateFormat formatterDate = new SimpleDateFormat("EEEE d MMMM");
		while (cur.moveToNext()) {
			ids.add(cur.getLong(PROJECTION_ID_INDEX));
			titles.add(cur.getString(PROJECTION_TITLE_INDEX));
			int color = cur.getInt(PROJECTION_COLOR_INDEX);
			if (color == 0) color = cur.getInt(PROJECTION_COLOR2_INDEX);
			colors.add(color);
			String loc = cur.getString(PROJECTION_LOCATION_INDEX);
			locations.add(loc != null && loc.length() > 0);
			alarms.add(cur.getInt(PROJECTION_ALARM_INDEX) != 0);
			beginTime.setTimeInMillis(cur.getLong(PROJECTION_BEGIN_INDEX));
			endTime.setTimeInMillis(cur.getLong(PROJECTION_END_INDEX));
			if (cur.getInt(PROJECTION_ALL_DAY_INDEX) != 0) {
				boolean sameDay = (endTime.getTimeInMillis() - beginTime.getTimeInMillis()) / MILLIS_PER_HOUR < 25;
				if (sameDay) {
					times.add(formatterDate.format(beginTime.getTime()));
				} else if (currentTime.getTimeInMillis() > beginTime.getTimeInMillis()) {
					times.add("-> " + formatterDate.format(endTime.getTime()));
				} else {
					times.add(formatterDate.format(beginTime.getTime()) + " - " + formatterDate.format(endTime.getTime()));
				}
			} else {
				boolean sameDay = (beginTime.get(Calendar.YEAR) == endTime.get(Calendar.YEAR)) &&
						(beginTime.get(Calendar.DAY_OF_YEAR) == endTime.get(Calendar.DAY_OF_YEAR));
				if (sameDay) {
					times.add(formatterDateTime.format(beginTime.getTime()) + " - " + formatterTime.format(endTime.getTime()));
				} else if (currentTime.getTimeInMillis() > beginTime.getTimeInMillis()) {
					times.add("-> " + formatterDateTime.format(endTime.getTime()));
				} else {
					times.add(formatterDateTime.format(beginTime.getTime()) + " - " + formatterDateTime.format(endTime.getTime()));
				}
			}
			if (ids.size() == max) break;
		}
		cur.close();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return ids.size();
	}

	@Override
	public String getItem(int i) {
		return titles.get(i) + " (" + times.get(i) + ")";
	}

	@Override
	public long getItemId(int i) {
		return ids.get(i);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup container) {
		if (convertView == null) {
			convertView = activity.getLayoutInflater().inflate(R.layout.list_item, container, false);
		}
		((TextView)convertView.findViewById(R.id.title)).setText(titles.get(position));
		((TextView)convertView.findViewById(R.id.time)).setText(times.get(position));
		convertView.findViewById(R.id.color).setBackgroundColor(colors.get(position));
		convertView.findViewById(R.id.alarm).setVisibility(alarms.get(position)? View.VISIBLE : View.GONE);
		convertView.findViewById(R.id.location).setVisibility(locations.get(position)? View.VISIBLE : View.GONE);
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, getItemId(position));
				Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
				activity.startActivity(intent);
			}
		});
		return convertView;
	}
}
