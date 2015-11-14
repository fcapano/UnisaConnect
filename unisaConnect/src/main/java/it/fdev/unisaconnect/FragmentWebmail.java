package it.fdev.unisaconnect;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;

import it.fdev.mailSync.MailChecker;
import it.fdev.mailSync.MailIntentService;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

public class FragmentWebmail extends MySimpleFragment {

	private SharedPrefDataManager mDataManager;
	private Fragment thisFragment;
	private LinearLayout mailListView;
	private CheckBox checkMailCheckbox;
	
	private IntentFilter mIntentFilter = new IntentFilter();
	private final BroadcastReceiver mHandlerBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onNewBroadcast(context, intent);
		}
	};

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	};
	
	public void onNewBroadcast(Context context, Intent intent) {
		try {
			if (MailIntentService.BROADCAST_STATE_MAIL_LIST.equals(intent.getAction())) {
				if (intent.hasExtra("list")) {
					Message[] mailList = (Message[]) intent.getParcelableArrayExtra("list");
					if (mailList != null && mailList.length > 0) {
						addMailList(mailList);
					} else {
						addMailList(null);
					}
				} else {
					addMailList(null);
				}
			}
		} catch (Exception e) {
			Log.e(Utils.TAG, "onReceiveBroadcast exception", e);
			addMailList(null);
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.fragment_webmail, container, false);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mIntentFilter.addAction(MailIntentService.BROADCAST_STATE_MAIL_LIST);

		mailListView = (LinearLayout) view.findViewById(R.id.mail_list);
		checkMailCheckbox = (CheckBox) view.findViewById(R.id.check_mail_option);
		thisFragment = this;

		mDataManager = new SharedPrefDataManager(mActivity);
		if (!mDataManager.loginDataExists()) { // Non sono memorizzati i dati utente
			Utils.createAlert(mActivity, getString(R.string.dati_errati), BootableFragmentsEnum.ACCOUNT, false);
			return;
		}

		checkMailCheckbox.setChecked(mDataManager.getMailDoCheck());
		checkMailCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mDataManager.setMailDoCheck(isChecked);
				MailChecker.autoSetAlarm(mActivity);
			}
		});
		
		Intent fetchMailIntent = new Intent(mActivity, MailIntentService.class);
		mActivity.startService(fetchMailIntent);

//		IMAPFolder folder = null;
//		Store store = null;
//
//		try {
//			Properties props = System.getProperties();
//			props.setProperty("mail.store.protocol", "imaps");
//			Session session = Session.getDefaultInstance(props, null);
//			store = session.getStore("imaps");
//			store.connect(MailChecker.IMAP_SERVER, mDataManager.getUser() + "@studenti.unisa.it", mDataManager.getPass());
//
//			folder = (IMAPFolder) store.getFolder("inbox");
//			if (!folder.isOpen()) {
//				folder.open(Folder.READ_ONLY);
//			}
//
//			int numMsg = folder.getMessageCount();
//			if (numMsg < 1) {
//				return;
//			}
//
//			int unreadNumMsg = folder.getUnreadMessageCount();
//			int oldestMessageToGet = Math.max(0, numMsg - 20);
//
//			Message[] messageList = folder.getMessages(oldestMessageToGet, numMsg);
//			addMailList(messageList);
//
//			Message lastMessage = messageList[0];
//			Date lastReceivedDate = lastMessage.getReceivedDate();
//			mDataManager.setMailLastRead(lastReceivedDate);
//			
//
//		} catch (MessagingException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (folder != null && folder.isOpen()) {
//					folder.close(true);
//				}
//				if (store != null) {
//					store.close();
//				}
//			} catch (MessagingException e) {
//				e.printStackTrace();
//			}
//		}

		return;
	}

	@Override
	public boolean goBack() {
		return super.goBack();
	}

	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_refresh_button);
		return actionsToShow;
	}

	@Override
	public void actionRefresh() {
		if (!isAdded()) {
			return;
		}
		if (!Utils.hasConnection(mActivity)) {
			Utils.goToInternetError(mActivity, thisFragment);
			return;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mActivity.unregisterReceiver(mHandlerBroadcast);
	}

	@Override
	public void onResume() {
		super.onResume();
		mActivity.registerReceiver(mHandlerBroadcast, mIntentFilter);
	}

	@Override
	public void onDestroy() {
		super.onStop();
	}

	@Override
	public int getTitleResId() {
		return R.string.webmail;
	}

	private void addMailList(Message[] messageList) {
		if (messageList == null || messageList.length <= 0) {
			Log.d(Utils.TAG, "list null");
			return;
		}
		
		
		LayoutInflater layoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		for (Message message : messageList) {
			try {
				LinearLayout cMailView = inflateMail(message, layoutInflater);
				mailListView.addView(cMailView);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}

	}

	private LinearLayout inflateMail(Message message, LayoutInflater layoutInflater) throws MessagingException {
		LinearLayout mailView = (LinearLayout) layoutInflater.inflate(R.layout.mail_row, null);

		TextView senderView = (TextView) mailView.findViewById(R.id.mail_sender);
		senderView.setText(message.getFrom()[0].toString());
		
		TextView subjectView = (TextView) mailView.findViewById(R.id.mail_subject);
		subjectView.setText(message.getSubject());
		
		TextView contentView = (TextView) mailView.findViewById(R.id.mail_text);
		try {
			contentView.setText(message.getContent().toString().substring(0, 50));
		} catch (IOException e) {
			contentView.setVisibility(View.GONE);
			e.printStackTrace();
		}
		
		return mailView;
	}

}
