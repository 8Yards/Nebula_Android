package android.demo;


public class UserData {
	   private static UserData instance = null;
	   
	   private String userName = null ;
	   private String userPassword = null ;

	   protected UserData() {
	      // Exists only to defeat instantiation.
	   }

	   public static UserData getInstance() {
	      if(instance == null) {
	         instance = new UserData();
	      }
	      return instance;
	   }

	   public void setUserName(String name)   { userName = name ; }
	   public void setUserPassword(String pass)   { userPassword = pass ; }

	   public String getUserName() { return userName ; }
	   public String getUserPassword() { return userPassword ; }

	}