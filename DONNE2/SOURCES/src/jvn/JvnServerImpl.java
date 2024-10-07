/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;



public class JvnServerImpl extends UnicastRemoteObject 
				implements JvnLocalServer, JvnRemoteServer{ 
	
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;


	private Hashtable<Integer, JvnObject> objectList;
	private JvnRemoteCoord coordinateur;



  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		Registry registry = LocateRegistry.getRegistry(6090);
        try {
            this.coordinateur = (JvnRemoteCoord) registry.lookup("RemoteCoord");
		} catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
		// to be completed
		// Export le serveur en RMI
	}
	
  /**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return js;
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public  void jvnTerminate() throws jvn.JvnException {
    // to be completed 
	} 
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException {
		try {
			int id = coordinateur.jvnGetObjectId();
			JvnObject object = new JvnObjectImpl(id, o);
			objectList.put(id, object);
			return object;
		} catch (Exception e) {
			throw new jvn.JvnException();
		}

	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public  void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
		try {
			this.coordinateur.jvnRegisterObject(jon, jo, js);
		} catch (Exception e) {
			throw new jvn.JvnException();
		}
	}
	
	/**
	* Provide the reference of a JVN object beeing given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public  JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
		try {
			return coordinateur.jvnLookupObject(jon, js);

		} catch (Exception e) {
			throw new jvn.JvnException();
		}
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
	public Serializable jvnLockRead(int joi) throws JvnException {
		Serializable o = null;
		try {
			o = coordinateur.jvnLockRead(joi, js);
		} catch (JvnException e) {
			throw new JvnException();
		} catch (java.rmi.RemoteException e) {
			System.out.println("Problème jvnLockRead - RemoteException");
		}
		return o;
	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
	public Serializable jvnLockWrite(int joi) throws JvnException {
		Serializable o = null;
		try {
			o = coordinateur.jvnLockWrite(joi, js);
		} catch (JvnException e) {
			throw new JvnException();
		} catch (java.rmi.RemoteException e) {
			System.out.println("Problème jvnLockRead - RemoteException");
		}
		return o;
	}	

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
  public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException,jvn.JvnException {
		(objectList.get(joi)).jvnInvalidateReader();
	};
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		(objectList.get(joi)).jvnLockWrite();
		return null;
	};
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		(objectList.get(joi)).jvnInvalidateWriterForReader();
		return null;
	 };

}

 
