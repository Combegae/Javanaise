/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;


public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{
	

  /**
	 * 
	 */
  private static final long serialVersionUID = 7411182992619482422L;
  private int lastIdUsed = 0;

  private Hashtable<String, JvnObject> nameHash;
  private Hashtable<Integer, String> idHash;
  private Hashtable<Integer, Set<JvnRemoteServer>> lockReadList;
  private Hashtable<Integer, JvnRemoteServer> lockWriteList;
  private Hashtable<Integer, Serializable> localMemory;


/**
  * Default constructor
  * @throws JvnException
  **/
	public JvnCoordImpl() throws Exception {
      Registry registry = LocateRegistry.getRegistry(6090);
      registry.rebind("Coordinateur", this);

      nameHash = new Hashtable<String, JvnObject>();
      idHash = new Hashtable<Integer, String>();
      lockReadList = new Hashtable<Integer, Set<JvnRemoteServer>>();
      lockWriteList = new Hashtable<Integer, JvnRemoteServer>();
      localMemory = new Hashtable<Integer, Serializable>();
	}

  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId() throws java.rmi.RemoteException,jvn.JvnException {
    lastIdUsed += 1;
    return lastIdUsed;
  }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
      throws java.rmi.RemoteException, jvn.JvnException {
    System.out.println("test");
    int jId = jo.jvnGetObjectId();
    idHash.put(jId, jon);
    nameHash.put(jon, jo);
    localMemory.put(jId, jo.jvnGetSharedObject());
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, jvn.JvnException {
    try {
      JvnObject object = nameHash.get(jon);
      return object;
    } catch (Exception e) {
      throw new JvnException();
    }

  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
  public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
    System.out.println("Coord - jvLockRead \n");
    String objectName = idHash.get(joi);
    Serializable objSerializable;
    JvnObject objectLocal = nameHash.get(objectName);
    if (objectLocal == null) {
        throw new JvnException();
    }
    synchronized(objectLocal) {
      JvnRemoteServer serverDistant = lockWriteList.get(joi);
      if (serverDistant != null) {
        try {
          objSerializable = serverDistant.jvnInvalidateWriterForReader(joi);
          System.out.println(" Coordinateur: Salut je recup bien objet dans lockRead ");
        } catch (Exception e) {
          throw e;
        }
        lockWriteList.remove(joi);

        Set<JvnRemoteServer> LockReadServerList = lockReadList.get(joi);
        if (LockReadServerList == null) {
          LockReadServerList = new HashSet<JvnRemoteServer>();
        }
        LockReadServerList.add(js);
        lockReadList.put(joi, LockReadServerList);

        localMemory.put(joi, objSerializable);
      }
      else {
        objSerializable = localMemory.get(joi);
      }
      

      Set<JvnRemoteServer> LockReadServerList = lockReadList.get(joi);
      if (LockReadServerList == null) {
        LockReadServerList = new HashSet<JvnRemoteServer>();
      }
      LockReadServerList.add(js);
      lockReadList.put(joi, LockReadServerList);

    }
    System.out.println("Coordinateur: LockRead serialisable: " + objSerializable + "serveur :" + js);
    return objSerializable;
   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
  public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
    String objectName = idHash.get(joi);
    Serializable objSerializable;
    JvnObject objectLocal = nameHash.get(objectName);
    if (objectLocal == null) {
      throw new JvnException();
    }
    synchronized (objectLocal) {
      try {
        JvnRemoteServer serverDistant = lockWriteList.get(joi);
        if (serverDistant != null) {
          objSerializable = serverDistant.jvnInvalidateWriter(joi);
          lockWriteList.put(joi, js);
          localMemory.put(joi, objSerializable);

        } else {
          Set<JvnRemoteServer> LockReadServerList = lockReadList.get(joi);
          if (LockReadServerList == null) {
            LockReadServerList = new HashSet<JvnRemoteServer>();
          }
          for (JvnRemoteServer server : LockReadServerList) {
            server.jvnInvalidateReader(joi);
          }
          
          lockWriteList.put(joi, js);
          objSerializable = localMemory.get(joi);
        }
      } catch (Exception e) {
        throw e;
      }
      
    }
    System.out.println("Coordinateur: LockWrite serialisable: " + objSerializable + "serveur :" + js);
    return objSerializable;
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
      Set<Integer> enumWriteKey = lockWriteList.keySet(); 
      for (int key : enumWriteKey) {
        JvnRemoteServer server = lockWriteList.get(key);
        if (server == js) {
          lockWriteList.remove(key);
        }
      }

      Set<Integer> enumReadKey = lockReadList.keySet(); 
      for (int keyRead : enumReadKey) {
        Set<JvnRemoteServer> serverlist = lockReadList.get(keyRead);
        for (JvnRemoteServer serverRead : serverlist) {
          if (serverRead == js) {
            serverlist.remove(js);
            lockReadList.put(keyRead, serverlist);
          }
        }
      }

      
    }
}

 
