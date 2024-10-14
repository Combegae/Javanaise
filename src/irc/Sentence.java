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
		data = new String("");
	}
	public void write(String text) {
		data = text;
	}
	public String read() {
		return data;	
	}
	
}