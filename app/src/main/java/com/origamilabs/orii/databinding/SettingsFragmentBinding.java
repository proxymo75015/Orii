package com.origamilabs.orii.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.main.SharedViewModel;
import com.origamilabs.orii.ui.main.settings.SettingsViewModel;

/* loaded from: classes2.dex */
public abstract class SettingsFragmentBinding extends ViewDataBinding {
    public final ConstraintLayout constraintLayout;
    public final ConstraintLayout gestureCallControlConstraintLayout;
    public final TextView gestureCallControlContentTextView;
    public final SwitchCompat gestureCallControlSwitch;
    public final TextView gestureCallControlTitleTextView;
    public final ConstraintLayout gestureDownDoubleTapConstraintLayout;
    public final TextView gestureDownDoubleTapContent1TextView;
    public final TextView gestureDownDoubleTapContent2TextView;
    public final SwitchCompat gestureDownDoubleTapSwitch;
    public final TextView gestureDownDoubleTapTitleTextView;
    public final ConstraintLayout gestureFlatDoubleTapConstraintLayout;
    public final TextView gestureFlatDoubleTapContentTextView;
    public final SwitchCompat gestureFlatDoubleTapSwitch;
    public final TextView gestureFlatDoubleTapTitleTextView;
    public final ConstraintLayout gestureFlatTripleTapConstraintLayout;
    public final TextView gestureFlatTripleTapContentTextView;
    public final ImageView gestureFlatTripleTapQuestionMarkImageView;
    public final SwitchCompat gestureFlatTripleTapSwitch;
    public final TextView gestureFlatTripleTapTitleTextView;
    public final TextView gestureFlatTripleTapWebHookUrlTextView;
    public final ConstraintLayout gestureReservedConstraintLayout;
    public final TextView gestureReservedContentTextView;
    public final SwitchCompat gestureReservedSwitch;
    public final TextView gestureReservedTitleTextView;
    public final ConstraintLayout gestureReverseDoubleTapConstraintLayout;
    public final TextView gestureReverseDoubleTapContentTextView;
    public final ImageView gestureReverseDoubleTapQuestionMarkImageView;
    public final SwitchCompat gestureReverseDoubleTapSwitch;
    public final TextView gestureReverseDoubleTapTitleTextView;
    public final TextView gestureReverseDoubleTapWebHookUrlTextView;
    public final ConstraintLayout gestureSideDoubleTapConstraintLayout;
    public final TextView gestureSideDoubleTapContentTextView;
    public final SwitchCompat gestureSideDoubleTapSwitch;
    public final TextView gestureSideDoubleTapTitleTextView;
    public final CardView gestureTapStrengthCardView;
    public final TextView gestureTapStrengthContentTextView;
    public final SeekBar gestureTapStrengthSeekBar;
    public final TextView gestureTapStrengthStrongTextView;
    public final TextView gestureTapStrengthTitleTextView;
    public final TextView gestureTapStrengthWeakTextView;
    public final ConstraintLayout gestureUpDoubleTapConstraintLayout;
    public final TextView gestureUpDoubleTapContent1TextView;
    public final TextView gestureUpDoubleTapContent2TextView;
    public final SwitchCompat gestureUpDoubleTapSwitch;
    public final TextView gestureUpDoubleTapTitleTextView;
    public final CardView gesturesCardView;
    public final TextView gesturesContentTextView;
    public final AppCompatCheckBox gesturesControllerCheckBox;
    public final TextView gesturesControllerTextView;
    public final TextView gesturesTitleTextView;
    public final TextView imageView3;
    public final TextView imageView4;
    public final ImageView imageView5;

    @Bindable
    protected SettingsViewModel mSettingsViewModel;

    @Bindable
    protected SharedViewModel mSharedViewModel;
    public final ImageView micModeLeftImageView;
    public final ImageView micModeRightImageView;
    public final CardView readSpeedCardView;
    public final TextView readSpeedContentTextView;
    public final TextView readSpeedTitleTextView;
    public final SeekBar readoutSpeedSeekBar;
    public final CardView swapMicCardView;
    public final TextView swapMicContentTextView;
    public final TextView swapMicTitleTextView;

    public abstract void setSettingsViewModel(SettingsViewModel settingsViewModel);

    public abstract void setSharedViewModel(SharedViewModel sharedViewModel);

