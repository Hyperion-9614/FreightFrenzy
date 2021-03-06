/*
Copyright (c) 2017 Robert Atkinson

All rights reserved.

Derived in part from information in various resources, including FTDI, the
Android Linux implementation, FreeBsc, UsbSerial, and others.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.robotcore.internal.ftdi;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by bob on 3/18/2017.
 */
@SuppressWarnings("WeakerAccess")
public class FtDeviceManager extends FtConstants
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    public static final String TAG = "FtDeviceManager";

    private static FtDeviceManager theInstance = null;

    protected static final String ACTION_FTDI_USB_PERMISSION = "org.firstinspires.ftc.ftdi.permission";  // https://developer.android.com/guide/topics/connectivity/usb/host.html

    private static final Context mContext = AppUtil.getDefContext();
    private static PendingIntent mPendingIntent = null;
    private static final List<VendorAndProductIds> mSupportedDevices = new ArrayList<VendorAndProductIds>(Arrays.asList(new VendorAndProductIds(0x0403, 24597),    // 0x6015    X series
            new VendorAndProductIds(0x0403, 24596),    // 0x6014    ft232h
            new VendorAndProductIds(0x0403, 24593),    // 0x6011    ft4232h
            new VendorAndProductIds(0x0403, 24592),    // 0x6010    FT2232 or FT2232H
            new VendorAndProductIds(0x0403, 24577),    // 0x6001    232AM, FT232B or FT232R
            new VendorAndProductIds(0x0403, 24582),    // 0x6006    Direct Driver Recovery PID
            new VendorAndProductIds(0x0403, 24604),    // 0x601c    ft4222?
            new VendorAndProductIds(0x0403, 0xFAC1),   //           USB Instruments PS40M10
            new VendorAndProductIds(0x0403, 0xFAC2),   //           USB Instruments DS1M12
            new VendorAndProductIds(0x0403, 0xFAC3),   //           USB Instruments DS100M10
            new VendorAndProductIds(0x0403, 0xFAC4),   //           USB Instruments DS60M10
            new VendorAndProductIds(0x0403, 0xFAC5),   //           USB Instruments EasySYNC LA100
            new VendorAndProductIds(0x0403, 0xFAC6),   //           USB2-F-7x01 CANPlus Adapter
            new VendorAndProductIds(0x0403, 24594),    // 0x6012    ES001H
            new VendorAndProductIds(0x08ac, 4133),     // 0x1025    Macraigor - customer request
            new VendorAndProductIds(0x15d6, 1),        // 0x0001    Keith Support Request 8/10/04
            new VendorAndProductIds(0x0403, 24599))); // 0x6017    Additional VID/PID
    private static UsbManager mUsbManager;

    private ArrayList<FtDevice> mFtdiDevices;

    private final BroadcastReceiver mUsbPlugEvents = new BroadcastReceiver()
        {
        public void onReceive(Context context, Intent intent)
            {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
                {
                UsbDevice dev = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                RobotLog.vv(TAG, "ACTION_USB_DEVICE_DETACHED: %s", dev.getDeviceName());

                for (FtDevice ftDev = FtDeviceManager.this.findDevice(dev); ftDev != null; ftDev = FtDeviceManager.this.findDevice(dev))
                    {
                    ftDev.close();
                    synchronized (FtDeviceManager.this.mFtdiDevices)
                        {
                        FtDeviceManager.this.mFtdiDevices.remove(ftDev);
                        }
                    }
                }

            else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
                {
                UsbDevice dev = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                RobotLog.vv(TAG, "ACTION_USB_DEVICE_ATTACHED: %s", dev.getDeviceName());

                FtDeviceManager.this.addOrUpdateUsbDevice(dev);
                }
            }
        };

    private static final BroadcastReceiver mUsbDevicePermissions = new BroadcastReceiver()
        {
        public void onReceive(Context context, Intent intent)
            {
            String action = intent.getAction();
            if (ACTION_FTDI_USB_PERMISSION.equals(action))
                {
                synchronized (this)
                    {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                        {
                        RobotLog.vv(TAG, "permission granted for device " + device.getDeviceName());
                        }
                    else
                        {
                        RobotLog.ee(TAG, "permission denied for device " + device.getDeviceName());
                        }
                    }
                }

            }
        };

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    private FtDeviceManager()
        {
        if (!findUsbManager())
            {
            throw new RuntimeException("unable to find usb manager"); // This should never happen
            }

        this.mFtdiDevices = new ArrayList<FtDevice>();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, new Intent(ACTION_FTDI_USB_PERMISSION), /*134217728*/ /*0x8000000*/ PendingIntent.FLAG_UPDATE_CURRENT);
        mContext.getApplicationContext().registerReceiver(mUsbDevicePermissions, new IntentFilter(ACTION_FTDI_USB_PERMISSION));
        mContext.getApplicationContext().registerReceiver(this.mUsbPlugEvents, filter);
        }

    public static synchronized FtDeviceManager getInstance()
        {
        if (theInstance == null)
            {
            theInstance = new FtDeviceManager();
            }
        return theInstance;
        }

    //----------------------------------------------------------------------------------------------
    // Operations
    //----------------------------------------------------------------------------------------------

    private FtDevice findDevice(UsbDevice usbDev)
        {
        synchronized (this.mFtdiDevices)
            {
            for (FtDevice ftDevice : this.mFtdiDevices)
                {
                if (ftDevice.getUsbDevice().equals(usbDev))
                    {
                    return ftDevice;
                    }
                }
            }
        return null;
        }

    public boolean isFtDevice(UsbDevice dev)
        {
        boolean result = false;
        if (mContext == null)
            {
            return result;
            }
        else
            {
            VendorAndProductIds vidPid = new VendorAndProductIds(dev.getVendorId(), dev.getProductId());
            if (mSupportedDevices.contains(vidPid))
                {
                result = true;
                }
            return result;
            }
        }

    // TODO: Ask AppUtil for the permssion instead of mUsbManager: that will allow us to 
    // block until we get a definitive yes or no, rather than the interim hit or miss we presently have
    private boolean isPermitted(UsbDevice dev)
        {
        boolean result = false;
        if (!mUsbManager.hasPermission(dev))
            {
            RobotLog.vv(TAG, "requesting permissions for device=%s", dev.getDeviceName());
            mUsbManager.requestPermission(dev, mPendingIntent);
            }

        if (mUsbManager.hasPermission(dev))
            {
            result = true;
            }

        return result;
        }

    private static boolean findUsbManager()
        {
        if (mUsbManager == null && mContext != null)
            {
            mUsbManager = (UsbManager) mContext.getApplicationContext().getSystemService(Context.USB_SERVICE);
            }

        return mUsbManager != null;
        }

    public boolean setVIDPID(int vendorId, int productId)
        {
        boolean rc = false;
        if (vendorId != 0 && productId != 0)
            {
            VendorAndProductIds vidpid = new VendorAndProductIds(vendorId, productId);
            if (mSupportedDevices.contains(vidpid))
                {
                return true;
                }

            if (!mSupportedDevices.add(vidpid))
                {
                }
            else
                {
                rc = true;
                }
            }
        else
            {
            RobotLog.ee(TAG, "Invalid parameter to setVIDPID");
            }

        return rc;
        }

    public int[][] getVIDPID()
        {
        int listSize = mSupportedDevices.size();
        int[][] arrayVIDPID = new int[2][listSize];

        for (int i = 0; i < listSize; ++i)
            {
            VendorAndProductIds vidpid = (VendorAndProductIds) mSupportedDevices.get(i);
            arrayVIDPID[0][i] = vidpid.getVendorId();
            arrayVIDPID[1][i] = vidpid.getProductId();
            }

        return arrayVIDPID;
        }

    private void clearDevices()
        {
        synchronized (this.mFtdiDevices)
            {
            int nr_dev = this.mFtdiDevices.size();

            for (int i = 0; i < nr_dev; ++i)
                {
                this.mFtdiDevices.remove(0);
                }

            }
        }

    public int createDeviceInfoList()
        {
        HashMap deviceList = mUsbManager.getDeviceList();
        Iterator deviceIterator = deviceList.values().iterator();
        ArrayList<FtDevice> devices = new ArrayList<FtDevice>();
        while (true)
            {
            // Find the next FT-compatible USB device
            UsbDevice usbDevice;
            do  {
            if (!deviceIterator.hasNext())
                {
                // We're done: swap in the new device list
                synchronized (this.mFtdiDevices)
                    {
                    this.clearDevices();
                    this.mFtdiDevices = devices;

                    // Return the number of devices
                    int deviceCount = this.mFtdiDevices.size();
                    RobotLog.vv(TAG, "createDeviceInfoList(): %d USB devices", deviceCount);
                    return deviceCount;
                    }
                }

            usbDevice = (UsbDevice) deviceIterator.next();
            }
            while (!this.isFtDevice(usbDevice));

            int numInterfaces = usbDevice.getInterfaceCount();

            for (int i = 0; i < numInterfaces; ++i)
                {
                if (this.isPermitted(usbDevice))    // TODO: why check perms INSIDE of loop?
                    {
                    addOrUpdatePermittedUsbDevice(devices, usbDevice, usbDevice.getInterface(i));
                    }
                }
            }
        }

    public synchronized int getDeviceInfoList(int numDevs, FtDeviceInfo[] deviceList)
        {
        for (int i = 0; i < numDevs; ++i)
            {
            deviceList[i] = (this.mFtdiDevices.get(i)).mDeviceInfo;
            }

        return this.mFtdiDevices.size();
        }

    public synchronized FtDeviceInfo getDeviceInfoListDetail(int index)
        {
        return index <= this.mFtdiDevices.size() && index >= 0
                ? (this.mFtdiDevices.get(index)).mDeviceInfo
                : null;
        }

    public static int getLibraryVersion()
        {
        return 540016640 /*0x20300000*/;
        }

    private boolean tryOpen(FtDevice ftDev, FtDeviceManagerParams params)
        {
        boolean result = false;
        if (ftDev == null)
            {
            }
        else
            {
            ftDev.setContext(mContext);
            if (params != null)
                {
                ftDev.setDriverParameters(params);
                }

            try {
                if (ftDev.openDevice(mUsbManager) && ftDev.isOpen())
                    {
                    result = true;
                    }
                }
            catch (FtDeviceIOException e)
                {
                // ignore
                }
            }
        return result;
        }

    public synchronized FtDevice openByUsbDevice(UsbDevice dev, FtDeviceManagerParams params)
        {
        RobotLog.vv(TAG, "openByUsbDevice(%s)", VendorAndProductIds.from(dev));
        FtDevice ftDev = null;
        if (this.isFtDevice(dev))
            {
            // Try to find this device in our list of enumerated devices
            ftDev = this.findDevice(dev);

            // If we can't find it, then see if it's currently attached (we might be in an attachment
            // notification race) and attach it now if so
            if (ftDev == null)
                {
                RobotLog.vv(TAG, "device not found: adding it on the fly");
                //
                addOrUpdateUsbDevice(dev);
                ftDev = this.findDevice(dev);
                //
                if (ftDev==null)
                    {
                    RobotLog.ee(TAG, "add failed");
                    }
                }

            if (!this.tryOpen(ftDev, params))
                {
                ftDev = null;
                }
            }
        return ftDev;
        }

    public synchronized FtDevice openByUsbDevice(UsbDevice dev)
        {
        return this.openByUsbDevice(dev, (FtDeviceManagerParams) null);
        }

    public synchronized FtDevice openByIndex(int index, FtDeviceManagerParams params)
        {
        FtDevice ftDev = null;
        if (index < 0)
            {
            }
        else
            {
            ftDev = this.mFtdiDevices.get(index);
            if (!this.tryOpen(ftDev, params))
                {
                ftDev = null;
                }
            }
        return ftDev;
        }

    public synchronized FtDevice openByIndex(int index)
        {
        return this.openByIndex(index, (FtDeviceManagerParams) null);
        }

    public synchronized FtDevice openBySerialNumber(String serialNumber, FtDeviceManagerParams params)
        {
        FtDeviceInfo devInfo = null;
        FtDevice ftDev = null;

        for (int i = 0; i < this.mFtdiDevices.size(); ++i)
            {
            FtDevice tmpDev = (FtDevice) this.mFtdiDevices.get(i);
            if (tmpDev != null)
                {
                devInfo = tmpDev.mDeviceInfo;
                if (devInfo == null)
                    {
                    RobotLog.ee(TAG, "***devInfo cannot be null***");
                    }
                else if (devInfo.serialNumber.equals(serialNumber))
                    {
                    ftDev = tmpDev;
                    break;
                    }
                }
            }

        if (!this.tryOpen(ftDev, params))
            {
            ftDev = null;
            }
        return ftDev;
        }

    public synchronized FtDevice openBySerialNumber(String serialNumber)
        {
        return this.openBySerialNumber(serialNumber, (FtDeviceManagerParams) null);
        }

    public synchronized FtDevice openByDescription(String description, FtDeviceManagerParams params)
        {
        FtDeviceInfo devInfo = null;
        FtDevice ftDev = null;

        for (int i = 0; i < this.mFtdiDevices.size(); ++i)
            {
            FtDevice tmpDev = this.mFtdiDevices.get(i);
            if (tmpDev != null)
                {
                devInfo = tmpDev.mDeviceInfo;
                if (devInfo == null)
                    {
                    RobotLog.ee(TAG, "***devInfo cannot be null***");
                    }
                else if (devInfo.productName.equals(description))
                    {
                    ftDev = tmpDev;
                    break;
                    }
                }
            }

        if (!this.tryOpen(ftDev, params))
            {
            ftDev = null;
            }

        return ftDev;
        }

    public synchronized FtDevice openByDescription(String description)
        {
        return this.openByDescription(description, (FtDeviceManagerParams) null);
        }

    public synchronized FtDevice openByLocation(int location, FtDeviceManagerParams params)
        {
        FtDeviceInfo devInfo = null;
        FtDevice ftDev = null;

        for (int i = 0; i < this.mFtdiDevices.size(); ++i)
            {
            FtDevice tmpDev = (FtDevice) this.mFtdiDevices.get(i);
            if (tmpDev != null)
                {
                devInfo = tmpDev.mDeviceInfo;
                if (devInfo == null)
                    {
                    RobotLog.ee(TAG, "***devInfo cannot be null***");
                    }
                else if (devInfo.location == location)
                    {
                    ftDev = tmpDev;
                    break;
                    }
                }
            }

        if (!this.tryOpen(ftDev, params))
            {
            ftDev = null;
            }

        return ftDev;
        }

    public synchronized FtDevice openByLocation(int location)
        {
        return this.openByLocation(location, (FtDeviceManagerParams) null);
        }

    public int addOrUpdateUsbDevice(UsbDevice usbDevice)
        {
        RobotLog.vv(TAG, "addOrUpdateUsbDevice(%s)", VendorAndProductIds.from(usbDevice));
        int result = 0;
        AppUtil.getInstance().setUsbFileSystemRoot(usbDevice);
        if (this.isFtDevice(usbDevice))
            {
            int numInterfaces = usbDevice.getInterfaceCount();
            for (int i = 0; i < numInterfaces; ++i)
                {
                if (this.isPermitted(usbDevice))
                    {
                    if (addOrUpdatePermittedUsbDevice(this.mFtdiDevices, usbDevice, usbDevice.getInterface(i)))
                        {
                        ++result;
                        }
                    }
                }
            }

        return result;
        }

    protected boolean addOrUpdatePermittedUsbDevice(List<FtDevice> devices, UsbDevice usbDevice, UsbInterface usbInterface)
        {
        synchronized (this.mFtdiDevices)
            {
            try {
                FtDevice ftDev = this.findDevice(usbDevice);
                if (ftDev == null)
                    {
                    ftDev = new FtDevice(mContext, mUsbManager, usbDevice, usbInterface);
                    }
                else
                    {
                    ftDev.setContext(mContext);
                    this.mFtdiDevices.remove(ftDev);
                    }

                devices.add(ftDev);
                return true;
                }
            catch (FtDeviceIOException|RobotUsbException e)
                {
                RobotLog.ee(TAG, e, "can't open FT_Device(%s) on interface(%s)", usbDevice, usbInterface);
                }
            }
        return false;
        }

    }
