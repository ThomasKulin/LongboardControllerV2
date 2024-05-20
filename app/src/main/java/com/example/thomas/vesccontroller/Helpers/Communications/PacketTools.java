package com.example.thomas.vesccontroller.Helpers.Communications;

import android.util.Log;

import com.example.thomas.vesccontroller.Activities.Board_Activity;

import static android.content.ContentValues.TAG;
import static com.example.thomas.vesccontroller.Helpers.Communications.buffer.buffer_append_bool;
import static com.example.thomas.vesccontroller.Helpers.Communications.buffer.buffer_append_float32_auto;
import static com.example.thomas.vesccontroller.Helpers.Communications.buffer.buffer_append_int32;
import static com.example.thomas.vesccontroller.Helpers.Communications.buffer.buffer_append_uint32;
import static com.example.thomas.vesccontroller.Helpers.Communications.buffer.buffer_get_bool;
import static com.example.thomas.vesccontroller.Helpers.Communications.buffer.buffer_get_float32;
import static com.example.thomas.vesccontroller.Helpers.Communications.buffer.buffer_get_float32_auto;
import static com.example.thomas.vesccontroller.Helpers.Communications.buffer.buffer_get_int32;
import static com.example.thomas.vesccontroller.Helpers.Communications.buffer.buffer_get_uint32;


/**
 * Created by Thomas on 2018-03-15.
 */

public class PacketTools {

    static byte[] payload = new byte[1024];

    public static boolean unpackPayload(int[] message, int lenMes, int lenPay){
        int crcMessage = 0;
        int crcPayload = 0;
        byte srcPos = 2;

        //Rebuild src:
        crcMessage = message[lenMes-3] << 8;
        crcMessage &= 0xFF00;
        crcMessage += message[lenMes -2];
        Log.d(TAG, "PacketTools: SRC received: " + crcMessage);

        //Convert int type to byte
        byte[] message_byte = new byte[message.length];
        for(int temp = 0; temp < message.length; temp++ ) {
            message_byte[temp] = (byte) message[temp];
        }

        //Extract payload:
        if(message[0] == 2){
            srcPos = 2;
        }
        else if(message[0] ==3){
            srcPos = 3;
        }
        else{
            return false; //not a valid state
        }
        System.arraycopy(message_byte, srcPos, payload, 0, lenPay);
        crcPayload = crc.crc16(payload, lenPay);

        if(crcPayload == crcMessage){
            return true;
        }
        else{
            return false;
        }
    }

