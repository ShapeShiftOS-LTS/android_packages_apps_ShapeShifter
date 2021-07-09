/*
 * Copyright (C) 2021 ShapeShiftOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ssos.shapeshifter.fragments.ui;

import static android.os.UserHandle.USER_SYSTEM;

import android.app.AlertDialog;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;

import android.os.SystemProperties;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import androidx.preference.*;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.OverlayCategoryPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import com.android.settings.R;
import com.ssos.support.preferences.SystemSettingListPreference;

import com.ssos.shapeshifter.preferences.CustomOverlayPreferenceController;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class StyleSystemOverlayPreferenceFragment extends DashboardFragment implements Indexable {
    private static final String TAG = "StyleSystemOverlayPreferenceFragment";

    private IOverlayManager mOverlayManager;
    private PackageManager mPackageManager;
    private static final String STYLE_OVERLAY_SETTINGS_CARDS = "style_overlay_settings_cards";
    private static final String STYLE_OVERLAY_SETTINGS_DASHBOARD_ICONS = "style_overlay_settings_dashboard_icons";

    private SystemSettingListPreference mCards;
    private SystemSettingListPreference mDashboardIcons;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ContentResolver resolver = getActivity().getContentResolver();
        Context mContext = getContext();
        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
        mCards = (SystemSettingListPreference) findPreference(STYLE_OVERLAY_SETTINGS_CARDS);
        mDashboardIcons = (SystemSettingListPreference) findPreference(STYLE_OVERLAY_SETTINGS_DASHBOARD_ICONS);

        mCustomSettingsObserver.observe();
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STYLE_OVERLAY_SETTINGS_CARDS),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STYLE_OVERLAY_SETTINGS_DASHBOARD_ICONS),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.STYLE_OVERLAY_SETTINGS_CARDS))) {
                updateSettingsCards();
            } else if (uri.equals(Settings.System.getUriFor(Settings.System.STYLE_OVERLAY_SETTINGS_DASHBOARD_ICONS))) {
                updateSettingsIcons();
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mCards) {
            mCustomSettingsObserver.observe();
            return true;
        } else if (preference == mDashboardIcons) {
            mCustomSettingsObserver.observe();
            return true;
        }
        return false;
    }

    private void updateSettingsCards() {
        ContentResolver resolver = getActivity().getContentResolver();

        boolean settingsCardsVisible = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.STYLE_OVERLAY_SETTINGS_CARDS, 0, UserHandle.USER_CURRENT) == 0;
        boolean settingsCardsNone = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.STYLE_OVERLAY_SETTINGS_CARDS, 0, UserHandle.USER_CURRENT) == 1;

        if (settingsCardsVisible) {
            setDefaultSettingsCard(mOverlayManager);
            setDefaultSettingsCardIntell(mOverlayManager);
        } else if (settingsCardsNone) {
            enableSettingsCard(mOverlayManager, "com.android.theme.settings_card.elevation");
            enableSettingsCardIntell(mOverlayManager, "com.android.theme.settings_card.elevationintell");
        }
    }

    private void updateSettingsIcons() {
        ContentResolver resolver = getActivity().getContentResolver();

        boolean dashboardAOSP = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.STYLE_OVERLAY_SETTINGS_DASHBOARD_ICONS, 0, UserHandle.USER_CURRENT) == 1;
        boolean dashboardOOS11 = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.STYLE_OVERLAY_SETTINGS_DASHBOARD_ICONS, 0, UserHandle.USER_CURRENT) == 0;

        if (dashboardOOS11) {
            setDefaultSettingsDashboardIcons(mOverlayManager);
        } else if (dashboardAOSP) {
            enableSettingsDashboardIcons(mOverlayManager, "com.android.theme.settings_dashboard.aosp");
        }
    }

    public static void setDefaultSettingsCard(IOverlayManager overlayManager) {
        for (int i = 0; i < CARDS.length; i++) {
            String card = CARDS[i];
            try {
                overlayManager.setEnabled(card, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setDefaultSettingsDashboardIcons(IOverlayManager overlayManager) {
        for (int i = 0; i < DASHBOARD_ICONS.length; i++) {
            String card = DASHBOARD_ICONS[i];
            try {
                overlayManager.setEnabled(card, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setDefaultSettingsCardIntell(IOverlayManager overlayManager) {
        for (int i = 0; i < CARDS_INTELL.length; i++) {
            String card = CARDS_INTELL[i];
            try {
                overlayManager.setEnabled(card, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void enableSettingsCard(IOverlayManager overlayManager, String overlayName) {
        try {
            setDefaultSettingsCard(overlayManager);
            overlayManager.setEnabled(overlayName, true, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void enableSettingsCardIntell(IOverlayManager overlayManager, String overlayName) {
        try {
            setDefaultSettingsCardIntell(overlayManager);
            overlayManager.setEnabled(overlayName, true, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void enableSettingsDashboardIcons(IOverlayManager overlayManager, String overlayName) {
        try {
            setDefaultSettingsDashboardIcons(overlayManager);
            overlayManager.setEnabled(overlayName, true, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void handleOverlays(String packagename, Boolean state, IOverlayManager mOverlayManager) {
        try {
            mOverlayManager.setEnabled(packagename,
                    state, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static final String[] CARDS = {
        "com.android.theme.settings_card.elevation"
    };

    public static final String[] CARDS_INTELL = {
        "com.android.theme.settings_card.elevationintell"
    };

    public static final String[] DASHBOARD_ICONS = {
        "com.android.theme.settings_dashboard.aosp"
    };

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.style_overlay;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.adaptive_icon_shape"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.icon_pack"));
        controllers.add(new CustomOverlayPreferenceController(context,
                "android.theme.customization.custom_overlays"));
        return controllers;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.style_overlay;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
