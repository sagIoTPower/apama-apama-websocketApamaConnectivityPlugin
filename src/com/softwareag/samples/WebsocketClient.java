/**
 * $Copyright (c) 2016 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.$
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG
 * $Revision: 291193 $ $Date: 2016-09-21 12:56:23 +0100 (Wed, 21 Sep 2016) $
 */

package com.softwareag.samples;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static java.util.Arrays.asList;

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

public class WebsocketClient extends AbstractTransport {
	/** The port that the server listens on, dictated by the 'port' element of configuration */

	private String url;
	private String username;
	private String password;
    private static HostSide staticHostSide;
    private WebSocketContainer container;
    //private static String userPass = "webapiuser:!try3.14webapi!";
    
	/**
	 * The HTML content served up in response to a GET request.
	 */
	//private final String index_body;

	public WebsocketClient(org.slf4j.Logger logger, TransportConstructorParameters params) throws IllegalArgumentException, Exception {
		super(logger, params);
		password = (String)MapHelper.getString(config, "password");
		username = (String)MapHelper.getString(config, "username");
		url = (String)MapHelper.getString(config, "url");
		if(config.size() > 3) throw new IllegalArgumentException("Extraneous configuration (only 'url','username', 'password'  is required)");
	}

	/** Bind the port and start listening for HTTP requests - we don't want the port to be open until the host application is initialised 
	 * @throws DeploymentException */
	@Override
	public void hostReady() throws IOException {	
		try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            logger.info("Connecting to " + url);
            String userPass = username + ":" + password;
            ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
                public void beforeRequest(Map headers) {
                    headers.put("Authorization", asList("Basic " + printBase64Binary(userPass.getBytes())));
                }
            };
            ClientEndpointConfig authorizationConfiguration = ClientEndpointConfig.Builder.create()
                    .configurator(configurator)
                    .build();
            container.connectToServer(MyClientEndpoint.class, authorizationConfiguration, URI.create(url));
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
		logger.info("Listening on port " + url);			
	}
	
	/** Stop the httpserver by closing the listening socket and disallowing any new exchanges from being processed. */
	@Override
	public void shutdown() throws Exception {
		if (container != null) {
			logger.info("Shutting down Client on port " + url);
			//Close server immediately without any delay
			logger.info("Client shutdown complete on port " + url);
		}
	}

	/**
	 * This transport is uni-directional, it does not accept messages from the host. If modified to support other request types that require
	 * a response from the host, one could write this method to actually do something
	 */
	@Override
	public void sendBatchTowardsTransport(List<Message> m) {
//		for (Message message : m) {
//			logger.info("Message received:" + message.toString());
//			try {
//				String payload = message.getPayload().toString();
//				EchoInnerEndpoint.broadcastToAll(payload);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}		
      
	//@ClientEndpoint
	public static class MyClientEndpoint extends Endpoint{
	    //@OnOpen
	    public void onOpen(Session session) {
	        System.out.println("Connected to endpoint: " + session.getBasicRemote());
	    }

	    //@OnMessage
	    public void processMessage(String message) {
	        System.out.println("Received message in client: " + message);
			Message m = new Message(message);
	    	
			// Send the message hostwards
			// Has to be synchronized because the HttpServer implementation is multi-threaded, and the connectivity API asks that you do not
			// send messages simultaneously on multiple threads
			synchronized(staticHostSide) {
				staticHostSide.sendBatchTowardsHost(Collections.singletonList(m));
			}
	    }

//	    //@OnError
//	    public void processError(Throwable t) {
//	        t.printStackTrace();
//	    }

	    @Override
	    public void onOpen(final Session session, EndpointConfig ec) {
	      session.addMessageHandler(new MessageHandler.Whole<String>() {

	        @Override
	        public void onMessage(String message)  {
		        System.out.println("Received message in client: " + message);
				Message m = new Message(message);
		    	
				// Send the message hostwards
				// Has to be synchronized because the HttpServer implementation is multi-threaded, and the connectivity API asks that you do not
				// send messages simultaneously on multiple threads
				synchronized(staticHostSide) {
					staticHostSide.sendBatchTowardsHost(Collections.singletonList(m));
				}
	        }
	    });
	    }
	}
}