    public static Object processReadPacket(){
         //packet ID is first byte in payload
        PassByReference index = new PassByReference(0); //allows us to change value of index from within buffer functions
        byte[] payload2 = new byte[1024];
        System.arraycopy(payload, 1, payload2, 0, payload.length-1);

        switch (getPacketId()){
            case COMM_GET_VALUES: { //packet contains value data
                mc_values values = new mc_values();
                values.temp_mos = buffer.buffer_get_float16(payload2, (float) 1e1, index);
                values.temp_motor = buffer.buffer_get_float16(payload2, (float) 1e1, index);
                values.current_motor = buffer_get_float32(payload2, (float) 1e2, index);
                values.current_in = buffer_get_float32(payload2, (float) 1e2, index);
                values.avg_id = buffer_get_float32(payload2, (float) 1e2, index);
                values.avg_iq = buffer_get_float32(payload2, (float) 1e2, index);
                values.duty_now = buffer.buffer_get_float16(payload2, (float) 1e3, index);
                values.rpm = buffer_get_int32(payload2, index);
                values.v_in = buffer.buffer_get_float16(payload2, (float) 1e1, index);
                values.amp_hours = buffer_get_float32(payload2, (float) 1e4, index);
                values.amp_hours_charged = buffer_get_float32(payload2, (float) 1e4, index);
                values.watt_hours = buffer_get_float32(payload2, (float) 1e4, index);
                values.watt_hours_charged = buffer_get_float32(payload2, (float) 1e4, index);
                values.tachometer = buffer_get_int32(payload2, index);
                values.tachometer_abs = buffer_get_int32(payload2, index);
                values.fault_code = mc_fault_code.values()[payload2[index.tempInt++]];
                values.pid_pos = buffer_get_float32(payload2, (float) 1e6, index);
                values.controller_id = payload2[index.tempInt++];
                resetPacket();
                return values;
            }
            case COMM_GET_STATES: { //packet contains value data
                mc_states states = new mc_states();
                states.phi = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                states.phi_dot = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                states.psi = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                states.psi_dot = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                states.theta = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                states.theta_dot = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                states.hdg = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                states.hdg_dot = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                resetPacket();
                return states;
            }
            case COMM_GET_MCCONF: {
                mc_configuration configuration = new mc_configuration();
                configuration.pwm_mode = mc_pwm_mode.values()[payload2[index.tempInt++]];
                configuration.comm_mode = mc_comm_mode.values()[payload2[index.tempInt++]];
                configuration.motor_type = mc_motor_type.values()[payload2[index.tempInt++]];
                configuration.sensor_mode = mc_sensor_mode.values()[payload2[index.tempInt++]];

                configuration.l_current_max = buffer_get_float32_auto(payload2, index);
                configuration.l_current_min = buffer_get_float32_auto(payload2, index);
                configuration.l_in_current_max = buffer_get_float32_auto(payload2, index);
                configuration.l_in_current_min = buffer_get_float32_auto(payload2, index);
                configuration.l_abs_current_max = buffer_get_float32_auto(payload2,index);
                configuration.l_min_erpm = buffer_get_float32_auto(payload2, index);
                configuration.l_max_erpm = buffer_get_float32_auto(payload2, index);
                configuration.l_erpm_start = buffer_get_float32_auto(payload2, index);
                configuration.l_max_erpm_fbrake = buffer_get_float32_auto(payload2, index);
                configuration.l_max_erpm_fbrake_cc = buffer_get_float32_auto(payload2, index);
                configuration.l_min_vin = buffer_get_float32_auto(payload2, index);
                configuration.l_max_vin = buffer_get_float32_auto(payload2, index);
                configuration.l_battery_cut_start = buffer_get_float32_auto(payload2, index);
                configuration.l_battery_cut_end = buffer_get_float32_auto(payload2, index);
                configuration.l_slow_abs_current = buffer_get_bool(payload2, index);
                configuration.l_temp_fet_start = buffer_get_float32_auto(payload2, index);
                configuration.l_temp_fet_end = buffer_get_float32_auto(payload2, index);
                configuration.l_temp_motor_start = buffer_get_float32_auto(payload2, index);
                configuration.l_temp_motor_end = buffer_get_float32_auto(payload2, index);
                configuration.l_temp_accel_dec = buffer_get_float32_auto(payload2, index);
                configuration.l_min_duty = buffer_get_float32_auto(payload2,index);
                configuration.l_max_duty = buffer_get_float32_auto(payload2, index);
                configuration.l_watt_max = buffer_get_float32_auto(payload2, index);
                configuration.l_watt_min = buffer_get_float32_auto(payload2, index);

                configuration.sl_min_erpm = buffer_get_float32_auto(payload2, index);
                configuration.sl_min_erpm_cycle_int_limit = buffer_get_float32_auto(payload2, index);
                configuration.sl_max_fullbreak_current_dir_change = buffer_get_float32_auto(payload2, index);
                configuration.sl_cycle_int_limit = buffer_get_float32_auto(payload2, index);
                configuration.sl_phase_advance_at_br = buffer_get_float32_auto(payload2, index);
                configuration.sl_cycle_int_rpm_br = buffer_get_float32_auto(payload2, index);
                configuration.sl_bemf_coupling_k = buffer_get_float32_auto(payload2, index);

                System.arraycopy(payload2, index.tempInt, configuration.hall_table,0,8);
                index.tempInt += 8;
                configuration.hall_sl_erpm = buffer_get_float32_auto(payload2, index);

                configuration.foc_current_kp = buffer_get_float32_auto(payload2, index);
                configuration.foc_current_ki = buffer_get_float32_auto(payload2, index);
                configuration.foc_f_sw = buffer_get_float32_auto(payload2,index);
                configuration.foc_dt_us = buffer_get_float32_auto(payload2,index);
                configuration.foc_encoder_inverted = buffer_get_bool(payload2, index);
                configuration.foc_encoder_offset = buffer_get_float32_auto(payload2, index);
                configuration.foc_encoder_ratio = buffer_get_float32_auto(payload2, index);
                configuration.foc_sensor_mode = mc_foc_sensor_mode.values()[payload2[index.tempInt++]];
                configuration.foc_pll_kp = buffer_get_float32_auto(payload2, index);
                configuration.foc_pll_ki = buffer_get_float32_auto(payload2, index);
                configuration.foc_motor_l = buffer_get_float32_auto(payload2, index);
                configuration.foc_motor_r = buffer_get_float32_auto(payload2, index);
                configuration.foc_motor_flux_linkage = buffer_get_float32_auto(payload2, index);
                configuration.foc_observer_gain = buffer_get_float32_auto(payload2, index);
                configuration.foc_observer_gain_slow = buffer_get_float32_auto(payload2, index);
                configuration.foc_duty_dowmramp_kp = buffer_get_float32_auto(payload2, index);
                configuration.foc_duty_dowmramp_ki = buffer_get_float32_auto(payload2, index);
                configuration.foc_openloop_rpm = buffer_get_float32_auto(payload2, index);
                configuration.foc_sl_openloop_hyst = buffer_get_float32_auto(payload2, index);
                configuration.foc_sl_openloop_time = buffer_get_float32_auto(payload2, index);
                configuration.foc_sl_d_current_duty = buffer_get_float32_auto(payload2, index);
                configuration.foc_sl_d_current_factor = buffer_get_float32_auto(payload2, index);
//                System.arraycopy(payload2, index.tempInt, configuration.foc_hall_table,0,8);
//                index.tempInt += 8;
                for(int j = 0; j<configuration.foc_hall_table.length;j++){ //must remove sign extension
                    configuration.foc_hall_table[j] = (short)(((short)payload2[index.tempInt++])&0xFF);
                }
                configuration.foc_sl_erpm = buffer_get_float32_auto(payload2, index);
                configuration.foc_sample_v0_v7 = buffer_get_bool(payload2, index);
                configuration.foc_sample_high_current = buffer_get_bool(payload2, index);
                configuration.foc_sat_comp = buffer_get_float32_auto(payload2, index);
                configuration.foc_temp_comp = buffer_get_bool(payload2, index);
                configuration.foc_temp_comp_base_temp = buffer_get_float32_auto(payload2, index);
                configuration.foc_current_filter_const = buffer_get_float32_auto(payload2, index);

                configuration.s_pid_kp = buffer_get_float32_auto(payload2, index);
                configuration.s_pid_ki = buffer_get_float32_auto(payload2, index);
                configuration.s_pid_kd = buffer_get_float32_auto(payload2, index);
                configuration.s_pid_kd_filter = buffer_get_float32_auto(payload2, index);
                configuration.s_pid_min_erpm = buffer_get_float32_auto(payload2, index);
                configuration.s_pid_allow_braking = buffer_get_bool(payload2, index);

                configuration.p_pid_kp = buffer_get_float32_auto(payload2, index);
                configuration.p_pid_ki = buffer_get_float32_auto(payload2, index);
                configuration.p_pid_kd = buffer_get_float32_auto(payload2,index);
                configuration.p_pid_kd_filter = buffer_get_float32_auto(payload2, index);
                configuration.p_pid_ang_div = buffer_get_float32_auto(payload2, index);

                configuration.cc_startup_boost_duty = buffer_get_float32_auto(payload2, index);
                configuration.cc_min_current = buffer_get_float32_auto(payload2, index);
                configuration.cc_gain = buffer_get_float32_auto(payload2, index);
                configuration.cc_ramp_step_max = buffer_get_float32_auto(payload2, index);

                configuration.m_fault_stop_time_ms = buffer_get_int32(payload2, index);
                configuration.m_duty_ramp_step = buffer_get_float32_auto(payload2, index);
                configuration.m_current_backoff_gain = buffer_get_float32_auto(payload2, index);
                configuration.m_encoder_counts = buffer_get_uint32(payload2, index);
                configuration.m_sensor_port_mode = sensor_port_mode.values()[payload2[index.tempInt++]];
                configuration.m_invert_direction = buffer_get_bool(payload2, index);
                configuration.m_drv8301_oc_mode = drv8301_oc_mode.values()[payload2[index.tempInt++]];
                configuration.m_drv8301_oc_adj = payload2[index.tempInt++];
                configuration.m_bldc_f_sw_min = buffer_get_float32_auto(payload2,index);
                configuration.m_bldc_f_sw_max = buffer_get_float32_auto(payload2, index);
                configuration.m_dc_f_sw = buffer_get_float32_auto(payload2,index);
                configuration.m_ntc_motor_beta = buffer_get_float32_auto(payload2, index);
                resetPacket();
                return configuration;
            }
            default:
                resetPacket();
                mc_values values = new mc_values();
                values.fault_code = mc_fault_code.FAULT_CODE_NO_DATA;
                return values;
        }
    }

