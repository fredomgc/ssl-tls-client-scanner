package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.ClientHello;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class ProxyServer {

	public void run() {

		HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
				.withPort(6880)
				.withFiltersSource(new MyHttpFiltersSourceAdapter())
				.start();
	}

	private Integer hexToInt(String hex) {
		return Integer.parseInt(hex.trim(), 16);
	}

	private void handleClientHello(byte[] bytes, String source) {
		if (!ClientHello.isClientHello(bytes)) {
			return; //nothing to do
		}
		
		ClientHello clientHello = new ClientHello(bytes);
		//clientHello.
		
		//totototototo
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

					byte[] bytes = new byte[buf.readableBytes()];
					int readerIndex = buf.readerIndex();
					buf.getBytes(readerIndex, bytes);

					//ty byty jsou v desitkove soustave, proto pro porovnavani toto:
					/**
					 * TODO Toto pole bytes stačí přečíst a zkontrolovat
					 */
					//160301
					if (bytes[0] == hexToInt("16") && bytes[1] == hexToInt("03") && bytes[2] == hexToInt("01")) {
						System.err.println("Sláva to je client hello s velikosti " + bytes.length);
						System.out.println(">>> " + ByteBufUtil.hexDump(buf));
					}

					Channel ch = ctx.channel();
					String peerHost = ((java.net.InetSocketAddress) ch.remoteAddress()).getAddress().getHostAddress();
					System.err.println("Hej zdroj je: " + peerHost);

					handleClientHello(bytes, peerHost);

					super.channelRead(ctx, msg);
				}
			});

			/**
			 * Toto se musí vrátit
			 */
			return new HttpFiltersAdapter(originalRequest) {

			};
		}
	}

}
