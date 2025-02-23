package com.origamilabs.orii;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.origamilabs.orii.databinding.ActivityCatchLogBindingImpl;
import com.origamilabs.orii.databinding.ActivityTutorialBindingImpl;
import com.origamilabs.orii.databinding.AuthFragmentBindingImpl;
import com.origamilabs.orii.databinding.AuthFragmentBindingJaRJPImpl;
import com.origamilabs.orii.databinding.ConnectionFragmentBindingImpl;
import com.origamilabs.orii.databinding.DialogSettingGestureWebHookUrlBindingImpl;
import com.origamilabs.orii.databinding.FirmwareTestFragmentBindingImpl;
import com.origamilabs.orii.databinding.FragmentUnscrewBindingImpl;
import com.origamilabs.orii.databinding.HelpFragmentBindingImpl;
import com.origamilabs.orii.databinding.HomeFragmentBindingImpl;
import com.origamilabs.orii.databinding.LoginFragmentBindingImpl;
import com.origamilabs.orii.databinding.RegisterFragmentBindingImpl;
import com.origamilabs.orii.databinding.SettingsFragmentBindingImpl;
import com.origamilabs.orii.databinding.UpdatingFragmentBindingImpl;
import com.origamilabs.orii.databinding.UsingOriiFragmentBindingImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* loaded from: classes2.dex */
public class DataBinderMapperImpl extends DataBinderMapper {
    private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(14);
    private static final int LAYOUT_ACTIVITYCATCHLOG = 1;
    private static final int LAYOUT_ACTIVITYTUTORIAL = 2;
    private static final int LAYOUT_AUTHFRAGMENT = 3;
    private static final int LAYOUT_CONNECTIONFRAGMENT = 4;
    private static final int LAYOUT_DIALOGSETTINGGESTUREWEBHOOKURL = 5;
    private static final int LAYOUT_FIRMWARETESTFRAGMENT = 6;
    private static final int LAYOUT_FRAGMENTUNSCREW = 7;
    private static final int LAYOUT_HELPFRAGMENT = 8;
    private static final int LAYOUT_HOMEFRAGMENT = 9;
    private static final int LAYOUT_LOGINFRAGMENT = 10;
    private static final int LAYOUT_REGISTERFRAGMENT = 11;
    private static final int LAYOUT_SETTINGSFRAGMENT = 12;
    private static final int LAYOUT_UPDATINGFRAGMENT = 13;
    private static final int LAYOUT_USINGORIIFRAGMENT = 14;

