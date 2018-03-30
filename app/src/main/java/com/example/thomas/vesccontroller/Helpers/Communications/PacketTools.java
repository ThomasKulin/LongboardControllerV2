package com.example.thomas.vesccontroller.Helpers.Communications;

import android.util.Log;

import com.example.thomas.vesccontroller.Activities.Board_Activity;

import static android.content.ContentValues.TAG;


/**
 * Created by Thomas on 2018-03-15.
 */

public class PacketTools {

    static byte[] payload = new byte[256];

    public static boolean unpackPayload(int[] message, int lenMes, int lenPay){
        int crcMessage = 0;
        int crcPayload = 0;

        //Rebuild src:
        crcMessage = message[lenMes-3] << 8;
        crcMessage &= 0xFF00;
        crcMessage += message[lenMes -2];
        Log.d(TAG, "PacketTools: SRC receeived: " + crcMessage);

        //Convert int type to byte
        byte[] message_byte = new byte[message.length];
        for(int temp = 0; temp < message.length; temp++ ) {
            message_byte[temp] = (byte) message[temp];
        }

        //Extract payload:
        System.arraycopy(message_byte, 2, payload, 0, message[1]);
        crcPayload = crc.crc16(payload, message[1]);

        if(crcPayload == crcMessage){
            return true;
        }
        else{
            return false;
        }
    }

    public static mc_values processReadPacket(){
        COMM_PACKET_ID  packetId;
        packetId = COMM_PACKET_ID.values()[payload[0]]; //packet ID is first byte in payload
        PassByReference index = new PassByReference(0); //allows us to change value of index from within buffer funcitons
        mc_values values = new mc_values();
        byte[] payload2 = new byte[256];
        System.arraycopy(payload, 1, payload2, 0, payload.length-1);

        switch (packetId){
            case COMM_GET_VALUES: { //packet contains value data
                //old FW
//                values.temp_mos1 = buffer.buffer_get_float16(payload2, (float) 10.0, index);
//                values.temp_mos2 = buffer.buffer_get_float16(payload2, (float) 10.0, index);
//                values.temp_mos3 = buffer.buffer_get_float16(payload2, (float) 10.0, index);
//                values.temp_mos4 = buffer.buffer_get_float16(payload2, (float) 10.0, index);
//                values.temp_mos5 = buffer.buffer_get_float16(payload2, (float) 10.0, index);
//                values.temp_mos6 = buffer.buffer_get_float16(payload2, (float) 10.0, index);
//                values.temp_pcb = buffer.buffer_get_float16(payload2, (float) 10.0, index);
//
//                values.avgMotorCurrent = buffer.buffer_get_float32(payload2, (float) 100.0, index);
//                values.avgInputCurrent = buffer.buffer_get_float32(payload2, (float) 100.0, index);
//                values.dutyCycleNow = buffer.buffer_get_float16(payload2, (float) 1000.0, index);
//                values.rpm = buffer.buffer_get_int32(payload2, index);
//                values.inpVoltage = buffer.buffer_get_float16(payload2, (float) 10.0, index);
//                values.ampHours = buffer.buffer_get_float32(payload2, (float) 10000.0, index);
//                values.ampHoursCharged = buffer.buffer_get_float32(payload2, (float) 10000.0, index);
//                values.wattHours = buffer.buffer_get_float32(payload2, (float) 10000.0, index);
//                values.wattHoursCharged = buffer.buffer_get_float32(payload2, (float) 10000.0, index);
//                values.tachometer = buffer.buffer_get_int32(payload2, index);
//                values.tachometerAbs = buffer.buffer_get_int32(payload2, index);
//                values.fault_code = mc_fault_code.values()[payload2[index.tempInt++]];
                //new FW
                values.temp_mos = buffer.buffer_get_float16(payload2, (float) 1e1, index);
                values.temp_motor = buffer.buffer_get_float16(payload2, (float) 1e1, index);
                values.current_motor = buffer.buffer_get_float32(payload2, (float) 1e2, index);
                values.current_in = buffer.buffer_get_float32(payload2, (float) 1e2, index);
                values.avg_id = buffer.buffer_get_float32(payload2, (float) 1e2, index);
                values.avg_iq = buffer.buffer_get_float32(payload2, (float) 1e2, index);
                values.duty_now = buffer.buffer_get_float16(payload2, (float) 1e3, index);
                values.rpm = buffer.buffer_get_int32(payload2, index);
                values.v_in = buffer.buffer_get_float16(payload2, (float) 1e1, index);
                values.amp_hours = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                values.amp_hours_charged = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                values.watt_hours = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                values.watt_hours_charged = buffer.buffer_get_float32(payload2, (float) 1e4, index);
                values.tachometer = buffer.buffer_get_int32(payload2, index);
                values.tachometer_abs = buffer.buffer_get_int32(payload2, index);
                values.fault_code = mc_fault_code.values()[payload2[index.tempInt++]];
                values.pid_pos = buffer.buffer_get_float32(payload2, (float) 1e6, index);
                resetPacket();
                return values;
            }
            default:
                resetPacket();
                values.fault_code = mc_fault_code.FAULT_CODE_NO_DATA;
                return values;
        }
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
        messageSend[count] = 0;

        //-- Write to Bluetooth--

        Board_Activity.writeBT(messageSend, 0, count);

        //-----------------------
        return count;
    }

