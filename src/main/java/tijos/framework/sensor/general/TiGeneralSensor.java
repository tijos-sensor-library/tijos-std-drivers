package tijos.framework.sensor.general;

import java.io.IOException;

import tijos.framework.devicecenter.TiADC;
import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.eventcenter.ITiEvent;
import tijos.framework.eventcenter.ITiEventListener;
import tijos.framework.eventcenter.TiEventService;
import tijos.framework.eventcenter.TiEventType;
import tijos.framework.eventcenter.TiGPIOEvent;

/**
 * 
 * General and simple sensor, like temperature, light, sound ... it is normally
 * composed with a GPIO pin and a ADC. DOUT could be got from a TiGPIO pin, AOUT
 * could be got from a TiADC pin
 */
public class TiGeneralSensor implements ITiEventListener {
	/**
	 * TiGeneralSensor signal pin id
	 */
	private int signalPin;
	/**
	 * TiGeneralSensor analog channel id
	 */
	private int analogChannel;
	/**
	 * TiGeneralSensor event time
	 */
	private long eventTime;
	/**
	 * TiGPIO object
	 */
	private TiGPIO gpioObj = null;
	/**
	 * TiADC object
	 */
	private TiADC adcObj = null;
	/**
	 * TiGeneralSensor event listener
	 */
	private ITiGeneralSensorEventListener gernalSensorLc = null;

	@Override
	public TiEventType getType() {
		return TiEventType.GPIO;
	}

	@Override
	public void onEvent(ITiEvent evt) {
		synchronized (this) {
			TiGPIOEvent event = (TiGPIOEvent) evt;
			if (event.getPin() == signalPin) {
				eventTime = event.getTime();
				if (gernalSensorLc != null)
					gernalSensorLc.onThresholdNotify(this);
			}
		}
	}

	/**
	 * Set the TiGeneralSensor event listener
	 * 
	 * @param lc
	 *            listener object or null[IN]
	 * @throws IOException
	 */
	public void setEventListener(ITiGeneralSensorEventListener lc) throws IOException {
		synchronized (this) {
			if (gernalSensorLc == null && lc != null) {
				gpioObj.setEventParameters(signalPin, TiGPIO.EVT_FALLINGEDGE, 1000);// >=1ms
				TiEventService.getInstance().addListener(this);
				gernalSensorLc = lc;
			} else if (gernalSensorLc != null && lc == null) {
				gpioObj.setEventParameters(signalPin, TiGPIO.EVT_NONE, 0);
				TiEventService.getInstance().unregisterEvent(this);
				gernalSensorLc = null;
			} else {
			}
		}
		return;
	}

	/**
	 * TiGeneralSensor initialization, without adc
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param signalPinID
	 *            signal pin id[IN]
	 * @param analogChannelID
	 *            analog channel id[IN]   
	 * @throws IOException
	 */
	public TiGeneralSensor(TiGPIO gpio, int signalPinID) throws IOException {
		gpio.setWorkMode(signalPinID, TiGPIO.INPUT_FLOATING);
		gpioObj = gpio;
		signalPin = signalPinID;
		eventTime = -1;
	}

	/**
	 * TiGeneralSensor initialization, with adc
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param signalPinID
	 *            signal pin id[IN]
	 * @param adc
	 *            TiADC object[IN]
	 * @param analogChannelID
	 *            analog channel id[IN]   
	 * @throws IOException
	 */
	public TiGeneralSensor(TiGPIO gpio, int signalPinID, TiADC adc, int analogChannelID) throws IOException {
		this(gpio, signalPinID);
		adcObj = adc;
		analogChannel = analogChannelID;
	}

	/**
	 * Gets the event time
	 * 
	 * @return event time, unit:us
	 */
	public long getEventTime() {
		return eventTime;
	}

	/**
	 * Gets the analog output value of AOUT
	 * 
	 * @return voltage value
	 * @throws IOException
	 */
	public int getAnalogOutput() throws IOException {
		if (adcObj == null)
			return 0;
		
		return adcObj.getRawValue(analogChannel);
	}

	/**
	 * Gets the value of DOUT
	 * 
	 * @return level
	 * @throws IOException
	 */
	public int getDigitalOutput() throws IOException {
		return gpioObj.readPin(signalPin);
	}

	/**
	 * Gets the signal pin id
	 * 
	 * @return pin id
	 */
	public int getSignalPinID() {
		return signalPin;
	}
}