    static {
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.activity_catch_log, 1);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.activity_tutorial, 2);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.auth_fragment, 3);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.connection_fragment, 4);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.dialog_setting_gesture_web_hook_url, 5);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.firmware_test_fragment, 6);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.fragment_unscrew, 7);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.help_fragment, 8);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.home_fragment, 9);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.login_fragment, 10);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.register_fragment, 11);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.settings_fragment, 12);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.updating_fragment, 13);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.using_orii_fragment, 14);
    }

    @Override // androidx.databinding.DataBinderMapper
    public ViewDataBinding getDataBinder(DataBindingComponent dataBindingComponent, View view, int i) {
        int i2 = INTERNAL_LAYOUT_ID_LOOKUP.get(i);
        if (i2 <= 0) {
            return null;
        }
        Object tag = view.getTag();
        if (tag == null) {
            throw new RuntimeException("view must have a tag");
        }
        switch (i2) {
            case 1:
                if ("layout/activity_catch_log_0".equals(tag)) {
                    return new ActivityCatchLogBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for activity_catch_log is invalid. Received: " + tag);
            case 2:
                if ("layout/activity_tutorial_0".equals(tag)) {
                    return new ActivityTutorialBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for activity_tutorial is invalid. Received: " + tag);
            case 3:
                if ("layout/auth_fragment_0".equals(tag)) {
                    return new AuthFragmentBindingImpl(dataBindingComponent, view);
                }
                if ("layout-ja-rJP/auth_fragment_0".equals(tag)) {
                    return new AuthFragmentBindingJaRJPImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for auth_fragment is invalid. Received: " + tag);
            case 4:
                if ("layout/connection_fragment_0".equals(tag)) {
                    return new ConnectionFragmentBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for connection_fragment is invalid. Received: " + tag);
            case 5:
                if ("layout/dialog_setting_gesture_web_hook_url_0".equals(tag)) {
                    return new DialogSettingGestureWebHookUrlBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for dialog_setting_gesture_web_hook_url is invalid. Received: " + tag);
            case 6:
                if ("layout/firmware_test_fragment_0".equals(tag)) {
                    return new FirmwareTestFragmentBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for firmware_test_fragment is invalid. Received: " + tag);
            case 7:
                if ("layout/fragment_unscrew_0".equals(tag)) {
                    return new FragmentUnscrewBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for fragment_unscrew is invalid. Received: " + tag);
            case 8:
                if ("layout/help_fragment_0".equals(tag)) {
                    return new HelpFragmentBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for help_fragment is invalid. Received: " + tag);
            case 9:
                if ("layout/home_fragment_0".equals(tag)) {
                    return new HomeFragmentBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for home_fragment is invalid. Received: " + tag);
            case 10:
                if ("layout/login_fragment_0".equals(tag)) {
                    return new LoginFragmentBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for login_fragment is invalid. Received: " + tag);
            case 11:
                if ("layout/register_fragment_0".equals(tag)) {
                    return new RegisterFragmentBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for register_fragment is invalid. Received: " + tag);
            case 12:
                if ("layout/settings_fragment_0".equals(tag)) {
                    return new SettingsFragmentBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for settings_fragment is invalid. Received: " + tag);
            case 13:
                if ("layout/updating_fragment_0".equals(tag)) {
                    return new UpdatingFragmentBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for updating_fragment is invalid. Received: " + tag);
            case 14:
                if ("layout/using_orii_fragment_0".equals(tag)) {
                    return new UsingOriiFragmentBindingImpl(dataBindingComponent, view);
                }
                throw new IllegalArgumentException("The tag for using_orii_fragment is invalid. Received: " + tag);
            default:
                return null;
        }
    }

    @Override // androidx.databinding.DataBinderMapper
    public ViewDataBinding getDataBinder(DataBindingComponent dataBindingComponent, View[] viewArr, int i) {
        if (viewArr == null || viewArr.length == 0 || INTERNAL_LAYOUT_ID_LOOKUP.get(i) <= 0 || viewArr[0].getTag() != null) {
            return null;
        }
        throw new RuntimeException("view must have a tag");
    }

    @Override // androidx.databinding.DataBinderMapper
    public int getLayoutId(String str) {
        Integer num;
        if (str == null || (num = InnerLayoutIdLookup.sKeys.get(str)) == null) {
            return 0;
        }
        return num.intValue();
    }

    @Override // androidx.databinding.DataBinderMapper
    public String convertBrIdToString(int i) {
        return InnerBrLookup.sKeys.get(i);
    }

    @Override // androidx.databinding.DataBinderMapper
    public List<DataBinderMapper> collectDependencies() {
        ArrayList arrayList = new ArrayList(1);
        arrayList.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
        return arrayList;
    }

    private static class InnerBrLookup {
        static final SparseArray<String> sKeys = new SparseArray<>(18);

        private InnerBrLookup() {
        }

        static {
            sKeys.put(0, "_all");
            sKeys.put(1, "isPlaying");
            sKeys.put(2, "isSignUpProgress");
            sKeys.put(3, "showMenuButton");
            sKeys.put(4, "showHelpButton");
            sKeys.put(5, "canFirmwareForceUpdate");
            sKeys.put(6, "isLoginProgress");
            sKeys.put(7, "settingsViewModel");
            sKeys.put(8, "showMaskView");
            sKeys.put(9, "updatingViewModel");
            sKeys.put(10, "showBackButton");
            sKeys.put(11, "sharedViewModel");
            sKeys.put(12, "viewModel");
            sKeys.put(13, "canFirmwareUpdate");
            sKeys.put(14, "showNextButton");
            sKeys.put(15, "showCircleView");
            sKeys.put(16, "showLog");
        }
    }

    private static class InnerLayoutIdLookup {
        static final HashMap<String, Integer> sKeys = new HashMap<>(15);

        private InnerLayoutIdLookup() {
        }

        static {
            sKeys.put("layout/activity_catch_log_0", Integer.valueOf(R.layout.activity_catch_log));
            sKeys.put("layout/activity_tutorial_0", Integer.valueOf(R.layout.activity_tutorial));
            HashMap<String, Integer> hashMap = sKeys;
            Integer valueOf = Integer.valueOf(R.layout.auth_fragment);
            hashMap.put("layout/auth_fragment_0", valueOf);
            sKeys.put("layout-ja-rJP/auth_fragment_0", valueOf);
            sKeys.put("layout/connection_fragment_0", Integer.valueOf(R.layout.connection_fragment));
            sKeys.put("layout/dialog_setting_gesture_web_hook_url_0", Integer.valueOf(R.layout.dialog_setting_gesture_web_hook_url));
            sKeys.put("layout/firmware_test_fragment_0", Integer.valueOf(R.layout.firmware_test_fragment));
            sKeys.put("layout/fragment_unscrew_0", Integer.valueOf(R.layout.fragment_unscrew));
            sKeys.put("layout/help_fragment_0", Integer.valueOf(R.layout.help_fragment));
            sKeys.put("layout/home_fragment_0", Integer.valueOf(R.layout.home_fragment));
            sKeys.put("layout/login_fragment_0", Integer.valueOf(R.layout.login_fragment));
            sKeys.put("layout/register_fragment_0", Integer.valueOf(R.layout.register_fragment));
            sKeys.put("layout/settings_fragment_0", Integer.valueOf(R.layout.settings_fragment));
            sKeys.put("layout/updating_fragment_0", Integer.valueOf(R.layout.updating_fragment));
            sKeys.put("layout/using_orii_fragment_0", Integer.valueOf(R.layout.using_orii_fragment));
        }
    }
}
