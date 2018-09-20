package com.example.thomas.vesccontroller.Helpers;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

/**
 * Created by Thomas on 2017-12-28.
 */

public class BoardProfile implements Serializable{
    //home
    private String name;
    private double version;
    private double totalDist;
    private double avgSpeed;
    private double maxSpeed;

    //parameters
    public String btDeviceAddress;
    public double wheelRatio; // (motor pulley / wheel pulley)*(wheeldiameter/2) * 1/60 * 2pi     ... converts motor rpm to velocity
    public double maxVoltage;
    public double minVoltage;
    public byte cellCount;
    public int motorPoles;
    public int thresholdTemp;
    public int maxTemp;
    public boolean reverseEnabled;
    public int maxAccel;
    public int maxRegen;

    public BoardProfile(){
        this.name = "DEFAULT BOARD";
        this.version = 1.0;
        this.totalDist = 0.0;
        this.avgSpeed = 0.0;
        this. maxSpeed = 0.0;

        this.btDeviceAddress = "00:00:00:00:00:00";
        this. wheelRatio = 1.0;
        this.maxVoltage = 25.2; //4.2 V/cell
        this.minVoltage = 21.0; //3.5 V/cell
        this.cellCount = 6;
        this.motorPoles = 14;
        this.thresholdTemp = 70;
        this.maxTemp = 80;
        this.reverseEnabled = true;
        this.maxAccel = 40;
        this.maxRegen = 8;
    }
    public BoardProfile(String name, double version, double totalDist, double avgSpeed, double maxSpeed, String btDeviceAddress,
                        double wheelRatio, double maxVoltage, double minVoltage, byte cellCount, int motorPoles, int thresholdTemp,
                        int maxTemp, boolean reverseEnabled, int maxAccel, int maxRegen){
        this.name = name;
        this.version = version;
        this.totalDist = totalDist;
        this.avgSpeed = avgSpeed;
        this.maxSpeed = maxSpeed;

        this.btDeviceAddress = btDeviceAddress;
        this.wheelRatio = wheelRatio;
        this.maxVoltage = maxVoltage;
        this.minVoltage = minVoltage;
        this.cellCount = cellCount;
        this.motorPoles = motorPoles;
        this.thresholdTemp = thresholdTemp;
        this.maxTemp = maxTemp;
        this.reverseEnabled = reverseEnabled;
        this.maxAccel = maxAccel;
        this.maxRegen = maxRegen;
    }
    public BoardProfile(BoardProfile newBoard){
        this.name = newBoard.name;
        this.version = newBoard.version;
        this.totalDist = newBoard.totalDist;
        this.avgSpeed = newBoard.avgSpeed;
        this.maxSpeed = newBoard.maxSpeed;

        this.btDeviceAddress = newBoard.btDeviceAddress;
        this.wheelRatio = newBoard.wheelRatio;
        this.maxVoltage = newBoard.maxVoltage;
        this.minVoltage = newBoard.minVoltage;
        this.cellCount = newBoard.cellCount;
        this.motorPoles = newBoard.motorPoles;
        this.thresholdTemp = newBoard.thresholdTemp;
        this.maxTemp = newBoard.maxTemp;
        this.reverseEnabled = newBoard.reverseEnabled;
        this.maxAccel = newBoard.maxAccel;
        this.maxRegen = newBoard.maxRegen;
    }

