package com.greatnowhere.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.format.datetime.DateFormatter;

public class Utils {

	/**
	 * Returns first non-null argument
	 * @param args
	 * @return
	 */
	public static <T> T coalesce(T... args) {
		for (T arg : args) {
			if ( arg != null ) return arg;
		}
		return null;
	}
	
	/**
	 * Returns first non-empty & non-blank string
	 */
	public static String coalesceString(String... args) {
		for (String arg : args) {
			if ( stringHasValue(arg) ) return arg;
		}
		return null;
	}
	
	public static boolean stringHasValue(String s) {
		return ( s != null && s.length() > 0);
	}
	
	/**
	 * Returns a word from sentence. Words are separated by space. Word index is 0-based. Returns null if out of bounds or sentence is null
	 * @param sentence
	 * @param wordIndex, 0 based
	 * @return
	 */
	public static String getWord(String sentence, int wordIndex) {
		StringTokenizer _words = new StringTokenizer(sentence, " ", false);
		
		String _retval = null;
		if ( _words != null && _words.countTokens() > wordIndex ) {
			for ( int i=0; _words.countTokens()>0; i++) {
				_retval = _words.nextToken();
				if ( i == wordIndex ) {
					break;
				}
			}
		}
		return _retval;
	}
	
	public static String getWords(String sentence, int startWordIndex, int endWordIndex) {
		String _retval = "";
		for ( int i=startWordIndex; i<=endWordIndex; i++) {
			_retval += Utils.coalesce(getWord(sentence, i)," ");
		}
		return _retval.trim();
	}

	
	/**
	 * Replaces specified word in a sentence.
	 * @param sentence
	 * @param wordIndex
	 * @param newWord
	 * @return
	 */
	public static String setWord(String sentence, int wordIndex, String newWord) {
		String[] _words = StringUtils.split(sentence, " ");
		String _retval = sentence;
		newWord = coalesce(newWord,"");
		if ( _words != null ) {
			if ( (_words.length-1) < wordIndex ) {
				_retval = sentence + " " + newWord;
			} else {
				_words[wordIndex] = newWord;
				_retval = StringUtils.join(_words, " ");
			}
		}
		return _retval;
	}
	
	/**
	 * Gets first name of a fullname. Full name can contain 2, 3, 4 or more names
	 * @param fullName
	 * @return
	 */
	public static String getFirstName(String fullName) {
		fullName = normalizeWhitespace(fullName);
		if ( getWordCount(fullName) > 3 ) {
			return getWords(fullName, 0, 1);
		} else {
			return getWord(fullName, 0);
		}
	}
	
	public static String getLastName1(String fullName) {
		fullName = normalizeWhitespace(fullName);
		if ( getWordCount(fullName) > 3 ) {
			return getWord(fullName, 2);
		} else {
			return getWord(fullName, 1);
		}
	}
	
	public static String getLastName2(String fullName) {
		fullName = normalizeWhitespace(fullName);
		if ( getWordCount(fullName) > 3 ) {
			return getWords(fullName, 3, 999);
		} else {
			return getWord(fullName, 2);
		}
	}
	
	public static String setFirstName(String fullName,String firstName) {
		fullName = coalesce(fullName,"");
		if ( getWordCount(fullName) > 3 ) {
			fullName = setWord(fullName,1,"");
		}
		fullName = normalizeWhitespace(fullName);
		return setWord(fullName, 0, firstName);
	}
	
	public static String setLastName1(String fullName,String lastName) {
		fullName = coalesce(fullName,"");
		if ( getWordCount(fullName) > 3 ) {
			fullName = setWord(fullName,2,lastName);
		} else {
			fullName = setWord(fullName,1,lastName);
		}
		fullName = normalizeWhitespace(fullName);
		return fullName;
	}
	
	public static String setLastName2(String fullName,String lastName2) {
		fullName = coalesce(fullName,"");
		if ( getWordCount(fullName) > 3 ) {
			// blank out any last names past last name 2 (index = 3)
			for ( int i=4; i<=getWordCount(fullName); i++) {
				fullName = setWord(fullName,i,"");
			}
			fullName = normalizeWhitespace(fullName);
			fullName = setWord(fullName,3,lastName2);
		} else {
			fullName = setWord(fullName,2,lastName2);
		}
		fullName = normalizeWhitespace(fullName);
		return fullName;
	}
	
	public static String normalizeWhitespace(String s) {

		if ( s == null ) return null;
		// eliminate double whitespace
		while ( s.indexOf("  ") != -1 ) {
			s = s.replace("  ", " ");
		}
		return s.trim();
		
	}
	
