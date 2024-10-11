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
        if (this.verrou == Verrou.RC || this.verrou == Verrou.RWC) {
            this.verrou = Verrou.R;
        }
        else if(this.verrou == Verrou.WC){
            this.verrou = Verrou.RWC;
        }
        else if (this.verrou == Verrou.NL) {
            o = JvnServerImpl.jvnGetServer().jvnLockRead(this.id);
            this.verrou = Verrou.R;
        }
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        if (this.verrou == Verrou.WC || this.verrou == Verrou.RWC) {
            this.verrou = Verrou.W;
        } else if (this.verrou == Verrou.NL || this.verrou == Verrou.R || this.verrou == Verrou.RC) {
            o = JvnServerImpl.jvnGetServer().jvnLockWrite(this.id);
            this.verrou = Verrou.W;
        }
        
    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {
        if (this.verrou == Verrou.R) {
            this.verrou = Verrou.RC;
        } else if (this.verrou == Verrou.W) {
            this.verrou = Verrou.WC;
        }
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
                System.err.println("Default invalidateReader: " + verrou );
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
            case RWC:
                this.verrou = Verrou.RC;
            default:
                break;
        }
        return o;
    }


    
}
