package com.pi4j.gpio.extension.horter;

/*-
 * #%L
 * **********************************************************************
ORGANIZATION  :  Pi4J
PROJECT       :  Pi4J :: GPIO Extension
FILENAME      :  HorterDacGpioProvider.java

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

import com.pi4j.gpio.extension.base.DacGpioProvider;
import com.pi4j.gpio.extension.base.DacGpioProviderBase;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * <p>
 * This GPIO provider implements the horter DAC I2C GPIO expansion board as native Pi4J GPIO pins.
 * It is a 10-bit DAC providing 4 output channels.
 * More information about the board can be found here:
 * https://www.horter-shop.de/de/home/93-bausatz-i2c-analog-input-modul-5-kanal-10-bit-4260404260752.html
 *
 * </p>
 *
 * <p>
 * The horter DAC is connected via I2C connection to the Raspberry Pi and provides 4 analog output channels.
 * The values set are in the range [0:1023] (max 10 bit value).
 * </p>
 *
 * @see https://www.horter-shop.de/de/home/93-bausatz-i2c-analog-input-modul-5-kanal-10-bit-4260404260752.html
 * @author Christian Flaute
 */
public class HorterDacGpioProvider extends DacGpioProviderBase implements DacGpioProvider {

    public static final String NAME = "com.pi4j.gpio.extension.horter.HorterDacGpioProvider";
    public static final String DESCRIPTION = "Horter DAC GPIO Provider";

    // these addresses belong to HorterDacGpio
    public static final int HorterDacGpio_0x58 = 0x58;

    private boolean i2cBusOwner = false;
    private final I2CBus bus;
    private final I2CDevice device;

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 1023;

    public HorterDacGpioProvider(int busNumber, int address) throws UnsupportedBusNumberException, IOException {
        this(I2CFactory.getInstance(busNumber), address);
        i2cBusOwner = true;
    }

    public HorterDacGpioProvider(I2CBus bus, int address) throws UnsupportedBusNumberException, IOException {
        super(HorterDacPin.ALL);

        // set reference to I2C communications bus instance
        this.bus = bus;

        // create I2C device instance
        device = bus.getDevice(address);
    }

    @Override
    public void shutdown() {

        // prevent reentrant
        if (isShutdown()) {
            return;
        }

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
     * Set the analog output value to an output pin on the DAC immediately.
     *
     * @param pin analog output pin
     * @param value raw value to send to the DAC. (Between: 0..4095)
     */
    @Override
    public void setValue(Pin pin, double value) {

        // validate range
        if (value < getMinSupportedValue()) {
            value = getMinSupportedValue();
        } else if (value > getMaxSupportedValue()) {
            value = getMaxSupportedValue();
        }

        int intValue = (int) value;

        try {
            String pinName = pin.getName();

            byte buffer[] = new byte[3];

            if (HorterDacPin.GPIO_00.getName().equals(pinName)) {
                buffer[0] = 0x00;
            } else if (HorterDacPin.GPIO_01.getName().equals(pinName)) {
                buffer[0] = 0x01;
            } else if (HorterDacPin.GPIO_02.getName().equals(pinName)) {
                buffer[0] = 0x02;
            } else if (HorterDacPin.GPIO_03.getName().equals(pinName)) {
                buffer[0] = 0x03;
            } else {
                return;
            }

            buffer[1] = (byte) (intValue & 0xff); // lsb
            buffer[2] = (byte) ((intValue >> 8) & 0xff); // msb

            // write packet of data to the I2C bus
            device.write(buffer, 0, buffer.length);

            // update the pin cache and dispatch any events
            super.setValue(pin, value);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write DAC output value.", e);
        }
    }

    /**
     * Get the minimum supported analog value for the DAC implementation.
     *
     * @return Returns the minimum supported analog value.
     */
    @Override
    public double getMinSupportedValue() {
        return MIN_VALUE;
    }

    /**
     * Get the maximum supported analog value for the DAC implementation.
     *
     * @return Returns the maximum supported analog value.
     */
    @Override
    public double getMaxSupportedValue() {
        return MAX_VALUE;
    }
}
