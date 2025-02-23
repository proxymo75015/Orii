package com.origamilabs.orii.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.origamilabs.orii.R;

/* loaded from: classes2.dex */
public abstract class HelpFragmentBinding extends ViewDataBinding {
    public final TextView appVersionTextView;
    public final CardView cardViewAppVersion;
    public final CardView cardViewFeedback;
    public final CardView cardViewFirmware;
    public final CardView cardViewFirmwareTestMode;
    public final CardView cardViewLog;
    public final CardView cardViewPrivacyPolicy;
    public final CardView cardViewSupport;
    public final CardView cardViewTutorial;
    public final TextView firmwareTextView;
    public final ImageView imageView2;
    public final ImageView imageView4;
    public final ImageView imageView6;

    @Bindable
    protected Boolean mShowLog;
    public final TextView textView;
    public final TextView textView3;
    public final TextView textView5;
    public final TextView textView8;

    public abstract void setShowLog(Boolean bool);

    protected HelpFragmentBinding(Object obj, View view, int i, TextView textView, CardView cardView, CardView cardView2, CardView cardView3, CardView cardView4, CardView cardView5, CardView cardView6, CardView cardView7, CardView cardView8, TextView textView2, ImageView imageView, ImageView imageView2, ImageView imageView3, TextView textView3, TextView textView4, TextView textView5, TextView textView6) {
        super(obj, view, i);
        this.appVersionTextView = textView;
        this.cardViewAppVersion = cardView;
        this.cardViewFeedback = cardView2;
        this.cardViewFirmware = cardView3;
        this.cardViewFirmwareTestMode = cardView4;
        this.cardViewLog = cardView5;
        this.cardViewPrivacyPolicy = cardView6;
        this.cardViewSupport = cardView7;
        this.cardViewTutorial = cardView8;
        this.firmwareTextView = textView2;
        this.imageView2 = imageView;
        this.imageView4 = imageView2;
        this.imageView6 = imageView3;
        this.textView = textView3;
        this.textView3 = textView4;
        this.textView5 = textView5;
        this.textView8 = textView6;
    }

    public Boolean getShowLog() {
        return this.mShowLog;
    }

    public static HelpFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static HelpFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (HelpFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.help_fragment, viewGroup, z, obj);
    }

    public static HelpFragmentBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static HelpFragmentBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (HelpFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.help_fragment, null, false, obj);
    }

    public static HelpFragmentBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static HelpFragmentBinding bind(View view, Object obj) {
        return (HelpFragmentBinding) bind(obj, view, R.layout.help_fragment);
    }
}
