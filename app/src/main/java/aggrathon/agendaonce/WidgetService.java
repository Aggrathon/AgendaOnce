package aggrathon.agendaonce;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
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

class WidgetServiceFactory implements RemoteViewsService.RemoteViewsFactory {
	private ArrayList<EventData> events = new ArrayList<>();
	private Context mContext;
	private int mAppWidgetId;

	public WidgetServiceFactory(Context context, Intent intent) {
		mContext = context;
		mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onDataSetChanged() {
		events = EventData.Factory().ReadCalendar(mContext, 30, 20);
	}

	@Override
	public void onDestroy() {
		events.clear();
	}

	@Override
	public int getCount() {
		return events.size();
	}

	@Override
	public RemoteViews getViewAt(int position) {
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
		rv.setTextViewText(R.id.title, events.get(position).title);
		rv.setTextViewText(R.id.time, events.get(position).time);
		rv.setInt(R.id.color, "setBackgroundColor", events.get(position).color);
		rv.setViewVisibility(R.id.alarm, events.get(position).alarm? View.VISIBLE : View.INVISIBLE);
		rv.setViewVisibility(R.id.location, events.get(position).location? View.VISIBLE : View.INVISIBLE);

		// Next, we set a fill-intent which will be used to fill-in the pending intent template
		Bundle extras = new Bundle();
		extras.putLong(AgendaWidget.EXTRA_ITEM, events.get(position).id);
		Intent fillInIntent = new Intent();
		fillInIntent.putExtras(extras);
		rv.setOnClickFillInIntent(R.id.frame, fillInIntent);
//		rv.setOnClickFillInIntent(R.id.frame, AgendaActivity.OpenCalendarIntent());

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