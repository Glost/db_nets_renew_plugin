package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.NetInstance;

import de.renew.unify.Aggregate;

import de.renew.util.TextToken;

import java.io.IOException;
import java.io.ObjectOutput;

import java.lang.reflect.Field;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This class implements the <code>ObjectAccessor</code>
 * interface and nothing more.
 *
 * @author Thomas Jacob
 * @since Renew 1.6
 **/
public class ObjectAccessorImpl extends UnicastRemoteObject
        implements ObjectAccessor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ObjectAccessorImpl.class);


    /**
     * The object for the accessor.
     **/
    protected final Object object;

    /**
     * The simulation environment this object is situated in.
     **/
    protected final SimulationEnvironment environment;

    /**
     * Creates a new <code>ObjectAccessor</code> for the given object.
     * <p>
     * This constructor is protected to ensure that no
     * <code>ObjectAccessor</code> instances are created for
     * <code>null</code> references.
     * Use {@link #createObjectAccessor} instead.
     * </p>
     *
     * @param object       the object for the accessor.
     *
     * @param environment  the simulation environment where this
     *                     object is situated in.
     *
     * @exception RemoteException
     *   if a RMI failure occured.
     **/
    protected ObjectAccessorImpl(Object object,
                                 SimulationEnvironment environment)
            throws RemoteException {
        super(0, SocketFactoryDeterminer.getInstance(),
              SocketFactoryDeterminer.getInstance());
        assert object != null : "ObjectAccessor instance for null reference is not allowed.";
        this.object = object;
        this.environment = environment;
    }

    /**
     * Creates a new <code>ObjectAccessor</code> for the given object.
     *
     * @param object       the object for the accessor.
     *
     * @param environment  the simulation environment where this
     *                     object is situated in.
     *
     * @return the new object accessor instance.
     *         Returns <code>null</code>, if <code>object == null</code>.
     *
     * @exception RemoteException
     *   if a RMI failure occured.
     **/
    public static ObjectAccessor createObjectAccessor(Object object,
                                                      SimulationEnvironment environment)
            throws RemoteException {
        if (object == null) {
            return null;
        } else {
            return new ObjectAccessorImpl(object, environment);
        }
    }

    /**
     * Returns the object wrapped by this object accessor.
     * This method is available only in the local implementation,
     * not in the remote interface.
     *
     * @return  the wrapped object.
     **/
    public Object getObject() {
        return object;
    }

    /**
     * @see ObjectAccessor#asNetInstance()
     **/
    public NetInstanceAccessor asNetInstance()
            throws RemoteException, ClassCastException {
        return new NetInstanceAccessorImpl((NetInstance) object, environment);
    }

    /**
     * @see ObjectAccessor#asAggregate()
     **/
    public AggregateAccessor asAggregate()
            throws RemoteException, ClassCastException {
        return new AggregateAccessorImpl((Aggregate) object, environment);
    }

    /**
     * @see ObjectAccessor#asTextToken()
     **/
    public TextTokenAccessor asTextToken()
            throws RemoteException, ClassCastException {
        return new TextTokenAccessorImpl((TextToken) object, environment);
    }

    /**
     * @see ObjectAccessor#asString()
     **/
    public String asString() throws RemoteException {
        return object.toString();
    }

    /**
     * @see ObjectAccessor#getFieldCount()
     **/
    public int getFieldCount() throws RemoteException {
        return object.getClass().getFields().length;
    }

    /**
     * @see ObjectAccessor#getFieldNames()
     **/
    public String[] getFieldNames() throws RemoteException {
        Field[] fields = object.getClass().getFields();
        String[] names = new String[fields.length];
        for (int fieldNr = 0; fieldNr < fields.length; fieldNr++) {
            names[fieldNr] = fields[fieldNr].getName();
        }
        return names;
    }

    /**
     * @see ObjectAccessor#getFieldValues()
     **/
    public String[] getFieldValues()
            throws RemoteException, IllegalAccessException {
        Future<String[]> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<String[]>() {
                public String[] call() throws Exception {
                    Field[] fields = object.getClass().getFields();
                    String[] values = new String[fields.length];
                    for (int fieldNr = 0; fieldNr < fields.length; fieldNr++) {
                        values[fieldNr] = fields[fieldNr].get(object).toString();
                    }
                    return values;
                }
            });
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            logger.error("Simulation thread threw an exception", e);
        }

        // We should never return nothing but some error occured befor.
        return null;
    }

    /**
     * @see ObjectAccessor#getField(int)
     **/
    public ObjectAccessor getField(int i)
            throws RemoteException, IllegalAccessException {
        return createObjectAccessor(object.getClass().getFields()[i].get(object),
                                    environment);
    }

    /**
     * @see ObjectAccessor#isInstanceOf(Class)
     **/
    public boolean isInstanceOf(Class<?> testClass) throws RemoteException {
        return testClass.isInstance(object);
    }

    /**
     * Serializes the object contained in this accessor
     * to a given object output stream.
     *
     * @param out    The stream to write to.
     *
     * @exception IOException
     *   if an I/O problem occurs.
     **/
    public void writeTo(ObjectOutput out) throws IOException {
        out.writeObject(object);
    }

    /**
     * Returns the simulation environment where the object
     * wrapped by this accessor is situated in.
     * This method is available only in the local implementation,
     * not in the remote interface.
     *
     * @return  the simulation environment the object is situated
     *          in.
     **/
    protected SimulationEnvironment getEnvironment() {
        return environment;
    }
}