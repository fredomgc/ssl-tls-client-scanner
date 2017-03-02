package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.ClientHello;
import cz.ondrejsmetak.entity.ReportClientHello;
import cz.ondrejsmetak.scanner.ClientHelloScanner;
import cz.ondrejsmetak.tool.Helper;
import cz.ondrejsmetak.tool.Log;
import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ProxyServer {

	/**
	 * Connection on the local port
	 */
	private Socket client = null;

	/**
	 * Connection to the remote server on the given remote port
	 */
	private Socket server = null;

	/**
	 * ServerSocket that is listening for connections on local port
	 */
	private ServerSocket serverSocket;

	/**
	 * Main thread used to run this ProxyServer
	 */
	private Thread mainThred;

	/**
	 * Is this ProxyServer running
	 */
	private static boolean running = true;

	/**
	 * Used to count captued Client Hello packets
	 */
	private AtomicInteger clientHelloCounter = new AtomicInteger(0);

	public void run() {
		Runnable r = () -> {
			try {
				doRun();
			} catch (IOException ex) {
				Log.debugException(ex);
			}
		};

		mainThred = new Thread(r);
		mainThred.start();
	}

	private void doRun() throws IOException {
		int localPort = ConfigurationRegister.getInstance().getLocalPort();
		int remotePort = ConfigurationRegister.getInstance().getRemotePort();
		String remoteHost = ConfigurationRegister.getInstance().getRemoteHost();

		serverSocket = new ServerSocket(localPort);

		final byte[] request = new byte[1024];
		byte[] reply = new byte[4096];

		while (running) {
			try {
				// Wait for a connection on the local port
				client = serverSocket.accept();

				final InputStream streamFromClient = client.getInputStream();
				final OutputStream streamToClient = client.getOutputStream();

				// Make a connection to the real server.
				// If we cannot connect to the server, send an error to the
				// client, disconnect, and continue waiting for connections.
				try {
					server = new Socket(remoteHost, remotePort);
				} catch (IOException ex) {
					Log.debugException(ex);
					client.close();
					continue;
				}

				// Get server streams.
				final InputStream streamFromServer = server.getInputStream();
				final OutputStream streamToServer = server.getOutputStream();

				// a thread to read the client's requests and pass them
				// to the server. A separate thread for asynchronous.
				Thread t = new Thread() {
					public void run() {
						int bytesRead;
						try {
							while ((bytesRead = streamFromClient.read(request)) != -1) {
								hijackStreamFromClient(request, server.getLocalSocketAddress().toString().replaceAll("/", ""));
								streamToServer.write(request, 0, bytesRead);
								streamToServer.flush();
							}
						} catch (IOException ex) {
							Log.debugException(ex);
						}

						// the client closed the connection to us, so close our
						// connection to the server.
						try {
							streamToServer.close();
						} catch (IOException ex) {
							Log.debugException(ex);
						}
					}
				};

				// Start the client-to-server request thread running
				t.start();

				// Read the server's responses
				// and pass them back to the client.
				int bytesRead;
				try {
					while ((bytesRead = streamFromServer.read(reply)) != -1) {
						streamToClient.write(reply, 0, bytesRead);
						streamToClient.flush();
					}
				} catch (IOException ex) {
					Log.debugException(ex);
				}

				// The server closed its connection to us, so we close our
				// connection to our client.
				streamToClient.close();
			} catch (IOException ex) {
				Log.debugException(ex);
			} finally {
				try {
					if (server != null) {
						server.close();
					}
					if (client != null) {
						client.close();
					}
				} catch (IOException ex) {
					Log.debugException(ex);
				}
			}
		}
	}

	private void hijackStreamFromClient(byte[] request, String source) {
		handleClientHello(request, source);
	}

	public void stop() {
		try {
			running = false;

			if (serverSocket != null) {
				serverSocket.close();
			}

			if (server != null) {
				server.close();
			}

			if (client != null) {
				client.close();
			}

		} catch (IOException ex) {
			Log.debugException(ex);
		}
	}

	/**
	 * Runs analysis over captured Client Hello
	 *
	 * @param bytes captured client hello as array of bytes
	 * @param source source IP adress
	 */
	private void handleClientHello(byte[] bytes, String source) {
		if (!ClientHello.isClientHello(bytes)) {
			return; //nothing to do
		}

		if (ConfigurationRegister.getInstance().isDebug()) {
			Log.infoln(String.format("Captured Client Hello from [%s], analysis started.", source));
		}

		int clientHelloId = this.clientHelloCounter.incrementAndGet();

		ClientHello clientHello = new ClientHello(bytes);
		ClientHelloScanner scanner = new ClientHelloScanner(clientHello);
		ReportRegister.getInstance().addReportClientHello(new ReportClientHello(clientHelloId, scanner.getReportMessages()));
	}

	/**
	 * Checks, if configured port is available for binding
	 *
	 * @return true if port is available, false otherwise
	 */
	public static boolean checkPort() {
		int localPort = ConfigurationRegister.getInstance().getLocalPort();

		if (!Helper.isLocalPortAvailable(localPort)) {
			Log.errorln(String.format("Port [%s] is already being used. Please specify different port.", localPort));
			return false;
		}

		return true;
	}

}
