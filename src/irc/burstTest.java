package irc;

import jvn.JvnProxy;
import jvn.JvnServerImpl;

public class burstTest {

    public static void main(String argv[]) {
        SentenceItf jo = null;
        try {

            JvnServerImpl js = JvnServerImpl.jvnGetServer();
            // look up the IRC object in the JVN server
            // if not found, create it, and register it in the JVN server
            jo = (SentenceItf) JvnProxy.lookUpObject("IRC");

            if (jo == null) {
                jo = (SentenceItf) JvnProxy.newInstance(new Sentence(), "IRC");
            }

        } catch (Exception e) {
            System.out.println("IRC problem : " + e.getMessage());
        }
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            // TODO: handle exception
        }
        int number;
        String text;
        System.out.println("Je commence ma boucle");
        jo.write(Integer.toString(1));
        for (int i = 0; i < 100; i++) {
            text = jo.read();
            System.out.println("le texte est: " + text);
            number = Integer.parseInt(text);
            System.out.println("NUMBER LU = "+ number);
            jo.write(Integer.toString(i));
            System.out.println("NUMBER ECRIT= "+ number);
        }
        System.out.println("j'ai fini ma boucle");
    }
}