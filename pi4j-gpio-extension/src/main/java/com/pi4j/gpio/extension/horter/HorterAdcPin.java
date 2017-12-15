package com.pi4j.gpio.extension.horter;

/*-
 * #%L
 * **********************************************************************
ORGANIZATION  :  Pi4J
PROJECT       :  Pi4J :: GPIO Extension
FILENAME      :  HorterAdcPin.java

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
import java.util.EnumSet;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.impl.PinImpl;

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
public class HorterAdcPin {

    public static final Pin GPIO_00 = createAnalogInputPin(0, "ANALOG INPUT 0");
    public static final Pin GPIO_01 = createAnalogInputPin(1, "ANALOG INPUT 1");
    public static final Pin GPIO_02 = createAnalogInputPin(2, "ANALOG INPUT 2");
    public static final Pin GPIO_03 = createAnalogInputPin(3, "ANALOG INPUT 3");
    public static final Pin GPIO_04 = createAnalogInputPin(4, "ANALOG INPUT 4");

    public static Pin[] ALL = { HorterAdcPin.GPIO_00, HorterAdcPin.GPIO_01, HorterAdcPin.GPIO_02, HorterAdcPin.GPIO_03,
            HorterAdcPin.GPIO_04 };

    private static Pin createAnalogInputPin(int channel, String name) {
        return new PinImpl(HorterAdcGpioProvider.NAME, channel, name, EnumSet.of(PinMode.ANALOG_INPUT));
    }
}
