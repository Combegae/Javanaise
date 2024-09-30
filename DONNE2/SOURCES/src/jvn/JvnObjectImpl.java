package jvn;

import java.io.Serializable;


public class JvnObjectImpl implements JvnObject {

    public enum Verrou {
        NL, RC, WC, R, W, RWC
    }

    int id;
    Verrou verrou;
    Serializable o;

    
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
        if (this.verrou == Verrou.RC || this.verrou == Verrou.WC || this.verrou == Verrou.RWC) {
            this.verrou = Verrou.W; // ATTENTION RC plusieurs personne peut l'avoir
        } else if (this.verrou == Verrou.NL || this.verrou == Verrou.R) {
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
        notify();
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
    public void jvnInvalidateReader() throws JvnException {
        if (this.verrou == Verrou.R || this.verrou == Verrou.W) {
            try {
                wait();
            } catch (Exception e) {
                System.err.println("Problème invalidate reader: " + e);
            }
            
        }

        
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        return null;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        return null;
    }


    
}
