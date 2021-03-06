package com.ykbjson.lib.unity3dplugin;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.unity3d.player.UnityPlayer;
import com.ykbjson.lib.unity3dplugin.base.impl.BaseActivity;
import com.ykbjson.lib.unity3dplugin.internal.IGetUnity3DCall;
import com.ykbjson.lib.unity3dplugin.internal.IOnUnity3DCall;
import com.ykbjson.lib.unity3dplugin.internal.IUnityPlayerContainer;

/**
 * Desription：Base Unity3D Player Activity.
 * <BR/>
 * If you want to implement Fragment to load the Unity scene, then the Fragment needs to refer to {@link UnityPlayerFragment} and {@link UnityPlayerFragmentV4}
 *    To implement, then overload the {@link #generateIOnUnity3DCallDelegate(UnityPlayer, Bundle)} method to return the conforming Fragment.
 * <BR/>
 * Creator：yankebin
 * <BR/>
 * CreatedAt：2018/12/1
 */
public abstract class UnityPlayerActivity extends BaseActivity implements IGetUnity3DCall, IOnUnity3DCall, IUnityPlayerContainer {
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    protected IOnUnity3DCall mOnUnity3DCallDelegate;

    @Override
    @CallSuper
    public void onViewCreated(@NonNull Bundle params, @NonNull View contentView) {
        initUnityPlayer(params);
    }

    /**
     * Initialize Unity3D related
     *
     * @param bundle {@link Bundle}
     */
    protected void initUnityPlayer(@NonNull Bundle bundle) {
        mUnityPlayer = new UnityPlayer(this);
        mOnUnity3DCallDelegate = generateIOnUnity3DCallDelegate(mUnityPlayer, bundle);
        if (null != mOnUnity3DCallDelegate) {
            if (mOnUnity3DCallDelegate instanceof Fragment) {
                getSupportFragmentManager().beginTransaction().replace(unityPlayerContainerId(),
                        (Fragment) mOnUnity3DCallDelegate, ((Fragment) mOnUnity3DCallDelegate)
                                .getClass().getName()).commit();
            } else if (mOnUnity3DCallDelegate instanceof android.app.Fragment) {
                getFragmentManager().beginTransaction().replace(unityPlayerContainerId(),
                        (android.app.Fragment) mOnUnity3DCallDelegate, ((android.app.Fragment) mOnUnity3DCallDelegate)
                                .getClass().getName()).commit();
            } else if (mOnUnity3DCallDelegate instanceof View) {
                final ViewGroup unityContainer = findViewById(unityPlayerContainerId());
                unityContainer.addView((View) mOnUnity3DCallDelegate);
            } else {
                throw new IllegalArgumentException("Not support type : " + mOnUnity3DCallDelegate.toString());
            }
        } else {
            mOnUnity3DCallDelegate = this;
            final ViewGroup unityContainer = findViewById(unityPlayerContainerId());
            unityContainer.addView(mUnityPlayer);
            mUnityPlayer.requestFocus();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
    }

    // Quit Unity
    @Override
    protected void onDestroy() {
        if (null != mUnityPlayer) {
            mUnityPlayer.quit();
        }
        super.onDestroy();
    }

    // Pause Unity
    @Override
    protected void onPause() {
        super.onPause();
        if (null != mUnityPlayer) {
            mUnityPlayer.pause();
        }
    }

    // Resume Unity
    @Override
    protected void onResume() {
        super.onResume();
        if (null != mUnityPlayer) {
            mUnityPlayer.resume();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (null != mUnityPlayer) {
            mUnityPlayer.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mUnityPlayer) {
            mUnityPlayer.stop();
        }
    }

    // Low Memory Unity
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (null != mUnityPlayer) {
            mUnityPlayer.lowMemory();
        }
    }

    // Trim Memory Unity
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL) {
            if (null != mUnityPlayer) {
                mUnityPlayer.lowMemory();
            }
        }
    }

    // This ensures the layout will be correct.
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (null != mUnityPlayer) {
            mUnityPlayer.configurationChanged(newConfig);
        }
    }

    // Notify Unity of the focus change.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (null != mUnityPlayer) {
            mUnityPlayer.windowFocusChanged(hasFocus);
        }
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE) {
            if (null != mUnityPlayer) {
                return mUnityPlayer.injectEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyUp(keyCode, event);
        }
        if (null != mUnityPlayer) {
            return mUnityPlayer.injectEvent(event);
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event);
        }
        if (null != mUnityPlayer) {
            return mUnityPlayer.injectEvent(event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != mUnityPlayer) {
            return mUnityPlayer.injectEvent(event);
        }
        return super.onTouchEvent(event);
    }

    /*API12*/
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (null != mUnityPlayer) {
            return mUnityPlayer.injectEvent(event);
        }
        return super.onGenericMotionEvent(event);
    }

    @Nullable
    @Override
    public Context gatContext() {
        return this;
    }

    @NonNull
    @Override
    public IOnUnity3DCall getOnUnity3DCall() {
        //Perhaps this method is called after Unity is created after the activity is created,
        // so there is no problem for the time being.
        return mOnUnity3DCallDelegate;
    }

    @Nullable
    protected IOnUnity3DCall generateIOnUnity3DCallDelegate(@NonNull UnityPlayer unityPlayer,
                                                            @Nullable Bundle bundle) {
        return null;
    }
}