    public static COMM_PACKET_ID getPacketId() {
        COMM_PACKET_ID packetId;
        packetId = COMM_PACKET_ID.values()[payload[0]];
        return packetId;
    }

    static int packSendPayload(byte[] payload, int lenPay){
        int crcPayload = crc.crc16(payload, lenPay);
        int messagelength = lenPay+6;
        int count = 0;
        byte[] messageSend = new byte[messagelength];

        if(lenPay <= 256){ //packet is less than 256 bytes long
            messageSend[count++] = 2;
            messageSend[count++] = (byte)lenPay; //lenPay must be below 2^8 or 256
        }
        else{ //packet is greater than 256 bytes long
            messageSend[count++] = 3;
            messageSend[count++] = (byte)(lenPay >> 8);
            messageSend[count++] = (byte)(lenPay & 0xFF);
        }
        System.arraycopy(payload, 0, messageSend, count, lenPay); //copy payload into messagesend

        count += lenPay;
        messageSend[count++] = (byte)(crcPayload >> 8);
        messageSend[count++] = (byte)(crcPayload & 0xFF);
        messageSend[count++] = 3;
        //messageSend[count] = 0;

        //-- Write to Bluetooth--

        Board_Activity.writeBT(messageSend, 0, count);

        //-----------------------
        return count;
    }

    public static void vescUartGetValue(COMM_PACKET_ID packet_id){
        PassByReference index = new PassByReference(0);
        byte[] command;

        switch (packet_id) {
            case COMM_GET_VALUES:
                Board_Activity.VescSelect = 0;  //master
                command = new byte[1];
                command[0] = (byte) COMM_PACKET_ID.COMM_GET_VALUES.ordinal();
                packSendPayload(command, 1);
                break;
            case COMM_FORWARD_CAN:
                Board_Activity.VescSelect = 1; //slave
                command = new byte[3];
                command[index.tempInt++] = (byte) COMM_PACKET_ID.COMM_FORWARD_CAN.ordinal();
                command[index.tempInt++] = 2;  //hard coding for slave CAN ID = 2
                command[2] = (byte) COMM_PACKET_ID.COMM_GET_VALUES.ordinal();
                packSendPayload(command, 3);
                break;
            case COMM_GET_MCCONF:
                command = new byte[1];
                command[0] = (byte) COMM_PACKET_ID.COMM_GET_MCCONF.ordinal();
                packSendPayload(command, 1);
                break;
        }
    }

