package com.wzt.media.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.wzt.media.BuildConfig;

import java.lang.ref.WeakReference;

public class WeakHandler extends Handler {

    public interface IHandler {
        void handleMessage(Message msg);
    }

    private WeakReference<IHandler> mWeakRef;

    public WeakHandler(IHandler handlermessage) {
        super();
        mWeakRef = new WeakReference<>(handlermessage);
    }
    public WeakHandler(IHandler handlermessage, Looper looper) {
        super(looper);
        mWeakRef = new WeakReference<>(handlermessage);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mWeakRef == null) {
            return;
        }
        if (mWeakRef.get() != null) {
            try {
                mWeakRef.get().handleMessage(msg);
            }catch (Exception e){//防止垃圾回收时的线程问题
                e.printStackTrace();
                if(BuildConfig.DEBUG) {
                    throw e;
                }
            }
        }
    }
}
