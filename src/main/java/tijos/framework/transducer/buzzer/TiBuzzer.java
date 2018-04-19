package tijos.framework.transducer.buzzer;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;

public class TiBuzzer {
	/**
	 * TiBuzzer pin id
	 */
	private int buzzerPin;
	/**
	 * TiBuzzer high level active flag
	 */
	private boolean highActive;
	/**
	 * TiBuzzer turned on flag
	 */
	private boolean buzzerOn;
	/**
	 * TiGPIO object
	 */
	private TiGPIO gpioObj = null;

	/**
	 * TiBuzzer initialization, default:highLevel=false
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param signalPinID
	 *            signal pin id[IN]
	 * @throws IOException
	 */
	public TiBuzzer(TiGPIO gpio, int signalPinID) throws IOException {
		this(gpio, signalPinID, false);
	}

	/**
	 * TiBuzzer initialization
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param signalPinID
	 *            signal pin id[IN]
	 * @param highLevel
	 *            true:high level active, false:low level active[IN]
	 * @throws IOException
	 */
	public TiBuzzer(TiGPIO gpio, int signalPinID, boolean highLevel) throws IOException {
		gpio.setWorkMode(signalPinID, TiGPIO.OUTPUT_PP);
		gpio.writePin(signalPinID, highLevel ? 0 : 1);
		gpioObj = gpio;
		buzzerPin = signalPinID;
		highActive = highLevel;
		buzzerOn = false;
	}

	/**
	 * Turn on
	 * 
	 * @throws IOException
	 * 
	 */
	public void turnOn() throws IOException {
		synchronized (this) {
			gpioObj.writePin(buzzerPin, highActive ? 1 : 0);
			buzzerOn = true;
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
			gpioObj.writePin(buzzerPin, highActive ? 0 : 1);
			buzzerOn = false;
		}
	}

	/**
	 * Check if turned on
	 * 
	 * @return true or false
	 */
	public boolean isTurnedOn() {
		synchronized (this) {
			return buzzerOn;
		}
	}

	/**
	 * Gets the signal pin id
	 * 
	 * @return pin id
	 */
	public int getSignalPinID() {
		return buzzerPin;
	}
}
