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
import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.main.home.update.updating.UpdatingViewModel;

/* loaded from: classes2.dex */
public abstract class UpdatingFragmentBinding extends ViewDataBinding {
    public final ImageView backButton;
    public final ImageView helpImageView;
    public final TextView hintTextView;
    public final ImageView imageView15;

    @Bindable
    protected UpdatingViewModel mUpdatingViewModel;
    public final CircularProgressBar progressBar;
    public final TextView returnTextView;
    public final TextView statusTextView;
    public final TextView textView32;
    public final ConstraintLayout updateinfo;

    public abstract void setUpdatingViewModel(UpdatingViewModel updatingViewModel);

    protected UpdatingFragmentBinding(Object obj, View view, int i, ImageView imageView, ImageView imageView2, TextView textView, ImageView imageView3, CircularProgressBar circularProgressBar, TextView textView2, TextView textView3, TextView textView4, ConstraintLayout constraintLayout) {
        super(obj, view, i);
        this.backButton = imageView;
        this.helpImageView = imageView2;
        this.hintTextView = textView;
        this.imageView15 = imageView3;
        this.progressBar = circularProgressBar;
        this.returnTextView = textView2;
        this.statusTextView = textView3;
        this.textView32 = textView4;
        this.updateinfo = constraintLayout;
    }

    public UpdatingViewModel getUpdatingViewModel() {
        return this.mUpdatingViewModel;
    }

    public static UpdatingFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static UpdatingFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (UpdatingFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.updating_fragment, viewGroup, z, obj);
    }

    public static UpdatingFragmentBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static UpdatingFragmentBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (UpdatingFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.updating_fragment, null, false, obj);
    }

    public static UpdatingFragmentBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static UpdatingFragmentBinding bind(View view, Object obj) {
        return (UpdatingFragmentBinding) bind(obj, view, R.layout.updating_fragment);
    }
}
