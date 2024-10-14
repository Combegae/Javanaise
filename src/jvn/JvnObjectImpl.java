package jvn;

import java.io.Serializable;
import java.rmi.Remote;


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
        System.out.println("Objet: mon verrou est: " + this.verrou );
        if (this.verrou == Verrou.RC || this.verrou == Verrou.RWC) {
            this.verrou = Verrou.R;
        }
        else if(this.verrou == Verrou.WC){
            this.verrou = Verrou.RWC;
        }
        else if (this.verrou == Verrou.NL) {
            Serializable serialisableCoord = JvnServerImpl.jvnGetServer().jvnLockRead(this.id);
            if(serialisableCoord != null){
                o = serialisableCoord;
            }
            this.verrou = Verrou.R;
        }
        System.out.println("Objet: mon verrou devient: " + this.verrou );
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        System.out.println("Objet: mon verrou est: " + this.verrou );
        Serializable serialisableCoord;
        if (this.verrou == Verrou.WC ) {
            this.verrou = Verrou.W;
        } else if (this.verrou == Verrou.NL || this.verrou == Verrou.R || this.verrou == Verrou.RC) {
            System.out.println("Serverlocal lockWrite: Calling Coord lockwrite");

            serialisableCoord = JvnServerImpl.jvnGetServer().jvnLockWrite(this.id);
            if(serialisableCoord != null){
                o = serialisableCoord;
            }
            this.verrou = Verrou.W;
        } else  if(this.verrou == Verrou.RWC  ){
            this.verrou = Verrou.W;
        }
        System.out.println("Objet: mon verrou devient: " + this.verrou );
        
    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {
        System.out.println("ENTREE JVNUNLOCK");
        System.out.println("Objet: mon verrou est: " + this.verrou );
        if (this.verrou == Verrou.R) {
            System.out.println("JvnUnlock - passage en RC");
            this.verrou = Verrou.RC;
        } else if (this.verrou == Verrou.W) {
            this.verrou = Verrou.WC;
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
            case W:
            case R:
                try {
                    wait();
                } catch (Exception e) {
                    System.out.println("Error :");
                }
                break;
            case RC:
                this.verrou = Verrou.NL;
                break;
            case RWC:
                try {
                    wait();
                } catch (Exception e) {
                    System.out.println("Error :");
                }
                break;
            default:
                System.err.println("Objet: Default invalidateReader: " + verrou );
                break;
        }
        this.verrou = Verrou.NL; 

        
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        System.out.println("Entré invlade writer");
        switch (this.verrou) {
            case W:
                try {
                    wait();
                } catch (Exception e) {
                    System.out.println("Error :");
                }
                break;
            case WC:
                break;

            case RWC:
                try {
                    wait();
                } catch (Exception e) {
                    System.out.println("Error :");
                }
                break;
            
            default:
                System.err.println("Objet: Default InvalidateWriter: " + verrou );
                break;
        }
        this.verrou = Verrou.NL; 


        return o;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        System.out.println("Entré invlade writer for reader");
        switch (this.verrou) {
            case W:
                while (this.verrou == Verrou.W) {
                    try {
                        wait();
                    } catch (Exception e) {
                        System.out.println("Error :");
                    }
                }
                System.out.println("passage en RC :");
                this.verrou = Verrou.RC;
                break;
            case WC:
            case RWC:
                System.out.println("passage en RC :");
                this.verrou = Verrou.RC;
            default:
                break;
        }
        return o;
    }


    
}
