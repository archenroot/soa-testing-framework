/*
 * Copyright (C) 2013 zANGETSu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.ibm.soatf.tool;

import java.util.Random;

/**
 *
 * @author zANGETSu
 */
public final class RandomGenerator {

    public final static String getRandomAlphabetical() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random(System.nanoTime());
        for (int i = 0; i < 20; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    public final static String getRandomAlphabetical(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random(System.nanoTime());
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 
     * @param length if greater than 9 return only a number between 0 and Integer.MAX_VALUE value due to constraint in XSD (xs:int upper boundary is Integer.MAX_VALUE)
     * @return 
     */
    public static int getNumeric(int length) {
        Random random = new Random(System.nanoTime());
        int maxValue;
        if(length > 9) {
            maxValue = Integer.MAX_VALUE;
        } else {
            maxValue = (int) Math.pow(10, length);
        }
        return random.nextInt(maxValue);
    }

    public final static int RandomInt() {
        return 1;
    }

    public final static Integer RandomInteger() {
        return 1;
    }

    public final static float getRandomFloat() {
        return 0;

    }

    public final static Double RandomDouble() {
        return null;

    }

    public final static long getRandomLong() {
        return 0;

    }

    public final static CharSequence getRandomCharSequence() {
        return null;

    }

    public static String getRandomAlphaNumeric(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
    
    public static final String GetRandomAscii(){
        return null;
        
    }

}