    public static void vescUartGetValue(){
        byte[] command = new byte[1];
        command[0] = (byte) COMM_PACKET_ID.COMM_GET_VALUES.ordinal();
        packSendPayload(command, 1);
    }

    public static void vescUartSetValue(Object value, COMM_PACKET_ID type){
        PassByReference index = new PassByReference(0);
        int len;
        byte[] payload;

        switch (type) {
            case COMM_SET_DUTY:
                break;
            case COMM_SET_CURRENT:
                len = 5;
                payload = new byte[len];
                payload[index.tempInt++] = (byte) COMM_PACKET_ID.COMM_SET_CURRENT.ordinal();
                buffer.buffer_append_int32(payload, (int) ((float)value * 1000), index);
                packSendPayload(payload, len);
                break;
            case COMM_SET_CURRENT_BRAKE:
                len = 5;
                payload = new byte[len];
                payload[index.tempInt++] = (byte) COMM_PACKET_ID.COMM_SET_CURRENT_BRAKE.ordinal();
                buffer.buffer_append_int32(payload, (int) ((float)value * 1000), index);
                packSendPayload(payload, len);
                break;
            case COMM_SET_RPM:
                break;
            case COMM_SET_SERVO_POS:
                break;
            case COMM_SET_MCCONF:
                break;
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

    static void resetPacket(){
        for(int i = 0; i < 255; i++){
            payload[i] = 0;
        }
    }



    enum mc_state{
        MC_STATE_OFF,
        MC_STATE_DETECTING ,
        MC_STATE_RUNNING,
        MC_STATE_FULL_BRAKE,
    }

    enum mc_pwm_mode{
        PWM_MODE_NONSYNCHRONOUS_HISW , // This mode is not recommended
                PWM_MODE_SYNCHRONOUS, // The recommended and most tested mode
                PWM_MODE_BIPOLAR // Some glitches occasionally, can kill MOSFETs
    } 

    enum mc_comm_mode{
        COMM_MODE_INTEGRATE ,
                COMM_MODE_DELAY
    }

    enum mc_sensor_mode{
        SENSOR_MODE_SENSORLESS ,
                SENSOR_MODE_SENSORED,
                SENSOR_MODE_HYBRID
    }

    enum mc_foc_sensor_mode{
        FOC_SENSOR_MODE_SENSORLESS ,
                FOC_SENSOR_MODE_ENCODER,
                FOC_SENSOR_MODE_HALL
    }

    enum mc_motor_type{
        MOTOR_TYPE_BLDC ,
                MOTOR_TYPE_DC,
                MOTOR_TYPE_FOC
    }

    enum mc_fault_code{
        FAULT_CODE_NONE ,
                FAULT_CODE_OVER_VOLTAGE,
                FAULT_CODE_UNDER_VOLTAGE,
                FAULT_CODE_DRV8302,
                FAULT_CODE_ABS_OVER_CURRENT,
                FAULT_CODE_OVER_TEMP_FET,
                FAULT_CODE_OVER_TEMP_MOTOR,
                FAULT_CODE_NO_DATA
    }

    enum mc_control_mode{
        CONTROL_MODE_DUTY ,
                CONTROL_MODE_SPEED,
                CONTROL_MODE_CURRENT,
                CONTROL_MODE_CURRENT_BRAKE,
                CONTROL_MODE_POS,
                CONTROL_MODE_NONE
    }

    enum disp_pos_mode{
        DISP_POS_MODE_NONE ,
                DISP_POS_MODE_INDUCTANCE,
                DISP_POS_MODE_OBSERVER,
                DISP_POS_MODE_ENCODER,
                DISP_POS_MODE_PID_POS,
                DISP_POS_MODE_PID_POS_ERROR,
                DISP_POS_MODE_ENCODER_OBSERVER_ERROR
    }

    enum sensor_port_mode{
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

    class mc_configuration{
        // Switching and drive
        mc_pwm_mode pwm_mode;
        mc_comm_mode comm_mode;
        mc_motor_type motor_type;
        mc_sensor_mode sensor_mode;
        // Limits
        float l_current_max;
        float l_current_min;
        float l_in_current_max;
        float l_in_current_min;
        float l_abs_current_max;
        float l_min_erpm;
        float l_max_erpm;
        float l_max_erpm_fbrake;
        float l_max_erpm_fbrake_cc;
        float l_min_vin;
        float l_max_vin;
        float l_battery_cut_start;
        float l_battery_cut_end;
        boolean l_slow_abs_current;
        boolean l_rpm_lim_neg_torque;
        float l_temp_fet_start;
        float l_temp_fet_end;
        float l_temp_motor_start;
        float l_temp_motor_end;
        float l_min_duty;
        float l_max_duty;
        // Overridden limits (Computed during runtime)
        float lo_current_max;
        float lo_current_min;
        float lo_in_current_max;
        float lo_in_current_min;
        // Sensorless
        float sl_min_erpm;
        float sl_min_erpm_cycle_int_limit;
        float sl_max_fullbreak_current_dir_change;
        float sl_cycle_int_limit;
        float sl_phase_advance_at_br;
        float sl_cycle_int_rpm_br;
        float sl_bemf_coupling_k;
        // Hall sensor
        byte[] hall_table = new byte[8]; //int8_t
        float hall_sl_erpm;
        // FOC
        float foc_current_kp;
        float foc_current_ki;
        float foc_f_sw;
        float foc_dt_us;
        float foc_encoder_offset;
        boolean foc_encoder_inverted;
        float foc_encoder_ratio;
        float foc_motor_l;
        float foc_motor_r;
        float foc_motor_flux_linkage;
        float foc_observer_gain;
        float foc_pll_kp;
        float foc_pll_ki;
        float foc_duty_dowmramp_kp;
        float foc_duty_dowmramp_ki;
        float foc_openloop_rpm;
        float foc_sl_openloop_hyst;
        float foc_sl_openloop_time;
        float foc_sl_d_current_duty;
        float foc_sl_d_current_factor;
        mc_foc_sensor_mode foc_sensor_mode;
        byte[] foc_hall_table = new byte[8]; //uint8_t
        float foc_sl_erpm;
        // Speed PID
        float s_pid_kp;
        float s_pid_ki;
        float s_pid_kd;
        float s_pid_min_erpm;
        // Pos PID
        float p_pid_kp;
        float p_pid_ki;
        float p_pid_kd;
        float p_pid_ang_div;
        // Current controller
        float cc_startup_boost_duty;
        float cc_min_current;
        float cc_gain;
        float cc_ramp_step_max;
        // Misc
        int m_fault_stop_time_ms;
        float m_duty_ramp_step;
        float m_duty_ramp_step_rpm_lim;
        float m_current_backoff_gain;
        int m_encoder_counts; //uint32_t
        sensor_port_mode m_sensor_port_mode;
    } 

// Applications to use
    enum app_use{
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

// Nunchuk control types
    enum chuk_control_type{
        CHUK_CTRL_TYPE_NONE ,
                CHUK_CTRL_TYPE_CURRENT,
                CHUK_CTRL_TYPE_CURRENT_NOREV
    }

    class chuk_config{
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
    enum NRF_SPEED{
        NRF_SPEED_250K ,
                NRF_SPEED_1M,
                NRF_SPEED_2M
    }

    enum NRF_POWER{
        NRF_POWER_M18DBM,
                NRF_POWER_M12DBM,
                NRF_POWER_M6DBM,
                NRF_POWER_0DBM
    }

    enum NRF_AW{
        NRF_AW_3 ,
                NRF_AW_4,
                NRF_AW_5
    }

    enum NRF_CRC{
        NRF_CRC_DISABLED ,
                NRF_CRC_1B,
                NRF_CRC_2B
    }

    enum NRF_RETR_DELAY{
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
        COMM_FW_VERSION,
                COMM_JUMP_TO_BOOTLOADER,
                COMM_ERASE_NEW_APP,
                COMM_WRITE_NEW_APP_DATA,
                COMM_GET_VALUES,
                COMM_SET_DUTY,
                COMM_SET_CURRENT,
                COMM_SET_CURRENT_BRAKE,
                COMM_SET_RPM,
                COMM_SET_POS,
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
                COMM_NON
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
    enum LED_EXT_STATE{
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

    enum MOTE_PACKET{
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

