package tijos.framework.sensor.ds18b20;

import java.io.IOException;

import tijos.framework.devicecenter.TiOWMaster;
import tijos.framework.util.Delay;
import tijos.framework.util.crc.CRC8;

public class TiDS18B20 {
	/**
	 * TiDS18B20 io id
	 */
	private int dsIo;
	/**
	 * TiDS18B20 io bits
	 */
	private int dsBits;
	/**
	 * TiDS18B20 max wait time, unit:ms
	 */
	private int dsWaitTime;
	/**
	 * TiDS18B20 rom list
	 */
	private byte[] dsROM;
	/**
	 * TiOWMaster object
	 */
	private TiOWMaster owObj;

	/**
	 * TiDS18B20 initialization
	 * 
	 * @param ow
	 *            TiOWMaster object[IN]
	 * @param ioID
	 *            TiOWMaster io id[IN]
	 * @throws IOException
	 */
	public TiDS18B20(TiOWMaster ow, int ioID) throws IOException {
		ow.setWorkMode(ioID, TiOWMaster.IO_STANDARD);
		dsIo = ioID;
		owObj = ow;
		dsBits = 12;
		dsWaitTime = 800;
		dsROM = null;
	}

	/**
	 * Device enumeration, maximum support 8
	 * 
	 * @return device ROM object list
	 * @throws IOException
	 */
	public TiDS18B20ROM[] enumeration() throws IOException {
		int maxCount = 8, count = 0;
		long contentiousMask = 0;
		TiDS18B20ROM[] list = new TiDS18B20ROM[maxCount];
		synchronized (owObj) {
			do {
				long romCode = 0;
				owObj.reset(dsIo);
				owObj.writeBits(dsIo, 0xf0, 8);
				int bitsLoop = 64;
				long bitMask = 1;
				while (bitsLoop-- > 0) {
					switch (owObj.readBits(dsIo, 2)) {
					case 0x00:
						if ((contentiousMask & bitMask) > 0) {
							if ((contentiousMask & ~((bitMask << 1) - 1)) > 0) {
								owObj.writeBits(dsIo, 0, 1);
							} else {
								romCode |= bitMask;
								contentiousMask ^= bitMask;
								owObj.writeBits(dsIo, 1, 1);
							}
						} else {
							contentiousMask |= bitMask;
							owObj.writeBits(dsIo, 0, 1);
						}
						break;
					case 0x01:
						romCode |= bitMask;
						owObj.writeBits(dsIo, 1, 1);
						break;
					case 0x02:
						owObj.writeBits(dsIo, 0, 1);
						break;
					default:
						return null;
					}
					bitMask <<= 1;
				}
				byte[] code = new byte[8];
				for (int i = 0; i < 8; i++) {
					code[i] = (byte) (romCode & 0xff);
					romCode >>= 8;
				}
				if (CRC8.compute(code) == ((romCode >> 56) & 0xff)) {
					list[count] = new TiDS18B20ROM(romCode);
					count++;
				} else {
					maxCount--;
				}
			} while (count < maxCount && contentiousMask > 0);
		}
		TiDS18B20ROM[] enumList = new TiDS18B20ROM[count];
		for (int i = 0; i < count; i++) {
			enumList[i] = list[i];
		}
		return enumList;
	}

	/**
	 * Select the device with ROM object
	 * 
	 * @param rom
	 *            device ROM object[IN]
	 */
	public void select(TiDS18B20ROM rom) {
		synchronized (owObj) {
			dsROM = rom.getRomData();
		}
		return;
	}

	/**
	 * Select a device without ROM object
	 * 
	 */
	public void selectSingle() {
		synchronized (owObj) {
			dsROM = null;
		}
		return;
	}

	/**
	 * Startup temperature measurement
	 * 
	 * @throws IOException
	 */
	public void measure() throws IOException {
		synchronized (owObj) {
			owObj.reset(dsIo);
			if (dsROM == null || dsROM.length != 8) {
				owObj.writeBits(dsIo, 0xcc, 8);
			} else {
				owObj.writeBits(dsIo, 0x55, 8);
				for (int i = 0; i < 8; i++) {
					owObj.writeBits(dsIo, dsROM[i] & 0xff, 8);
				}
			}
			owObj.writeBits(dsIo, 0x44, 8);
			Delay.msDelay(dsWaitTime);
		}
	}

	/**
	 * Gets the temperature
	 * 
	 * @return temperature value
	 * @throws IOException
	 */
	public double getTemperature() throws IOException {
		short temperature;
		synchronized (owObj) {
			owObj.reset(dsIo);
			if (dsROM == null || dsROM.length != 8) {
				owObj.writeBits(dsIo, 0xcc, 8);
			} else {
				owObj.writeBits(dsIo, 0x55, 8);
				for (int i = 0; i < 8; i++) {
					owObj.writeBits(dsIo, dsROM[i] & 0xff, 8);
				}
			}
			owObj.writeBits(dsIo, 0xbe, 8);
			short tmpL = (short) owObj.readBits(dsIo, 8);
			short tmpH = (short) owObj.readBits(dsIo, 8);
			temperature = (short) (tmpL | (tmpH << 8));
		}
		return temperature * 0.0625;
	}

	/**
	 * Set the resolution
	 * 
	 * @param bits
	 *            the resolution bit number, 9/10/11/12
	 * @throws IOException
	 */
	public void setResolution(int bits) throws IOException {
		int dsbits, dswait;
		int dsacc;
		
		if(bits < 9 || bits > 12)
			throw new IllegalArgumentException("unsupported bits.");
		
		synchronized (owObj) {
			owObj.reset(dsIo);
			if (dsROM == null || dsROM.length != 8) {
				owObj.writeBits(dsIo, 0xcc, 8);
			} else {
				owObj.writeBits(dsIo, 0x55, 8);
				for (int i = 0; i < 8; i++) {
					owObj.writeBits(dsIo, dsROM[i] & 0xff, 8);
				}
			}
			switch (bits) {
			case 9:
				dsbits = 9;
				dswait = 94;
				dsacc = 0x1f;
				break;
			case 10:
				dsbits = 10;
				dswait = 188;
				dsacc = 0x3f;
				break;
			case 11:
				dsbits = 11;
				dswait = 375;
				dsacc = 0x5f;
				break;
			default:
				dsbits = 12;
				dswait = 750;
				dsacc = 0x7f;
				break;
			}
			owObj.writeBits(dsIo, 0x4e, 8);
			owObj.writeBits(dsIo, 75, 8);
			owObj.writeBits(dsIo, 70, 8);
			owObj.writeBits(dsIo, dsacc, 8);
			dsBits = dsbits;
			dsWaitTime = dswait;
		}
	}

	/**
	 * Gets the current resolution
	 * 
	 * @return resolution bit number, default:12
	 */
	public int getResolution() {
		return dsBits;
	}
}
