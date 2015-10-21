/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.astifter.circatext;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.astifter.circatextutils.CircaTextConsts;
import com.astifter.circatextutils.CircaTextUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

public class CircaTextWearableConfigActivity extends Activity implements
        WearableListView.ClickListener, WearableListView.OnScrollListener {
    private static final String TAG = "DigitalWatchFaceConfig";

    private GoogleApiClient mGoogleApiClient;
    private TextView mHeader;
    private WatchFaceListAdapter watchFaceListAdapter;
    private WearableListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        mHeader = (TextView) findViewById(R.id.header);

        listView = (WearableListView) findViewById(R.id.watchface_picker);
        listView.setHasFixedSize(true);
        listView.setClickListener(this);
        listView.addOnScrollListener(this);
        
        WatchFaceConfig ctf = new WatchFaceConfig(CircaTextConsts.WatchFaces.CIRCATEXTv1.toString(),
                                                  "CircaText", "Textual time representation.");
        WatchFaceConfig rtf = new WatchFaceConfig(CircaTextConsts.WatchFaces.CIRCATEXTv1ROUND.toString(),
                                                  "CircaText Round", "Textual time representation.");
        WatchFaceConfig rwf = new WatchFaceConfig(CircaTextConsts.WatchFaces.REGULAR.toString(),
                                                  "Regular", "Conventional time.");
        WatchFaceConfig[] watchfaces = { ctf, rwf, rtf };
        watchFaceListAdapter = new WatchFaceListAdapter(watchfaces);
        listView.setAdapter(watchFaceListAdapter);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnected: " + connectionHint);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnectionSuspended: " + cause);
                        }
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnectionFailed: " + result);
                        }
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        CircaTextUtil.fetchConfigDataMap(mGoogleApiClient, new CircaTextUtil.FetchConfigDataMapCallback() {
            @Override
            public void onConfigDataMapFetched(DataMap config) {
                String selectedWatchface = config.getString(CircaTextConsts.KEY_WATCHFACE);
                WatchFaceConfig[] watchfaces = watchFaceListAdapter.getWatchFaces();
                for (int i = 0; i < watchfaces.length; i++) {
                    if (watchfaces[i].equals(selectedWatchface)) {
                        listView.scrollToPosition(i);
                        break;
                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override // WearableListView.ClickListener
    public void onClick(WearableListView.ViewHolder viewHolder) {
        WatchFaceViewHolder watchFaceViewHolder = (WatchFaceViewHolder) viewHolder;
        updateConfigDataItem(watchFaceViewHolder.mWatchFace.getLabel());
        finish();
    }

    @Override // WearableListView.ClickListener
    public void onTopEmptyRegionClick() {
    }

    @Override // WearableListView.OnScrollListener
    public void onScroll(int scroll) {
    }

    @Override // WearableListView.OnScrollListener
    public void onAbsoluteScrollChange(int scroll) {
        float newTranslation = Math.min(-scroll, 0);
        mHeader.setTranslationY(newTranslation);
    }

    @Override // WearableListView.OnScrollListener
    public void onScrollStateChanged(int scrollState) {
    }

    @Override // WearableListView.OnScrollListener
    public void onCentralPositionChanged(int centralPosition) {
    }

    private void updateConfigDataItem(final String watchFace) {
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putString(CircaTextConsts.KEY_WATCHFACE, watchFace);
        CircaTextUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }

    private static class WatchFaceItem extends LinearLayout implements WearableListView.OnCenterProximityListener {
        private static final int ANIMATION_DURATION_MS = 150;
        private static final float SHRINK_CIRCLE_RATIO = .75f;

        private static final float SHRINK_LABEL_ALPHA = .5f;
        private static final float EXPAND_LABEL_ALPHA = 1f;

        private final TextView mSubTitle;
        private final TextView mLabel;
        private final CircledImageView mCircle;
        private final float mExpandCircleRadius;
        private final float mShrinkCircleRadius;
        private final ObjectAnimator mExpandCircleAnimator;
        private final ObjectAnimator mExpandLabelAnimator;
        private final AnimatorSet mExpandAnimator;
        private final ObjectAnimator mShrinkCircleAnimator;
        private final ObjectAnimator mShrinkLabelAnimator;
        private final AnimatorSet mShrinkAnimator;
        private WatchFaceConfig watchFaceConfig;

        public WatchFaceItem(Context context) {
            super(context);
            View.inflate(context, R.layout.watchface_picker_item, this);

            mLabel = (TextView) findViewById(R.id.label);
            mSubTitle = (TextView) findViewById(R.id.sublabel);
            mCircle = (CircledImageView) findViewById(R.id.circle);

            mExpandCircleRadius = mCircle.getCircleRadius();
            mShrinkCircleRadius = mExpandCircleRadius * SHRINK_CIRCLE_RATIO;

            mShrinkCircleAnimator = ObjectAnimator.ofFloat(mCircle, "circleRadius",
                    mExpandCircleRadius, mShrinkCircleRadius);
            mShrinkLabelAnimator = ObjectAnimator.ofFloat(mLabel, "alpha",
                    EXPAND_LABEL_ALPHA, SHRINK_LABEL_ALPHA);
            mShrinkAnimator = new AnimatorSet().setDuration(ANIMATION_DURATION_MS);
            mShrinkAnimator.playTogether(mShrinkCircleAnimator, mShrinkLabelAnimator);

            mExpandCircleAnimator = ObjectAnimator.ofFloat(mCircle, "circleRadius",
                    mShrinkCircleRadius, mExpandCircleRadius);
            mExpandLabelAnimator = ObjectAnimator.ofFloat(mLabel, "alpha",
                    SHRINK_LABEL_ALPHA, EXPAND_LABEL_ALPHA);
            mExpandAnimator = new AnimatorSet().setDuration(ANIMATION_DURATION_MS);
            mExpandAnimator.playTogether(mExpandCircleAnimator, mExpandLabelAnimator);
        }

        @Override
        public void onCenterPosition(boolean animate) {
            if (animate) {
                mShrinkAnimator.cancel();
                if (!mExpandAnimator.isRunning()) {
                    mExpandCircleAnimator.setFloatValues(mCircle.getCircleRadius(), mExpandCircleRadius);
                    mExpandLabelAnimator.setFloatValues(mLabel.getAlpha(), EXPAND_LABEL_ALPHA);
                    mExpandAnimator.start();
                }
            } else {
                mExpandAnimator.cancel();
                mCircle.setCircleRadius(mExpandCircleRadius);
                mLabel.setAlpha(EXPAND_LABEL_ALPHA);
            }
        }

        @Override
        public void onNonCenterPosition(boolean animate) {
            if (animate) {
                mExpandAnimator.cancel();
                if (!mShrinkAnimator.isRunning()) {
                    mShrinkCircleAnimator.setFloatValues(mCircle.getCircleRadius(), mShrinkCircleRadius);
                    mShrinkLabelAnimator.setFloatValues(mLabel.getAlpha(), SHRINK_LABEL_ALPHA);
                    mShrinkAnimator.start();
                }
            } else {
                mShrinkAnimator.cancel();
                mCircle.setCircleRadius(mShrinkCircleRadius);
                mLabel.setAlpha(SHRINK_LABEL_ALPHA);
            }
        }

        private void setWatchFace(WatchFaceConfig watchFaceName) {
            watchFaceConfig = watchFaceName;
            mLabel.setText(watchFaceName.getName());
            mSubTitle.setText(watchFaceName.getSubTitle());
        }

        public String getLabel() {
            return this.watchFaceConfig.getObjectName();
        }
    }

    private static class WatchFaceViewHolder extends WearableListView.ViewHolder {
        private final WatchFaceItem mWatchFace;

        public WatchFaceViewHolder(WatchFaceItem watchFaceItem) {
            super(watchFaceItem);
            mWatchFace = watchFaceItem;
        }
    }

    private class WatchFaceListAdapter extends WearableListView.Adapter {
        private final WatchFaceConfig[] mWatchFaces;

        public WatchFaceListAdapter(WatchFaceConfig[] wfs) {
            mWatchFaces = wfs;
        }

        @Override
        public WatchFaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WatchFaceViewHolder(new WatchFaceItem(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            WatchFaceViewHolder watchFaceViewHolder = (WatchFaceViewHolder) holder;

            WatchFaceConfig watchFaceName = mWatchFaces[position];
            watchFaceViewHolder.mWatchFace.setWatchFace(watchFaceName);

            RecyclerView.LayoutParams layoutParams =
                    new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                  ViewGroup.LayoutParams.WRAP_CONTENT);
            watchFaceViewHolder.itemView.setLayoutParams(layoutParams);
        }

        @Override
        public int getItemCount() {
            return mWatchFaces.length;
        }

        public WatchFaceConfig[] getWatchFaces() {
            return mWatchFaces;
        }
    }

    private class WatchFaceConfig {
        private final String object;
        private final String name;
        private final String subtitle;

        public WatchFaceConfig(String o, String n, String s) {
            this.object = o;
            this.name = n;
            this.subtitle = s;
        }

        public String getObjectName() {
            return object;
        }

        public String getSubTitle() {
            return subtitle;
        }

        public String getName() {
            return name;
        }

        public boolean equals(String o) {
            return this.object.equals(o);
        }
    }
}
