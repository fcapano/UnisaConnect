package it.fdev.unisaconnect;

import java.util.ArrayList;

import it.fdev.scrapers.MenuMensaScraper;
import it.fdev.scrapers.MenuMensaScraper.MenuMensa;
import it.fdev.scrapers.MenuMensaScraper.PiattoMensa;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MensaFragment extends MySimpleFragment {

	private MenuMensaScraper mensaScraper;
	private boolean alreadyStarted = false;
	private MenuMensa menu;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.menu_mensa, container, false);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getMenu(false);
	}

	@Override
	public void setVisibleActions() {
		activity.setActionRefreshVisible(true);
	}

	@Override
	public void actionRefresh() {
		getMenu(true);
	}

	public void getMenu(boolean force) {
		if (!isAdded()) {
			return;
		}
		if (!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, this);
			return;
		}
		if (force || !alreadyStarted) {
			alreadyStarted = true;
			if (mensaScraper != null && mensaScraper.isRunning) {
				return;
			}
			Utils.createDialog(activity, getString(R.string.caricamento), false);
			mensaScraper = new MenuMensaScraper();
			mensaScraper.setCallerMenuMensaFragment(this);
			mensaScraper.execute(activity);
		} else {
			mostraMenu(menu);
		}
	}

	public void mostraMenu(MenuMensa menu) {
		View menuContainerView = activity.findViewById(R.id.menu_list_container);
		View menuNDView = activity.findViewById(R.id.menu_non_disponibile);
		if (menu == null && this.menu == null) {
			menuContainerView.setVisibility(View.GONE);
			menuNDView.setVisibility(View.VISIBLE);
			return;
		} else {
			menuContainerView.setVisibility(View.VISIBLE);
			menuNDView.setVisibility(View.GONE);
		}
		if (menu != null) {
			this.menu = menu;
		}
		LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout menuListView = (LinearLayout) activity.findViewById(R.id.menu_list);
		menuListView.removeAllViews();

		ArrayList<PiattoMensa> firstCourses = this.menu.getFirstCourses();
		ArrayList<PiattoMensa> secondCourses = this.menu.getSecondCourses();
		ArrayList<PiattoMensa> sideCourses = this.menu.getSideCourses();
		ArrayList<PiattoMensa> fruitCourses = this.menu.getFruitCourses();
		ArrayList<PiattoMensa> takeAwayCourses = this.menu.getTakeAwayBasketCourses();
		ArrayList<PiattoMensa> otherCourses = this.menu.getOtherCourses();

		if (firstCourses != null && firstCourses.size() > 0) {
			LinearLayout courseView = inflateCourse(firstCourses, getString(R.string.mensa_primi), layoutInflater);
			menuListView.addView(courseView);
		}

		if (secondCourses != null && secondCourses.size() > 0) {
			LinearLayout courseView = inflateCourse(secondCourses, getString(R.string.mensa_secondi), layoutInflater);
			menuListView.addView(courseView);
		}

		if (sideCourses != null && sideCourses.size() > 0) {
			LinearLayout courseView = inflateCourse(sideCourses, getString(R.string.mensa_contorni), layoutInflater);
			menuListView.addView(courseView);
		}

		if (otherCourses != null) {
			LinearLayout courseView = inflateCourse(otherCourses, getString(R.string.mensa_altro), layoutInflater);
			menuListView.addView(courseView);
		} else {
			if (fruitCourses != null && fruitCourses.size() > 0) {
				LinearLayout courseView = inflateCourse(fruitCourses, getString(R.string.mensa_frutta), layoutInflater);
				menuListView.addView(courseView);
			}
			if (takeAwayCourses != null && takeAwayCourses.size() > 0) {
				LinearLayout courseView = inflateCourse(takeAwayCourses, getString(R.string.mensa_centino), layoutInflater);
				menuListView.addView(courseView);
			}
		}

	}

	private LinearLayout inflateCourse(ArrayList<PiattoMensa> courses, String name, LayoutInflater layoutInflater) {
		LinearLayout courseView = (LinearLayout) layoutInflater.inflate(R.layout.menu_mensa_course, null);
		TextView labelView = (TextView) courseView.findViewById(R.id.course_label);
		labelView.setText(name);

		for (PiattoMensa cCourse : courses) {
			String nome = cCourse.getNomePiatto();
			String ingredientiIT = cCourse.getIngredientiIt();
			String ingredientiEN = cCourse.getIngradientiEn();
			if (nome != null) {
				LinearLayout detailsView = (LinearLayout) layoutInflater.inflate(R.layout.menu_mensa_course_details, null);
				TextView nameView = (TextView) detailsView.findViewById(R.id.course_name);
				TextView ingredientsITView = (TextView) detailsView.findViewById(R.id.course_ingredients_it);
				TextView ingredientsENView = (TextView) detailsView.findViewById(R.id.course_ingredients_en);
				nameView.setText(nome);
				if (ingredientiIT == null) {
					ingredientsITView.setVisibility(TextView.GONE);
				} else {
					ingredientsITView.setText(ingredientiIT);
				}
				if (ingredientiEN == null) {
					ingredientsENView.setVisibility(TextView.GONE);
				} else {
					ingredientsENView.setText(ingredientiEN);
				}
				courseView.addView(detailsView);
			}
		}
		return courseView;
	}

	@Override
	public void onStop() {
		if (mensaScraper != null && mensaScraper.isRunning) {
			mensaScraper.cancel(true);
		}
		super.onStop();
	}
}
