package com.dawn.print_brother;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.brother.sdk.BrotherAndroidLib;
import com.brother.sdk.common.Callback;
import com.brother.sdk.common.ConnectorDescriptor;
import com.brother.sdk.common.ConnectorManager;
import com.brother.sdk.common.IConnector;
import com.brother.sdk.common.Job;
import com.brother.sdk.common.device.CountrySpec;
import com.brother.sdk.common.device.Device;
import com.brother.sdk.common.device.Duplex;
import com.brother.sdk.common.device.MediaSize;
import com.brother.sdk.common.device.Resolution;
import com.brother.sdk.common.device.printer.PrintMargin;
import com.brother.sdk.common.device.printer.PrintOrientation;
import com.brother.sdk.common.device.printer.PrintQuality;
import com.brother.sdk.common.device.printer.PrintScale;
import com.brother.sdk.print.PrintJob;
import com.brother.sdk.print.PrintParameters;
import com.brother.sdk.usb.discovery.UsbConnectorDiscovery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 兄弟打印机工厂类
 */
public class PrintBrotherFactory {
    // 单例模式
    private static PrintBrotherFactory printBrotherFactory;
    private Context mContext;

    private PrintBrotherFactory(Context context) {
        this.mContext = context;
    }

    public static PrintBrotherFactory getInstance(Context context) {
        if (printBrotherFactory == null) {
            synchronized (PrintBrotherFactory.class) {
                if (printBrotherFactory == null) {
                    printBrotherFactory = new PrintBrotherFactory(context);
                }
            }
        }
        return printBrotherFactory;
    }


    private UsbConnectorDiscovery mDiscovery;
    private ConnectorDescriptor fDescriptor;
    private final PrintParameters mPrintParameters = new PrintParameters();
    private PrintJob mPrintJob = null;
    private IConnector fConnector;
    public void initPrintBrother(){
        BrotherAndroidLib.initialize(mContext.getApplicationContext());
        mDiscovery = new UsbConnectorDiscovery();
        mDiscovery.startDiscover(new ConnectorManager.OnDiscoverConnectorListener(){

            @Override
            public void onDiscover(ConnectorDescriptor descriptor) {
                // 发现打印机
                Log.e("dawn", "onDiscover: " + descriptor);
                Log.e("dawn", "onDiscover: " + descriptor.support(ConnectorDescriptor.Function.Print));
                Log.e("dawn", "onDiscover: " + descriptor.support(ConnectorDescriptor.Function.Scan));
                if (descriptor.support(ConnectorDescriptor.Function.Print) || descriptor.support(ConnectorDescriptor.Function.Scan)) {
                    fDescriptor = descriptor;
                    validateDevice(descriptor);
                    setPrintSize();
                }
            }
        });
    }

    private void validateDevice(ConnectorDescriptor descriptor) {
        try {
            fConnector = descriptor.createConnector(CountrySpec.fromISO_3166_1_Alpha2(mContext.getResources().getConfiguration().locale.getCountry()));
            if (fConnector != null) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int failNum = 0;
    public void printImage(Uri path){
        Thread thread = new Thread(() -> {
            List<File> images = new ArrayList<File>();
            try{
                InputStream in = mContext.getContentResolver().openInputStream(path);
                File mSelectedImage = saveToCache(in, mContext.getExternalCacheDir());
                images.add(mSelectedImage);
            }catch (Exception e){
                e.printStackTrace();
            }

            Job.JobState jobState = Job.JobState.ErrorJob;
            try
            {
                mPrintParameters.quality = PrintQuality.Draft;
                mPrintParameters.resolution = new Resolution(300, 300);
                mPrintParameters.margin = PrintMargin.Normal;
                mPrintParameters.scale = PrintScale.FitToPrintableArea;
                mPrintParameters.orientation = PrintOrientation.AutoRotation;
                mPrintJob = new PrintJob(mPrintParameters, mContext, images, new Callback()
                {
                    @Override
                    public void onUpdateProcessProgress(int value)
                    {
                    }
                    @Override
                    public void onNotifyProcessAlive()
                    {
                    }
                });
                jobState = fConnector.submit(mPrintJob);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (jobState != Job.JobState.SuccessJob){
                    Log.e("dawn", "onDiscover: " + "打印失败");
                    if(failNum == 0){
                        failNum = 1;
                        printImage(path);
                    }
                }else{
                    Log.e("dawn", "onDiscover: " + "打印成功");
                    failNum = 0;
                }
            }
        });
        thread.start();

    }
    /**
     * @param in
     * @return
     */
    private static File saveToCache(InputStream in, File folder)
    {
        try
        {
            File tempFile = File.createTempFile("tempImage", ".tmp", folder);
            FileOutputStream fout = new FileOutputStream(tempFile);
            copyFile(fout, in);
            fout.flush();
            fout.close();

            return tempFile;
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    /**
     * @param out
     * @param in
     * @throws java.io.IOException
     */
    public static void copyFile(OutputStream out, InputStream in) throws IOException
    {
        byte[] data = new byte[4096];
        int nRead = 0;
        while ((nRead = in.read(data, 0, data.length)) > 0)
        {
            out.write(data, 0, nRead);
        }
    }

    private void setPrintSize(){
        if(fConnector == null)
            return;
        Device device = fConnector.getDevice();
        if ( device != null && device.printer != null)
        {
            List<MediaSize> params = device.printer.capabilities.paperSizes;

            CharSequence[] docSizes = new CharSequence[params.size()];
            int index = 0;
            for (MediaSize m : params)
            {
                docSizes[index++] = m.name;
                Log.i("dawn", "setPrintSize: " + m.name);
                if(m.name != null && !m.name.isEmpty()){
                    if(m.name.contains("a3")){
                        mPrintParameters.paperSize = m;
                        break;
                    }
                }
            }


        }
    }


}
