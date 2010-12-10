/*
 * author nina mulkijanyan
 */

package org.nebula.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class ConversationThread {
	
	private String id ;
	private List<Conversation> myConversations = new ArrayList<Conversation>();
	private int seqNo = 0;
	private String myUsername ;
	
	private static String dateFormatString = "yyyyMMddhhmmss";
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatString) ;
	private static Calendar calendar = Calendar.getInstance() ;

	
	public ConversationThread(String id, String myUsername ) {
		this.id = id ;
		this.myUsername = myUsername ;
	}
	
	public String getId() {
		return id ;
	}
	
	public List<Conversation> getConversations() {
		return myConversations ; 
	}
	
	public Conversation addConversation(String conversationId, String rcl) {
		Conversation newConversation = new Conversation(conversationId, rcl) ;
		myConversations.add(newConversation) ;
		return newConversation;
	}
	
	public Conversation addConversation(String rcl) {
		
		Conversation newConversation = new Conversation(createNewConversationId(), rcl) ;
		myConversations.add(newConversation) ;
		return newConversation;
	}
	
	public String createNewConversationId() {
		
		seqNo++;
		return myUsername + seqNo ;
	}

	public static String createNewThreadId(String username) {
		return  username + simpleDateFormat.format(calendar.getTime()) ;
	}

	
	public Conversation getConversation(String id) throws Exception {

		Conversation curConversation ;
		Iterator<Conversation> iter = myConversations.iterator() ;
		
		while (iter.hasNext()) {
			curConversation = iter.next() ;
			if (curConversation.getId().equals(id))
					return curConversation ;
		}
		return null ;
	}
	
	public Conversation getLatestConversation() throws Exception{
		
		if (myConversations.size() == 0)
			throw new Exception("Conversation thread is empty") ;
		
		Conversation maxConversation;
		Conversation curConversation ;
		
		Iterator<Conversation> iter = myConversations.iterator() ;
		
		maxConversation = myConversations.get(0) ;
		
		while (iter.hasNext()) {
			curConversation = iter.next() ;
			if (curConversation.getDate().compareTo(maxConversation.getDate()) > 0)
					maxConversation = curConversation ;
		}
		
		return maxConversation;
	}
	
	public int getSeqNo() {
		return seqNo ;
	}
	
	public String updateOldConversation (String oldId) throws Exception {
		String newId = createNewConversationId() ;
		getConversation(oldId).setId(newId) ;
		return newId ;
	}
	
	public void updateOldConversation (String oldId, String newId) throws Exception {

		if (getConversation(oldId) != null)
			getConversation(oldId).setId(newId) ;
	}

}
