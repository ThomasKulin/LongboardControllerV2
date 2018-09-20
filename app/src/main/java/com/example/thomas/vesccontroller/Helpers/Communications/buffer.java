package com.example.thomas.vesccontroller.Helpers.Communications;

/**
 * Created by Thomas on 2018-03-16.
 */

public class buffer {

    //-----------------------------------SEND------------------------------------------

    static void buffer_append_int16(byte[] buffer, int number, PassByReference index){
        short num = (short)(number & 0xFFFF);
        buffer[(index.tempInt)++] = (byte)(num >> 8);
        buffer[(index.tempInt)++] = (byte)(num & 0xFF);
    }
    static void buffer_append_uint16(byte[] buffer, int number, PassByReference index){
        short num = (short)(number & 0xFFFF);
        buffer[(index.tempInt)++] = (byte)((num >> 8) & 0xFF);
        buffer[(index.tempInt)++] = (byte)(num & 0xFF);
    }
    static void buffer_append_int32(byte[] buffer, int number, PassByReference index){
        buffer[(index.tempInt)++] = (byte)((number >> 24) & 0xFF);
        buffer[(index.tempInt)++] = (byte)((number >> 16) & 0xFF);
        buffer[(index.tempInt)++] = (byte)((number >> 8) & 0xFF);
        buffer[(index.tempInt)++] = (byte)(number & 0xFF);
    }
    static void buffer_append_uint32(byte[] buffer, long number, PassByReference index){
        buffer[(index.tempInt)++] = (byte)((number >> 24) & 0xFF);
        buffer[(index.tempInt)++] = (byte)((number >> 16) & 0xFF);
        buffer[(index.tempInt)++] = (byte)((number >> 8) & 0xFF);
        buffer[(index.tempInt)++] = (byte)(number & 0xFF);
    }
    static void buffer_append_float16(byte[] buffer, float number, float scale, PassByReference index){
        short num = (short)(((int)(number*scale)) & 0xFFFF);
        buffer_append_int16(buffer, num, index);
    }
    static void buffer_append_float32(byte[] buffer, float number, float scale, PassByReference index){
        int num = (int)(number*scale);
        buffer_append_int32(buffer, num, index);
    }
    static void buffer_append_float32_auto(byte[] buffer, float number, PassByReference index) {
        FRexpResult result = frexp(number);
        int e = result.exponent;
        float sig = result.mantissa;
        float sig_abs = Math.abs(sig);
        long sig_i = 0;  //uint32_t

        if (sig_abs >= 0.5) {
            sig_i = (long)((sig_abs - 0.5f) * 2.0f * 8388608.0f);
            e += 126;
        }

        long res = ((e & 0xFF) << 23) | (sig_i & 0x7FFFFF); //uint32_t
        if (sig < 0) {
            res |= 1 << 31;
        }

        buffer_append_uint32(buffer, (int)res, index);
    }
    static void buffer_append_bool(byte[] buffer, boolean value, PassByReference index) {
        if (value == true)
        {
            buffer[index.tempInt] = 1;
            (index.tempInt)++;
        }
        else
        {
            buffer[index.tempInt] = 0;
            (index.tempInt)++;
        }
    }

    //-------------------------------------------------------------------------------------


    //-----------------------------------RECEIVE------------------------------------------

    static int buffer_get_int16(byte[] buffer, PassByReference index){
        short pt1 = (short) ((((short) buffer[index.tempInt]) << 8)&0xFF00); //shift 8 bits to front of short then discard anything (should be nothing but fuck it) in the lower 8 bits
        short pt2 = ((short) (buffer[index.tempInt + 1] & 0xFF)); //convert to short then discard any leading 1's to avoid sign extension
        int res = pt1 | pt2;
        index.tempInt += 2;
        return res;
    }
    static int buffer_get_int32(byte[] buffer, PassByReference index){
        int res = (((buffer[index.tempInt]) << 24) & 0xFF000000) | (((buffer[index.tempInt + 1]) << 16) & 0x00FF0000)|
                (((buffer[index.tempInt + 2]) << 8) & 0x0000FF00) | ((buffer[index.tempInt + 3])& 0x000000FF);
        index.tempInt += 4;
        return res;
    }
    static long buffer_get_uint32(byte[] buffer, PassByReference index){
        long res = (((buffer[index.tempInt]) << 24) & 0xFF000000) | (((buffer[index.tempInt + 1]) << 16) & 0x00FF0000)|
                (((buffer[index.tempInt + 2]) << 8) & 0x0000FF00) | ((buffer[index.tempInt + 3])& 0x000000FF);
        index.tempInt += 4;
        return res;
    }
    static float buffer_get_float16(byte[] buffer, float scale, PassByReference index){
        return (float) (buffer_get_int16(buffer, index) / scale);
    }
    static float buffer_get_float32(byte[] buffer, float scale, PassByReference index){
        return (float) (buffer_get_int32(buffer, index) / scale);
    }
    static float buffer_get_float32_auto(byte[] buffer, PassByReference index) {
        int res = buffer_get_int32(buffer, index);

        int e = (res >> 23) & 0xFF;
        long sig_i = res & 0x7FFFFF;
        boolean neg;

        if((res & (1 << 31))==0)
            neg = false;
        else
            neg = true;

        float sig = (float) 0.0;
        if (e != 0 || sig_i != 0) {
            sig = (float) ((float)sig_i / (8388608.0 * 2.0) + 0.5);
            e -= 126;
        }

        if (neg) {
            sig = -sig;
        }

        return (float)(sig * Math.pow(2,e));
    }
    static boolean buffer_get_bool(byte[] buffer, PassByReference index) {

        if (buffer[index.tempInt] == 1)
        {
            (index.tempInt)++;
            return true;
        }
		else
        {
            (index.tempInt)++;
            return false;
        }
    }


    //-----------------------Helper Functions-------------------------------------------------------
    public static class FRexpResult {
        public int exponent = 0;
        public float mantissa = (float) 0.;
    }

    public static FRexpResult frexp(float value) {
        final FRexpResult result = new FRexpResult();
        long bits = (long) Float.floatToIntBits(value);
        float realMant = (float) 1.;

        // Test for NaN, infinity, and zero.
        if (Float.isNaN(value) || value + value == value || Float.isInfinite(value)) {
            result.exponent = 0;
            result.mantissa = value;
        }
        else {
            boolean neg = (bits < 0);
            int exponent = (int)((bits >> 23) & 0xFFL);
            long mantissa = bits & 0x3FFFFFL;

            if(exponent == 0) {
                exponent++;
            }
            else {
                mantissa = mantissa | (1L<<23);
            }

            // bias the exponent - actually biased by 1023.
            // we are treating the mantissa as m.0 instead of 0.m
            //  so subtract another 52.
            exponent -= 150;
            realMant = mantissa;
            // normalize
            while(realMant >= 1.0) {
                mantissa >>= 1;
                realMant /= 2.;
                exponent++;
            }
            if(neg) {
                realMant = realMant * -1;
            }
            result.exponent = exponent;
            result.mantissa = realMant;
        }
        return result;
    }
}
