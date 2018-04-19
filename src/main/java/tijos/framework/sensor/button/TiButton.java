package tijos.framework.sensor.button;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.eventcenter.ITiEvent;
import tijos.framework.eventcenter.ITiEventListener;
import tijos.framework.eventcenter.TiEventService;
import tijos.framework.eventcenter.TiEventType;
import tijos.framework.eventcenter.TiGPIOEvent;

public class TiButton implements ITiEventListener {
	/**
	 * TiButton pin id
	 */
	private int buttonPin;
	/**
	 * TiButton high level active flag
	 */
	private boolean highActive;
	/**
	 * TiButton event time
	 */
	private long eventTime;
	/**
	 * TiGPIO object
	 */
	private TiGPIO gpioObj;
	/**
	 * Event listener
	 */
	private ITiButtonEventListener buttonLc = null;

	@Override
	public TiEventType getType() {
		return TiEventType.GPIO;
	}

	@Override
	public void onEvent(ITiEvent evt) {
		synchronized (this) {
			TiGPIOEvent event = (TiGPIOEvent) evt;
			if (event.getPin() == buttonPin) {
				switch (event.getEvent()) {
				case TiGPIO.EVT_FALLINGEDGE:
					eventTime = event.getTime();
					if (highActive) {
						if (buttonLc != null)
							buttonLc.onReleased(this);
					}
					else {
						if (buttonLc != null)
							buttonLc.onPressed(this);
					}
					break;
				case TiGPIO.EVT_RISINGEDGE:
					eventTime = event.getTime();
					if (highActive) {
						if (buttonLc != null)
							buttonLc.onPressed(this);
					}
					else {
						if (buttonLc != null)
							buttonLc.onReleased(this);
					}
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * TiButton initialization, default:highLevel=false
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param signalPinID
	 *            signal pin id[IN]
	 * @throws IOException
	 */
	public TiButton(TiGPIO gpio, int signalPinID) throws IOException {
		this(gpio, signalPinID, false);
	}

	/**
	 * TiButton initialization
	 * 
	 * @param gpio
	 *            TiGPIO object[IN]
	 * @param signalPinID
	 *            signal pin id[IN]
	 * @param highLevel
	 *            true:high level active, false:low level active[IN]
	 * @throws IOException
	 */
	public TiButton(TiGPIO gpio, int signalPinID, boolean highLevel) throws IOException {
		gpio.setWorkMode(signalPinID, highLevel ? TiGPIO.INPUT_FLOATING : TiGPIO.INPUT_PULLUP);
		gpioObj = gpio;
		buttonPin = signalPinID;
		highActive = highLevel;
		eventTime = -1;
	}

	/**
	 * Set the TiButton event listener
	 * 
	 * @param lc
	 *            listener or null[IN]
	 * @throws IOException
	 */
	public void setEventListener(ITiButtonEventListener lc) throws IOException {
		synchronized (this) {
			if (buttonLc == null && lc != null) {
				gpioObj.setEventParameters(buttonPin, TiGPIO.EVT_BOTHEDGE, 10000);// >=10ms
				TiEventService.getInstance().addListener(this);
				buttonLc = lc;
			} else if (buttonLc != null && lc == null) {
				gpioObj.setEventParameters(buttonPin, TiGPIO.EVT_NONE, 0);
				TiEventService.getInstance().unregisterEvent(this);
				buttonLc = null;
			} else {
			}
		}
		return;
	}

	/**
	 * Gets the event occur time, unit:us
	 * 
	 * @return time value
	 */
	public long getEventTime() {
		return eventTime;
	}

	/**
	 * Gets the signal pin id
	 * 
	 * @return pin id
	 */
	public int getSignalPinID() {
		return buttonPin;
	}
}
