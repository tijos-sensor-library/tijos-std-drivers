package tijos.framework.transducer.relay;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;

public class TiRelay1CH {
	/**
	 * TiRelay1CH pin id
	 */
	private int RelayPin;
	/**
	 * TiRelay1CH high level active flag
	 */
	private boolean highActive;
	/**
	 * TiRelay1CH turned on flag
	 */
	private boolean relayOn;
	/**
	 * TiGPIO flag
	 */
	private TiGPIO gpioObj;

	/**
	 * TiRelay1CH initialization, default:highLevel=true
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param signalPinID
	 *            signal pin id[IN]
	 * @throws IOException
	 */
	public TiRelay1CH(TiGPIO gpio, int signalPinID) throws IOException {
		this(gpio, signalPinID, true);
	}

	/**
	 * TiRelay1CH initialization
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param signalPinID
	 *            signal pin id[IN]
	 * @param highLevel
	 *            true:high level active, false:low level active[IN]
	 * @throws IOException
	 */
	public TiRelay1CH(TiGPIO gpio, int signalPinID, boolean highLevel) throws IOException {
		gpio.setWorkMode(signalPinID, TiGPIO.OUTPUT_PP);
		gpio.writePin(signalPinID, highLevel ? 0 : 1);
		gpioObj = gpio;
		RelayPin = signalPinID;
		highActive = highLevel;
		relayOn = false;
	}

	/**
	 * Turn on
	 * 
	 * @throws IOException
	 * 
	 */
	public void turnOn() throws IOException {
		synchronized (this) {
			gpioObj.writePin(RelayPin, highActive ? 1 : 0);
			relayOn = true;
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
			gpioObj.writePin(RelayPin, highActive ? 0 : 1);
			relayOn = false;
		}
	}

	/**
	 * Check if turned on
	 * 
	 * @return true or false
	 */
	public boolean isTurnedOn() {
		synchronized (this) {
			return relayOn;
		}
	}

	/**
	 * Gets the signal pin id
	 * 
	 * @return pin id
	 */
	public int getSignalPinID() {
		return RelayPin;
	}
}
