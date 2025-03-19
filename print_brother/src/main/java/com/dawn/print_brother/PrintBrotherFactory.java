package com.dawn.print_brother;

import android.content.Context;

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

    

}
