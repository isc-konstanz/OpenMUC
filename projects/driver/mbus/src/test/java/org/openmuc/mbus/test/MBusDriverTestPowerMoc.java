package org.openmuc.mbus.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmuc.framework.driver.mbus.MBusDriver;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MBusDriver.class)
public class MBusDriverTestPowerMoc {

    @Test
    public void testGetDriverInfo() {
        MBusDriver mdriver = new MBusDriver();
        Assert.assertTrue(mdriver.getInfo().getId().equals("mbus"));
    }

//    /**
//     * Test the connect Method of MBusDriver without the functionality of jMBus Called the
//     * {@link #connect(String channelAdress, String bautrate) connect} Method
//     * 
//     * @throws Exception
//     */
//    @Test
//    public void testConnectSucceed() throws Exception {
//        String channelAdress = "/dev/ttyS100:5";
//        String bautrate = "2400";
//        connect(channelAdress, bautrate);
//    }
//
//    @Test
//    public void testConnectSucceedWithSecondary() throws Exception {
//        String channelAdress = "/dev/ttyS100:74973267a7320404";
//        String bautrate = "2400";
//        connect(channelAdress, bautrate);
//    }
//
//    @Test
//    public void testConnectionBautrateIsEmpty() throws Exception {
//        String channelAdress = "/dev/ttyS100:5";
//        String bautrate = "";
//        connect(channelAdress, bautrate);
//    }
//
//    @Test
//    public void TestConnectTwoTimes() throws Exception {
//        String channelAdress = "/dev/ttyS100:5";
//        String bautrate = "2400";
//        MBusDriver mdriver = new MBusDriver();
//        MBusConnection mockedMBusConnection = PowerMockito.mock(MBusConnection.class);
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.doNothing().when(mockedMBusConnection).open();
//        PowerMockito.doNothing().when(mockedMBusConnection).linkReset(Matchers.anyInt());
//        PowerMockito.when(mockedMBusConnection.read(Matchers.anyInt())).thenReturn(null);
//        Assert.assertNotNull(mdriver.connect(channelAdress, bautrate));
//        Assert.assertNotNull(mdriver.connect(channelAdress, bautrate));
//    }
//
//    /**
//     * This Testmethod will test the connect Method of MBus Driver, without testing jMBus Library functions. With
//     * Mockito and PowerMockito its possible to do this. At first it will create an MBusDriver Objekt. Then we mocking
//     * an MBusConnection Objects without functionality. If new MBusConnection will created, it will return the mocked Object
//     * "mockedMBusConnection". If the linkReset Method will called, it will do nothing. If the read Method will call, we return
//     * null.
//     * 
//     * @param deviceAddress
//     * @param bautrate
//     * @throws IOException
//     * @throws InterruptedIOException
//     * @throws Exception
//     * @throws ArgumentSyntaxException
//     * @throws ConnectionException
//     */
//    private void connect(String deviceAddress, String bautrate) throws Exception {
//        MBusDriver mdriver = new MBusDriver();
//        MBusConnection mockedMBusConnection = PowerMockito.mock(MBusConnection.class);
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.doNothing().when(mockedMBusConnection).open();
//        PowerMockito.doNothing().when(mockedMBusConnection).linkReset(Matchers.anyInt());
//        PowerMockito.when(mockedMBusConnection.read(Matchers.anyInt())).thenReturn(null);
//        Assert.assertNotNull(mdriver.connect(deviceAddress, bautrate));
//    }
//
//    @Test(expected = ArgumentSyntaxException.class)
//    public void testConnectionArgumentSyntaxExceptionNoPortSet() throws Exception {
//        String channelAdress = "/dev/ttyS100:";
//        String bautrate = "2400";
//        connect(channelAdress, bautrate);
//    }
//
//    @Test(expected = ArgumentSyntaxException.class)
//    public void testConnectWithWrongSecondary() throws Exception {
//        String channelAdress = "/dev/ttyS100:74973267a20404";
//        String bautrate = "2400";
//        connect(channelAdress, bautrate);
//    }
//
//    @Test(expected = ArgumentSyntaxException.class)
//    public void testConnectionChannelAddressEmpty() throws Exception {
//        String channelAdress = "";
//        String bautrate = "2400";
//        connect(channelAdress, bautrate);
//    }
//
//    @Test(expected = ArgumentSyntaxException.class)
//    public void testConnectionArgumentSyntaxExceptionChannelAddressWrongSyntax() throws Exception {
//        String channelAdress = "/dev/ttyS100:a";
//        String bautrate = "2400";
//        connect(channelAdress, bautrate);
//    }
//
//    @Test(expected = ArgumentSyntaxException.class)
//    public void testConnectionArgumentSyntaxExceptionToManyArguments() throws Exception {
//        String channelAdress = "/dev/ttyS100:5:1";
//        String bautrate = "2400";
//        connect(channelAdress, bautrate);
//    }
//
//    @Test(expected = ArgumentSyntaxException.class)
//    public void testConnectionArgumentSyntaxExceptionBautIsNotANumber() throws Exception {
//        String channelAdress = "/dev/ttyS100:5";
//        String bautrate = "asd";
//        connect(channelAdress, bautrate);
//    }

//    @Test(expected = ConnectionException.class)
//    public void testMBusConnectionOpenThrowsIllArgException() throws Exception {
//        MBusDriver mdriver = new MBusDriver();
//        MBusConnection mockedMBusConnection = PowerMockito.mock(MBusConnection.class);
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.doThrow(new IOException()).when(mockedMBusConnection).open();
//        PowerMockito.doNothing().when(mockedMBusConnection).linkReset(Matchers.anyInt());
//        PowerMockito.when(mockedMBusConnection.read(Matchers.anyInt())).thenReturn(null);
//        mdriver.connect("/dev/ttyS100:5", "2400");
//    }
//
//    @Test(expected = ConnectionException.class)
//    public void testMBusConnectionLinkResetThrowsIOException() throws Exception {
//        MBusDriver mdriver = new MBusDriver();
//        MBusConnection mockedMBusConnection = PowerMockito.mock(MBusConnection.class);
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.doThrow(new IOException()).when(mockedMBusConnection).linkReset(Matchers.anyInt());
//        PowerMockito.doNothing().when(mockedMBusConnection).open();
//        PowerMockito.when(mockedMBusConnection.read(Matchers.anyInt())).thenReturn(null);
//        mdriver.connect("/dev/ttyS100:5", "2400");
//    }
//
//    @Test(expected = ConnectionException.class)
//    public void testMBusConnectionReadThrowsInterruptedIOException() throws Exception {
//        MBusDriver mdriver = new MBusDriver();
//        MBusConnection mockedMBusConnection = PowerMockito.mock(MBusConnection.class);
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.doThrow(new InterruptedIOException()).when(mockedMBusConnection).read(Matchers.anyInt());
//        PowerMockito.doNothing().when(mockedMBusConnection).linkReset(Matchers.anyInt());
//        PowerMockito.doNothing().when(mockedMBusConnection).open();
//        mdriver.connect("/dev/ttyS100:5", "2400");
//    }
//
//    @Test(expected = ConnectionException.class)
//    public void testMBusConnectionReadThrowsInterruptedIOExceptionAtSecondRun() throws Exception {
//        MBusDriver mdriver = new MBusDriver();
//        MBusConnection mockedMBusConnection = PowerMockito.mock(MBusConnection.class);
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.doNothing().when(mockedMBusConnection).open();
//        PowerMockito.doNothing().when(mockedMBusConnection).linkReset(Matchers.anyInt());
//        PowerMockito.when(mockedMBusConnection.read(Matchers.anyInt())).thenReturn(null);
//        Assert.assertNotNull(mdriver.connect("/dev/ttyS100:5", "2400"));
//        PowerMockito.doThrow(new InterruptedIOException()).when(mockedMBusConnection).read(Matchers.anyInt());
//        PowerMockito.doNothing().when(mockedMBusConnection).linkReset(Matchers.anyInt());
//        PowerMockito.doNothing().when(mockedMBusConnection).open();
//        mdriver.connect("/dev/ttyS100:5", "2400");
//    }
//
//    // ******************* SCAN TESTS ********************//
//
//    public void scan(String settings) throws Exception {
//        final MBusDriver mdriver = new MBusDriver();
//        DriverDeviceScanListener ddsl = new DriverDeviceScanListener() {
//
//            @Override
//            public void scanProgressUpdate(int progress) {
//                // TODO Auto-generated method stub
//                System.out.println("Progress: " + progress + "%");
//
//            }
//
//            @Override
//            public void deviceFound(DeviceScanInfo scanInfo) {
//                System.out.println("Device Found: " + scanInfo.toString());
//
//            }
//        };
//
//        MBusConnection mockedMBusConnection = PowerMockito.mock(MBusConnection.class);
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.when(mockedMBusConnection.read(1)).thenReturn(new VariableDataStructure(null, 0, 0, null, null));
//        PowerMockito.when(mockedMBusConnection.read(250)).thenThrow(new InterruptedIOException());
//        PowerMockito.when(mockedMBusConnection.read(Matchers.anyInt())).thenThrow(new InterruptedIOException());
//        class InterruptScanThread implements Runnable {
//
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(100);
//                    mdriver.interruptDeviceScan();
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//            }
//
//        }
//        new InterruptScanThread().run();
//        mdriver.scanForDevices(settings, ddsl);
//
//    }
//
//    @Test
//    public void testScanForDevices() throws Exception {
//
//        scan("/dev/ttyS100:2400");
//    }
//
//    @Test
//    public void testScanForDevicesWithOutBautRate() throws Exception {
//
//        scan("/dev/ttyS100");
//    }
//
//    @Test(expected = ArgumentSyntaxException.class)
//    public void testScanForDevicesArgumentSyntaxException() throws Exception {
//        // NO Setting is set!
//        scan(new String());
//    }
//
//    @Test(expected = ArgumentSyntaxException.class)
//    public void testScanForDevicesBautrateIsNotANumberArgumentSyntaxException() throws Exception {
//        // Bautrate isn't a number
//        scan("/dev/ttyS100:aaa");
//    }
//
//    @Test(expected = ArgumentSyntaxException.class)
//    public void testScanForDevicesToManyArgumentsArgumentSyntaxException() throws Exception {
//        // TO Many Arguments
//        scan("/dev/ttyS100:2400:assda");
//    }
//
//    @Test(expected = ScanInterruptedException.class)
//    public void testInterrupedException() throws Exception {
//        final MBusDriver mdriver = new MBusDriver();
//        DriverDeviceScanListener ddsl = new DriverDeviceScanListener() {
//
//            @Override
//            public void scanProgressUpdate(int progress) {
//                // TODO Auto-generated method stub
//                System.out.println("Progress: " + progress + "%");
//
//            }
//
//            @Override
//            public void deviceFound(DeviceScanInfo scanInfo) {
//                System.out.println("Device Found: " + scanInfo.toString());
//                mdriver.interruptDeviceScan();
//            }
//        };
//
//        MBusConnection mockedMBusConnection = PowerMockito.mock(MBusConnection.class);
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.when(mockedMBusConnection.read(Matchers.anyInt()))
//                .thenReturn(new VariableDataStructure(null, 0, 0, null, null));
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.doNothing().when(mockedMBusConnection).open();
//        PowerMockito.doNothing().when(mockedMBusConnection).linkReset(Matchers.anyInt());
//        PowerMockito.when(mockedMBusConnection.read(Matchers.anyInt())).thenReturn(null);
//
//        Assert.assertNotNull(mdriver.connect("/dev/ttyS100:5", "2400"));
//        mdriver.scanForDevices("/dev/ttyS100:2400", ddsl);
//
//    }
//
//    @Test(expected = ArgumentSyntaxException.class)
//    public void testScanMBusConnectionOpenThrowsIllArgException() throws Exception {
//        MBusDriver mdriver = new MBusDriver();
//        MBusConnection mockedMBusConnection = PowerMockito.mock(MBusConnection.class);
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.doThrow(new IllegalArgumentException()).when(mockedMBusConnection).open();
//        PowerMockito.doNothing().when(mockedMBusConnection).linkReset(Matchers.anyInt());
//        PowerMockito.when(mockedMBusConnection.read(Matchers.anyInt())).thenReturn(null);
//        mdriver.scanForDevices("/dev/ttyS100:2400", null);
//    }
//
//    @Test(expected = ScanException.class)
//    public void testScanMBusConnectionOpenIOException() throws Exception {
//        MBusDriver mdriver = new MBusDriver();
//        MBusConnection mockedMBusConnection = PowerMockito.mock(MBusConnection.class);
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.doThrow(new IOException()).when(mockedMBusConnection).open();
//        PowerMockito.doNothing().when(mockedMBusConnection).linkReset(Matchers.anyInt());
//        PowerMockito.when(mockedMBusConnection.read(Matchers.anyInt())).thenReturn(null);
//        mdriver.scanForDevices("/dev/ttyS100:2400", null);
//    }
//
//    @Test(expected = ScanException.class)
//    public void testScanMBusConnectionReadThrowsIOException() throws Exception {
//        final MBusDriver mdriver = new MBusDriver();
//        DriverDeviceScanListener ddsl = new DriverDeviceScanListener() {
//
//            @Override
//            public void scanProgressUpdate(int progress) {
//                // TODO Auto-generated method stub
//                System.out.println("Progress: " + progress + "%");
//
//            }
//
//            @Override
//            public void deviceFound(DeviceScanInfo scanInfo) {
//                System.out.println("Device Found: " + scanInfo.toString());
//
//            }
//        };
//
//        MBusConnection mockedMBusConnection = PowerMockito.mock(MBusConnection.class);
//        PowerMockito.whenNew(MBusConnection.class).withAnyArguments().thenReturn(mockedMBusConnection);
//        PowerMockito.when(mockedMBusConnection.read(1)).thenReturn(new VariableDataStructure(null, 0, 0, null, null));
//        PowerMockito.when(mockedMBusConnection.read(250)).thenThrow(new InterruptedIOException());
//        PowerMockito.when(mockedMBusConnection.read(Matchers.anyInt())).thenThrow(new IOException());
//        class InterruptScanThread implements Runnable {
//
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(100);
//                    mdriver.interruptDeviceScan();
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//            }
//
//        }
//        new InterruptScanThread().run();
//        mdriver.scanForDevices("/dev/ttyS100:2400", ddsl);
//
//    }

}
