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
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.tutorial.TutorialViewPager;

/* loaded from: classes2.dex */
public abstract class ActivityTutorialBinding extends ViewDataBinding {
    public final TextView backTextView;
    public final ImageView circle0ImageView;
    public final ImageView circle10ImageView;
    public final ImageView circle11ImageView;
    public final ImageView circle12ImageView;
    public final ImageView circle13ImageView;
    public final ImageView circle14ImageView;
    public final ImageView circle15ImageView;
    public final ImageView circle1ImageView;
    public final ImageView circle2ImageView;
    public final ImageView circle3ImageView;
    public final ImageView circle4ImageView;
    public final ImageView circle5ImageView;
    public final ImageView circle6ImageView;
    public final ImageView circle7ImageView;
    public final ImageView circle8ImageView;
    public final ImageView circle9ImageView;
    public final TutorialViewPager container;
    public final ImageView helpImageView;

    @Bindable
    protected Boolean mShowBackButton;

    @Bindable
    protected Boolean mShowCircleView;

    @Bindable
    protected Boolean mShowHelpButton;

    @Bindable
    protected Boolean mShowMaskView;

    @Bindable
    protected Boolean mShowMenuButton;

    @Bindable
    protected Boolean mShowNextButton;
    public final ConstraintLayout mainContent;
    public final ImageView maskBackgroundImageView;
    public final TextView menuTextView;
    public final TextView nextTextView;
    public final View tutorialMaskBackLineView;
    public final TextView tutorialMaskBackTextView;
    public final ConstraintLayout tutorialMaskLayout;
    public final View tutorialMaskMenuLineView;
    public final TextView tutorialMaskMenuTextView;
    public final View tutorialMaskNextLineView;
    public final TextView tutorialMaskNextTextView;

    public abstract void setShowBackButton(Boolean bool);

    public abstract void setShowCircleView(Boolean bool);

    public abstract void setShowHelpButton(Boolean bool);

    public abstract void setShowMaskView(Boolean bool);

    public abstract void setShowMenuButton(Boolean bool);

    public abstract void setShowNextButton(Boolean bool);

    protected ActivityTutorialBinding(Object obj, View view, int i, TextView textView, ImageView imageView, ImageView imageView2, ImageView imageView3, ImageView imageView4, ImageView imageView5, ImageView imageView6, ImageView imageView7, ImageView imageView8, ImageView imageView9, ImageView imageView10, ImageView imageView11, ImageView imageView12, ImageView imageView13, ImageView imageView14, ImageView imageView15, ImageView imageView16, TutorialViewPager tutorialViewPager, ImageView imageView17, ConstraintLayout constraintLayout, ImageView imageView18, TextView textView2, TextView textView3, View view2, TextView textView4, ConstraintLayout constraintLayout2, View view3, TextView textView5, View view4, TextView textView6) {
        super(obj, view, i);
        this.backTextView = textView;
        this.circle0ImageView = imageView;
        this.circle10ImageView = imageView2;
        this.circle11ImageView = imageView3;
        this.circle12ImageView = imageView4;
        this.circle13ImageView = imageView5;
        this.circle14ImageView = imageView6;
        this.circle15ImageView = imageView7;
        this.circle1ImageView = imageView8;
        this.circle2ImageView = imageView9;
        this.circle3ImageView = imageView10;
        this.circle4ImageView = imageView11;
        this.circle5ImageView = imageView12;
        this.circle6ImageView = imageView13;
        this.circle7ImageView = imageView14;
        this.circle8ImageView = imageView15;
        this.circle9ImageView = imageView16;
        this.container = tutorialViewPager;
        this.helpImageView = imageView17;
        this.mainContent = constraintLayout;
        this.maskBackgroundImageView = imageView18;
        this.menuTextView = textView2;
        this.nextTextView = textView3;
        this.tutorialMaskBackLineView = view2;
        this.tutorialMaskBackTextView = textView4;
        this.tutorialMaskLayout = constraintLayout2;
        this.tutorialMaskMenuLineView = view3;
        this.tutorialMaskMenuTextView = textView5;
        this.tutorialMaskNextLineView = view4;
        this.tutorialMaskNextTextView = textView6;
    }

    public Boolean getShowHelpButton() {
        return this.mShowHelpButton;
    }

    public Boolean getShowMenuButton() {
        return this.mShowMenuButton;
    }

    public Boolean getShowBackButton() {
        return this.mShowBackButton;
    }

    public Boolean getShowNextButton() {
        return this.mShowNextButton;
    }

    public Boolean getShowCircleView() {
        return this.mShowCircleView;
    }

    public Boolean getShowMaskView() {
        return this.mShowMaskView;
    }

    public static ActivityTutorialBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static ActivityTutorialBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z, Object obj) {
        return (ActivityTutorialBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.activity_tutorial, viewGroup, z, obj);
    }

    public static ActivityTutorialBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static ActivityTutorialBinding inflate(LayoutInflater layoutInflater, Object obj) {
        return (ActivityTutorialBinding) ViewDataBinding.inflateInternal(layoutInflater, R.layout.activity_tutorial, null, false, obj);
    }

    public static ActivityTutorialBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static ActivityTutorialBinding bind(View view, Object obj) {
        return (ActivityTutorialBinding) bind(obj, view, R.layout.activity_tutorial);
    }
}
