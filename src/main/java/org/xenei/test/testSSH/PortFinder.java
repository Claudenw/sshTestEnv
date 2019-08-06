package org.xenei.test.testSSH;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class PortFinder {

    public PortFinder() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Returns a free port number on localhost.
     * <p>
     * Heavily inspired from org.eclipse.jdt.launching.SocketUtil (to avoid a
     * dependency to JDT just because of this). Slightly improved with close()
     * missing in JDT. And throws exception instead of returning -1.
     * </p>
     *
     * @return a free port number on localhost
     * @throws IllegalStateException
     *             if unable to find a free port
     */
    public static int findFreePort() {
        try (ServerSocket socket = new ServerSocket( 0 ))
        {
            socket.setReuseAddress( true );
            return socket.getLocalPort();
        } catch (final IOException ignore)
        {
            throw new IllegalStateException( "Could not find a free TCP/IP port" );
        }

    }

    /**
     * Returns a free port number on localhost.
     * <p>
     * Heavily inspired from org.eclipse.jdt.launching.SocketUtil (to avoid a
     * dependency to JDT just because of this). Slightly improved with close()
     * missing in JDT. And throws exception instead of returning -1.
     * </p>
     *
     * @return a free port number on localhost
     * @throws IllegalStateException
     *             if unable to find a free port
     */
    public static int[] findFreePorts(final int num) {
        final int[] retval = new int[num];
        final List<ServerSocket> sockets = new ArrayList<>();
        ServerSocket socket = null;
        try
        {
            for (int i = 0; i < num; i++)
            {
                socket = new ServerSocket( 0 );
                socket.setReuseAddress( true );
                retval[i] = socket.getLocalPort();
                sockets.add( socket );
                socket = null;
            }
            return retval;
        } catch (final IOException ignore)
        {
            // do nothing
        } finally
        {

            if (socket != null)
            {
                try
                {
                    socket.close();
                } catch (final IOException ignore)
                {
                    // do nothing
                }
            }
            for (final ServerSocket s : sockets)
            {
                try
                {
                    s.close();
                } catch (final IOException ignore)
                {
                    // do nothing
                }
            }
        }
        throw new IllegalStateException( String.format( "Could not find %s free TCP/IP port(s)", num ) );
    }

}
