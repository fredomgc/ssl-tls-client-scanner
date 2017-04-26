package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.ClientHello;
import cz.ondrejsmetak.entity.ClientCertificate;
import cz.ondrejsmetak.entity.Payload;
import cz.ondrejsmetak.entity.Protocol;
import cz.ondrejsmetak.entity.ReportClientHello;
import cz.ondrejsmetak.entity.ReportMessage;
import cz.ondrejsmetak.entity.ServerCertificate;
import cz.ondrejsmetak.scanner.ClientHelloScanner;
import cz.ondrejsmetak.tool.Helper;
import cz.ondrejsmetak.tool.Log;
import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import sun.security.ssl.Debug;

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
	private SSLSocket server = null;

	/**
	 * ServerSocket that is listening for connections on local port
	 */
	private static ServerSocket serverSocket;

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
	private final AtomicInteger clientHelloCounter = new AtomicInteger(0);

	/**
	 * Certificate offered to the client to secure communication
	 */
	private ClientCertificate clientCertificate = null;

	/**
	 * Protocol used during communication with a client
	 */
	private Protocol clientProtocol = new Protocol(Protocol.Type.TLSv12);

	/**
	 * Was there succesfull SSL/TLS handshake during communication with client?
	 */
	private boolean succesfullHandshake = false;

	/**
	 * Use secure (SSL/TLS) socket for communication with client?
	 */
	private boolean secureSocket = false;

	public void run() {

		Runnable r = () -> {
			try {
				running = true;
				doRun();
			} catch (IOException ex) {
				Log.debugException(ex);
			} catch (CertificateEncodingException ex) {
				Log.debugException(ex);
			}
		};

		mainThred = new Thread(r);
		mainThred.start();
	}

	private synchronized void doRun() throws IOException, CertificateEncodingException {
		int localPort = ConfigurationRegister.getInstance().getLocalPort();
		int remotePort = ConfigurationRegister.getInstance().getRemotePort();
		String remoteHost = ConfigurationRegister.getInstance().getRemoteHost();

		serverSocket = createServerSocket();

		final byte[] request = new byte[1024];
		byte[] reply = new byte[4096 * 3];

		while (running) {
			try {
				try {

					client = serverSocket.accept();
					addHandshakeCompletedListener(client);

				} catch (Exception e) {

				}

				if (client == null) {
					continue;
				}
				
				final InputStream streamFromClient = client.getInputStream();
				final OutputStream streamToClient = client.getOutputStream();

				// Make a connection to the real server.
				// If we cannot connect to the server, send an error to the
				// client, disconnect, and continue waiting for connections.
				try {
					//server = new Socket(remoteHost, remotePort);

					SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
					server = (SSLSocket) factory.createSocket(remoteHost, 443);
					String[] suites = server.getSupportedCipherSuites();
					server.setEnabledCipherSuites(suites);
					server.startHandshake();

					SSLSession session = server.getSession();

					//System.err.println("Vzdalene pripojeno k: " + server.getRemoteSocketAddress());
					//System.err.println("Zvolena suie: " + session.getCipherSuite());
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
							/*suppress exceptions*/
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

				//http://www.geeksforgeeks.org/java-io-filterinputstream-class-in-java/
				//http://stackoverflow.com/questions/7743534/filter-search-and-replace-array-of-bytes-in-an-inputstream
				// Read the server's responses
				// and pass them back to the client.
				int bytesRead;
				BufferedInputStream br = new BufferedInputStream(streamFromServer);

				try {
					while ((bytesRead = br.read(reply, 0, reply.length)) != -1) {
						//send everything we have to the client
						streamToClient.write(reply, 0, bytesRead);
						streamToClient.flush();
					}
				} catch (IOException ex) {
					/*suppress exceptions*/
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

	private void addHandshakeCompletedListener(Socket socket) {
		if (!(socket instanceof SSLSocket)) {
			return;
		}

		((SSLSocket) socket).addHandshakeCompletedListener((HandshakeCompletedEvent hce) -> {
			succesfullHandshake = true;
		});
	}

	private ServerSocket createServerSocket() throws IOException {
		int localPort = ConfigurationRegister.getInstance().getLocalPort();

		/**
		 * Without SSL/TLS
		 */
		if (!secureSocket) {
			return new ServerSocket(localPort);
		}

		/**
		 * With SSL/TLS
		 */
		SSLServerSocketFactory socketFactory = createSSLServerSocketFactory(clientCertificate);
		if (socketFactory == null) {
			throw new IllegalStateException("Can't continue with uninitialized ServerSocketFactory!");
		}

		ServerSocket socket = (SSLServerSocket) socketFactory.createServerSocket(localPort);
		((SSLServerSocket) socket).setEnabledProtocols(getTranslatedClientProtocol());

		return (SSLServerSocket) socket;
	}

	private String[] getTranslatedClientProtocol() {
		List<String> protocols = new ArrayList<>();

		switch (clientProtocol.getType()) {
			case SSLv2:
				protocols.add("SSLv2Hello");
				protocols.add("SSLv3");
				break;

			case SSLv3:
				protocols.add("SSLv3");
				break;

			case TLSv10:
				protocols.add("TLSv1");
				break;

			case TLSv11:
				protocols.add("TLSv1.1");
				break;

			case TLSv12:
				protocols.add("TLSv1.2");
				break;

			default:
				protocols.add("TLSv1.2");
				break;
		}

		return protocols.toArray(new String[protocols.size()]);
	}

	private void hijackStreamFromClient(byte[] request, String source) {
		handleClientHello(request, source);
	}

	public void reload() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
			serverSocket = createServerSocket();
		} catch (IOException ex) {
			//??
			//Logger.getLogger(ProxyServer.class.getName()).log(Level.SEVERE, null, ex);
		}
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

	public boolean isSecureSocket() {
		return secureSocket;
	}

	public void setSecureSocket(boolean secureSocket) {
		this.secureSocket = secureSocket;
	}
	
	public void setClientProtocol(Protocol clientProtocol) {
		this.clientProtocol = clientProtocol;
	}

	public Protocol getClientProtocol() {
		return this.clientProtocol;
	}

	public void setConfigurationCertificate(ClientCertificate certificate) {
		this.clientCertificate = certificate;
	}

	public ClientCertificate getClientCertificate() {
		return this.clientCertificate;
	}

	/**
	 * Starts certificate testing with the currently selected protocol and
	 * certificate
	 */
	public void startCertificateTest() {
		secureSocket = true;
		succesfullHandshake = false;
	}

	/**
	 * Stops certificate test and saves current result
	 *
	 * @return true, if handshake occured during testing, false otherwise
	 */
	public boolean stopCertificateTest() {
		if (clientCertificate.getMode().isMustBe() && !succesfullHandshake) {
			//add report
			String message = String.format("Certificate named [%s] MUST BE supported, but there wasn't captured significant communication!", clientCertificate.getName());
			ReportMessage rp = new ReportMessage(message, ReportMessage.Category.CERTIFICATE, clientCertificate.getMode(), ReportMessage.Type.ERROR);
			ReportRegister.getInstance().addReportCertificate(rp);
		}

		if (clientCertificate.getMode().isMustNotBe() && succesfullHandshake) {
			//add report
			String message = String.format("Certificate named [%s] MUST NOT BE supported, but there was captured significant communication!", clientCertificate.getName());
			ReportMessage rp = new ReportMessage(message, ReportMessage.Category.CERTIFICATE, clientCertificate.getMode(), ReportMessage.Type.ERROR);
			ReportRegister.getInstance().addReportCertificate(rp);
		}

		return succesfullHandshake;
	}

	/**
	 * Starts procotol testing with the currently selected protocol and
	 * certificate
	 */
	public void startProtocolTest() {
		secureSocket = true;
		succesfullHandshake = false;
	}

	/**
	 * Stops protocol test and saves current result
	 *
	 * @return true, if handshake occured during testing, false otherwise
	 */
	public boolean stopProtocolTest() {
		if (clientProtocol.getMode().isMustBe() && !succesfullHandshake) {
			String message = String.format("Protocol [%s] MUST BE supported, but there wasn't captured significant communication!", clientProtocol.getType());
			ReportMessage rp = new ReportMessage(message, ReportMessage.Category.PROTOCOL, clientProtocol.getMode(), ReportMessage.Type.ERROR);
			ReportRegister.getInstance().addReportProtocol(rp);
		}

		if (clientProtocol.getMode().isMustNotBe() && succesfullHandshake) {
			String message = String.format("Protocol [%s] MUST NOT BE supported, but there wasn captured significant communication!", clientProtocol.getType());
			ReportMessage rp = new ReportMessage(message, ReportMessage.Category.PROTOCOL, clientProtocol.getMode(), ReportMessage.Type.ERROR);
			ReportRegister.getInstance().addReportProtocol(rp);
		}

		return succesfullHandshake;
	}

	/**
	 * Creates SSLServerSocketFactory, that will be using custom certificate
	 *
	 * @param certificate certificate used to secure communication
	 * @return a newly created factory
	 */
	public SSLServerSocketFactory createSSLServerSocketFactory(ClientCertificate certificate) {

		try {
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(certificate.getKeystore(), certificate.getPassword().toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(certificate.getKeystore());

			SSLContext sslContext = SSLContext.getInstance("TLSv1.2"); //be aware, this value is not respected by JDK 

			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new java.security.SecureRandom());

			return sslContext.getServerSocketFactory();
		} catch (Exception ex) {
			Log.debugException(ex);
			return null;
		}

	}
}
