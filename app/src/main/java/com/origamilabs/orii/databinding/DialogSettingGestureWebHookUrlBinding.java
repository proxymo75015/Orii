package com.origamilabs.orii.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.origamilabs.orii.R;

/* loaded from: classes2.dex */
public abstract class DialogSettingGestureWebHookUrlBinding extends ViewDataBinding {
    public final Button dialogWebHookCancelButton;
    public final Button dialogWebHookEnterButton;
    public final ImageView dialogWebHookQuestionMarkImageView;
    public final TextView dialogWebHookTitleTextView;
    public final ImageView dialogWebHookUrlClearImageView;
    public final ConstraintLayout dialogWebHookUrlConstraintLayout;
    public final EditText dialogWebHookUrlEditText;
    public final TextView dialogWebHookUrlPromptTextView;

    protected DialogSettingGestureWebHookUrlBinding(Object obj, View view, int i, Button button, Button button2, ImageView imageView, TextView textView, ImageView imageView2, ConstraintLayout constraintLayout, EditText editText, TextView textView2) {
        super(obj, view, i);
        this.dialogWebHookCancelButton = button;
        this.dialogWebHookEnterButton = button2;
        this.dialogWebHookQuestionMarkImageView = imageView;
        this.dialogWebHookTitleTextView = textView;
        this.dialogWebHookUrlClearImageView = imageView2;
        this.dialogWebHookUrlConstraintLayout = constraintLayout;
        this.dialogWebHookUrlEditText = editText;
        this.dialogWebHookUrlPromptTextView = textView2;
    }

    public static DialogSettingGestureWebHookUrlBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static DialogSettingGestureWebHookUrlBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (DialogSettingGestureWebHookUrlBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.dialog_setting_gesture_web_hook_url, viewGroup, z, obj);
    }

    public static DialogSettingGestureWebHookUrlBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static DialogSettingGestureWebHookUrlBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (DialogSettingGestureWebHookUrlBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.dialog_setting_gesture_web_hook_url, null, false, obj);
    }

    public static DialogSettingGestureWebHookUrlBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static DialogSettingGestureWebHookUrlBinding bind(View view, Object obj) {
        return (DialogSettingGestureWebHookUrlBinding) bind(obj, view, R.layout.dialog_setting_gesture_web_hook_url);
    }
}
