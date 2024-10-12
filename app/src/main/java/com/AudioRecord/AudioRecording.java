package com.AudioRecord;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import com.utils.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @author netodevel
 */
public class AudioRecording {

    public String getmFileName() {
        return mFileName;
    }

    private String mFileName;
    private final Context mContext;

    public MediaPlayer mMediaPlayer;
    private AudioListener audioListener;
    private final MediaRecorder mRecorder;
    private long mStartingTimeMillis = 0;

    public AudioRecording(Context context) {
        mRecorder = new MediaRecorder();
        this.mContext = context;
    }

    public AudioRecording setNameFile(String nameFile) {
        this.mFileName = nameFile;
        return this;
    }

    public AudioRecording start(AudioListener audioListener) {
        this.audioListener = audioListener;

        try {

            mRecorder.reset();

            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setOutputFile(mContext.getCacheDir() + mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

        } catch (IOException e) {
            this.audioListener.onError(e);
        }
        return this;
    }

    public void stop(Boolean cancel) {
        try {
            mRecorder.stop();
        } catch (RuntimeException e) {
            deleteOutput();
        }
        mRecorder.release();
        long mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);

        if (mElapsedMillis < 1000) {
            deleteOutput();
            audioListener.onCancel();
            return;
        }

        RecordingItem recordingItem = new RecordingItem();
        recordingItem.setFilePath(mContext.getCacheDir() + mFileName);
        recordingItem.setName(mFileName);
        recordingItem.setLength((int) mElapsedMillis);
        recordingItem.setTime(System.currentTimeMillis());

        audioListener.onStop(recordingItem);
    }

    private void deleteOutput() {
        File file = new File(mContext.getCacheDir() + mFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public void play(RecordingItem recordingItem) {
        try {
            this.mMediaPlayer = new MediaPlayer();
            Logger.d("recordingItem.getFilePath()", "::" + recordingItem.getFilePath());
            this.mMediaPlayer.setDataSource(recordingItem.getFilePath());
            this.mMediaPlayer.prepare();
            this.mMediaPlayer.setLooping(false);
            this.mMediaPlayer.start();

            new Thread(this::run).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (mMediaPlayer != null) {
            new Thread(this::run).start();
        }
    }

    public void run() {

        int currentPosition = mMediaPlayer.getCurrentPosition();
        int total = mMediaPlayer.getDuration();


        while (mMediaPlayer != null && mMediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mMediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
        }
    }

    public void pauseplay() {
        if (mMediaPlayer != null) {
            new Thread(this::run).start();
        }
    }
}