    public static void vescUartSetValue(Object value, COMM_PACKET_ID type){
        PassByReference index = new PassByReference(0);
        int len;
        byte[] payload;

        switch (type) {
            case COMM_SET_DUTY:
                break;
            case COMM_SET_CURRENT: {
                len = 5;
                payload = new byte[len];
                payload[index.tempInt++] = (byte) COMM_PACKET_ID.COMM_SET_CURRENT.ordinal();
                buffer_append_int32(payload, (int) ((float) value * 1000), index);
                packSendPayload(payload, len);
                break;
            }
            case COMM_FORWARD_CAN: {
                len = 7;
                payload = new byte[len];
                payload[index.tempInt++] = (byte) COMM_PACKET_ID.COMM_FORWARD_CAN.ordinal();
                payload[index.tempInt++] = 2;  //hard coding for slave CAN ID = 2

                payload[index.tempInt++] = (byte) COMM_PACKET_ID.COMM_SET_CURRENT.ordinal();
                buffer_append_int32(payload, (int) ((float) value * 1000), index);
                packSendPayload(payload, len);
                break;
            }
            case COMM_SET_CURRENT_BRAKE: {
                len = 5;
                payload = new byte[len];
                payload[index.tempInt++] = (byte) COMM_PACKET_ID.COMM_SET_CURRENT_BRAKE.ordinal();
                buffer_append_int32(payload, (int) ((float) value * 1000), index);
                packSendPayload(payload, len);
                break;
            }
            case COMM_SET_RPM:
                break;
            case COMM_SET_SERVO_POS:
                break;
            case COMM_SET_MCCONF: {
                mc_configuration values = (mc_configuration) value;
                len = 450;
                payload = new byte[len];

                payload[index.tempInt++] = (byte) COMM_PACKET_ID.COMM_SET_MCCONF.ordinal();

                payload[index.tempInt++] = (byte) values.pwm_mode.ordinal();
                payload[index.tempInt++] = (byte) values.comm_mode.ordinal();
                payload[index.tempInt++] = (byte) values.motor_type.ordinal();
                payload[index.tempInt++] = (byte) values.sensor_mode.ordinal();

                buffer_append_float32_auto(payload, values.l_current_max, index);
                buffer_append_float32_auto(payload, values.l_current_min, index);
                buffer_append_float32_auto(payload, values.l_in_current_max, index);
                buffer_append_float32_auto(payload, values.l_in_current_min, index);
                buffer_append_float32_auto(payload, values.l_abs_current_max, index);
                buffer_append_float32_auto(payload, values.l_min_erpm, index);
                buffer_append_float32_auto(payload, values.l_max_erpm, index);
                buffer_append_float32_auto(payload, values.l_erpm_start, index);
                buffer_append_float32_auto(payload, values.l_max_erpm_fbrake, index);
                buffer_append_float32_auto(payload, values.l_max_erpm_fbrake_cc, index);
                buffer_append_float32_auto(payload, values.l_min_vin, index);
                buffer_append_float32_auto(payload, values.l_max_vin, index);
                buffer_append_float32_auto(payload, values.l_battery_cut_start, index);
                buffer_append_float32_auto(payload, values.l_battery_cut_end, index);
                buffer_append_bool(payload, values.l_slow_abs_current, index);
                buffer_append_float32_auto(payload, values.l_temp_fet_start, index);
                buffer_append_float32_auto(payload, values.l_temp_fet_end, index);
                buffer_append_float32_auto(payload, values.l_temp_motor_start, index);
                buffer_append_float32_auto(payload, values.l_temp_motor_end, index);
                buffer_append_float32_auto(payload, values.l_temp_accel_dec, index);
                buffer_append_float32_auto(payload, values.l_min_duty, index);
                buffer_append_float32_auto(payload, values.l_max_duty, index);
                buffer_append_float32_auto(payload, values.l_watt_max, index);
                buffer_append_float32_auto(payload, values.l_watt_min, index);

                buffer_append_float32_auto(payload, values.sl_min_erpm, index);
                buffer_append_float32_auto(payload, values.sl_min_erpm_cycle_int_limit, index);
                buffer_append_float32_auto(payload, values.sl_max_fullbreak_current_dir_change, index);
                buffer_append_float32_auto(payload, values.sl_cycle_int_limit, index);
                buffer_append_float32_auto(payload, values.sl_phase_advance_at_br, index);
                buffer_append_float32_auto(payload, values.sl_cycle_int_rpm_br, index);
                buffer_append_float32_auto(payload, values.sl_bemf_coupling_k, index);

                System.arraycopy(values.hall_table,0, payload, index.tempInt,8);
                index.tempInt += 8;
                buffer_append_float32_auto(payload, values.hall_sl_erpm, index);

                buffer_append_float32_auto(payload, values.foc_current_kp, index);
                buffer_append_float32_auto(payload, values.foc_current_ki, index);
                buffer_append_float32_auto(payload, values.foc_f_sw, index);
                buffer_append_float32_auto(payload, values.foc_dt_us, index);
                buffer_append_bool(payload, values.foc_encoder_inverted, index);
                buffer_append_float32_auto(payload, values.foc_encoder_offset, index);
                buffer_append_float32_auto(payload, values.foc_encoder_ratio, index);
                payload[index.tempInt++] = (byte) values.foc_sensor_mode.ordinal();
                buffer_append_float32_auto(payload, values.foc_pll_kp, index);
                buffer_append_float32_auto(payload, values.foc_pll_ki, index);
                buffer_append_float32_auto(payload, values.foc_motor_l, index);
                buffer_append_float32_auto(payload, values.foc_motor_r, index);
                buffer_append_float32_auto(payload, values.foc_motor_flux_linkage, index);
                buffer_append_float32_auto(payload, values.foc_observer_gain, index);
                buffer_append_float32_auto(payload, values.foc_observer_gain_slow, index);
                buffer_append_float32_auto(payload, values.foc_duty_dowmramp_kp, index);
                buffer_append_float32_auto(payload, values.foc_duty_dowmramp_ki, index);
                buffer_append_float32_auto(payload, values.foc_openloop_rpm, index);
                buffer_append_float32_auto(payload, values.foc_sl_openloop_hyst, index);
                buffer_append_float32_auto(payload, values.foc_sl_openloop_time, index);
                buffer_append_float32_auto(payload, values.foc_sl_d_current_duty, index);
                buffer_append_float32_auto(payload, values.foc_sl_d_current_factor, index);

                for(int j = 0; j<values.foc_hall_table.length;j++){ //must remove sign extension
                    values.foc_hall_table[j] = (short)(((short)payload[index.tempInt++])&0xFF);
                    payload[index.tempInt++] = (byte) (values.foc_hall_table[j] & 0xFF);
                }

                buffer_append_float32_auto(payload, values.foc_sl_erpm, index);
                buffer_append_bool(payload, values.foc_sample_v0_v7, index);
                buffer_append_bool(payload, values.foc_sample_high_current, index);
                buffer_append_float32_auto(payload, values.foc_sat_comp, index);
                buffer_append_bool(payload, values.foc_temp_comp, index);
                buffer_append_float32_auto(payload, values.foc_temp_comp_base_temp, index);
                buffer_append_float32_auto(payload, values.foc_current_filter_const, index);

                buffer_append_float32_auto(payload, values.s_pid_kp, index);
                buffer_append_float32_auto(payload, values.s_pid_ki, index);
                buffer_append_float32_auto(payload, values.s_pid_kd, index);
                buffer_append_float32_auto(payload, values.s_pid_kd_filter, index);
                buffer_append_float32_auto(payload, values.s_pid_min_erpm, index);
                buffer_append_bool(payload, values.s_pid_allow_braking, index);

                buffer_append_float32_auto(payload, values.p_pid_kp, index);
                buffer_append_float32_auto(payload, values.p_pid_ki, index);
                buffer_append_float32_auto(payload, values.p_pid_kd, index);
                buffer_append_float32_auto(payload, values.p_pid_kd_filter, index);
                buffer_append_float32_auto(payload, values.p_pid_ang_div, index);

                buffer_append_float32_auto(payload, values.cc_startup_boost_duty, index);
                buffer_append_float32_auto(payload, values.cc_min_current, index);
                buffer_append_float32_auto(payload, values.cc_gain, index);
                buffer_append_float32_auto(payload, values.cc_ramp_step_max, index);

                buffer_append_int32(payload, values.m_fault_stop_time_ms, index);
                buffer_append_float32_auto(payload, values.m_duty_ramp_step, index);
                buffer_append_float32_auto(payload, values.m_current_backoff_gain, index);
                buffer_append_uint32(payload, values.m_encoder_counts, index);
                payload[index.tempInt++] = (byte) values.m_sensor_port_mode.ordinal();
                buffer_append_bool(payload, values.m_invert_direction, index);
                payload[index.tempInt++] = (byte) values.m_drv8301_oc_mode.ordinal();
                payload[index.tempInt++] = (byte) values.m_drv8301_oc_adj;
                buffer_append_float32_auto(payload, values.m_bldc_f_sw_min, index);
                buffer_append_float32_auto(payload, values.m_bldc_f_sw_max, index);
                buffer_append_float32_auto(payload, values.m_dc_f_sw, index);
                buffer_append_float32_auto(payload, values.m_ntc_motor_beta, index);
                packSendPayload(payload,index.tempInt+1);
                break;
            }
            case COMM_SET_APPCONF:
                break;
            case COMM_REBOOT:
                break;
            case COMM_SET_CHUCK_DATA:
                break;
            case COMM_CUSTOM_APP_DATA:
                break;
        }
    }

