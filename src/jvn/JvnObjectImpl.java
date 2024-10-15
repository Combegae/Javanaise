package jvn;

import java.io.Serializable;
import java.rmi.Remote;

import irc.SentenceItf;


public class JvnObjectImpl implements Remote, JvnObject {

    public enum Verrou {
        NL, RC, WC, R, W, RWC
    }

    private int id;
    private Verrou verrou;
    public Serializable o;

    

    public JvnObjectImpl(int id, Serializable o) {
        this.verrou = Verrou.NL;
        this.id = id;
        this.o = o;
    }


    @Override
    public void jvnLockRead() throws JvnException {

        try {
            switch (this.verrou) {
                case RC:
                    this.verrou = Verrou.R;
                    break;
            
                case WC:
                    this.verrou = Verrou.RWC;
                    break;
                case NL:
                    o = JvnServerImpl.jvnGetServer().jvnLockRead(this.id);
                    System.out.println("o est Modif");
                    if (o == null) {
                        System.out.println("o  NULLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");  
                    }
                    this.verrou = Verrou.R;
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new JvnException();
        }
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        try {
            Serializable serialisableCoord;
            switch (this.verrou) {
                case WC:
                    this.verrou = Verrou.W;
                    break;
                case NL:
                case RC:
                    o = JvnServerImpl.jvnGetServer().jvnLockWrite(this.id);
                    System.out.println("o est Modif writeeeee");
                    if (o == null) {
                        System.out.println("o  NULLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");  
                    }
                    this.verrou = Verrou.W;
                    break;
                default:
                    break;
            }
            System.out.println("Objet: mon verrou devient: " + this.verrou );
        } catch (Exception e) {
            throw new JvnException();
        }
        
    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {

        System.out.println("Objet: mon verrou est: " + this.verrou);
        
        switch (this.verrou) {
            case R:
                this.verrou = Verrou.RC;
                break;
            case W:
                this.verrou = Verrou.WC;
                break;
            case RWC:
                this.verrou = Verrou.WC;
            default:
                break;
        }
        System.out.println("Objet: mon verrou devient: " + this.verrou );
        try {
            this.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return this.id;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException { //GetObjectState
        return o; 
    }

    @Override
    public synchronized void jvnInvalidateReader() throws JvnException {
        System.out.println("ENTREE JVNINVALIDATEREADER \n");
        switch (this.verrou) {
            case RWC:
            case R:
                while (this.verrou == Verrou.R || this.verrou == Verrou.RWC) {
                    try {
                        wait();
                    } catch (Exception e) {
                        System.out.println("Error :");
                    }
                }
                break;
            default:
                System.err.println("Objet: Default invalidateReader: " + verrou );
                break;
        }
        this.verrou = Verrou.NL;
        System.err.println("Mon verrou devient: " + verrou );

        
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        System.out.println("Entré invlade writer");
        switch (this.verrou) {
            case RWC:
            case W:
                while (this.verrou == Verrou.W || this.verrou == Verrou.RWC) {
                    try {
                        wait();
                    } catch (Exception e) {
                        System.out.println("Error :");
                    }
                }
                break;
            default:
                break;
        }
        this.verrou = Verrou.NL;
        System.err.println("Mon verrou devient: " + verrou );


        return o;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        switch (this.verrou) {
            case W:
                while (this.verrou == Verrou.W) {
                    try {
                        wait();
                    } catch (Exception e) {
                        System.out.println("Error :");
                    }
                }
                this.verrou = Verrou.RC;
                break;
            case WC:
                this.verrou = Verrou.NL;
            case RWC:
                this.verrou = Verrou.R;
            default:
                break;
        }
        return o;
    }


    
}
