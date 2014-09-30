package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.StaffMember;
import it.fdev.utils.DrawableManager;
import it.fdev.utils.DrawableManager.DrawableManagerListener;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class FragmentStaffDetails extends MySimpleFragment {
	
	public static final String ARG_DETAILS = "details";

	private StaffMember mStaffMember;
	private DrawableManager mDrawableManager;
	private ImageLoader mImageLoader;

	private ImageView picture, pictureSmall;
	private View imageContainer;
	private VideoView videoView;
	private View infoCard, mapCard, emailCard, ricevimentoCard, websiteCard, phoneCard, faxCard;
	private TextView fullnameText, roleText, deptText, mapText, emailText, ricevimentoText, websiteText;
	private TextView phone1Text, phone2Text, phone3Text, phone4Text;
	private TextView fax1Text, fax2Text, fax3Text, fax4Text;
	private View mapSeparator;
	private View mapButton;

	private final double imageScreenPercentageHeight = 0.4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDrawableManager = new DrawableManager();
		mImageLoader = ImageLoader.getInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_staff_details, container, false);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mStaffMember = getArguments().getParcelable(ARG_DETAILS);
		if (mStaffMember == null || !(mStaffMember instanceof StaffMember)) {
			mActivity.goToLastFrame();
			return;
		}

		infoCard = (View) view.findViewById(R.id.info_card);
		mapCard = (View) view.findViewById(R.id.map_card);
		emailCard = (View) view.findViewById(R.id.email_card);
		ricevimentoCard = (View) view.findViewById(R.id.ricevimento_card);
		websiteCard = (View) view.findViewById(R.id.website_card);
		phoneCard = (View) view.findViewById(R.id.phone_card);
		faxCard = (View) view.findViewById(R.id.fax_card);

		picture = (ImageView) view.findViewById(R.id.picture);
		pictureSmall = (ImageView) view.findViewById(R.id.picture_small);
		imageContainer = view.findViewById(R.id.image_container);
		videoView = (VideoView) view.findViewById(R.id.video);

		fullnameText = (TextView) view.findViewById(R.id.fullname);
		roleText = (TextView) view.findViewById(R.id.roleText);
		deptText = (TextView) view.findViewById(R.id.departmentText);
		deptText = (TextView) view.findViewById(R.id.departmentText);
		mapText = (TextView) view.findViewById(R.id.mapText);
		emailText = (TextView) view.findViewById(R.id.emailText);
		ricevimentoText = (TextView) view.findViewById(R.id.ricevimentoText);
		websiteText = (TextView) view.findViewById(R.id.websiteText);

		phone1Text = (TextView) view.findViewById(R.id.phone1Text);
		phone2Text = (TextView) view.findViewById(R.id.phone2Text);
		phone3Text = (TextView) view.findViewById(R.id.phone3Text);
		phone4Text = (TextView) view.findViewById(R.id.phone4Text);

		fax1Text = (TextView) view.findViewById(R.id.fax1Text);
		fax2Text = (TextView) view.findViewById(R.id.fax2Text);
		fax3Text = (TextView) view.findViewById(R.id.fax3Text);
		fax4Text = (TextView) view.findViewById(R.id.fax4Text);
		
		mapSeparator = view.findViewById(R.id.map_separator);
		mapButton = view.findViewById(R.id.map_button);
		
		// Scelgo l'altezza dell'immagine in base alla grandezza del display
		Display display = mActivity.getWindowManager().getDefaultDisplay();
		int totalHeight;
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
			totalHeight = display.getHeight(); // deprecated
		} else {
			Point size = new Point();
			display.getSize(size);
			totalHeight = size.y;
		}
		picture.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) (totalHeight * imageScreenPercentageHeight)));

		attachLongpressCopy(R.string.informazioni, fullnameText);
		attachLongpressCopy(R.string.informazioni, roleText);
		attachLongpressCopy(R.string.informazioni, deptText);
		attachLongpressCopy(R.string.informazioni, mapText);
		attachLongpressCopy(R.string.email, emailText);
		attachLongpressCopy(R.string.ricevimento, ricevimentoText);
		attachLongpressCopy(R.string.website, websiteText);
		attachLongpressCopy(R.string.telefono, phone1Text);
		attachLongpressCopy(R.string.telefono, phone2Text);
		attachLongpressCopy(R.string.telefono, phone3Text);
		attachLongpressCopy(R.string.telefono, phone4Text);
		attachLongpressCopy(R.string.fax, fax1Text);
		attachLongpressCopy(R.string.fax, fax2Text);
		attachLongpressCopy(R.string.fax, fax3Text);
		attachLongpressCopy(R.string.fax, fax4Text);

		picture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});

		emailText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.sendMail(mActivity, mStaffMember.getEmail(), "", "");
			}
		});

		websiteText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(mStaffMember.getWebsite()));
				startActivity(i);
			}
		});
		
		mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mStaffMember.getLatitudine() <= 0 || mStaffMember.getLongitudine() <= 0) {
					return;
				}
				FragmentMap fragmentMap = new FragmentMap();
				Bundle args = new Bundle();
				args.putDouble("", mStaffMember.getLatitudine());
				args.putDouble("", mStaffMember.getLongitudine());
				fragmentMap.setArguments(args);
				mActivity.switchContent(fragmentMap);
			}
		});

		phone1Text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.startDial(mActivity, mStaffMember.getPhoneList().get(0));
			}
		});
		phone2Text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.startDial(mActivity, mStaffMember.getPhoneList().get(1));
			}
		});
		phone3Text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.startDial(mActivity, mStaffMember.getPhoneList().get(2));
			}
		});
		phone4Text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.startDial(mActivity, mStaffMember.getPhoneList().get(3));
			}
		});
		return;
	}

	private void attachLongpressCopy(final int title, TextView v) {
		v.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				copyText(getString(title), (TextView) v);
				return true;
			}
		});
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void copyText(String titolo, TextView textView) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(textView.getText());
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData.newPlainText(titolo, textView.getText());
			clipboard.setPrimaryClip(clip);
		}
		Toast.makeText(mActivity, getString(R.string.testo_copiato), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onResume() {
		super.onResume();
		showData();
	}

	public void showData() {
		if (!isAdded()) {
			return;
		}
		if (mStaffMember == null)
			return;

		String fullname = mStaffMember.getFullname();
		String role = mStaffMember.getRole();
		String department = mStaffMember.getDepartment();
		String map = mStaffMember.getMapInfo();
		String email = mStaffMember.getEmail();
		String ricevimento = mStaffMember.getRicevimento();
		String website = mStaffMember.getWebsite();
		double latitudine = mStaffMember.getLatitudine();
		double longitudine = mStaffMember.getLongitudine();
		ArrayList<String> phones = mStaffMember.getPhoneList();
		ArrayList<String> faxes = mStaffMember.getFaxList();

		if (mStaffMember.getImgSmallURL() == null || mStaffMember.getImgSmallURL().isEmpty()) {
			imageContainer.setVisibility(View.GONE);
		} else if(mStaffMember.getMatricola().equals("028309")) {
			videoView.setVisibility(View.VISIBLE);
			imageContainer.setVisibility(View.VISIBLE);
			picture.setVisibility(View.INVISIBLE);
			pictureSmall.setVisibility(View.INVISIBLE);
			Uri videoURI = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.v028309 );
			videoView.setVideoURI(videoURI);
			videoView.start();
			videoView.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					videoView.seekTo(0);
					videoView.start();
					return true;
				}
			});
		} else {
			// picture.setVisibility(View.GONE);
			// pictureSmall.setVisibility(View.GONE);
			// loadingContainer.setVisibility(View.VISIBLE);
			mImageLoader.displayImage(mStaffMember.getImgSmallURL(), pictureSmall, new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					imageContainer.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					pictureSmall.setVisibility(View.VISIBLE);
					// loadingContainer.setVisibility(View.GONE);
					if (Utils.hasConnection(mActivity)) {
						// loadingContainer.setVisibility(View.VISIBLE);
						mDrawableManager.fetchDrawableOnThread(mStaffMember.getImgBigURL(), new DrawableManagerListener() {
							@Override
							public void onLoadingComplete(Drawable image) {
								Animation fadeIn = new AlphaAnimation(0, 1);
								fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
								fadeIn.setDuration(300);
								AnimationSet animation = new AnimationSet(false); //change to false
								animation.addAnimation(fadeIn);
								picture.setAnimation(animation);

//								Animation fadeOut = new AlphaAnimation(1, 0);
//								fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
//								fadeOut.setStartOffset(1000);
//								fadeOut.setDuration(500);
//								AnimationSet animation1 = new AnimationSet(false); //change to false
//								animation1.addAnimation(fadeOut);
//								pictureSmall.setAnimation(animation1);
								
								picture.setImageDrawable(image);
//								pictureSmall.setVisibility(View.GONE);
								picture.setVisibility(View.VISIBLE);
								// loadingContainer.setVisibility(View.GONE);
							}

							@Override
							public void onLoadingError() {
								// loadingContainer.setVisibility(View.GONE);
								 picture.setVisibility(View.GONE);
								// pictureSmall.setVisibility(View.VISIBLE);
							}
						});
					} else {
						// loadingContainer.setVisibility(View.GONE);
						picture.setVisibility(View.GONE);
					}
				}

				@Override
				public void onLoadingCancelled(String imageUri, View view) {
				}
			});
		}

		fullnameText.setText(fullname);

		if ((role == null || role.isEmpty()) && (department == null || department.isEmpty())) {
			infoCard.setVisibility(View.GONE);
		}
		if (role == null || role.isEmpty()) {
			roleText.setVisibility(View.GONE);
		} else {
			roleText.setText(role);
		}
		if (department == null || department.isEmpty()) {
			deptText.setVisibility(View.GONE);
		} else {
			deptText.setText(department);
		}

		if (map == null || map.isEmpty()) {
			mapCard.setVisibility(View.GONE);
		} else {
			mapText.setText(map);
		}

		if (email == null || email.isEmpty()) {
			emailCard.setVisibility(View.GONE);
		} else {
			emailText.setText(email);
		}

		if (ricevimento == null || ricevimento.isEmpty()) {
			ricevimentoCard.setVisibility(View.GONE);
		} else {
			ricevimentoText.setText(ricevimento);
		}

		if (website == null || website.isEmpty()) {
			websiteCard.setVisibility(View.GONE);
		} else {
			websiteText.setText(website);
		}
		
		if (latitudine > 0 && longitudine > 0) {
			mapSeparator.setVisibility(View.VISIBLE);
			mapButton.setVisibility(View.VISIBLE);
		} else {
			mapSeparator.setVisibility(View.GONE);
			mapButton.setVisibility(View.GONE);
		}

		// AAARRRGGG impara a programmare! -> layoutInflater!!!
		if (phones.size() > 0)
			phone1Text.setText(phones.get(0));
		else {
			phoneCard.setVisibility(View.GONE);
		}
		if (phones.size() > 1)
			phone2Text.setText(phones.get(1));
		else
			phone2Text.setVisibility(View.GONE);

		if (phones.size() > 2)
			phone3Text.setText(phones.get(2));
		else
			phone3Text.setVisibility(View.GONE);

		if (phones.size() > 3)
			phone4Text.setText(phones.get(3));
		else
			phone4Text.setVisibility(View.GONE);

		if (faxes.size() > 0)
			fax1Text.setText(faxes.get(0));
		else {
			faxCard.setVisibility(View.GONE);
		}
		if (faxes.size() > 1)
			fax2Text.setText(faxes.get(1));
		else
			fax2Text.setVisibility(View.GONE);

		if (faxes.size() > 2)
			fax3Text.setText(faxes.get(2));
		else
			fax3Text.setVisibility(View.GONE);

		if (faxes.size() > 3)
			fax4Text.setText(faxes.get(3));
		else
			fax4Text.setVisibility(View.GONE);
		
	}

	@Override
	public int getTitleResId() {
		return R.string.rubrica;
	}

}
