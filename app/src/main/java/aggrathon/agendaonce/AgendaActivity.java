package aggrathon.agendaonce;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;


public class AgendaActivity extends AppCompatActivity {

	public static int CALENDAR_READ_PERMISSION = 13249;

	ListView listAgenda;
	AgendaListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agenda_activity);
		listAgenda = findViewById(R.id.agenda_list);
		adapter = new AgendaListAdapter(this);
		listAgenda.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
			adapter.RefreshEvents();
		} else {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, CALENDAR_READ_PERMISSION);
		}
		super.onResume();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == CALENDAR_READ_PERMISSION) {
			adapter.RefreshEvents();
			AppWidgetManager mgr = AppWidgetManager.getInstance(this);
			int[] ids = mgr.getAppWidgetIds(new ComponentName(getApplication(), AgendaWidget.class));
			mgr.notifyAppWidgetViewDataChanged(ids, R.id.agenda_list);
		} else
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	public void OnCalendar(View v) {
		startActivity(OpenCalendarIntent());
	}

	public static Intent OpenCalendarIntent() {
		Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
		builder.appendPath("time");
		ContentUris.appendId(builder, System.currentTimeMillis());
		return new Intent(Intent.ACTION_VIEW).setData(builder.build());
	}

	public static Intent OpenEventIntent(long id) {
		Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id);
		return new Intent(Intent.ACTION_VIEW).setData(uri);
	}
}
