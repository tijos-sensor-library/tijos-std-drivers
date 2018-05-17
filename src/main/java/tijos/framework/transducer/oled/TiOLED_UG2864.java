package tijos.framework.transducer.oled;

import java.io.IOException;

import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.text.TiDotMatrix;

public class TiOLED_UG2864 {
	/**
	 * TiOLED_UG2864 address
	 */
	private int oledAddress;
	/**
	 * TiI2CMaster object
	 */
	private TiI2CMaster i2cmObj;
	/**
	 * TiDotMatrix object
	 */
	private TiDotMatrix dotMatrixObj;
	/**
	 * Current line index
	 */
	private int currentLineId;
	/**
	 * Current column index
	 */
	private int currentColumnId;

	/**
	 * Set Position
	 * 
	 * @param line
	 *            line index,range:0-7
	 * @param column
	 *            column index,range:0-127
	 * @throws IOException
	 */
	private void oledSetPosition(int line, int column) throws IOException {
		byte[] cmds = { (byte) (0xb0 | line), (byte) (((0xf0 & column) >> 4) | 0x10), (byte) ((0x0f & column) | 0x00) };
		i2cmObj.write(oledAddress, 0x00, cmds, 0, cmds.length);
	}

	/**
	 * Write ascii16 data
	 * 
	 * @param lineAsc
	 *            ascii16 line index,range:0-3
	 * @param columnAsc
	 *            ascii16 column index,range:0-15
	 * @param dotMatrix
	 *            the dot matrix data buffer
	 * @throws IOException
	 */
	private void oledWriteAsc16(int lineAsc, int columnAsc, byte[] dotMatrix) throws IOException {
		oledSetPosition(lineAsc * 2, columnAsc * 8);
		i2cmObj.write(oledAddress, 0x40, dotMatrix, 0, 8);
		oledSetPosition(lineAsc * 2 + 1, columnAsc * 8);
		i2cmObj.write(oledAddress, 0x40, dotMatrix, 8, 8);
	}

	/**
	 * TiOLED_UG2864 initialization
	 * 
	 * @param i2c
	 *            TiI2CMaster object[IN]
	 * @param address
	 *            slave address[IN]
	 * @param fontStyle
	 *            font style[IN]
	 * @throws IOException
	 */
	public TiOLED_UG2864(TiI2CMaster i2c, int address) throws IOException {
		dotMatrixObj = new TiDotMatrix(TiDotMatrix.ASC16);
		oledAddress = address;
		currentLineId = 0;
		currentColumnId = 0;
		i2c.setWorkBaudrate(400);
		i2cmObj = i2c;
	}

	/**
	 * Turn on screen
	 * 
	 * @throws IOException
	 */
	public void turnOn() throws IOException {
		byte[] cmds = { (byte) 0xAE, (byte) 0x20, (byte) 0x10, (byte) 0xb0, (byte) 0xc8, (byte) 0x00, (byte) 0x10,
				(byte) 0x40, (byte) 0x81, (byte) 0xff, (byte) 0xa1, (byte) 0xa6, (byte) 0xa8, (byte) 0x3F, (byte) 0xa4,
				(byte) 0xd3, (byte) 0x00, (byte) 0xd5, (byte) 0xf0, (byte) 0xd9, (byte) 0x22, (byte) 0xda, (byte) 0x12,
				(byte) 0xdb, (byte) 0x20, (byte) 0x8d, (byte) 0x14, (byte) 0xaf };
		i2cmObj.write(oledAddress, 0x00, cmds, 0, cmds.length);
	}

	/**
	 * Turn off screen
	 * 
	 * @throws IOException
	 */
	public void turnOff() throws IOException {
		byte[] cmds = { (byte) 0x8d, (byte) 0x10, (byte) 0xae };
		i2cmObj.write(oledAddress, 0x00, cmds, 0, cmds.length);
	}

