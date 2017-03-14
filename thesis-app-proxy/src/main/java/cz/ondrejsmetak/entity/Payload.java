package cz.ondrejsmetak.entity;

/**
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 */
public class Payload {
	
	byte[] data;
	int bytesToRead;

	public Payload(byte[] data, int bytesToRead) {
		this.data = new byte[data.length];	
		System.arraycopy(data, 0, this.data, 0, data.length);
		
		this.bytesToRead = bytesToRead;
	}

	public byte[] getData() {
		return data;
	}

	public int getBytesToRead() {
		return bytesToRead;
	}	
}
