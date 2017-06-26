# apama-apama-websocketApamaConnectivityPlugin

SAMPLE

   Sample Java Connectivity Plug-in implementing an websocket client and server transport

PREREQUISITE FOR THIS SAMPLE
	Copy following libraries in libs to APAMA_HOME/lib/ws:
		grizzly-framework-2.3.25.jar
		grizzly-http-2.3.25.jar
		grizzly-http-server-2.3.25.jar
		javax.websocket-api-1.1.jar
		tyrus-client-1.13.1.jar
		tyrus-container-grizzly-client-1.13.1.jar
		tyrus-container-grizzly-server-1.13.1.jar
		tyrus-container-servlet-1.13.1.jar
		tyrus-core-1.13.1.jar
		tyrus-server-1.13.1.jar
		tyrus-spi-1.13.1.jar


DESCRIPTION

   Transport source code and associated EPL application to demonstrate the
   ability to use websocket connection to send/receive events into/from the correlator via
   this transport.

COPYRIGHT NOTICE

   $Copyright (c) 2016 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.$
   Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG

FILES

   README.txt                  	This file
   build.xml                   	ANT build file

   src/**/WebsocketClient.java  Source for the Websocket client in Apama
   src/**/WebsocketServer.java  Source for the Websocket server in Apama
   WebsocketMonitor.mon         EPL code to respond to events submitted through the client / server chain
   CorrelatorConfig.yaml        Connectivity plug-ins configuration file
   websocketserver.properties   Properties file defining substitutions for the configuration file

BUILDING THE SAMPLE

   It is recommended (especially for Windows users) that you copy this sample
   folder to an area of your APAMA_WORK directory rather than running it
   directly from the installation directory. For Windows users with UAC
   enabled this step is required to avoid access denied errors when writing to
   the sample directory.

   Running and building of the sample requires access to the Correlator and
   Apama command line tools.

   To ensure that the environment is configured correctly for Apama, all the
   commands below should be executed from an Apama Command Prompt, or from a
   shell or command prompt where the bin\apama_env script has been run (or
   sourced on Unix).

   ** To build the sample **

   Run ant in the current directory to build the sample:

   > ant
   
   A successful build will produce an output file websocket-sample.jar
   containing the compiled websocket server transport.
   
   ** To test websocket from firefox **
   		add plugin :https://addons.mozilla.org/en-gb/firefox/addon/simple-websocket-client/

RUNNING THE SAMPLE

   1. Start the Apama Correlator specifying the connectivity config file and
      properties file

      (unix)
      > correlator --config websocketserver.properties --config CorrelatorConfig.yaml -j -J "-Djava.class.path=${apama_home}\lib\ws\javax.inject-1.jar;${apama_home}\lib\ws\grizzly-framework-2.3.25.jar;${apama_home}\lib\ws\grizzly-http-2.3.25.jar;${apama_home}\lib\ws\grizzly-http-server-2.3.25.jar;${apama_home}\lib\ws\tyrus-spi-1.13.1.jar;${apama_home}\lib\ws\tyrus-core-1.13.1.jar;${apama_home}\lib\ws\tyrus-server-1.13.1.jar;${apama_home}\lib\ws\javax.websocket-api-1.1.jar;${apama_home}\lib\ws\tyrus-container-servlet-1.13.1.jar;${apama_home}\lib\ws\tyrus-container-grizzly-client-1.13.1.jar;${apama_home}\lib\ws\tyrus-container-grizzly-server-1.13.1.jar;${apama_home}\lib\ws\tyrus-client-1.13.1.jar"
      (windows)
      > correlator --config websocketserver.properties --config CorrelatorConfig.yaml -j -J "-Djava.class.path=%APAMA_HOME%\lib\ws\javax.inject-1.jar;%APAMA_HOME%\lib\ws\grizzly-framework-2.3.25.jar;%APAMA_HOME%\lib\ws\grizzly-http-2.3.25.jar;%APAMA_HOME%\lib\ws\grizzly-http-server-2.3.25.jar;%APAMA_HOME%\lib\ws\tyrus-spi-1.13.1.jar;%APAMA_HOME%\lib\ws\tyrus-core-1.13.1.jar;%APAMA_HOME%\lib\ws\tyrus-server-1.13.1.jar;%APAMA_HOME%\lib\ws\javax.websocket-api-1.1.jar;%APAMA_HOME%\lib\ws\tyrus-container-servlet-1.13.1.jar;%APAMA_HOME%\lib\ws\tyrus-container-grizzly-client-1.13.1.jar;%APAMA_HOME%\lib\ws\tyrus-container-grizzly-server-1.13.1.jar;%APAMA_HOME%\lib\ws\tyrus-client-1.13.1.jar"

   2. Inject the demo application and the connectivity plug-in EPL

      (unix)
      > engine_inject $APAMA_HOME/ConnectivityPlugins.mon
      > engine_inject WebsocketMonitor.mon
      
      (windows)
      > engine_inject %APAMA_HOME%\ConnectivityPlugins.mon
      > engine_inject WebsocketMonitor.mon

   3. Open a web browser and open "websocket plugin"  and open socket on ws://localhost:9999/apamaWebsocketServer/apamaEcho
      (this instruction assumes that you are running the correlator on the
       same machine as your browser)
   
   4. Use the text box to write a JSON-formatted string:

        {"stringField":"asdf", "integerField":40, "seqField":[1.0, 2.0, 3.0]}

      Click the button to send it 
      server plug-in that is currently running in the correlator:
         {"seqField":[1.0,2.0,3.0],"stringField":"Return dasdf","integerField":40}


SAMPLE OUTPUT


   The JSON codec is placed between the host and the websocket server transport
   so that any JSON that is 'PUT' into the chain this way gets mapped into
   structured data that the correlator can interpret as an EPL event.

   The EPL monitor 'WebsocketMonitor' in this demo listens for these events and
   logs their contents. By default the correlator log goes to the console, and
   if you submit the JSON from the instructions above, you'll see a line in
   the log like:

		2017-03-08 16:04:38.501 INFO  [4964] - WebsocketMonitor [1] Got an event through serverChain:WebsocketEventServer([1,2,3],40,"asdf")