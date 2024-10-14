package irc;

import jvn.ProxyAnnotation;

public interface SentenceItf {
    

	@ProxyAnnotation(name="write")
	public void write(String text);

    @ProxyAnnotation(name="read")
    public String read();

}
