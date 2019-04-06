package aggrathon.agendaonce;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

public class WidgetService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new WidgetServiceFactory(this.getApplicationContext(), intent);
	}
}

class WidgetServiceFactory extends BroadcastReceiver implements RemoteViewsService.RemoteViewsFactory {
	private ArrayList<EventData> events = new ArrayList<>();
	private Context mContext;
	private int mAppWidgetId;
	private PendingIntent alarmIntent;

	public WidgetServiceFactory(Context context, Intent intent) {
		mContext = context;
		mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		alarmIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, WidgetServiceFactory.class), 0);
	}

	// Calendar Update Listener registration
	@Override
	public void onCreate() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
		filter.addDataScheme("content");
		filter.addDataAuthority("com.android.calendar", null);
		mContext.registerReceiver(this, filter);
	}

	// Calendar Update Listener unregistration
	@Override
	public void onDestroy() {
		events.clear();
		mContext.unregisterReceiver(this);
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		am.cancel(alarmIntent);
	}

	// Calendar Update Listener
	@Override
	public void onReceive(Context context, Intent intent) {
		AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		mgr.notifyAppWidgetViewDataChanged(new int[]{mAppWidgetId}, R.id.agenda_list);
	}

	@Override
	public int getCount() {
		return events.size();
	}

	@Override
	public void onDataSetChanged() {
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		am.cancel(alarmIntent);
		events = EventData.Factory().ReadCalendar(mContext, 30, 20);
		long end = 0;
		for (EventData e : events) {
			if (e.millisEnd > 0 && (e.millisEnd < end || end == 0))
				end = e.millisEnd;
		}
		if (end != 0)
			am.set(AlarmManager.RTC, end, alarmIntent);
	}

	@Override
	public RemoteViews getViewAt(int position) {
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
		rv.setTextViewText(R.id.title, events.get(position).title);
		rv.setTextViewText(R.id.time, events.get(position).time);
		rv.setInt(R.id.color, "setBackgroundColor", events.get(position).color);
		rv.setViewVisibility(R.id.alarm, events.get(position).alarm? View.VISIBLE : View.INVISIBLE);
		rv.setViewVisibility(R.id.location, events.get(position).location? View.VISIBLE : View.INVISIBLE);

		Bundle extras = new Bundle();
		extras.putLong(AgendaWidget.EVENT_ID, events.get(position).id);
		Intent fillInIntent = new Intent();
		fillInIntent.putExtras(extras);
		rv.setOnClickFillInIntent(R.id.frame, fillInIntent);

		return rv;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public long getItemId(int i) {
		return events.get(i).id;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
}