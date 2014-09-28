package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentBiblioPrepareSearch extends MySimpleFragment {
	
	public static final String BIBLIO_BASE_URL = "http://biblio-aleph.unisa.it/F/";
	public static final String BIBLIO_SET_SETTINGS_URL = BIBLIO_BASE_URL + "?file_name=find-b&func=option-update&SHORT_NO_LINES=20&AUTO_FULL=15&SHORT_FORMAT=000&SCAN_INCLUDE_AUT=N&x=0&y=0";

	View advancedSearchToggleCard, advancedSearchCard;
	EditText testoView, annoDaView, annoAView;
	CheckBox adjacentCheckbox;
	Spinner campoSpinner, linguaSpinner, formatoSpinner, areaDisciplinareSpinner;
	TextView cercaView;

	SharedPrefDataManager mDataManager;
	CharSequence[] campoValuesArray, linguaValuesArray, formatoValuesArray, areaDisciplinareValuesArray;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.fragment_biblio_prepare_search, container, false);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		advancedSearchToggleCard = view.findViewById(R.id.advanced_search_toggle_card);
		advancedSearchCard = view.findViewById(R.id.advanced_search_card);
		testoView = (EditText) view.findViewById(R.id.testo);
		annoDaView = (EditText) view.findViewById(R.id.anno_da);
		annoAView = (EditText) view.findViewById(R.id.anno_a);
		adjacentCheckbox = (CheckBox) view.findViewById(R.id.adjacent);
		campoSpinner = (Spinner) view.findViewById(R.id.campo);
		linguaSpinner = (Spinner) view.findViewById(R.id.lingua);
		formatoSpinner = (Spinner) view.findViewById(R.id.formato);
		areaDisciplinareSpinner = (Spinner) view.findViewById(R.id.area_disciplinare);
		
		cercaView = (TextView) view.findViewById(R.id.button_search);
		cercaView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rimandaARicerca();
			}
		});
		
		advancedSearchToggleCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.expand(advancedSearchCard, 2);
				advancedSearchToggleCard.setVisibility(View.GONE);
//				Utils.collapse(advancedSearchToggleCard, 1);
			}
		});
		
		campoValuesArray = resources.getTextArray(R.array.biblio_search_campo_VALUES);
		linguaValuesArray = resources.getTextArray(R.array.biblio_search_lingua_VALUES);
		formatoValuesArray = resources.getTextArray(R.array.biblio_search_formato_VALUES);
		areaDisciplinareValuesArray = resources.getTextArray(R.array.biblio_search_area_disciplinare_VALUES);
		
		mDataManager = new SharedPrefDataManager(mActivity);
		testoView.setText(mDataManager.getBiblioLastSearch());
		
	}
	
	private void rimandaARicerca() {
		String testo = testoView.getText().toString().trim();
		
		if (testo.length() < 1) {
			Toast.makeText(mActivity, R.string.cerca_testo_non_vuoto, Toast.LENGTH_LONG).show();
			return;
		}
		
		mDataManager.setBiblioLastSearch(testo);
		
		String annoDa = annoDaView.getText().toString();
		String annoA = annoAView.getText().toString();
		boolean adjacent = adjacentCheckbox.isChecked();
		
		String campoID = campoValuesArray[campoSpinner.getSelectedItemPosition()].toString();
		String linguaID = linguaValuesArray[linguaSpinner.getSelectedItemPosition()].toString();
		String formatoID = formatoValuesArray[formatoSpinner.getSelectedItemPosition()].toString();
		String areaID = areaDisciplinareValuesArray[areaDisciplinareSpinner.getSelectedItemPosition()].toString();
		
		/*
		 * http://biblio-aleph.unisa.it/F/12LCUIQRU5PHFLF8QD7RDJFQQ7INK9IBSJYTRP6TUBJGS1694F-02869?func=find-b&request=asdasd&find_code=WRD&adjacent=N&x=13&y=9&filter_code_1=WLN&filter_request_1=&filter_code_2=WYR&filter_request_2=&filter_code_3=WYR&filter_request_3=&filter_code_4=WFT&filter_request_4=&filter_code_5=WSB&filter_request_5=
		 * http://biblio-aleph.unisa.it/F/F4RJS25A41IMXU2RH972JE7578SVJ54FBIF68AL2U6PLG82FT6-02494?func=find-b&request=testo&find_code=WTI&adjacent=Y&x=47&y=13&filter_code_1=WLN&filter_request_1=ITA&filter_code_2=WYR&filter_request_2=da&filter_code_3=WYR&filter_request_3=a&filter_code_4=WFT&filter_request_4=BK&filter_code_5=WSB&filter_request_5=ECO
		 * 
		 * PARAMETRI:
		 * request 			-> testo
		 * find_code 		-> campo
		 * adjacent 		-> adjacent
		 * x				-> ?
		 * y				-> ?
		 * filter_code_1 	-> ?			-> DEF: WLN
		 * filter_request_1 -> lingua
		 * filter_code_2 	-> ?			-> DEF: WYR
		 * filter_request_2 -> annoDA
		 * filter_code_3 	-> ?			-> DEF: WYR
		 * filter_request_3 -> annoA
		 * filter_code_4 	-> ?			-> DEF: WTF
		 * filter_request_4 -> formato
		 * filter_code_5 	-> ?			-> DEF: WSB
		 * filter_request_5 -> areaDisciplinare
		 * 
		 */
		
		String uri = Uri.parse(BIBLIO_BASE_URL)
                .buildUpon()
                .appendQueryParameter("func", "find-b")
                .appendQueryParameter("request", testo)
                .appendQueryParameter("find_code", campoID)
                .appendQueryParameter("adjacent", ((adjacent) ? "Y" : "N"))
                .appendQueryParameter("x", "0")
                .appendQueryParameter("y", "0")
                .appendQueryParameter("filter_code_1", "WLN")
                .appendQueryParameter("filter_request_1", linguaID)
                .appendQueryParameter("filter_code_2", "WYR")
                .appendQueryParameter("filter_request_2", annoDa)
                .appendQueryParameter("filter_code_3", "WYR")
                .appendQueryParameter("filter_request_3", annoA)
                .appendQueryParameter("filter_code_4", "WTF")
                .appendQueryParameter("filter_request_4", formatoID)
                .appendQueryParameter("filter_code_5", "WSB")
                .appendQueryParameter("filter_request_5", areaID)
                .build()
                .toString();
		
		FragmentBiblioDoSearch fragmentDoSearch = new FragmentBiblioDoSearch();
		fragmentDoSearch.setURL(uri);
		mActivity.switchContent(fragmentDoSearch);
		
	}
	
	@Override
	public int getTitleResId() {
		return R.string.cerca_libro;
	}
	
}