	/**
	 * Clear screen
	 * 
	 * @throws IOException
	 */
	public void clear() throws IOException {
		synchronized (i2cmObj) {
			byte[] fillBuffer = new byte[128];
			for (int i = 0; i < 8; i++) {
				oledSetPosition(i, 0);
				i2cmObj.write(oledAddress, 0x40, fillBuffer, 0, fillBuffer.length);
			}
			currentLineId = 0;
			currentColumnId = 0;
		}
	}

	/**
	 * Print string text,<br>
	 * position automatic movement,<br>
	 * data that exceeds the length will be automatically deleted
	 * 
	 * @param lineId
	 *            line index[IN],range:0-3
	 * @param columnId
	 *            column index[IN],range:0-15
	 * @param text
	 *            string text[IN]
	 * @throws IOException
	 */
	public void print(int lineId, int columnId, String text) throws IOException {
		if (lineId < 0 || lineId > 3)
			lineId = 3;
		if (columnId < 0 || columnId > 15)
			columnId = 15;
		if (text.length() <= 0) {
			synchronized (i2cmObj) {
				currentColumnId = columnId;
				currentLineId = lineId;
			}
			return;
		}
		int matrixLeft = 64 - (lineId * 16 + columnId);
		if (matrixLeft > text.length())
			matrixLeft = text.length();
		int matrixIndex = 0;
		byte[][] dotMatrix = dotMatrixObj.convert(text);
		synchronized (i2cmObj) {
			while (matrixLeft-- > 0) {
				oledWriteAsc16(lineId, columnId, dotMatrix[matrixIndex++]);
				currentColumnId = columnId;
				columnId++;
				if (columnId >= 16) {
					columnId = 0;
					currentLineId = lineId;
					lineId++;
					if (lineId >= 4)
						lineId = 0;
				}
			}
		}
	}

	/**
	 * Output string text, <br>
	 * position automatic movement
	 * 
	 * @param text
	 *            string text[IN]
	 * @throws IOException
	 */
	public void output(String text) throws IOException {	
		int matrixTotal = text.length();
		if (matrixTotal <= 0)
			return;	
		int matrixIndex = 0;
		byte[][] dotMatrix = dotMatrixObj.convert(text);
		synchronized (i2cmObj) {
			int lineId = currentLineId;
			int columnId = currentColumnId;
			while (matrixTotal-- > 0) {
				oledWriteAsc16(lineId, columnId, dotMatrix[matrixIndex++]);
				currentColumnId = columnId;
				columnId++;
				if (columnId >= 16) {
					columnId = 0;
					currentLineId = lineId;
					lineId++;
					if (lineId >= 4)
						lineId = 0;
				}
			}
		}
	}

	/**
	 * Set the position
	 * 
	 * @param lineId
	 *            line index[IN], range:0-3
	 * @param columnId
	 *            column index[IN],range:0-15
	 */
	public void setPosition(int lineId, int columnId) {
		if (lineId < 0 || lineId > 3)
			lineId = 3;
		if (columnId < 0 || columnId > 15)
			columnId = 15;
		synchronized (i2cmObj) {
			currentLineId = lineId;
			currentColumnId = columnId;
		}
		return;
	}

	/**
	 * Gets the current line index of position
	 * 
	 * @return line index, range:0-3
	 */
	public int getPositionLine() {
		synchronized (i2cmObj) {
			return currentLineId;
		}
	}

	/**
	 * Gets the current column index of position
	 * 
	 * @return column index, range:0-15
	 */
	public int getPositionColumn() {
		synchronized (i2cmObj) {
			return currentColumnId;
		}
	}

	/**
	 * Gets the max line number
	 * 
	 * @return max line number
	 */
	public int getMaxLineNumber() {
		return dotMatrixObj.getVDotNum();
	}

	/**
	 * Gets the max column number
	 * 
	 * @return max column number
	 */
	public int getMaxColumnNumber() {
		return dotMatrixObj.getHDotNum();
	}
}
