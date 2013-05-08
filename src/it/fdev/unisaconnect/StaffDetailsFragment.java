package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.StaffMember;
import it.fdev.utils.DrawableManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class StaffDetailsFragment extends MySimpleFragment {
	
	private StaffMember staffMemberDetails;
	private DrawableManager drawableManager;
	private ImageLoader imageLoader;
	private View mainView;
	
	private ImageView picture;
	private TextView infoTitle, mapTitle, emailTitle, websiteTitle, phoneTitle, faxTitle;
	private View infoSep, mapSep, emailSep, websiteSep, phoneSep, faxSep;
	private TextView fullnameText, roleText, deptText, mapText, emailText, websiteText;
	private TextView phone1Text, phone2Text, phone3Text, phone4Text;
	private TextView fax1Text, fax2Text, fax3Text, fax4Text;
	
	private final double imageScreenPercentageHeight = 0.4;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		drawableManager = new DrawableManager();
		imageLoader = ImageLoader.getInstance();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainView = (View) inflater.inflate(R.layout.staff_details, container, false);
		return mainView;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		infoTitle = (TextView) mainView.findViewById(R.id.infoTitle);
		mapTitle = (TextView) mainView.findViewById(R.id.mapTitle);
		emailTitle = (TextView) mainView.findViewById(R.id.emailTitle);
		websiteTitle = (TextView) mainView.findViewById(R.id.websiteTitle);
		phoneTitle = (TextView) mainView.findViewById(R.id.phoneTitle);
		faxTitle = (TextView) mainView.findViewById(R.id.faxTitle);
		
		infoSep = (View) mainView.findViewById(R.id.infoSep);
		mapSep = (View) mainView.findViewById(R.id.mapSep);
		emailSep = (View) mainView.findViewById(R.id.emailSep);
		websiteSep = (View) mainView.findViewById(R.id.websiteSep);
		phoneSep = (View) mainView.findViewById(R.id.phoneSep);
		faxSep = (View) mainView.findViewById(R.id.faxSep);
		
		picture = (ImageView) mainView.findViewById(R.id.picture);
		fullnameText = (TextView) mainView.findViewById(R.id.fullname);
		roleText = (TextView) mainView.findViewById(R.id.roleText);
		deptText = (TextView) mainView.findViewById(R.id.departmentText);
		deptText = (TextView) mainView.findViewById(R.id.departmentText);
		mapText = (TextView) mainView.findViewById(R.id.mapText);
		emailText = (TextView) mainView.findViewById(R.id.emailText);
		websiteText = (TextView) mainView.findViewById(R.id.websiteText);
		
		phone1Text = (TextView) mainView.findViewById(R.id.phone1Text);
		phone2Text = (TextView) mainView.findViewById(R.id.phone2Text);
		phone3Text = (TextView) mainView.findViewById(R.id.phone3Text);
		phone4Text = (TextView) mainView.findViewById(R.id.phone4Text);
		
		fax1Text = (TextView) mainView.findViewById(R.id.fax1Text);
		fax2Text = (TextView) mainView.findViewById(R.id.fax2Text);
		fax3Text = (TextView) mainView.findViewById(R.id.fax3Text);
		fax4Text = (TextView) mainView.findViewById(R.id.fax4Text);
		
		
		Display display = activity.getWindowManager().getDefaultDisplay();
		int totalHeight;
		int sdk = android.os.Build.VERSION.SDK_INT;
		if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
			totalHeight = display.getHeight();  // deprecated
        } else {
        	Point size = new Point();
        	display.getSize(size);
        	totalHeight = size.y;
        }
//		int totalHeight = activity.findViewById(R.id.content_frame).getHeight();
		picture.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) (totalHeight*imageScreenPercentageHeight)));
		
		
		attachLongpressCopy(R.string.informazioni, fullnameText);
		attachLongpressCopy(R.string.informazioni, roleText);
		attachLongpressCopy(R.string.informazioni, deptText);
		attachLongpressCopy(R.string.informazioni, mapText);
		attachLongpressCopy(R.string.email, emailText);
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
				Utils.sendMail(activity, new String[] {staffMemberDetails.getEmail()}, "", "");
			}
		});
		
		websiteText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(staffMemberDetails.getWebsite()));
				startActivity(i);
			}
		});
		
		phone1Text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.startDial(activity, staffMemberDetails.getPhoneList().get(0));
			}
		});
		phone2Text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.startDial(activity, staffMemberDetails.getPhoneList().get(1));
			}
		});
		phone3Text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.startDial(activity, staffMemberDetails.getPhoneList().get(2));
			}
		});
		phone4Text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.startDial(activity, staffMemberDetails.getPhoneList().get(3));
			}
		});
