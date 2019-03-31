package aggrathon.agendaonce;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AgendaListAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<EventData> events;

	public AgendaListAdapter(Activity act) {
		activity = act;
		events = new ArrayList<>();
	}

	public void RefreshEvents() {
		events = EventData.Factory().ReadCalendar(activity, 30, 50);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return events.size();
	}

	@Override
	public String getItem(int i) {
		return events.get(i).title + " (" + events.get(i).time + ")";
	}

	@Override
	public long getItemId(int i) {
		return events.get(i).id;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup container) {
		if (convertView == null) {
			convertView = activity.getLayoutInflater().inflate(R.layout.widget_item, container, false);
		}
		((TextView)convertView.findViewById(R.id.title)).setText(events.get(position).title);
		((TextView)convertView.findViewById(R.id.time)).setText(events.get(position).time);
		convertView.findViewById(R.id.color).setBackgroundColor(events.get(position).color);
		convertView.findViewById(R.id.alarm).setVisibility(events.get(position).alarm? View.VISIBLE : View.INVISIBLE);
		convertView.findViewById(R.id.location).setVisibility(events.get(position).location? View.VISIBLE : View.INVISIBLE);
		convertView.setOnClickListener(events.get(position));
		return convertView;
	}
}
