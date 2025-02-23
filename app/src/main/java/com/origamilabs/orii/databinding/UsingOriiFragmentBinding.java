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
import com.facebook.drawee.view.SimpleDraweeView;
import com.origamilabs.orii.R;
import hiennguyen.me.circleseekbar.CircleSeekBar;

/* loaded from: classes2.dex */
public abstract class UsingOriiFragmentBinding extends ViewDataBinding {
    public final ImageView addImageView;
    public final ConstraintLayout constraintLayout;
    public final SimpleDraweeView gifView;

    @Bindable
    protected Boolean mIsPlaying;
    public final ImageView minusImageView;
    public final Button nextTextButton;
    public final ImageView playPauseImageView;
    public final CircleSeekBar progressBar;
    public final TextView textView25;
    public final TextView turnOnContentTextView;
    public final TextView usingOriiTitleTextView;

    public abstract void setIsPlaying(Boolean bool);

    protected UsingOriiFragmentBinding(Object obj, View view, int i, ImageView imageView, ConstraintLayout constraintLayout, SimpleDraweeView simpleDraweeView, ImageView imageView2, Button button, ImageView imageView3, CircleSeekBar circleSeekBar, TextView textView, TextView textView2, TextView textView3) {
        super(obj, view, i);
        this.addImageView = imageView;
        this.constraintLayout = constraintLayout;
        this.gifView = simpleDraweeView;
        this.minusImageView = imageView2;
        this.nextTextButton = button;
        this.playPauseImageView = imageView3;
        this.progressBar = circleSeekBar;
        this.textView25 = textView;
        this.turnOnContentTextView = textView2;
        this.usingOriiTitleTextView = textView3;
    }

    public Boolean getIsPlaying() {
        return this.mIsPlaying;
    }

    public static UsingOriiFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static UsingOriiFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (UsingOriiFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.using_orii_fragment, viewGroup, z, obj);
    }

    public static UsingOriiFragmentBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static UsingOriiFragmentBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (UsingOriiFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.using_orii_fragment, null, false, obj);
    }

    public static UsingOriiFragmentBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static UsingOriiFragmentBinding bind(View view, Object obj) {
        return (UsingOriiFragmentBinding) bind(obj, view, R.layout.using_orii_fragment);
    }
}