//		actionRefresh();
		return;
	}
	
	private void attachLongpressCopy(final int title, TextView v) {
		v.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
            	copyText(getString(title),v);
                return true;
            }
        });
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void copyText(String titolo, View view){
		TextView textView = (TextView) view;
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(textView.getText());
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE); 
            android.content.ClipData clip = android.content.ClipData.newPlainText(titolo,textView.getText());
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(activity, getString(R.string.testo_copiato), Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void setVisibleActions() {
	}
	
	@Override
	public void onResume() {
		super.onResume();
		actionRefresh();
	}
	
	@Override
	public void actionRefresh() {
		if (!isAdded()) {
			return;
		}
		if(staffMemberDetails == null)
			return;
		
		String fullname = staffMemberDetails.getFullname();
		String role = staffMemberDetails.getRole();
		String department = staffMemberDetails.getDepartment();
		String map = staffMemberDetails.getMapInfo();
		String email = staffMemberDetails.getEmail();
		String website = staffMemberDetails.getWebsite();
		ArrayList<String> phones = staffMemberDetails.getPhoneList();
		ArrayList<String> faxes = staffMemberDetails.getFaxList();
		
		
		if(staffMemberDetails.getSmallImgUrl().equals("")) {
			picture.setVisibility(View.GONE);
			fullnameText.setBackgroundColor(resources.getColor(android.R.color.transparent));
		} else {
			//Imposto quella di bassa qualità che dovrebbe essere già in cache mentre si carica l'altra
			imageLoader.displayImage(staffMemberDetails.getSmallImgUrl(), picture, new ImageLoadingListener() {
				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
				}
				@Override
				public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
					drawableManager.fetchDrawableOnThread(staffMemberDetails.getImgUrl(), picture);
				}
				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
				}
				@Override
				public void onLoadingStarted(String arg0, View arg1) {
				}
			});
		}
		
		fullnameText.setText(fullname);
		
		if( (role==null || role.equals("")) && (department==null || department.equals("")) ) {
			infoTitle.setVisibility(View.GONE);
			infoSep.setVisibility(View.GONE);
		}
		if(role==null || role.equals("")) {
			roleText.setVisibility(View.GONE);
		} else {
			roleText.setText(role);
		}
		if(department==null || department.equals("")) {
			deptText.setVisibility(View.GONE);
		} else {
			deptText.setText(department);
		}
		
		if(map==null || map.equals("")) {
			mapTitle.setVisibility(View.GONE);
			mapSep.setVisibility(View.GONE);
			mapText.setVisibility(View.GONE);
		} else {
			mapText.setText(map);
		}
		
		if(email==null || email.equals("")) {
			emailTitle.setVisibility(View.GONE);
			emailSep.setVisibility(View.GONE);
			emailText.setVisibility(View.GONE);
		} else {
			emailText.setText(email);
		}
		
		if(website==null || website.equals("")) {
			websiteTitle.setVisibility(View.GONE);
			websiteSep.setVisibility(View.GONE);
			websiteText.setVisibility(View.GONE);
		} else {
			websiteText.setText(website);
		}
		
		//AAARRRGGG impara a programmare! -> layoutInflater!!!
		if(phones.size()>0)
			phone1Text.setText(phones.get(0));
		else {
			phoneTitle.setVisibility(View.GONE);
			phoneSep.setVisibility(View.GONE);
			phone1Text.setVisibility(View.GONE);
		}
		if(phones.size()>1)
			phone2Text.setText(phones.get(1));
		else
			phone2Text.setVisibility(View.GONE);
		
		if(phones.size()>2)
			phone3Text.setText(phones.get(2));
		else
			phone3Text.setVisibility(View.GONE);
		
		if(phones.size()>3)
			phone4Text.setText(phones.get(3));
		else
			phone4Text.setVisibility(View.GONE);
		
		
		if(faxes.size()>0)
			fax1Text.setText(faxes.get(0));
		else {
			faxTitle.setVisibility(View.GONE);
			faxSep.setVisibility(View.GONE);
			fax1Text.setVisibility(View.GONE);
		}
		if(faxes.size()>1)
			fax2Text.setText(faxes.get(1));
		else
			fax2Text.setVisibility(View.GONE);
		
		if(faxes.size()>2)
			fax3Text.setText(faxes.get(2));
		else
			fax3Text.setVisibility(View.GONE);
		
		if(faxes.size()>3)
			fax4Text.setText(faxes.get(3));
		else
			fax4Text.setVisibility(View.GONE);
	}

	public void setMemberDetails(StaffMember staffMemberDetails) {
		this.staffMemberDetails = staffMemberDetails;
	}
	
}
