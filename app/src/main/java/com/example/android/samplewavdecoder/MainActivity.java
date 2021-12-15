package com.example.android.samplewavdecoder;

import android.app.Activity;
import android.util.Log;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.example.android.samplewavdecoder.databinding.ActivityMainBinding;

import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding mBinding;
    private String mDecodedText = "";

    private int decodeWavFile(String filename) {
        Single.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                try {
                    mDecodedText = "";
                    Wav.getInstance().decodeData(getApplicationContext(), filename);
                    return Wav.getInstance().readDecodedData1(getApplicationContext());
                } catch (Exception exception) {
                    Log.d(TAG, "Exception -> " + exception.getMessage());
                    exception.printStackTrace();
                    return null;
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateDecodeProgress);
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        loadLottie(Constant.ANIMATION_SELECT, true);
        setupClickListeners();

    }

    private void setupClickListeners() {
        mBinding.btnFile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadLottie(Constant.ANIMATION_LOADER, true);
                decodeWavFile(Constant.FILE_ONE);
            }
        });

        mBinding.btnFile2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadLottie(Constant.ANIMATION_LOADER, true);
                decodeWavFile(Constant.FILE_TWO);
            }
        });

        mBinding.btnFile3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadLottie(Constant.ANIMATION_LOADER, true);
                decodeWavFile(Constant.FILE_THREE);
            }
        });

        mBinding.btnViewDecodedFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.groupLottie.setVisibility(View.GONE);
                loadDecodedText(mDecodedText);
            }
        });
    }

    private void loadLottie(String filename, boolean loop) {
        mBinding.messageLottie.setAnimation(filename);
        mBinding.messageLottie.loop(loop);

        if (mBinding.messageLottie.isAnimating()) {
            mBinding.messageLottie.cancelAnimation();
        }
        mBinding.messageLottie.playAnimation();
        updateUi(filename);
    }

    private void updateUi(String filename) {
        Log.d(TAG, "updateUi() called with: filename = [" + filename + "]");
        resetViews();

        switch (filename) {
            case Constant.ANIMATION_LOADER:
                mBinding.messageText.setText(R.string.decoding_message);
                break;
            case Constant.ANIMATION_SELECT:
                mBinding.messageText.setText(R.string.select_the_file_to_decode);
                break;
            case Constant.ANIMATION_SUCCESS:
                mBinding.messageText.setText(R.string.file_decode_success);
                mBinding.btnViewDecodedFile.setVisibility(View.VISIBLE);
                break;
            case Constant.ANIMATION_FAIL:
                mBinding.messageText.setText(R.string.file_decode_fail);
                break;
        }

    }

    private void resetViews() {
        Log.d(TAG, "resetViews() called");
        if (mBinding.scrollViewPreview.getVisibility() == View.VISIBLE) {
            mBinding.scrollViewPreview.setVisibility(View.GONE);
        }

        if (mBinding.groupLottie.getVisibility() != View.VISIBLE) {
            mBinding.groupLottie.setVisibility(View.VISIBLE);
        }

        if (mBinding.btnViewDecodedFile.getVisibility() == View.VISIBLE) {
            mBinding.btnViewDecodedFile.setVisibility(View.GONE);
        }
    }

    private void updateDecodeProgress(String resultText) {
        Log.d(TAG, "updateDecodeProgress() called");
        if (resultText != null) {
            loadLottie(Constant.ANIMATION_SUCCESS, false);
            mDecodedText = getResources().getString(R.string.heading_decoded_message)+":"+"\n\n"+resultText;
        } else {
            loadLottie(Constant.ANIMATION_FAIL, false);
        }
    }

    private void loadDecodedText(String resultText) {
        mBinding.decodedText.setText(resultText);
        mBinding.groupLottie.setVisibility(View.GONE);
        mBinding.btnViewDecodedFile.setVisibility(View.GONE);
        mBinding.scrollViewPreview.setVisibility(View.VISIBLE);
        mBinding.decodedText.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
