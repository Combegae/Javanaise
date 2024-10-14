package irc;

import jvn.ProxyAnnotation;

public interface SentenceItf {
    

	@ProxyAnnotation(name="read")
	public void write(String text);

    @ProxyAnnotation(name="write")
    public String read();

}
