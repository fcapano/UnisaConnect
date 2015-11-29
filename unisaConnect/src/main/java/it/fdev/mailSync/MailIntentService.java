package it.fdev.mailSync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.sun.mail.imap.IMAPFolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import it.fdev.scraper.esse3.Esse3BasicScraper.LoadStates;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.EmailMessage;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;

public class MailIntentService extends IntentService {

	public final static String BROADCAST_STATE_MAIL_LIST = "it.fdev.mail.list";

	public static boolean isRunning = false;
    public static List<EmailMessage> messageList = null;

	private Context mContext;
	private SharedPrefDataManager mDataManager;

	public MailIntentService() {
		super("it.fdev.mail.service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		Context context = getApplicationContext();
		mDataManager = SharedPrefDataManager.getInstance(context);
		if (!mDataManager.loginDataExists()) { // Non sono memorizzati i dati utente
			broadcastStatus(mContext, MainActivity.BROADCAST_ERROR, LoadStates.NO_DATA, null);
			return;
		}
		
		isRunning = true;
		mContext = getApplicationContext();

		Utils.sendLoadingMessage(mContext, R.string.cerco_libro);

        messageList = getMailList();

		Utils.broadcastStatus(mContext, BROADCAST_STATE_MAIL_LIST, "list", null);
		isRunning = false;
		stopForeground(true);
        stopSelf();
		return;
	}

	private List<EmailMessage> getMailList() {
        List<EmailMessage> messages = new ArrayList<>();
		IMAPFolder folder = null;
		Store store = null;

		try {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imaps");
			Session session = Session.getDefaultInstance(props, null);
			store = session.getStore("imaps");
			store.connect(MailChecker.IMAP_SERVER, mDataManager.getUser() + "@studenti.unisa.it", mDataManager.getPass());

			folder = (IMAPFolder) store.getFolder("inbox");
			if (!folder.isOpen()) {
				folder.open(Folder.READ_ONLY);
			}

			int numMsg = folder.getMessageCount();
			if (numMsg < 1) {
				return null;
			}

			int oldestMessageToGet = Math.max(0, numMsg - 20);

			Message[] messageList = folder.getMessages(oldestMessageToGet, numMsg);
            for (Message m : messageList) {
                try {
                    String a = m.getFrom()[0].toString();
                    String b = m.getSubject();
//                    Enumeration d = m.getAllHeaders();
//                    while (d.hasMoreElements()) {
//                        Header header = (Header) d.nextElement();
//                        System.out.printf("%s: %s%n", header.getName(), header.getValue());
//                    }
                    Multipart c = (Multipart) m.getContent();

                    messages.add(new EmailMessage(m.getFrom()[0].toString(), m.getSubject(), m.getContent().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
			
			return messages;

		} catch (MessagingException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (folder != null && folder.isOpen()) {
					folder.close(true);
				}
				if (store != null) {
					store.close();
				}
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void broadcastStatus(Context ctx, String action, LoadStates state, String message) {
		Intent localIntent = new Intent(action);
		localIntent.putExtra("status", state);
		if (message != null) {
			localIntent.putExtra("message", message);
		}
		ctx.sendBroadcast(localIntent);
	}

}
