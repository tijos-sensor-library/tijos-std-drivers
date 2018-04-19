package tijos.framework.transducer.led;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;

public class TiLED {
	/**
	 * TiLED pin id
	 */
	private int ledPin;
	/**
	 * TiLED high level active flag
	 */
	private boolean highActive;
	/**
	 * TiLED turned on flag
	 */
	private boolean ledOn;
	/**
	 * TiGPIO object
	 */
	private TiGPIO gpioObj;

	/**
	 * TiLED initialization, default:highLevel=false
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param signalPinID
	 *            signal pin id[IN]
	 * @throws IOException
	 */
	public TiLED(TiGPIO gpio, int signalPinID) throws IOException {
		this(gpio, signalPinID, false);
	}

	/**
	 * TiLED initialization
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param signalPinID
	 *            signal pin id[IN]
	 * @param highLevel
	 *            true:high level active, false:low level active[IN]
	 * @throws IOException
	 */
	public TiLED(TiGPIO gpio, int signalPinID, boolean highLevel) throws IOException {
		gpio.setWorkMode(signalPinID, TiGPIO.OUTPUT_PP);
		gpio.writePin(signalPinID, highLevel ? 0 : 1);
		gpioObj = gpio;
		ledPin = signalPinID;
		highActive = highLevel;
		ledOn = false;
	}

	/**
	 * Turn on
	 * 
	 * @throws IOException
	 * 
	 */
	public void turnOn() throws IOException {
		synchronized (this) {
			gpioObj.writePin(ledPin, highActive ? 1 : 0);
			ledOn = true;
		}
	}

	/**
	 * Turn off
	 * 
	 * @throws IOException
	 * 
	 */
	public void turnOff() throws IOException {
		synchronized (this) {
			gpioObj.writePin(ledPin, highActive ? 0 : 1);
			ledOn = false;
		}
	}

	/**
	 * Turn over
	 * 
	 * @throws IOException
	 * 
	 */
	public void turnOver() throws IOException {
		synchronized (this) {
			if (ledOn)
				gpioObj.writePin(ledPin, highActive ? 0 : 1);
			else
				gpioObj.writePin(ledPin, highActive ? 1 : 0);
			ledOn = !ledOn;
		}
	}

	/**
	 * Check if turned on
	 * 
	 * @return true or false
	 */
	public boolean isTurnedOn() {
		synchronized (this) {
			return ledOn;
		}
	}

	/**
	 * Gets the signal pin id
	 * 
	 * @return pin id
	 */
	public int getSignalPinID() {
		return ledPin;
	}
}
