package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.ClientHello;
import cz.ondrejsmetak.entity.ReportMessage;
import cz.ondrejsmetak.scanner.ClientHelloScanner;
import cz.ondrejsmetak.tool.Helper;
import cz.ondrejsmetak.tool.Log;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import java.util.List;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ProxyServer {
	
	private HttpProxyServer server;
	
	public void run() {
		server = DefaultHttpProxyServer.bootstrap()
				.withPort(ConfigurationRegister.getInstance().getPort())
				.withFiltersSource(new MyHttpFiltersSourceAdapter())
				.start();
		
	}
	
	public void stop() {
		if (server != null) {
			server.stop();
		}
	}
	
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
	
	private class MyHttpFiltersSourceAdapter extends HttpFiltersSourceAdapter {
		
		@Override
		public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {

			/**
			 * Toto je ten hook na hexDump
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
				
			};
		}
	}
	
}