    public void setName(String name){
        this.name = name;
    }
    public void setVersion(double version){
        if(version>=0)
            this.version=version;
    }
    public void setTotalDist(double totalDist){
        if(totalDist>=0)
            this.totalDist=totalDist;
    }
    public void setAvgSpeed(double avgSpeed){
        if(avgSpeed>=0)
            this.avgSpeed=avgSpeed;
    }
    public void setMaxSpeed(double maxSpeed){
        if(maxSpeed>=0)
            this.maxSpeed=maxSpeed;
    }
    public void setBtDeviceAddress(String btDeviceAddress){
        if(btDeviceAddress!=null)
            this.btDeviceAddress=btDeviceAddress;
    }
    public void setWheelRatio(double wheelRatio){
        if(wheelRatio>0)
            this.wheelRatio=wheelRatio;
    }
    public void setMaxVoltage(double maxVoltage){
        if(maxVoltage>=0)
            this.maxVoltage=maxVoltage;
    }
    public void setMinVoltage(double minVoltage){
        if(minVoltage>=0)
            this.minVoltage=minVoltage;
    }
    public void setCellCount(byte cellCount){
        if(cellCount>=0)
            this.cellCount=cellCount;
    }
    public void setMotorPoles(int motorPoles){
        if(motorPoles>=0)
            this.motorPoles=motorPoles;
    }
    public void setThresholdTemp(int thresholdTemp){
        if(thresholdTemp>=0)
            this.thresholdTemp=thresholdTemp;
    }
    public void setMaxTemp(int maxTemp){
        if(maxTemp>=0)
            this.maxTemp=maxTemp;
    }
    public void setReverseEnabled(boolean reverseEnabled){
        this.reverseEnabled=reverseEnabled;
    }
    public void setMaxAccel(int maxAccel){
        if(maxAccel>=0)
            this.maxAccel=maxAccel;
    }
    public void setMaxRegen(int maxRegen){
        if(maxRegen>=0)
            this.maxRegen=maxRegen;
    }
    public String getName(){
        return this.name;
    }
    public double getVersion() {
        return this.version;
    }
    public double getTotalDist(){
        return this.totalDist;
    }
    public double getAvgSpeed() {
        return this.avgSpeed;
    }
    public double getMaxSpeed() {
        return this.maxSpeed;
    }
    public String getBtDeviceAddress(){
        return this.btDeviceAddress;
    }
    public double getwheelRatio() {
        return this.wheelRatio;
    }
    public double getMaxVoltage() {
        return this.maxVoltage;
    }
    public double getMinVoltage() {
        return this.minVoltage;
    }
    public byte getCellCount(){
        return this.cellCount;
    }
    public int getMotorPoles() {
        return this.motorPoles;
    }
    public int getThresholdTemp() {
        return this.thresholdTemp;
    }
    public int getMaxTemp() {
        return this.maxTemp;
    }
    public boolean isReverseEnabled() {
        return this.reverseEnabled;
    }
    public int getMaxAccel() {
        return this.maxAccel;
    }
    public int getMaxRegen() {
        return this.maxRegen;
    }

//    @Override
//    public boolean equals(Object profile){
//        if(!(profile instanceof BoardProfile)) {
//            return false;
//        }
//        BoardProfile profileB = (BoardProfile) profile;
//        //long as check to see if everything is equal
//        if (this.btDevice == null && profileB == null) {
//            return true;
//        }
//        if (this.btDevice == null || profileB == null) {
//            return false;
//        }
//        if(this.btDevice.equals(profileB.btDevice) && this.name.equals(profileB.name)&&this.version==profileB.version&&this.totalDist==profileB.totalDist&&
//                this.avgSpeed==profileB.avgSpeed&&this.maxSpeed==profileB.maxSpeed&&this.wheelRatio==profileB.wheelRatio&&
//                this.maxVoltage==profileB.maxVoltage&&this.minVoltage==profileB.minVoltage&&this.cellCount==profileB.cellCount&&
//                this.motorPoles==profileB.motorPoles&&this.thresholdTemp==profileB.thresholdTemp&&this.maxTemp==profileB.maxTemp&&
//                this.reverseEnabled==profileB.reverseEnabled&&this.maxAccel==profileB.maxAccel&&this.maxRegen==profileB.maxRegen){
//            return true;
//        }
//        else{
//            return false;
//        }
//    }

    @Override
    public boolean equals(Object profile){
        if(!(profile instanceof BoardProfile)) {
            return false;
        }
        BoardProfile profileB = (BoardProfile) profile;
        //long as check to see if everything is equal
        if (this.btDeviceAddress == null || profileB.btDeviceAddress == null) {
            return false;
        }
        if(this.btDeviceAddress.equals(profileB.btDeviceAddress) && this.name.equals(profileB.name)&&this.version==profileB.version&&this.totalDist==profileB.totalDist&&
                this.avgSpeed==profileB.avgSpeed&&this.maxSpeed==profileB.maxSpeed&&this.wheelRatio==profileB.wheelRatio&&
                this.maxVoltage==profileB.maxVoltage&&this.minVoltage==profileB.minVoltage&&this.cellCount==profileB.cellCount&&
                this.motorPoles==profileB.motorPoles&&this.thresholdTemp==profileB.thresholdTemp&&this.maxTemp==profileB.maxTemp&&
                this.reverseEnabled==profileB.reverseEnabled&&this.maxAccel==profileB.maxAccel&&this.maxRegen==profileB.maxRegen){
            return true;
        }
        else{
            return false;
        }
    }
}
