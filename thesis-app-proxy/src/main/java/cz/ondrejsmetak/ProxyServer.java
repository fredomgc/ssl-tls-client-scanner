package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.ClientHello;
import cz.ondrejsmetak.entity.Payload;
import cz.ondrejsmetak.entity.ReportClientHello;
import cz.ondrejsmetak.entity.ServerCertificate;
import cz.ondrejsmetak.scanner.ClientHelloScanner;
import cz.ondrejsmetak.tool.Helper;
import cz.ondrejsmetak.tool.Log;
import java.io.*;
import java.net.*;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
			} catch (CertificateEncodingException ex) {
				Log.debugException(ex);
			}
		};

		mainThred = new Thread(r);
		mainThred.start();
	}

	private void doRun() throws IOException, CertificateEncodingException {
		int localPort = ConfigurationRegister.getInstance().getLocalPort();
		int remotePort = ConfigurationRegister.getInstance().getRemotePort();
		String remoteHost = ConfigurationRegister.getInstance().getRemoteHost();

		serverSocket = new ServerSocket(localPort);

		final byte[] request = new byte[1024];
		byte[] reply = new byte[4096 * 3];

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
				List<Byte> buffer = new ArrayList<>();

				try {
					while ((bytesRead = br.read(reply, 0, reply.length)) != -1) {
						System.err.println("Precteno bytu: " + bytesRead);
						for (int i = 0; i < bytesRead; i++) {
							buffer.add(reply[i]);
						}

						byte[] primitive = new byte[buffer.size()];
						for (int i = 0; i < buffer.size(); i++) {
							primitive[i] = buffer.get(i);
						}

						ServerCertificate.ServerCertificateCheck check = ServerCertificate.isServerCerfificate(primitive);

						if (check == ServerCertificate.NO_SERVER_CERTIFICATE) {
							//send everything we have to the client
							streamToClient.write(primitive, 0, buffer.size());
							streamToClient.flush();
							buffer.clear();
						} else if (check.getEndIndex() > buffer.size()) {
							//not enough data, continue with capturing
							System.out.println("Ted se bufferujeme do budoucna");
						} else {
							System.out.println("Jsme certifikat a posilame cely buffer");
							System.out.println("Velikost bufferu: " + buffer.size());
							System.out.println("Velikost certifikatu: " + check.getLength());
							//System.out.println("Co cteme: " + Helper.toHexString(Arrays.copyOfRange(primitive, check.getStartIndex(), check.getEndIndex())));
							//System.out.println("Saham na index: " + check.getEndIndex() + ", ale pole ma velikost " + primitive.length);

							ServerCertificate certificate = new ServerCertificate(Arrays.copyOfRange(primitive, check.getStartIndex(), check.getEndIndex()));
							certificate.hack();
							
							//byte[] original = Arrays.copyOfRange(primitive, check.getStartIndex(), check.getEndIndex());
							//byte[] tampered = Helper.toByteArray(certificate.toHex());
							
							//if(!Arrays.equals(original, tampered)){
								//System.out.println("Original: " + Helper.toHexString(original));
								//System.out.println("Tampered: " + certificate.toHex());
								
								//System.out.println("Original: " + Arrays.toString(original));
								//System.out.println("Tampered: " + Arrays.toString(tampered));
								
								//System.out.println("Original a tampered se nerovna, a to je problem!");
							//}
							
							//System.out.println("Hacknuty certifikat: " + certificate.toHex());
							
							
							byte[] before = Arrays.copyOfRange(primitive, 0, check.getStartIndex());
							byte[] hack = Helper.toByteArray(certificate.toHex());
							byte[] rest = Arrays.copyOfRange(primitive, check.getEndIndex(), primitive.length);

							System.err.println("Velikost before: " + before.length + " hack: " + hack.length + " rest: " + rest.length + ", soucet: " + (before.length + hack.length + rest.length));

							byte[] merged = new byte[before.length + hack.length + rest.length];
							int index = 0;

							for (int i = 0; i < before.length; i++) {
								merged[index++] = before[i];
							}

							for (int i = 0; i < hack.length; i++) {
								merged[index++] = hack[i];
							}

							for (int i = 0; i < rest.length; i++) {
								merged[index++] = rest[i];
							}

							//here do something with certificate
							//and send it
							streamToClient.write(merged, 0, merged.length);
							streamToClient.flush();
							buffer.clear();

							System.out.println("---");
						}

					}
				} catch (IOException ex) {
					/*suppress exceptions*/
				}

//ZALOHA
//				try {
//					while ((bytesRead =   streamFromServer.read(reply, 0, reply.length)) != -1) {
//						System.err.println("Precteno bytu: " + bytesRead);
//
//						Payload hijackedPayload = hijackStreamFromServer(reply, bytesRead, "TODO?");
//
//						streamToClient.write(hijackedPayload.getData(), 0, hijackedPayload.getBytesToRead());
//						streamToClient.flush();
//
//					}
//				} catch (IOException ex) {
//					/*suppress exceptions*/
//				}
// \ ZALOHA
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
		//handleClientHello(request, source);
	}

	private Payload hijackStreamFromServer(byte[] request, int bytesRead, String source) {
		return handleCertificate(request, bytesRead);
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

	private Payload handleCertificate(byte[] bytes, int bytesRead) {
		ServerCertificate.ServerCertificateCheck found = ServerCertificate.isServerCerfificate(bytes);

		if (found == ServerCertificate.NO_SERVER_CERTIFICATE) {
			return new Payload(bytes, bytesRead);
			//return ServerCertificate.NO_SERVER_CERTIFICATE; //nothing to do
		}

		if (ConfigurationRegister.getInstance().isDebug()) {
			Log.infoln(String.format("Captured certificate. Doing some changes. " + found.getEndIndex()));
		}

		byte[] untouched = Arrays.copyOfRange(bytes, 0, found.getStartIndex() - 1);
		byte[] certificate = Arrays.copyOfRange(bytes, found.getStartIndex(), found.getEndIndex());
		byte[] rest = Arrays.copyOfRange(bytes, found.getEndIndex() + 1, bytes.length - bytesRead);

		//System.err.println("Bytes read je: " + bytesRead + " pritom bytes ma velikost " + bytes.length);
		//System.err.println("untouched je: " + untouched.length);
		//System.err.println("certificate je: " + certificate.length);
		//System.err.println("rest je: " + rest.length);
		return new Payload(bytes, bytesRead);

		//bytes = test;
		//ServerCertificate serverCertificate = new ServerCertificate(found);
		///Arrays.		
		//tady zkusit zmenit odpoved serveru
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
