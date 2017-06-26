/**
 * $Copyright (c) 2016 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.$
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG
 * $Revision: 291193 $ $Date: 2016-09-21 12:56:23 +0100 (Wed, 21 Sep 2016) $
 */

package com.softwareag.samples;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.glassfish.tyrus.server.Server;

import com.softwareag.connectivity.AbstractTransport;
import com.softwareag.connectivity.HostSide;
import com.softwareag.connectivity.MapHelper;
import com.softwareag.connectivity.Message;
import com.softwareag.connectivity.PluginConstructorParameters.TransportConstructorParameters;

/**
 * A minimalist HTTP server. Primarily accepts 'PUT' requests and turns them into hostwards messages. HTTP headers are put into the
 * metadata, and the request body is put into the payload as a string.
 *
 * Uses the configuration key 'port' to find out what port to bind to.
 *
 * It responds to 'GET' requests in a hardcoded fashion, serving up 'index.html' from this package in the JAR file if it exists - this is
 * only really used for demo purposes.
 *
 * UTF-8 character encoding is assumed throughout for simplicity, regardless of what the client wants.
 *
 * This is a sample that is not suitable for production use; it has not been stress-tested or made standards-compliant.
 */

public class WebsocketServer extends AbstractTransport {
	/** The port that the server listens on, dictated by the 'port' element of configuration */
	private int port;
	private String hostname;
	private String context;
	private Server srv;
    private static HostSide staticHostSide;
    
	/**
	 * The HTML content served up in response to a GET request.
	 */
	//private final String index_body;

	public WebsocketServer(org.slf4j.Logger logger, TransportConstructorParameters params) throws IllegalArgumentException, Exception {
		super(logger, params);
		
		//assign properties from configuration
		port = (int)MapHelper.getInteger(config, "port");
		hostname = (String)MapHelper.getString(config, "hostname");
		context = (String)MapHelper.getString(config, "context");
		
		if(config.size() > 3) throw new IllegalArgumentException("Extraneous configuration (only 'port', 'host, 'context'  is required)");
	}

	/** Bind the port and start listening for HTTP requests - we don't want the port to be open until the host application is initialised 
	 * @throws DeploymentException */
	@Override
	public void hostReady() throws IOException {	
		try {
			srv = new Server(hostname, port, context, null, EchoInnerEndpoint.class);
			srv.start();
			staticHostSide = hostSide;
		} catch (DeploymentException e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			throw new IOException(e);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			throw new IOException(e);
		}
		logger.info("Listening on port " + port);			
	}
	
	/** Stop the httpserver by closing the listening socket and disallowing any new exchanges from being processed. */
	@Override
	public void shutdown() throws Exception {
		if (srv != null) {
			logger.info("Shutting down HttpServer on port " + port);
			//Close server immediately without any delay
			srv.stop();
			logger.info("HttpServer shutdown complete on port " + port);
		}
	}

	/**
	 * This transport is uni-directional, it does not accept messages from the host. If modified to support other request types that require
	 * a response from the host, one could write this method to actually do something
	 */
	@Override
	public void sendBatchTowardsTransport(List<Message> m) {
		for (Message message : m) {
			logger.info("Message received:" + message.toString());
			try {
				String payload = message.getPayload().toString();
				EchoInnerEndpoint.broadcastToAll(payload);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}		
      
	@ServerEndpoint("/apamaEcho")
	public static class EchoInnerEndpoint {		
	    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());
	    @OnMessage
	    public String onMessage(String message) {
			Message m = new Message(message);
	    		    	
			// Send the message hostwards
			// Has to be synchronized because the HttpServer implementation is multi-threaded, and the connectivity API asks that you do not
			// send messages simultaneously on multiple threads
			synchronized(staticHostSide) {
				staticHostSide.sendBatchTowardsHost(Collections.singletonList(m));
			}
	        return message + " (from your server)";
	    }

	    @OnError
	    public void onError(Throwable t) {
	        t.printStackTrace();
	    }

	    @OnOpen
	    public void onOpen (Session peer) throws IOException {
	    	peer.getBasicRemote().sendText("onOpen");
	        peers.add(peer);
	    }

	    @OnClose
	    public void onClose (Session peer) {
	        peers.remove(peer);
	    }
	    
	    static public void broadcastToAll (String msg) throws Exception{
	    	for (Session session : peers) {
				session.getBasicRemote().sendText(msg);
			}
	    }
	}
}