	public static int getWordCount(String s) {

		if ( s == null ) return 0;
		s = normalizeWhitespace(s);
		
		int wordCount = 0;
		for ( int i=0; i<s.length(); i++ ) {
			wordCount++;
			i = s.indexOf(" ", i);
			if ( i == -1 ) break;
		}
		
		return wordCount;
		
	}
	
	/**
	 * Truncates collection to specified size
	 * @param c
	 * @param maxSize
	 * @return
	 */
	public static <T> Collection<T> truncateCollection(Collection<T> c,Integer maxSize) {
		Collection<T> _retval = new ArrayList<T>(c);
		
		if ( _retval != null && maxSize != null && _retval.size() > maxSize ) {
			for ( Iterator<T> _it = _retval.iterator(); _it.hasNext(); ) {
				_it.next();
				if ( _retval.size() <= maxSize ) break;
				_it.remove();
			}
		}
		return _retval;
	}
	
	/**
	 * Converts BigDec to Long. No fits on null values
	 * @param b
	 * @return
	 */
	public static Long getLongFromBigDecimal(BigDecimal b,Long valueIfNull) {
		Long _retval = new Long(valueIfNull);
		if ( b != null ) { _retval = b.longValue(); }
		return _retval;
	}
	
	/**
	 * Concatenates given strings. Does not throw error on NULL strings
	 * @param args
	 * @return
	 */
	public static String concatenate(String... args) {
		StringBuilder _sb = new StringBuilder();
		for (String arg : args) {
			if ( arg != null ) _sb.append(arg);
		}
		return _sb.toString();
	}

	/**
	 * Compares two objects. Will properly handle NULL objects
	 * 
	 * @param First object
	 * @param Second object
	 * @return Negative integer if o1<o2 (or o1 == null and o2 != null), 0 if o1
	 * equals o2 (or both are NULLs), positive integer if o2>o1 (or o1 == NULL
	 * and o2 != NULL)
	 */
	@SuppressWarnings("unused")
	public static int compareObjects(Object o1, Object o2) {
		int _diff;

		if (o1 == null) {
			if (o2 != null) {
				_diff = -1;
			} else {
				_diff = 0;
			}

		} else if (o2 == null) {
			if (o1 != null) {
				_diff = 1;
			} else {
				_diff = 0;
			}

		} else {
			_diff = o1.toString().compareTo(o2.toString());
		}

		return _diff;
	}

	/**
	 * Tries to pretty-print passed string as XML
	 * @param input
	 * @return Pretty formatted input, or same string if not XML
	 */
	public static String prettyFormatXML(String input) {
	    try {
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", 2);
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.transform(xmlInput, xmlOutput);
	        return xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	        return input;
	    }
	}

	public static String toDDMMYYY(Date d) {
		if ( d == null ) return null;
		
		DateFormatter _df = new DateFormatter("ddMMyyyy");
		return _df.print(d, Locale.US);
	}

	public static String toDateTime(Date d) {
		if ( d == null ) return null;
		
		DateFormatter _df = new DateFormatter("ddMMyyyy hh:mm");
		return _df.print(d, Locale.US);
	}
	
	public static boolean hasValue(Object obj) {
		if ( obj != null && obj.toString().compareToIgnoreCase("") > 0 ) return true;
		return false;
	}
	
	public static byte[] serializeObject(Serializable obj) {
		try
		{
			ByteArrayOutputStream _bout = new ByteArrayOutputStream();
			ObjectOutputStream _out = new ObjectOutputStream(_bout);
			_out.writeObject(obj);
			_out.close();
			return _bout.toByteArray();
		}
		catch(IOException ex) {
			throw new RuntimeException(ex);
		}		
	}
	
	public static Object deSerializeObject(byte[] s) {
		try
		{
			ByteArrayInputStream _bin = new ByteArrayInputStream(s);
			ObjectInputStream _in = new ObjectInputStream(_bin);
			return _in.readObject();
		}
		catch(IOException ex) {
			throw new RuntimeException(ex);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}		
	}
	
	public static int getRandom(int min,int max) {
		Random r = new Random();
		return min + r.nextInt(max-min);
	}
	
	public static int max(Integer... values) {
		Integer _retval = null;
		for ( Integer i : values ) {
			if ( i != null ) {
				if ( _retval == null ) _retval = new Integer(i);
				_retval = ( _retval.compareTo(i) < 0 ? new Integer(i) : _retval);
			}
		}
		return _retval;
	}
	
	public static int min(Integer... values) {
		Integer _retval = null;
		for ( Integer i : values ) {
			if ( i != null ) {
				if ( _retval == null ) _retval = new Integer(i);
				_retval = ( _retval.compareTo(i) > 0 ? new Integer(i) : _retval);
			}
		}
		return _retval;
	}
	
}
