package tijos.framework.sensor.mq;

import java.io.IOException;

import tijos.framework.devicecenter.TiADC;
import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.eventcenter.ITiEvent;
import tijos.framework.eventcenter.ITiEventListener;
import tijos.framework.eventcenter.TiEventService;
import tijos.framework.eventcenter.TiEventType;
import tijos.framework.eventcenter.TiGPIOEvent;

public class TiMQ implements ITiEventListener {
	/**
	 * TiMQn signal pin id
	 */
	private int signalPin;
	/**
	 * TiMQn analog channel id
	 */
	private int analogChannel;
	/**
	 * TiMQn event time
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
	 * TiMQn event listener
	 */
	private ITiMQEventListener mq2Lc = null;

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
				if (mq2Lc != null)
					mq2Lc.onThresholdNotify(this);
			}
		}
	}

	/**
	 * Set the TiMQn event listener
	 * 
	 * @param lc
	 *            listener object or null[IN]
	 * @throws IOException
	 */
	public void setEventListener(ITiMQEventListener lc) throws IOException {
		synchronized (this) {
			if (mq2Lc == null && lc != null) {
				gpioObj.setEventParameters(signalPin, TiGPIO.EVT_BOTHEDGE, 10000);// >=10ms
				TiEventService.getInstance().addListener(this);
				mq2Lc = lc;
			} else if (mq2Lc != null && lc == null) {
				gpioObj.setEventParameters(signalPin, TiGPIO.EVT_NONE, 0);
				TiEventService.getInstance().unregisterEvent(this);
				mq2Lc = null;
			} else {
			}
		}
		return;
	}

	/**
	 * TiMQn initialization, without adc
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param signalPinID
	 *            signal pin id[IN]
	 * @throws IOException
	 */
	public TiMQ(TiGPIO gpio, int signalPinID) throws IOException {
		gpio.setWorkMode(signalPinID, TiGPIO.INPUT_FLOATING);
		gpioObj = gpio;
		signalPin = signalPinID;
		eventTime = -1;
	}

	/**
	 * TiMQn initialization, with adc
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
	public TiMQ(TiGPIO gpio, int signalPinID, TiADC adc, int analogChannelID) throws IOException {
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
	 * Check if greater than the threshold value
	 * 
	 * @return true or false
	 * @throws IOException
	 */
	public boolean isGreaterThanThreshold() throws IOException {
		return gpioObj.readPin(signalPin) == 0;
	}

	/**
	 * Gets the analog output value of AOUT
	 * 
	 * @return voltage value
	 * @throws IOException
	 */
	public double getAnalogOutput() throws IOException {
		if (adcObj == null)
			return Double.NaN;
		return adcObj.getVoltageValue(analogChannel);
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