    protected SettingsFragmentBinding(Object obj, View view, int i, ConstraintLayout constraintLayout, ConstraintLayout constraintLayout2, TextView textView, SwitchCompat switchCompat, TextView textView2, ConstraintLayout constraintLayout3, TextView textView3, TextView textView4, SwitchCompat switchCompat2, TextView textView5, ConstraintLayout constraintLayout4, TextView textView6, SwitchCompat switchCompat3, TextView textView7, ConstraintLayout constraintLayout5, TextView textView8, ImageView imageView, SwitchCompat switchCompat4, TextView textView9, TextView textView10, ConstraintLayout constraintLayout6, TextView textView11, SwitchCompat switchCompat5, TextView textView12, ConstraintLayout constraintLayout7, TextView textView13, ImageView imageView2, SwitchCompat switchCompat6, TextView textView14, TextView textView15, ConstraintLayout constraintLayout8, TextView textView16, SwitchCompat switchCompat7, TextView textView17, CardView cardView, TextView textView18, SeekBar seekBar, TextView textView19, TextView textView20, TextView textView21, ConstraintLayout constraintLayout9, TextView textView22, TextView textView23, SwitchCompat switchCompat8, TextView textView24, CardView cardView2, TextView textView25, AppCompatCheckBox appCompatCheckBox, TextView textView26, TextView textView27, TextView textView28, TextView textView29, ImageView imageView3, ImageView imageView4, ImageView imageView5, CardView cardView3, TextView textView30, TextView textView31, SeekBar seekBar2, CardView cardView4, TextView textView32, TextView textView33) {
        super(obj, view, i);
        this.constraintLayout = constraintLayout;
        this.gestureCallControlConstraintLayout = constraintLayout2;
        this.gestureCallControlContentTextView = textView;
        this.gestureCallControlSwitch = switchCompat;
        this.gestureCallControlTitleTextView = textView2;
        this.gestureDownDoubleTapConstraintLayout = constraintLayout3;
        this.gestureDownDoubleTapContent1TextView = textView3;
        this.gestureDownDoubleTapContent2TextView = textView4;
        this.gestureDownDoubleTapSwitch = switchCompat2;
        this.gestureDownDoubleTapTitleTextView = textView5;
        this.gestureFlatDoubleTapConstraintLayout = constraintLayout4;
        this.gestureFlatDoubleTapContentTextView = textView6;
        this.gestureFlatDoubleTapSwitch = switchCompat3;
        this.gestureFlatDoubleTapTitleTextView = textView7;
        this.gestureFlatTripleTapConstraintLayout = constraintLayout5;
        this.gestureFlatTripleTapContentTextView = textView8;
        this.gestureFlatTripleTapQuestionMarkImageView = imageView;
        this.gestureFlatTripleTapSwitch = switchCompat4;
        this.gestureFlatTripleTapTitleTextView = textView9;
        this.gestureFlatTripleTapWebHookUrlTextView = textView10;
        this.gestureReservedConstraintLayout = constraintLayout6;
        this.gestureReservedContentTextView = textView11;
        this.gestureReservedSwitch = switchCompat5;
        this.gestureReservedTitleTextView = textView12;
        this.gestureReverseDoubleTapConstraintLayout = constraintLayout7;
        this.gestureReverseDoubleTapContentTextView = textView13;
        this.gestureReverseDoubleTapQuestionMarkImageView = imageView2;
        this.gestureReverseDoubleTapSwitch = switchCompat6;
        this.gestureReverseDoubleTapTitleTextView = textView14;
        this.gestureReverseDoubleTapWebHookUrlTextView = textView15;
        this.gestureSideDoubleTapConstraintLayout = constraintLayout8;
        this.gestureSideDoubleTapContentTextView = textView16;
        this.gestureSideDoubleTapSwitch = switchCompat7;
        this.gestureSideDoubleTapTitleTextView = textView17;
        this.gestureTapStrengthCardView = cardView;
        this.gestureTapStrengthContentTextView = textView18;
        this.gestureTapStrengthSeekBar = seekBar;
        this.gestureTapStrengthStrongTextView = textView19;
        this.gestureTapStrengthTitleTextView = textView20;
        this.gestureTapStrengthWeakTextView = textView21;
        this.gestureUpDoubleTapConstraintLayout = constraintLayout9;
        this.gestureUpDoubleTapContent1TextView = textView22;
        this.gestureUpDoubleTapContent2TextView = textView23;
        this.gestureUpDoubleTapSwitch = switchCompat8;
        this.gestureUpDoubleTapTitleTextView = textView24;
        this.gesturesCardView = cardView2;
        this.gesturesContentTextView = textView25;
        this.gesturesControllerCheckBox = appCompatCheckBox;
        this.gesturesControllerTextView = textView26;
        this.gesturesTitleTextView = textView27;
        this.imageView3 = textView28;
        this.imageView4 = textView29;
        this.imageView5 = imageView3;
        this.micModeLeftImageView = imageView4;
        this.micModeRightImageView = imageView5;
        this.readSpeedCardView = cardView3;
        this.readSpeedContentTextView = textView30;
        this.readSpeedTitleTextView = textView31;
        this.readoutSpeedSeekBar = seekBar2;
        this.swapMicCardView = cardView4;
        this.swapMicContentTextView = textView32;
        this.swapMicTitleTextView = textView33;
    }

    public SharedViewModel getSharedViewModel() {
        return this.mSharedViewModel;
    }

    public SettingsViewModel getSettingsViewModel() {
        return this.mSettingsViewModel;
    }

    public static SettingsFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static SettingsFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (SettingsFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.settings_fragment, viewGroup, z, obj);
    }

    public static SettingsFragmentBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static SettingsFragmentBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (SettingsFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.settings_fragment, null, false, obj);
    }

    public static SettingsFragmentBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static SettingsFragmentBinding bind(View view, Object obj) {
        return (SettingsFragmentBinding) bind(obj, view, R.layout.settings_fragment);
    }
}
