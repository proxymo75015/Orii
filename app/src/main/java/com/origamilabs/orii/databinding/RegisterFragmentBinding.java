package com.origamilabs.orii.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.origamilabs.orii.R;

/* loaded from: classes2.dex */
public abstract class RegisterFragmentBinding extends ViewDataBinding {
    public final Button backButton;
    public final EditText confirmPasswordInput;
    public final TextInputLayout confirmPasswordInputLayout;
    public final EditText emailInput;
    public final TextInputLayout emailInputLayout;
    public final ImageView emailSignUpBackgroundImageView;
    public final ProgressBar emailSignUpProgressBar;
    public final ConstraintLayout login;

    @Bindable
    protected Boolean mIsSignUpProgress;
    public final EditText passwordInput;
    public final TextInputLayout passwordInputLayout;
    public final Button signUpButton;
    public final TextView textView19;

    public abstract void setIsSignUpProgress(Boolean bool);

    protected RegisterFragmentBinding(Object obj, View view, int i, Button button, EditText editText, TextInputLayout textInputLayout, EditText editText2, TextInputLayout textInputLayout2, ImageView imageView, ProgressBar progressBar, ConstraintLayout constraintLayout, EditText editText3, TextInputLayout textInputLayout3, Button button2, TextView textView) {
        super(obj, view, i);
        this.backButton = button;
        this.confirmPasswordInput = editText;
        this.confirmPasswordInputLayout = textInputLayout;
        this.emailInput = editText2;
        this.emailInputLayout = textInputLayout2;
        this.emailSignUpBackgroundImageView = imageView;
        this.emailSignUpProgressBar = progressBar;
        this.login = constraintLayout;
        this.passwordInput = editText3;
        this.passwordInputLayout = textInputLayout3;
        this.signUpButton = button2;
        this.textView19 = textView;
    }

    public Boolean getIsSignUpProgress() {
        return this.mIsSignUpProgress;
    }

    public static RegisterFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static RegisterFragmentBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (RegisterFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.register_fragment, viewGroup, z, obj);
    }

    public static RegisterFragmentBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static RegisterFragmentBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (RegisterFragmentBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.register_fragment, null, false, obj);
    }

    public static RegisterFragmentBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static RegisterFragmentBinding bind(View view, Object obj) {
        return (RegisterFragmentBinding) bind(obj, view, R.layout.register_fragment);
    }
}
