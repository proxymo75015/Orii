package com.origamilabs.orii.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.facebook.drawee.view.SimpleDraweeView;
import com.origamilabs.orii.R;

/* loaded from: classes2.dex */
public abstract class FragmentUnscrewBinding extends ViewDataBinding {
    public final SimpleDraweeView gifView;
    public final TextView unscrewContentTextView;
    public final TextView unscrewTitleTextView;

    protected FragmentUnscrewBinding(Object obj, View view, int i, SimpleDraweeView simpleDraweeView, TextView textView, TextView textView2) {
        super(obj, view, i);
        this.gifView = simpleDraweeView;
        this.unscrewContentTextView = textView;
        this.unscrewTitleTextView = textView2;
    }

    public static FragmentUnscrewBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static FragmentUnscrewBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (FragmentUnscrewBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.fragment_unscrew, viewGroup, z, obj);
    }

    public static FragmentUnscrewBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static FragmentUnscrewBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (FragmentUnscrewBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.fragment_unscrew, null, false, obj);
    }

    public static FragmentUnscrewBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static FragmentUnscrewBinding bind(View view, Object obj) {
        return (FragmentUnscrewBinding) bind(obj, view, R.layout.fragment_unscrew);
    }
}
