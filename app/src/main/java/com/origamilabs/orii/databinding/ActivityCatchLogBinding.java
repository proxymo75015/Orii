package com.origamilabs.orii.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.main.help.CatchLogViewModel;

/* loaded from: classes2.dex */
public abstract class ActivityCatchLogBinding extends ViewDataBinding {
    public final Button catchLogButton;
    public final Button clearLogButton;
    public final EditText logEditText;

    @Bindable
    protected CatchLogViewModel mViewModel;
    public final Button saveLogButton;

    public abstract void setViewModel(CatchLogViewModel catchLogViewModel);

    protected ActivityCatchLogBinding(Object obj, View view, int i, Button button, Button button2, EditText editText, Button button3) {
        super(obj, view, i);
        this.catchLogButton = button;
        this.clearLogButton = button2;
        this.logEditText = editText;
        this.saveLogButton = button3;
    }

    public CatchLogViewModel getViewModel() {
        return this.mViewModel;
    }

    public static ActivityCatchLogBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static ActivityCatchLogBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (ActivityCatchLogBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.activity_catch_log, viewGroup, z, obj);
    }

    public static ActivityCatchLogBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static ActivityCatchLogBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (ActivityCatchLogBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.activity_catch_log, null, false, obj);
    }

    public static ActivityCatchLogBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static ActivityCatchLogBinding bind(View view, Object obj) {
        return (ActivityCatchLogBinding) bind(obj, view, R.layout.activity_catch_log);
    }
}
