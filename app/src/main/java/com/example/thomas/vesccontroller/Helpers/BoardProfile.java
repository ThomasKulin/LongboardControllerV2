package com.example.thomas.vesccontroller.Helpers;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Thomas on 2017-12-28.
 */

public class BoardProfile {
    //home
    private String name;
    private double version;
    private double totalDist;
    private double avgSpeed;
    private double maxSpeed;

    //parameters
    private BluetoothDevice btDevice;
    public double wheelRatio; // (wheel pulley / motor pulley)*(wheeldiameter/2) * 1/60 * 2pi     ... converts motor rpm to velocity
    public double maxVoltage;
    public double minVoltage;
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

        this.btDevice = null;
        this. wheelRatio = 1.0;
        this.maxVoltage = 25.2; //4.2 V/cell
        this.minVoltage = 21.0; //3.5 V/cell
        this.motorPoles = 14;
        this.thresholdTemp = 70;
        this.maxTemp = 80;
        this.reverseEnabled = true;
        this.maxAccel = 40;
        this.maxRegen = 8;
    }
    public BoardProfile(String name, double version, double totalDist, double avgSpeed, double maxSpeed, BluetoothDevice btDevice,
                        double wheelRatio, double maxVoltage, double minVoltage,int motorPoles, int thresholdTemp,
                        int maxTemp, boolean reverseEnabled, int maxAccel, int maxRegen){
        this.name = name;
        this.version = version;
        this.totalDist = totalDist;
        this.avgSpeed = avgSpeed;
        this.maxSpeed = maxSpeed;

        this.btDevice = btDevice;
        this.wheelRatio = wheelRatio;
        this.maxVoltage = maxVoltage;
        this.minVoltage = minVoltage;
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

        this.btDevice = newBoard.btDevice;
        this.wheelRatio = newBoard.wheelRatio;
        this.maxVoltage = newBoard.maxVoltage;
        this.minVoltage = newBoard.minVoltage;
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
    public void setBtDevice(BluetoothDevice btDevice){
        if(btDevice!=null)
            this.btDevice=btDevice;
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
    public BluetoothDevice getBtDevice(){
        return this.btDevice;
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
}
