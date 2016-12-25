package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.ClientHello;
import cz.ondrejsmetak.scanner.ClientHelloScanner;
import cz.ondrejsmetak.tool.Helper;
import cz.ondrejsmetak.tool.Log;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

/**
 * Proxy server for SSL/TLS communication
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ProxyServer {

	/**
	 * Instance of HTTP proxy server
	 */
	private HttpProxyServer server;

	/**
	 * Checks, if configured port is available for binding
	 *
	 * @return true if port is available, false otherwise
	 */
	public static boolean checkPort() {
		int localPort = ConfigurationRegister.getInstance().getPort();

		if (!Helper.isLocalPortAvailable(localPort)) {
			Log.errorln(String.format("Port [%s] is already being used. Please specify different port.", localPort));
			return false;
		}

		return true;
	}

	/**
	 * Starts proxy server
	 */
	public void run() {
		server = DefaultHttpProxyServer.bootstrap()
				.withPort(ConfigurationRegister.getInstance().getPort())
				.withFiltersSource(new MyHttpFiltersSourceAdapter())
				.start();
	}

	/**
	 * Stops proxy server
	 */
	public void stop() {
		if (server != null) {
			server.stop();
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

		ClientHello clientHello = new ClientHello(bytes);
		ClientHelloScanner scanner = new ClientHelloScanner(clientHello);
		ReportRegister.getInstance().addReportMessages(scanner.getReportMessages());
	}

	/**
	 * Custom HTTP filter for proxy server
	 */
	private class MyHttpFiltersSourceAdapter extends HttpFiltersSourceAdapter {

		@Override
		public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {

			/**
			 * Add our custom adapter on first position. That means, that our
			 * code will be run first, then anything else. We don't modify sent
			 * data in any way, we "just" read them
			 */
			ctx.pipeline().addFirst(new ChannelInboundHandlerAdapter() {
				@Override
				public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
					ByteBuf buf = (ByteBuf) msg;

					/**
					 * Captured bytes
					 */
					byte[] bytes = new byte[buf.readableBytes()];
					int readerIndex = buf.readerIndex();
					buf.getBytes(readerIndex, bytes);

					/**
					 * Source of communication
					 */
					Channel ch = ctx.channel();
					String peerHost = ((java.net.InetSocketAddress) ch.remoteAddress()).getAddress().getHostAddress();

					/**
					 * If this is ClientHello, run scan
					 */
					handleClientHello(bytes, peerHost);

					/**
					 * Call super method
					 */
					super.channelRead(ctx, msg);
				}
			});

			/**
			 * This object must be returned
			 */
			return new HttpFiltersAdapter(originalRequest) {
				/*Empty code, because in fact, we don't want any filtering*/
			};
		}
	}

}