    private static void resetPacket(){
        for(int i = 0; i < 255; i++){
            payload[i] = 0;
        }
    }



    public enum mc_state{
        MC_STATE_OFF,
        MC_STATE_DETECTING ,
        MC_STATE_RUNNING,
        MC_STATE_FULL_BRAKE,
    }

    public enum mc_pwm_mode{
        PWM_MODE_NONSYNCHRONOUS_HISW , // This mode is not recommended
                PWM_MODE_SYNCHRONOUS, // The recommended and most tested mode
                PWM_MODE_BIPOLAR // Some glitches occasionally, can kill MOSFETs
    }

    public enum mc_comm_mode{
        COMM_MODE_INTEGRATE ,
                COMM_MODE_DELAY
    }

    public enum mc_sensor_mode{
        SENSOR_MODE_SENSORLESS ,
                SENSOR_MODE_SENSORED,
                SENSOR_MODE_HYBRID
    }

    public enum mc_foc_sensor_mode{
        FOC_SENSOR_MODE_SENSORLESS ,
                FOC_SENSOR_MODE_ENCODER,
                FOC_SENSOR_MODE_HALL
    }

    public enum mc_motor_type{
        MOTOR_TYPE_BLDC ,
                MOTOR_TYPE_DC,
                MOTOR_TYPE_FOC
    }

    public enum mc_fault_code{
        FAULT_CODE_NONE ,
                FAULT_CODE_OVER_VOLTAGE,
                FAULT_CODE_UNDER_VOLTAGE,
                FAULT_CODE_DRV8302,
                FAULT_CODE_ABS_OVER_CURRENT,
                FAULT_CODE_OVER_TEMP_FET,
                FAULT_CODE_OVER_TEMP_MOTOR,
                FAULT_CODE_NO_DATA
    }

    public enum mc_control_mode{
        CONTROL_MODE_DUTY ,
                CONTROL_MODE_SPEED,
                CONTROL_MODE_CURRENT,
                CONTROL_MODE_CURRENT_BRAKE,
                CONTROL_MODE_POS,
                CONTROL_MODE_NONE
    }

    public enum disp_pos_mode{
        DISP_POS_MODE_NONE ,
                DISP_POS_MODE_INDUCTANCE,
                DISP_POS_MODE_OBSERVER,
                DISP_POS_MODE_ENCODER,
                DISP_POS_MODE_PID_POS,
                DISP_POS_MODE_PID_POS_ERROR,
                DISP_POS_MODE_ENCODER_OBSERVER_ERROR
    }

    public enum sensor_port_mode{
        SENSOR_PORT_MODE_HALL ,
                SENSOR_PORT_MODE_ABI,
                SENSOR_PORT_MODE_AS5047_SPI
    }

    class mc_rpm_dep_struct{
        float cycle_int_limit;
        float cycle_int_limit_running;
        float cycle_int_limit_max;
        float comm_time_sum;
        float comm_time_sum_min_rpm;
        int comms;
        int time_at_comm; //uint32_t
    }

    public enum drv8301_oc_mode{
        DRV8301_OC_LIMIT,
                DRV8301_OC_LATCH_SHUTDOWN,
                DRV8301_OC_REPORT_ONLY,
                DRV8301_OC_DISABLED
    }

    public enum debug_sampling_mode{
        DEBUG_SAMPLING_OFF,
                DEBUG_SAMPLING_NOW,
                DEBUG_SAMPLING_START,
                DEBUG_SAMPLING_TRIGGER_START,
                DEBUG_SAMPLING_TRIGGER_FAULT,
                DEBUG_SAMPLING_TRIGGER_START_NOSEND,
                DEBUG_SAMPLING_TRIGGER_FAULT_NOSEND,
                DEBUG_SAMPLING_SEND_LAST_SAMPLES
    }

    public enum CAN_BAUD{
        CAN_BAUD_125K,
                CAN_BAUD_250K,
                CAN_BAUD_500K,
                CAN_BAUD_1M
    }

