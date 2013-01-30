package com.greatnowhere.utils;


import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtils {

	public static void log(Level level, Object origin, String message) {
		log(level, origin, message, null, false);
	}
	
	public static void log(Level level, Object origin, String message, Throwable t) {
		log(level, origin, message, t, false);
	}
	
	public static void log(Level level, Object origin, String message, Throwable t, boolean useCause) {
		// Throwable _t = ( t == null ? new RuntimeException() : t );
		Logger.getLogger(origin.getClass().getCanonicalName()).
		log(level, origin.getClass().getCanonicalName() + ": " + Utils.coalesce(message + " ",""), (useCause && t != null ? t.getCause() : t));
	}
	
	public static void warn(Object origin, String msg, Throwable t) {
		log(Level.WARNING,origin,msg,t,false);
	}
	
	public static void error(Object origin, String msg, Throwable t) {
		log(Level.SEVERE,origin,msg,t,false);
	}
	
	public static void info(Object origin, String msg, Throwable t) {
		log(Level.INFO,origin,msg,t,false);
	}
}
