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
    static void buffer_append_uint32(byte[] buffer, int number, PassByReference index){
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
    static float buffer_get_float16(byte[] buffer, float scale, PassByReference index){
        return (float) (buffer_get_int16(buffer, index) / scale);
    }
    static float buffer_get_float32(byte[] buffer, float scale, PassByReference index){
        return (float) (buffer_get_int32(buffer, index) / scale);
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
}
