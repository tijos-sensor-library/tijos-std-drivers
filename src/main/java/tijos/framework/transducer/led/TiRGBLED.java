package tijos.framework.transducer.led;

import java.io.IOException;

import tijos.framework.devicecenter.TiPWM;

public class TiRGBLED {
	/**
	 * TiRGBLED red channel id
	 */
	private int pwmRed;
	/**
	 * TiRGBLED green channel id
	 */
	private int pwmGreen;
	/**
	 * TiRGBLED blue channel id
	 */
	private int pwmBlue;
	/**
	 * TiRGBLED high level active flag
	 */
	private boolean highActive;
	/**
	 * TiRGBLED object
	 */
	private TiPWM pwmObj = null;

	/**
	 * TiRGBLED initialization, default:highLevel=false
	 * 
	 * @param pwm
	 *            TiPWM object[IN]
	 * @param redChannelID
	 *            read channel id[IN]
	 * @param greenChannelID
	 *            green channel id[IN]
	 * @param blueChannelID
	 *            blue channel id[IN]
	 * @throws IOException
	 */
	public TiRGBLED(TiPWM pwm, int redChannelID, int greenChannelID, int blueChannelID) throws IOException {
		this(pwm, redChannelID, greenChannelID, blueChannelID, true);
	}

	/**
	 * TiRGBLED initialization
	 * 
	 * @param pwm
	 *            TiPWM object[IN]
	 * @param redChannelID
	 *            read channel id[IN]
	 * @param greenChannelID
	 *            green channel id[IN]
	 * @param blueChannelID
	 *            blue channel id[IN]
	 * @param highLevel
	 *            true:high level active, false:low level active[IN]
	 * @throws IOException
	 */
	public TiRGBLED(TiPWM pwm, int redChannelID, int greenChannelID, int blueChannelID, boolean highLevel)
			throws IOException {
		pwm.setFrequency(1000);
		pwm.setDutyCycle(pwmRed, highLevel ? 0 : 1);
		pwm.setDutyCycle(pwmGreen, highLevel ? 0 : 1);
		pwm.setDutyCycle(pwmBlue, highLevel ? 0 : 1);
		pwm.updateFreqAndDuty();
		pwmObj = pwm;
		pwmRed = redChannelID;
		pwmGreen = greenChannelID;
		pwmBlue = blueChannelID;
		highActive = highLevel;
	}

	/**
	 * Set the frequency
	 * 
	 * @param frequency
	 *            frequency value,default:1000(1KHz), unit:Hz
	 * @throws IOException
	 */
	public void setFrequency(int frequency) throws IOException {
		synchronized (pwmObj) {
			pwmObj.setFrequency(frequency);
			pwmObj.updateFreqAndDuty();
		}
	}

	/**
	 * Gets the current frequency
	 * 
	 * @return frequency value,default:1000(1KHz), unit:Hz
	 * @throws IOException
	 */
	public int getFrequency() throws IOException {
		return pwmObj.getFrequency();
	}

	/**
	 * Set red brightness level
	 * 
	 * @param level
	 *            brightness level, range:0-255
	 * @throws IOException
	 */
	public void setRedBrightness(int level) throws IOException {
		synchronized (pwmObj) {
			pwmObj.setDutyCycle(pwmRed, highActive ? (double)level / 255 : (double)(255 - level) / 255);
		}
	}

	/**
	 * Set green brightness level
	 * 
	 * @param level
	 *            brightness level, range:0-255
	 * @throws IOException
	 */
	public void setGreenBrightness(int level) throws IOException {
		synchronized (pwmObj) {
			pwmObj.setDutyCycle(pwmGreen, highActive ? (double)level / 255 : (double)(255 - level) / 255);
		}
	}

	/**
	 * Set blue brightness level
	 * 
	 * @param level
	 *            brightness level, range:0-255
	 * @throws IOException
	 */
	public void setBlueBrightness(int level) throws IOException {
		synchronized (pwmObj) {
			pwmObj.setDutyCycle(pwmBlue, highActive ? (double)level / 255 : (double)(255 - level) / 255);
		}
	}

	/**
	 * Update the red,green,and blue brightness
	 * 
	 * @throws IOException
	 * 
	 */
	public void updateBrightness() throws IOException {
		synchronized (pwmObj) {
			pwmObj.updateFreqAndDuty();
		}
	}

	/**
	 * Gets the current red brightness level
	 * 
	 * @return brightness level
	 * @throws IOException
	 */
	public int getRedBrightness() throws IOException {
		double duty = pwmObj.getDutyCycle(pwmRed);
		return highActive ? (int)(duty * 255) : (int)((1 - duty) * 255);
	}

	/**
	 * Gets the current green brightness level
	 * 
	 * @return brightness level
	 * @throws IOException
	 */
	public int getGreenBrightness() throws IOException {
		double duty = pwmObj.getDutyCycle(pwmGreen);
		return highActive ? (int)(duty * 255) : (int)((1 - duty) * 255);
	}

	/**
	 * Gets the current blue brightness level
	 * 
	 * @return brightness level
	 * @throws IOException
	 */
	public int getBlueBrightness() throws IOException {
		double duty = pwmObj.getDutyCycle(pwmBlue);
		return highActive ? (int)(duty * 255) : (int)((1 - duty) * 255);
	}

	/**
	 * Gets the red channel id
	 * 
	 * @return channel id
	 */
	public int getRedChannelID() {
		return pwmRed;
	}

	/**
	 * Gets the green channel id
	 * 
	 * @return channel id
	 */
	public int getGreenChannelID() {
		return pwmGreen;
	}

	/**
	 * Gets the blue channel id
	 * 
	 * @return channel id
	 */
	public int getBlueChannelID() {
		return pwmBlue;
	}
}
