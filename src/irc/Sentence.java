/***
 * Sentence class : used for keeping the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

import jvn.ProxyAnnotation;

public class Sentence implements java.io.Serializable, SentenceItf {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String data;
	
  
	public Sentence() {
		System.out.println("SENTENCE: CREE");
		data = new String("");
	}

	public void write(String text) {
		System.out.println("SENTENCE: write :" + text);
		data = text;
	}

	public String read() {
		System.out.println("SENTENCE: Read :" + data);
		return data;	
	}
	
}