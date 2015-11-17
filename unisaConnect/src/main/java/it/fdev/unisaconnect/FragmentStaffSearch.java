package it.fdev.unisaconnect;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import it.fdev.unisaconnect.data.StaffDB;
import it.fdev.unisaconnect.data.StaffMember;
import it.fdev.unisaconnect.data.StaffMemberSummary;
import it.fdev.utils.ListAdapterStaff;
import it.fdev.utils.MyListFragment;

public class FragmentStaffSearch extends MyListFragment {
	
	private EditText editTextStaffName;
	private ImageView imgClearName;
	private StaffDB db = null;
	private ListAdapterStaff adapter;
	private ArrayList<StaffMemberSummary> listaRisultati = new ArrayList<StaffMemberSummary>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			db = new StaffDB(mActivity);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		adapter = new ListAdapterStaff(mActivity, R.layout.staff_search_row, new ArrayList<StaffMemberSummary>());
		setListAdapter(adapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_staff_search, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (db == null) {
			return;
		}
		
//		View lastUpdateViewContainer = view.findViewById(R.id.last_update_time_container);
//		TextView lastUpdateView = (TextView) view.findViewById(R.id.last_update_time);
//		try {
//			SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss", Locale.ITALY);
//			Date date = formatter.parse(StaffDB.DB_STAFF_DATE);
//			String dateFirstPart = new SimpleDateFormat("dd/MM/yy", Locale.ITALY).format(date);
//			String updateText = getString(R.string.aggiornato_il, dateFirstPart);
//			lastUpdateView.setText(updateText);
//			lastUpdateViewContainer.setVisibility(View.VISIBLE);
//		} catch (ParseException e) {
//			Log.e(Utils.TAG, "Error parsing date", e);
//			lastUpdateViewContainer.setVisibility(View.GONE);
//		}
		
//		/* Metto animazione */
//		LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.list_layout_controller);
//		/* Indico che la listView di questo ListFragment deve avere il mio controller per l'animazione */
//		getListView().setLayoutAnimation(controller);
		
		
		imgClearName = (ImageView) view.findViewById(R.id.staff_name_clear);
		imgClearName.setVisibility(View.INVISIBLE);
		imgClearName.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editTextStaffName.setText("");
			}
		});
		
		editTextStaffName = (EditText) view.findViewById(R.id.staff_name);
		editTextStaffName.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				initList();
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 0)
					imgClearName.setVisibility(View.VISIBLE);
				else
					imgClearName.setVisibility(View.INVISIBLE);
			}
		});
		initList();
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		if (db == null) {
			return;
		}
		StaffMemberSummary choosenMember = listaRisultati.get(position);
		StaffMember memberDetails = db.getStaffMember(choosenMember.getMatricola());
		FragmentStaffDetails staffDetailsFrag = new FragmentStaffDetails();
		Bundle args = new Bundle();
		args.putParcelable(FragmentStaffDetails.ARG_DETAILS, memberDetails);
        staffDetailsFrag.setArguments(args);
		mActivity.switchContent(staffDetailsFrag);
	}
	
	@Override
	public boolean executeSearch(String query) {
		if (db == null) {
			return false;
		}
		listaRisultati = db.searchStaffNameByWords(query);
		adapter.clear();
		adapter.addAll(listaRisultati);
		adapter.notifyDataSetChanged();
		this.setSelection(0);
		return false;
	}
	
	public void initList() {
		if (!isAdded()) {
			return;
		}
		if (db == null) {
			return;
		}
		String name = editTextStaffName.getText().toString();
		listaRisultati = db.searchStaffNameByWords(name);
		adapter.clear();
		adapter.addAll(listaRisultati);
		adapter.notifyDataSetChanged();
		this.setSelection(0);
	}
	
	@Override
	public void onDestroy() {
		if (db != null) {
			db.close();
		}
		super.onDestroy();
	}
	
	@Override
	public int getTitleResId() {
		return R.string.rubrica;
	}
	
}