package cz.ondrejsmetak;

import cz.ondrejsmetak.entity.ClientHello;
import cz.ondrejsmetak.tool.Helper;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Application {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		ProxyServer proxy = new ProxyServer();
		//proxy.run();
		
		String data = "16030100c1010000bd03036e730f1b74149777fadc38bbb87d0011bf93ecafa382ee3271e43c1289aa9b9f00001ec02bc02fcca9cca8c02cc030c00ac009c013c01400330039002f0035000a0100007600000018001600001366696d672d726573702e73657a6e616d2e637a00170000ff01000100000a00080006001700180019000b0002010000230000337400000010000e000c02683208687474702f312e31000500050100000000000d0018001604010501060102010403050306030203050204020202";		
		ClientHello clientHello = new ClientHello(Helper.toByteArray(data));
	}
	
}
