package com.origamilabs.orii.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.origamilabs.orii.R;

/* loaded from: classes2.dex */
public abstract class LoginFragmentBinding extends ViewDataBinding {
    public final Button backButton;
    public final EditText emailInput;
    public final TextInputLayout emailInputLayout;
    public final ImageView emailLoginBackgroundImageView;
    public final ProgressBar emailLoginProgressBar;
    public final Button forgotPasswordButton;

    @Bindable
    protected Boolean mIsLoginProgress;
    public final EditText passwordInput;
    public final TextInputLayout passwordInputLayout;
    public final Button signInButton;
    public final TextView signInWithEmailTextView;

    public abstract void setIsLoginProgress(Boolean bool);

    protected LoginFragmentBinding(Object obj, View view, int i, Button button, EditText editText, TextInputLayout textInputLayout, ImageView imageView, ProgressBar progressBar, Button button2, EditText editText2, TextInputLayout textInputLayout2, Button button3, TextView textView) {
        super(obj, view, i);
        this.backButton = button;
        this.emailInput = editText;
        this.emailInputLayout = textInputLayout;
        this.emailLoginBackgroundImageView = imageView;
        this.emailLoginProgressBar = progressBar;
        this.forgotPasswordButton = button2;
        this.passwordInput = editText2;
        this.passwordInputLayout = textInputLayout2;
        this.signInButton = button3;
        this.signInWithEmailTextView = textView;
    }

    public Boolean getIsLoginProgress() {
        return this.mIsLoginProgress;
    }

    public static LoginFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static LoginFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (LoginFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.login_fragment, viewGroup, z, obj);
    }

    public static LoginFragmentBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static LoginFragmentBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (LoginFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.login_fragment, null, false, obj);
    }

    public static LoginFragmentBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static LoginFragmentBinding bind(View view, Object obj) {
        return (LoginFragmentBinding) bind(obj, view, R.layout.login_fragment);
    }
}
