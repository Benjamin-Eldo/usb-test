package fr.benjamin;



import org.usb4java.LibUsb;

import javax.usb.*;
import java.util.Iterator;
import java.util.List;


public class Main {

    private static final short VENDOR_ID = 0x04e8;

    private static final short PRODUCT_ID = 0x6860;


    public static void main(String[] args) throws UsbException {
        LibUsb.init(null);
        UsbServices services = UsbHostManager.getUsbServices( );
        UsbHub root = services.getRootUsbHub( );
        UsbDevice device = findDevice(root,VENDOR_ID,PRODUCT_ID);
        System.out.println(device.toString());

        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        UsbInterface iface = configuration.getUsbInterface((byte) 1);
        iface.claim();

        UsbEndpoint endpoint = iface.getUsbEndpoint((byte) 0x83);
        UsbPipe pipe = endpoint.getUsbPipe();
        pipe.open();

        try
        {
            byte[] data = new byte[8];
            int received = pipe.syncSubmit(data);
            System.out.println(received + " bytes received");
        }
        finally
        {
            pipe.close();
        }
    }
    public static List<UsbDevice> listDevices(UsbHub hub) {
        List devices = hub.getAttachedUsbDevices( );
        Iterator iterator = devices.iterator( );
        while (iterator.hasNext( )) {
            UsbDevice device = (UsbDevice) iterator.next( );
            System.out.println(device);
            if (device.isUsbHub( )) {
                listDevices((UsbHub) device);
            }
        }
        return devices;
    }

    public static UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
    {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }


}
