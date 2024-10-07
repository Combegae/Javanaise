package irc;

import jvn.JvnCoordImpl;
import jvn.JvnRemoteCoord;

public class mainCoordinateur {


    public static void main(String args[]) {
        JvnRemoteCoord coordinateur;

        {
            try {
                coordinateur = new JvnCoordImpl();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
