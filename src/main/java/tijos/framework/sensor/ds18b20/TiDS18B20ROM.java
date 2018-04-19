package tijos.framework.sensor.ds18b20;

public class TiDS18B20ROM {
	/**
	 * TiDS18B20ROM rom data
	 */
	private byte[] dsRom = null;

	/**
	 * TiDS18B20ROM initialization
	 * 
	 * @param rom
	 *            rom integer[IN]
	 */
	public TiDS18B20ROM(long rom) {
		dsRom = new byte[8];
		dsRom[0] = (byte) (rom & 0xff);
		dsRom[1] = (byte) ((rom >> 8) & 0xff);
		dsRom[2] = (byte) ((rom >> 16) & 0xff);
		dsRom[3] = (byte) ((rom >> 24) & 0xff);
		dsRom[4] = (byte) ((rom >> 32) & 0xff);
		dsRom[5] = (byte) ((rom >> 40) & 0xff);
		dsRom[6] = (byte) ((rom >> 48) & 0xff);
		dsRom[7] = (byte) ((rom >> 56) & 0xff);
	}

	/**
	 * Gets the ROM data, 64bits
	 * 
	 * @return ROM data
	 */
	public byte[] getRomData() {
		return dsRom;
	}

	/**
	 * Gets the family code, 8bits
	 * 
	 * @return family code
	 */
	public int getFamilyCode() {
		return dsRom[0] & 0xff;
	}

	/**
	 * Gets the serial number, 48bits
	 * 
	 * @return serial number
	 */
	public byte[] getSerialNumber() {
		byte[] serial = new byte[6];
		System.arraycopy(dsRom, 1, serial, 0, 6);
		return serial;
	}

	/**
	 * Gets the checksum code, 8bits
	 * 
	 * @return checksum code
	 */
	public int getChecksumCode() {
		return dsRom[7] & 0xff;
	}
}
