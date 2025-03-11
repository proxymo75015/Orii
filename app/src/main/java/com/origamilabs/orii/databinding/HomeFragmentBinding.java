package com.origamilabs.orii.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.SharedViewModel;

/* loaded from: classes2.dex */
public abstract class HomeFragmentBinding extends ViewDataBinding {
    public final ImageView batteryLevelImageView;
    public final ConstraintLayout connectFailLayout;
    public final ConstraintLayout connectedLayout;
    public final TextView connectedTextView;
    public final ConstraintLayout connectingLayout;
    public final ImageView croppedOriiImageView;
    public final TextView failedHintTextView;
    public final ConstraintLayout homeLayout;

    @Bindable
    protected Boolean mCanFirmwareUpdate;

    @Bindable
    protected SharedViewModel mSharedViewModel;
    public final Button soundTestButton;
    public final TextView statusTextView;
    public final Button stopSearchingButton;
    public final TextView textView;
    public final Button timeOutRetryButton;
    public final Button updateButton;
    public final TextView welcomeTextView;

    public abstract void setCanFirmwareUpdate(Boolean bool);

    public abstract void setSharedViewModel(SharedViewModel sharedViewModel);

    protected HomeFragmentBinding(Object obj, View view, int i, ImageView imageView, ConstraintLayout constraintLayout, ConstraintLayout constraintLayout2, TextView textView, ConstraintLayout constraintLayout3, ImageView imageView2, TextView textView2, ConstraintLayout constraintLayout4, Button button, TextView textView3, Button button2, TextView textView4, Button button3, Button button4, TextView textView5) {
        super(obj, view, i);
        this.batteryLevelImageView = imageView;
        this.connectFailLayout = constraintLayout;
        this.connectedLayout = constraintLayout2;
        this.connectedTextView = textView;
        this.connectingLayout = constraintLayout3;
        this.croppedOriiImageView = imageView2;
        this.failedHintTextView = textView2;
        this.homeLayout = constraintLayout4;
        this.soundTestButton = button;
        this.statusTextView = textView3;
        this.stopSearchingButton = button2;
        this.textView = textView4;
        this.timeOutRetryButton = button3;
        this.updateButton = button4;
        this.welcomeTextView = textView5;
    }

    public SharedViewModel getSharedViewModel() {
        return this.mSharedViewModel;
    }

    public Boolean getCanFirmwareUpdate() {
        return this.mCanFirmwareUpdate;
    }

    public static HomeFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static HomeFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (HomeFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.home_fragment, viewGroup, z, obj);
    }

    public static HomeFragmentBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static HomeFragmentBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (HomeFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.home_fragment, null, false, obj);
    }

    public static HomeFragmentBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static HomeFragmentBinding bind(View view, Object obj) {
        return (HomeFragmentBinding) bind(obj, view, R.layout.home_fragment);
    }
}
