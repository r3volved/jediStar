package fr.jedistar.formats;

import java.awt.List;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.Reaction;

public class PendingMultiAction {

	//The user that's expected to do the action
	private User user;
	
	//The method that should be called
	private String methodName;
	
	//The object on which this method should be called
	private Object object;
	
	//The arguments for the method call
	private Object[] args;
	
	//The message on which the action is expected to be made
	private Message message;
	
	//The time at which this action expires
	private Calendar expiration;

	/**
	 * 
	 * @param user : The Discord user that may perform the action
	 * @param method : A method that should return a String and take a Reaction as first argument
	 * @param object : The object on which <i>method</i> will be called
	 * @param args : The arguments for the method call
	 * @param message : The message that triggered this action
	 * @param expiration : the time in minutes in which this action will expire
	 */
	public PendingMultiAction(User user, String methodName, Object object, Message message, Integer expiration,Object... args) {

		this.user = user;
		this.methodName = methodName;
		this.object = object;
		this.args = args;
		this.message = message;

		Calendar cal = Calendar.getInstance(TimeZone.getDefault());

		cal.add(Calendar.HOUR, expiration);

		this.expiration = cal;
	}
	
	public boolean isExpired() {
		return expiration.compareTo(Calendar.getInstance(TimeZone.getDefault())) < 0;
	}
	
	
	public void doAction(User user, Reaction reaction) {
		try {
			ArrayList<Class<?>> paramsTypes = new ArrayList<Class<?>>();
			
			Object[] params = new Object[args.length+2];
			
			params[0] = user;
			params[1] = reaction;
			
			for(int i=2;i<args.length+2;i++) {
				params[i] = args[i-2];
			}
			
			Class<?>[] classesArray = new Class<?>[params.length]; 
			
			
			for(int i=0;i<params.length;i++) {
				classesArray[i] = params[i].getClass();
			}
			
			Method method = object.getClass().getMethod(methodName, classesArray);
			
			if(method == null) {
				message.reply("A problem happened while executing this action");
			}
			
			message.reply((String) method.invoke(object, params));
		}
		catch(Exception e) {
			e.printStackTrace();
			message.reply("A problem happened while executing this action");
		}
	}

	public User getUser() {
		return user;
	}

	public Message getMessage() {
		return message;
	}
	
}
