package jvn;

import java.io.Serializable;
import java.util.concurrent.Semaphore;


public class JvnObjectImpl implements JvnObject {

    public enum Verrou {
        NL, RC, WC, R, W, RWC
    }

    private int id;
    private Verrou verrou;
    public Serializable o;

    
    private JvnServerImpl serverLocal;

    public JvnObjectImpl(int id, Serializable o) {
        this.serverLocal = JvnServerImpl.jvnGetServer();
        this.verrou = Verrou.NL;
        this.id = id;
        this.o = o;
    }


    @Override
    public void jvnLockRead() throws JvnException {
        if (this.verrou == Verrou.RC || this.verrou == Verrou.RWC) {
            this.verrou = Verrou.R;
        }
        else if(this.verrou == Verrou.WC){
            this.verrou = Verrou.RWC;
        }
        else if (this.verrou == Verrou.NL) {
            this.serverLocal.jvnLockRead(this.id);
            this.verrou = Verrou.R;
        }
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        if (this.verrou == Verrou.WC || this.verrou == Verrou.RWC) {
            this.verrou = Verrou.W;
        } else if (this.verrou == Verrou.NL || this.verrou == Verrou.R || this.verrou == Verrou.RC) {
            this.serverLocal.jvnLockWrite(this.id);
            this.verrou = Verrou.W;
        }
        
    }

    @Override
    public void jvnUnLock() throws JvnException {
        if (this.verrou == Verrou.R) {
            this.verrou = Verrou.RC;
        } else if (this.verrou == Verrou.W) {
            this.verrou = Verrou.WC;
        }
        notifyAll();
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
        switch (this.verrou) {
            case R:
                try {
                    wait();
                } catch (Exception e) {
                    System.out.println("Error :");
                }
                break;
            case RC:
                break;
            case RWC:
                try {
                    wait();
                } catch (Exception e) {
                    System.out.println("Error :");
                }
                break;
            default:
                System.err.println("Default invalidateReader");
                break;
        }
        this.verrou = Verrou.NL; 

        
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
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
                System.err.println("Default InvalidateWriter");
                break;
        }
        this.verrou = Verrou.NL; 


        return null;
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
            case RWC:
                this.verrou = Verrou.RC;
            default:
                break;
        }
        return null;
    }


    
}
