/**
 * This package defines the basic building block for defining nets
 * and net instances: places and transitions. It does not yet deal
 * with arcs and transition instances.
 *
 * <p>
 * Most classes in this package assume that their methods are called
 * within simulation threads only.  Assertions reflecting this
 * assumption have been added to all public accessible operations
 * (state-manipulating methods) and (de-)serialization methods of
 * these classes.  However, there are some exceptions, where
 * execution within a simulation thread is not guaranteed:
 * <ul>
 *   <li>{@link de.renew.net.IDRegistry},
 *       {@link de.renew.net.IDCounter},
 *       {@link de.renew.net.IDFactory},
 *       {@link de.renew.net.TrivialIDFactory}, and
 *       {@link de.renew.net.IDSource}
 *       may be used within the garbage collector's thread since
 *       they count references to objects.  The same holds for the
 *       methods {@link de.renew.net.PlaceInstance#reserve(Object)} and
 *       {@link de.renew.net.PlaceInstance#reserve(Object) .unreserve(Object)}
 *       that control ID reference counting.</li>
 *   <li>{@link de.renew.net.TimeSet} provides generic functionality that
 *       might be reused outside simulations.</li>
 *   <li>The static management part of {@link de.renew.net.Net} is not
 *       restricted to simulation threads.  However, the request for nets
 *       by name might initiate the simulation-thread-dependant net loading
 *       mechanism. Therefore, the {@link de.renew.net.Net#forName(String)}
 *       method includes a convenience wrapper.</li>
 *   <li>The weak reference lists ({@link de.renew.net.NetList},
 *       {@link de.renew.net.NetInstanceList}) are not relevant for the
 *       simulation itself.</li>
 * </ul>
 * </p>
 **/
package de.renew.net;

