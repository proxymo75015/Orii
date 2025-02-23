package com.origamilabs.orii.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.facebook.drawee.view.SimpleDraweeView;
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.tutorial.phase.one.ConnectionViewModel;

/* loaded from: classes2.dex */
public abstract class ConnectionFragmentBinding extends ViewDataBinding {
    public final ImageView blinkLightImageView;
    public final ConstraintLayout constraintLayout;

    @Bindable
    protected ConnectionViewModel mViewModel;
    public final SimpleDraweeView ringSimpleDraweeView;
    public final TextView textView24;
    public final TextView textView26;

    public abstract void setViewModel(ConnectionViewModel connectionViewModel);

    protected ConnectionFragmentBinding(Object obj, View view, int i, ImageView imageView, ConstraintLayout constraintLayout, SimpleDraweeView simpleDraweeView, TextView textView, TextView textView2) {
        super(obj, view, i);
        this.blinkLightImageView = imageView;
        this.constraintLayout = constraintLayout;
        this.ringSimpleDraweeView = simpleDraweeView;
        this.textView24 = textView;
        this.textView26 = textView2;
    }

    public ConnectionViewModel getViewModel() {
        return this.mViewModel;
    }

    public static ConnectionFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static ConnectionFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (ConnectionFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.connection_fragment, viewGroup, z, obj);
    }

    public static ConnectionFragmentBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static ConnectionFragmentBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (ConnectionFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.connection_fragment, null, false, obj);
    }

    public static ConnectionFragmentBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static ConnectionFragmentBinding bind(View view, Object obj) {
        return (ConnectionFragmentBinding) bind(obj, view, R.layout.connection_fragment);
    }
}