    public static class mc_configuration{
        // Switching and drive
        public mc_pwm_mode pwm_mode;
        public mc_comm_mode comm_mode;
        public mc_motor_type motor_type;
        public mc_sensor_mode sensor_mode;
        // Limits
        public float l_current_max;
        public float l_current_min;
        public float l_in_current_max;
        public float l_in_current_min;
        public float l_abs_current_max;
        public float l_min_erpm;
        public float l_max_erpm;
        public float l_erpm_start;
        public float l_max_erpm_fbrake;
        public float l_max_erpm_fbrake_cc;
        public float l_min_vin;
        public float l_max_vin;
        public float l_battery_cut_start;
        public float l_battery_cut_end;
        public boolean l_slow_abs_current;
        public float l_temp_fet_start;
        public float l_temp_fet_end;
        public float l_temp_motor_start;
        public float l_temp_motor_end;
        public float l_temp_accel_dec;
        public float l_min_duty;
        public float l_max_duty;
        public float l_watt_max;
        public float l_watt_min;
        // Overridden limits (Computed during runtime)
        public float lo_current_max;
        public float lo_current_min;
        public float lo_in_current_max;
        public float lo_in_current_min;
        public float lo_current_motor_max_now;
        public float lo_current_motor_min_now;
        // Sensorless
        public float sl_min_erpm;
        public float sl_min_erpm_cycle_int_limit;
        public float sl_max_fullbreak_current_dir_change;
        public float sl_cycle_int_limit;
        public float sl_phase_advance_at_br;
        public float sl_cycle_int_rpm_br;
        public float sl_bemf_coupling_k;
        // Hall sensor
        public byte[] hall_table = new byte[8]; //int8_t
        public float hall_sl_erpm;
        // FOC
        public float foc_current_kp;
        public float foc_current_ki;
        public float foc_f_sw;
        public float foc_dt_us;
        public float foc_encoder_offset;
        public boolean foc_encoder_inverted;
        public float foc_encoder_ratio;
        public float foc_motor_l;
        public float foc_motor_r;
        public float foc_motor_flux_linkage;
        public float foc_observer_gain;
        public float foc_observer_gain_slow;
        public float foc_pll_kp;
        public float foc_pll_ki;
        public float foc_duty_dowmramp_kp;
        public float foc_duty_dowmramp_ki;
        public float foc_openloop_rpm;
        public float foc_sl_openloop_hyst;
        public float foc_sl_openloop_time;
        public float foc_sl_d_current_duty;
        public float foc_sl_d_current_factor;
        mc_foc_sensor_mode foc_sensor_mode;
        public short[] foc_hall_table = new short[8]; //uint8_t
        public float foc_sl_erpm;
        public boolean foc_sample_v0_v7;
        public boolean foc_sample_high_current;
        public float foc_sat_comp;
        public boolean foc_temp_comp;
        public float foc_temp_comp_base_temp;
        public float foc_current_filter_const;
        // Speed PID
        public float s_pid_kp;
        public float s_pid_ki;
        public float s_pid_kd;
        public float s_pid_min_erpm;
        public float s_pid_kd_filter;
        public boolean s_pid_allow_braking;
        // Pos PID
        public float p_pid_kp;
        public float p_pid_ki;
        public float p_pid_kd;
        public float p_pid_kd_filter;
        public float p_pid_ang_div;
        // Current controller
        public float cc_startup_boost_duty;
        public float cc_min_current;
        public float cc_gain;
        public float cc_ramp_step_max;
        // Misc
        public int m_fault_stop_time_ms;
        public float m_duty_ramp_step;
        public float m_current_backoff_gain;
        public long m_encoder_counts; //uint32_t
        public sensor_port_mode m_sensor_port_mode;
        public boolean m_invert_direction;
        public drv8301_oc_mode m_drv8301_oc_mode;
        public int m_drv8301_oc_adj;
        public float m_bldc_f_sw_min;
        public float m_bldc_f_sw_max;
        public float m_dc_f_sw;
        public float m_ntc_motor_beta;
    } 

// Applications to use
    public enum app_use{
        APP_NONE ,
                APP_PPM,
                APP_ADC,
                APP_UART,
                APP_PPM_UART,
                APP_ADC_UART,
                APP_NUNCHUK,
                APP_NRF,
                APP_CUSTOM
    }

    // Throttle curve mode
    public enum thr_exp_mode{
        THR_EXP_EXPO,
                THR_EXP_NATURAL,
                THR_EXP_POLY
    }

// PPM control types
    public enum ppm_control_type{
        PPM_CTRL_TYPE_NONE,
                PPM_CTRL_TYPE_CURRENT,
                PPM_CTRL_TYPE_CURRENT_NOREV,
                PPM_CTRL_TYPE_CURRENT_NOREV_BRAKE,
                PPM_CTRL_TYPE_DUTY,
                PPM_CTRL_TYPE_DUTY_NOREV,
                PPM_CTRL_TYPE_PID,
                PPM_CTRL_TYPE_PID_NOREV
    }

    class ppm_config{
        ppm_control_type ctrl_type;
        float pid_max_erpm;
        float hyst;
        float pulse_start;
        float pulse_end;
        float pulse_center;
        boolean median_filter;
        boolean safe_start;
        float throttle_exp;
        float throttle_exp_brake;
        thr_exp_mode throttle_exp_mode;
        float ramp_time_pos;
        float ramp_time_neg;
        boolean multi_esc;
        boolean tc;
        float tc_max_diff;
    }

// ADC control types
    public enum adc_control_type{
        ADC_CTRL_TYPE_NONE,
                ADC_CTRL_TYPE_CURRENT,
                ADC_CTRL_TYPE_CURRENT_REV_CENTER,
                ADC_CTRL_TYPE_CURRENT_REV_BUTTON,
                ADC_CTRL_TYPE_CURRENT_REV_BUTTON_BRAKE_ADC,
                ADC_CTRL_TYPE_CURRENT_NOREV_BRAKE_CENTER,
                ADC_CTRL_TYPE_CURRENT_NOREV_BRAKE_BUTTON,
                ADC_CTRL_TYPE_CURRENT_NOREV_BRAKE_ADC,
                ADC_CTRL_TYPE_DUTY,
                ADC_CTRL_TYPE_DUTY_REV_CENTER,
                ADC_CTRL_TYPE_DUTY_REV_BUTTON,
                ADC_CTRL_TYPE_PID,
                ADC_CTRL_TYPE_PID_REV_CENTER,
                ADC_CTRL_TYPE_PID_REV_BUTTON
    }

