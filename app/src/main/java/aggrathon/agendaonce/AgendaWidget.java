package aggrathon.agendaonce;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class AgendaWidget extends AppWidgetProvider {
	public static final String CALENDAR = "aggrathon.agendaonce.agendawidget.CALENDAR";
	public static final String VIEW_ACTION = "aggrathon.agendaonce.agendawidget.VIEW_ACTION";
	public static final String EXTRA_ITEM = "aggrathon.agendaonce.agendawidget.EXTRA_ITEM";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("ASD", intent.getAction());
		if (intent.getAction().equals(VIEW_ACTION)) {
			long eventId = intent.getLongExtra(EXTRA_ITEM, 0);
			Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
			Intent intent2 = new Intent(Intent.ACTION_VIEW).setData(uri);
			context.startActivity(intent2);
		} else if (intent.getAction().equals(CALENDAR)) {
			Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
			builder.appendPath("time");
			ContentUris.appendId(builder, System.currentTimeMillis());
			Intent intent2 = new Intent(Intent.ACTION_VIEW).setData(builder.build());
			context.startActivity(intent2);
		}
		super.onReceive(context, intent);
	}

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.agenda_widget);
		// Widget Service Intent
		Intent serviceIntent = new Intent(context, WidgetService.class);
		serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
		views.setRemoteAdapter(R.id.agenda_list, serviceIntent);
		views.setEmptyView(R.id.agenda_list, R.id.calendar_button);
		// Calendar Intent
		Intent clickIntent = AgendaActivity.OpenCalendarIntent();
		PendingIntent clickPI = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.calendar_button, clickPI);
		// View Intent
		Intent viewIntent = new Intent(context, AgendaWidget.class);
		viewIntent.setAction(AgendaWidget.VIEW_ACTION);
		viewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		viewIntent.setData(Uri.parse(viewIntent.toUri(Intent.URI_INTENT_SCHEME)));
		PendingIntent viewPI = PendingIntent.getBroadcast(context, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setPendingIntentTemplate(R.id.agenda_list, viewPI);
		// Update
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		for (int appWidgetId : appWidgetIds) {
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}

