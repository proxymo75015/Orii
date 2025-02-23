package com.origamilabs.orii.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.origamilabs.orii.R;

/* loaded from: classes2.dex */
public abstract class AuthFragmentBinding extends ViewDataBinding {
    public final ConstraintLayout authMainLayout;
    public final TextView dontHaveAcTextView;
    public final Button emailLoginButton;
    public final Button fbLoginButton;
    public final Button googleLoginButton;
    public final ImageView imageView7;
    public final ProgressBar loginProgressBar;

    @Bindable
    protected Boolean mIsLoginProgress;
    public final View separatorLine;
    public final TextView signUpTextView;
    public final TextView textView18;

    public abstract void setIsLoginProgress(Boolean bool);

    protected AuthFragmentBinding(Object obj, View view, int i, ConstraintLayout constraintLayout, TextView textView, Button button, Button button2, Button button3, ImageView imageView, ProgressBar progressBar, View view2, TextView textView2, TextView textView3) {
        super(obj, view, i);
        this.authMainLayout = constraintLayout;
        this.dontHaveAcTextView = textView;
        this.emailLoginButton = button;
        this.fbLoginButton = button2;
        this.googleLoginButton = button3;
        this.imageView7 = imageView;
        this.loginProgressBar = progressBar;
        this.separatorLine = view2;
        this.signUpTextView = textView2;
        this.textView18 = textView3;
    }

    public Boolean getIsLoginProgress() {
        return this.mIsLoginProgress;
    }

    public static AuthFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static AuthFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (AuthFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.auth_fragment, viewGroup, z, obj);
    }

    public static AuthFragmentBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static AuthFragmentBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (AuthFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.auth_fragment, null, false, obj);
    }

    public static AuthFragmentBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static AuthFragmentBinding bind(View view, Object obj) {
        return (AuthFragmentBinding) bind(obj, view, R.layout.auth_fragment);
    }
}