    class adc_config{
        adc_control_type ctrl_type;
        float hyst;
        float voltage_start;
        float voltage_end;
        float voltage_center;
        float voltage2_start;
        float voltage2_end;
        boolean use_filter;
        boolean safe_start;
        boolean cc_button_inverted;
        boolean rev_button_inverted;
        boolean voltage_inverted;
        boolean voltage2_inverted;
        float throttle_exp;
        float throttle_exp_brake;
        thr_exp_mode throttle_exp_mode;
        float ramp_time_pos;
        float ramp_time_neg;
        boolean multi_esc;
        boolean tc;
        float tc_max_diff;
        long update_rate_hz; //uint32_t
    }

// Nunchuk control types
    public enum chuk_control_type{
        CHUK_CTRL_TYPE_NONE ,
                CHUK_CTRL_TYPE_CURRENT,
                CHUK_CTRL_TYPE_CURRENT_NOREV
    }

    public class chuk_config{
        chuk_control_type ctrl_type;
        float hyst;
        float rpm_lim_start;
        float rpm_lim_end;
        float ramp_time_pos;
        float ramp_time_neg;
        float stick_erpm_per_s_in_cc;
        boolean multi_esc;
        boolean tc;
        float tc_max_diff;
    } 

// NRF Datatypes
    public enum NRF_SPEED{
        NRF_SPEED_250K ,
                NRF_SPEED_1M,
                NRF_SPEED_2M
    }

    public enum NRF_POWER{
        NRF_POWER_M18DBM,
                NRF_POWER_M12DBM,
                NRF_POWER_M6DBM,
                NRF_POWER_0DBM
    }

    public enum NRF_AW{
        NRF_AW_3 ,
                NRF_AW_4,
                NRF_AW_5
    }

    public enum NRF_CRC{
        NRF_CRC_DISABLED ,
                NRF_CRC_1B,
                NRF_CRC_2B
    }

    public enum NRF_RETR_DELAY{
        NRF_RETR_DELAY_250US ,
                NRF_RETR_DELAY_500US,
                NRF_RETR_DELAY_750US,
                NRF_RETR_DELAY_1000US,
                NRF_RETR_DELAY_1250US,
                NRF_RETR_DELAY_1500US,
                NRF_RETR_DELAY_1750US,
                NRF_RETR_DELAY_2000US,
                NRF_RETR_DELAY_2250US,
                NRF_RETR_DELAY_2500US,
                NRF_RETR_DELAY_2750US,
                NRF_RETR_DELAY_3000US,
                NRF_RETR_DELAY_3250US,
                NRF_RETR_DELAY_3500US,
                NRF_RETR_DELAY_3750US,
                NRF_RETR_DELAY_4000US
    }

    class nrf_config{
        NRF_SPEED speed;
        NRF_POWER power;
        NRF_CRC crc_type;
        NRF_RETR_DELAY retry_delay;
        char retries;
        char channel;
        char[] address = new char[3];
        boolean send_crc_ack;
    } 

    class app_configuration{
        // Settings
        byte controller_id; //ubyte
        int timeout_msec; //uint32_t
        float timeout_brake_current;
        boolean send_can_status;
        int send_can_status_rate_hz; //uint32_t

        // Application to use
        app_use app_to_use;

        // UART application settings
        int app_uart_baudrate; //uint32_t

        // Nunchuk application settings
        chuk_config app_chuk_conf;

