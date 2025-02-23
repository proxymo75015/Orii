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
import com.origamilabs.orii.ui.main.SharedViewModel;

/* loaded from: classes2.dex */
public abstract class FirmwareTestFragmentBinding extends ViewDataBinding {
    public final ConstraintLayout connectFailLayout;
    public final ConstraintLayout connectedLayout;
    public final TextView connectedTextView;
    public final ConstraintLayout connectingLayout;
    public final ImageView croppedOriiImageView;
    public final TextView failedHintTextView;
    public final ConstraintLayout homeLayout;

    @Bindable
    protected Boolean mCanFirmwareForceUpdate;

    @Bindable
    protected SharedViewModel mSharedViewModel;
    public final TextView statusTextView;
    public final Button stopSearchingButton;
    public final TextView textView;
    public final Button timeOutRetryButton;
    public final Button updateButton;
    public final Button v68Button;
    public final Button v69Button;
    public final Button v70Button;
    public final Button v71Button;
    public final TextView welcomeTextView;

    public abstract void setCanFirmwareForceUpdate(Boolean bool);

    public abstract void setSharedViewModel(SharedViewModel sharedViewModel);

    protected FirmwareTestFragmentBinding(Object obj, View view, int i, ConstraintLayout constraintLayout, ConstraintLayout constraintLayout2, TextView textView, ConstraintLayout constraintLayout3, ImageView imageView, TextView textView2, ConstraintLayout constraintLayout4, TextView textView3, Button button, TextView textView4, Button button2, Button button3, Button button4, Button button5, Button button6, Button button7, TextView textView5) {
        super(obj, view, i);
        this.connectFailLayout = constraintLayout;
        this.connectedLayout = constraintLayout2;
        this.connectedTextView = textView;
        this.connectingLayout = constraintLayout3;
        this.croppedOriiImageView = imageView;
        this.failedHintTextView = textView2;
        this.homeLayout = constraintLayout4;
        this.statusTextView = textView3;
        this.stopSearchingButton = button;
        this.textView = textView4;
        this.timeOutRetryButton = button2;
        this.updateButton = button3;
        this.v68Button = button4;
        this.v69Button = button5;
        this.v70Button = button6;
        this.v71Button = button7;
        this.welcomeTextView = textView5;
    }

    public SharedViewModel getSharedViewModel() {
        return this.mSharedViewModel;
    }

    public Boolean getCanFirmwareForceUpdate() {
        return this.mCanFirmwareForceUpdate;
    }

    public static FirmwareTestFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static FirmwareTestFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (FirmwareTestFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.firmware_test_fragment, viewGroup, z, obj);
    }

    public static FirmwareTestFragmentBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static FirmwareTestFragmentBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (FirmwareTestFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.firmware_test_fragment, null, false, obj);
    }

    public static FirmwareTestFragmentBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static FirmwareTestFragmentBinding bind(View view, Object obj) {
        return (FirmwareTestFragmentBinding) bind(obj, view, R.layout.firmware_test_fragment);
    }
}
