package de.cisoft.utility;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Patterns;

public class Mail {
	public static List <String> getMailAddresses(Context context) {
		Pattern emailPattern = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(context).getAccounts();
		LinkedList <String> mails = new LinkedList <String>(); 
		for (Account account : accounts ) {
			if (emailPattern.matcher(account.name).matches()) {
				mails.add(account.name);
			} 
		}
		if (mails.size() == 0) {
			accounts = AccountManager.get(context).getAccountsByType("com.google");
			for (Account account : accounts ) {
				mails.add(account.name);
			}
		}
		if (mails.size() == 0) {
			return null;
		}
		return mails;
	}
}