        // NRF application settings
        nrf_config app_nrf_conf;
    } 

// Communication commands
    public enum COMM_PACKET_ID{
        COMM_FW_VERSION ,
        COMM_JUMP_TO_BOOTLOADER,
        COMM_ERASE_NEW_APP,
        COMM_WRITE_NEW_APP_DATA,
        COMM_GET_VALUES,
        COMM_SET_DUTY,
        COMM_SET_CURRENT,
        COMM_SET_CURRENT_BRAKE,
        COMM_SET_RPM,
        COMM_SET_POS,
        COMM_SET_HANDBRAKE,
        COMM_SET_DETECT,
        COMM_SET_SERVO_POS,
        COMM_SET_MCCONF,
        COMM_GET_MCCONF,
        COMM_GET_MCCONF_DEFAULT,
        COMM_SET_APPCONF,
        COMM_GET_APPCONF,
        COMM_GET_APPCONF_DEFAULT,
        COMM_SAMPLE_PRINT,
        COMM_TERMINAL_CMD,
        COMM_PRINT,
        COMM_ROTOR_POSITION,
        COMM_EXPERIMENT_SAMPLE,
        COMM_DETECT_MOTOR_PARAM,
        COMM_DETECT_MOTOR_R_L,
        COMM_DETECT_MOTOR_FLUX_LINKAGE,
        COMM_DETECT_ENCODER,
        COMM_DETECT_HALL_FOC,
        COMM_REBOOT,
        COMM_ALIVE,
        COMM_GET_DECODED_PPM,
        COMM_GET_DECODED_ADC,
        COMM_GET_DECODED_CHUK,
        COMM_FORWARD_CAN,
        COMM_SET_CHUCK_DATA,
        COMM_CUSTOM_APP_DATA,
        COMM_NRF_START_PAIRING,
        COMM_GPD_SET_FSW,
        COMM_GPD_BUFFER_NOTIFY,
        COMM_GPD_BUFFER_SIZE_LEFT,
        COMM_GPD_FILL_BUFFER,
        COMM_GPD_OUTPUT_SAMPLE,
        COMM_GPD_SET_MODE,
        COMM_GPD_FILL_BUFFER_INT8,
        COMM_GPD_FILL_BUFFER_INT16,
        COMM_GPD_SET_BUFFER_INT_SCALE,
        COMM_GET_VALUES_SETUP,
        COMM_SET_MCCONF_TEMP,
        COMM_SET_MCCONF_TEMP_SETUP,
        COMM_GET_VALUES_SELECTIVE,
        COMM_GET_VALUES_SETUP_SELECTIVE,
        COMM_EXT_NRF_PRESENT,
        COMM_EXT_NRF_ESB_SET_CH_ADDR,
        COMM_EXT_NRF_ESB_SEND_DATA,
        COMM_EXT_NRF_ESB_RX_DATA,
        COMM_EXT_NRF_SET_ENABLED,
        COMM_DETECT_MOTOR_FLUX_LINKAGE_OPENLOOP,
        COMM_DETECT_APPLY_ALL_FOC,
        COMM_JUMP_TO_BOOTLOADER_ALL_CAN,
        COMM_ERASE_NEW_APP_ALL_CAN,
        COMM_WRITE_NEW_APP_DATA_ALL_CAN,
        COMM_PING_CAN,
        COMM_APP_DISABLE_OUTPUT,
        COMM_TERMINAL_CMD_SYNC,
        COMM_GET_IMU_DATA,
        COMM_BM_CONNECT,
        COMM_BM_ERASE_FLASH_ALL,
        COMM_BM_WRITE_FLASH,
        COMM_BM_REBOOT,
        COMM_BM_DISCONNECT,
        COMM_BM_MAP_PINS_DEFAULT,
        COMM_BM_MAP_PINS_NRF5X,
        COMM_ERASE_BOOTLOADER,
        COMM_ERASE_BOOTLOADER_ALL_CAN,
        COMM_PLOT_INIT,
        COMM_PLOT_DATA,
        COMM_PLOT_ADD_GRAPH,
        COMM_PLOT_SET_GRAPH,
        COMM_GET_DECODED_BALANCE,
        COMM_BM_MEM_READ,
        COMM_WRITE_NEW_APP_DATA_LZO,
        COMM_WRITE_NEW_APP_DATA_ALL_CAN_LZO,
        COMM_BM_WRITE_FLASH_LZO,
        COMM_SET_CURRENT_REL,
        COMM_CAN_FWD_FRAME,
        COMM_SET_BATTERY_CUT,
        COMM_SET_BLE_NAME,
        COMM_SET_BLE_PIN,
        COMM_SET_CAN_MODE,
        COMM_GET_IMU_CALIBRATION,
        COMM_GET_MCCONF_TEMP,
        COMM_GET_STATES
    }

    // Logged fault data
    class fault_data{
        mc_fault_code fault;
        float current;
        float current_filtered;
        float voltage;
        float duty;
        float rpm;
        int tacho;
        int cycles_running;
        int tim_val_samp;
        int tim_current_samp;
        int tim_top;
        int comm_step;
        float temperature;
    } 

// External LED state
    public enum LED_EXT_STATE{
        LED_EXT_OFF ,
                LED_EXT_NORMAL,
                LED_EXT_BRAKE,
                LED_EXT_TURN_LEFT,
                LED_EXT_TURN_RIGHT,
                LED_EXT_BRAKE_TURN_LEFT,
                LED_EXT_BRAKE_TURN_RIGHT,
                LED_EXT_BATT
    }

    class chuck_data{
        int js_x;
        int js_y;
        int acc_x;
        int acc_y;
        int acc_z;
        boolean bt_c;
        boolean bt_z;
    } 

    class mote_state {
        byte js_x; //ubyte
        byte js_y; //ubyte
        boolean bt_c;
        boolean bt_z;
        boolean bt_push;
        float vbat;
    }

    public enum MOTE_PACKET{
        MOTE_PACKET_BATT_LEVEL ,
                MOTE_PACKET_BUTTONS,
                MOTE_PACKET_ALIVE,
                MOTE_PACKET_FILL_RX_BUFFER,
                MOTE_PACKET_FILL_RX_BUFFER_LONG,
                MOTE_PACKET_PROCESS_RX_BUFFER,
                MOTE_PACKET_PROCESS_SHORT_BUFFER,
    }

    public static class  mc_values{
        public float v_in;
        public float temp_mos;
        public float temp_motor;
        public float current_motor;
        public float avg_id;
        public float avg_iq;
        public float current_in;
        public float rpm;
        public float duty_now;
        public float amp_hours;
        public float amp_hours_charged;
        public float watt_hours;
        public float watt_hours_charged;
        public int tachometer;
        public int tachometer_abs;
        public mc_fault_code fault_code;
        public float pid_pos;
        public int controller_id;
    }

    public static class mc_states {
       public float phi;
       public float phi_dot;
       public float psi;
       public float psi_dot;
       public float theta;
       public float theta_dot;
       public float hdg;
       public float hdg_dot;
    }
    public static class bldcMeasure { // type used for old MC configuration
        public float temp_mos1;
        public float temp_mos2;
        public float temp_mos3;
        public float temp_mos4;
        public float temp_mos5;
        public float temp_mos6;
        public float temp_pcb;
        public float avgMotorCurrent;
        public float avgInputCurrent;
        public float dutyCycleNow;
        public long rpm;
        public float inpVoltage;
        public float ampHours;
        public float ampHoursCharged;
        public float wattHours;
        public float wattHoursCharged;
        public long tachometer;
        public long tachometerAbs;
        public mc_fault_code fault_code;
    }
}

