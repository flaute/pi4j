package com.pi4j.gpio.extension.horter;

/*-
 * #%L
 * **********************************************************************
ORGANIZATION  :  Pi4J
PROJECT       :  Pi4J :: GPIO Extension
FILENAME      :  HorterAdcGpioProvider.java

This file is part of the Pi4J project. More information about
this project can be found here:  http://www.pi4j.com/
**********************************************************************
 * %%
 * Copyright (C) 2012 - 2017 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Lesser Public License for more details.

You should have received a copy of the GNU General Lesser Public
License along with this program.  If not, see
<http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
import java.io.IOException;

import com.pi4j.gpio.extension.base.AdcGpioProvider;
import com.pi4j.gpio.extension.base.AdcGpioProviderBase;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * <p>
 * This GPIO provider implements the horter ADC I2C GPIO expansion board as native Pi4J GPIO pins.
 * It is a 10-bit ADC providing 5 input channels.
 * More information about the board can be found here:
 * https://www.horter-shop.de/de/i2c-hutschienen-module/172-bausatz-i2c-analog-input-modul-5-kanal-10-bit-4260404260745.html
 * </p>
 *
 * <p>
 * The horter ADC is connected via I2C connection to the Raspberry Pi and provides 5 analog input channels.
 * The values returned are in the range [0:1023] (max 10 bit value).
 * </p>
 *
 * @see https://www.horter-shop.de/de/i2c-hutschienen-module/172-bausatz-i2c-analog-input-modul-5-kanal-10-bit-4260404260745.html
 * @author Christian Flaute
 */
public class HorterAdcGpioProvider extends AdcGpioProviderBase implements AdcGpioProvider {

    public static final String NAME = "com.pi4j.gpio.extension.horter.HorterAdcGpioProvider";
    public static final String DESCRIPTION = "Horter ADC GPIO Provider";

    // these addresses belong to HorterAdcGpio
    public static final int HorterAdcGpio_0x08 = 0x08;

    private boolean i2cBusOwner = false;
    private final I2CBus bus;
    private final I2CDevice device;

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 1023;
    public static final int INVALID_VALUE = -1;

    public HorterAdcGpioProvider(int busNumber, int address) throws UnsupportedBusNumberException, IOException {
        this(I2CFactory.getInstance(busNumber), address, DEFAULT_MONITOR_INTERVAL);
        i2cBusOwner = true;
    }

    public HorterAdcGpioProvider(int busNumber, int address, int interval)
            throws UnsupportedBusNumberException, IOException {
        this(I2CFactory.getInstance(busNumber), address, interval);
        i2cBusOwner = true;
    }

    public HorterAdcGpioProvider(I2CBus bus, int address, int interval)
            throws UnsupportedBusNumberException, IOException {
        super(HorterAdcPin.ALL);

        // set reference to I2C communications bus instance
        this.bus = bus;

        // create I2C device instance
        device = bus.getDevice(address);

        // reset the pointer (must point to first byte)
        byte reset[] = { 0x00 };
        device.write(reset, 0, reset.length);

        // set all default pin cache values
        for (Pin pin : HorterAdcPin.ALL) {
            getPinCache(pin).setAnalogValue(getImmediateValue(pin));
        }

        // start monitoring thread
        this.setMonitorInterval(interval);
        this.setMonitorEnabled(true);

    }

    @Override
    public void shutdown() {

        super.shutdown();

        // if we are the owner of the I2C bus, then close it
        if (i2cBusOwner) {
            try {
                bus.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * This method will perform an immediate data acquisition directly to the ADC chip to get the requested pin's input
     * conversion value.
     *
     * @param pin requested input pin to acquire conversion value
     * @return conversion value for requested analog input pin
     * @throws IOException
     */
    @Override
    public double getImmediateValue(final Pin pin) throws IOException {

        // read 11 bytes: the 1st byte is a pointer followed by 5 pairs of LSB/MSB bytes with the values
        byte[] buffer = new byte[11];
        device.read(0x00, buffer, 0, buffer.length);

        String pinName = pin.getName();

        int lsb = 0;
        int msb = 0;

        if (HorterAdcPin.GPIO_00.getName().equals(pinName)) {
            lsb = buffer[1];
            msb = buffer[2];
        } else if (HorterAdcPin.GPIO_01.getName().equals(pinName)) {
            lsb = buffer[3];
            msb = buffer[4];
        } else if (HorterAdcPin.GPIO_02.getName().equals(pinName)) {
            lsb = buffer[5];
            msb = buffer[6];
        } else if (HorterAdcPin.GPIO_03.getName().equals(pinName)) {
            lsb = buffer[7];
            msb = buffer[8];
        } else if (HorterAdcPin.GPIO_04.getName().equals(pinName)) {
            lsb = buffer[9];
            msb = buffer[10];
        } else {
            return INVALID_VALUE;
        }

        // calculate the value
        return ((msb & 0xff) * 255) + (lsb & 0xff);
    }

    /**
     * Get the minimum supported analog value for the ADC implementation.
     *
     * @return Returns the minimum supported analog value.
     */
    @Override
    public double getMinSupportedValue() {
        return MIN_VALUE;
    }

    /**
     * Get the maximum supported analog value for the ADC implementation.
     *
     * @return Returns the maximum supported analog value.
     */
    @Override
    public double getMaxSupportedValue() {
        return MAX_VALUE;
    }
}
