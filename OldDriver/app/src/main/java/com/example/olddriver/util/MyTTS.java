package com.example.olddriver.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

/**
 * Created by lzp on 2016/12/19.
 */

public class MyTTS {
    private static SynthesizerListener listener = null;
    private static SpeechSynthesizer mTTS = null;

    public static void init(Context context){
        SpeechUtility.createUtility(context, SpeechConstant.APPID +"=5857b079");

        mTTS= SpeechSynthesizer.createSynthesizer(context, null);
        //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTTS.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTTS.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTTS.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTTS.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端

        //3.开始合成
        listener = new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                Log.i("tts", "begin");
            }

            @Override
            public void onBufferProgress(int i, int i1, int i2, String s) {
            }

            @Override
            public void onSpeakPaused() {
                Log.i("tts", "pause");
            }

            @Override
            public void onSpeakResumed() {

            }

            @Override
            public void onSpeakProgress(int i, int i1, int i2) {

            }

            @Override
            public void onCompleted(SpeechError speechError) {

            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {
                Log.i("tts", "event");
            }
        };
        //mTTS.startSpeaking("科大讯飞，让世界聆听我们的声音", listener);
    }

    public static void speakText(String src){
        /*if(mTTS.isSpeaking())
            mTTS.stopSpeaking();*/

        mTTS.startSpeaking(src, listener);
    }


}
